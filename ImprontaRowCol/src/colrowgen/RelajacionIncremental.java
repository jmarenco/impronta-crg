package colrowgen;

import general.Instancia;
import general.Pad;
import general.Semilla;
import general.Solucion;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

// Relajación que permite ir agregando puntos y mantiene cplex abierto. No funcionó bien en la práctica, el método
// actualizarRestricciones() demora demasiado tiempo

@Deprecated
public class RelajacionIncremental
{
	private Instancia _instancia;
	private List<Point> _puntos;
	private PadCache _pads;
	
	private IloCplex _cplex;
	private Map<IloNumVar, Pad> _vars;
	private Map<Pad, IloNumVar> _inverseVars;
	private Map<Coordinate, IloRange> _constr;
	private IloNumExpr _obj;
	private Solucion _solucion;
	private double _objValue;
	private long _start;
	private double _time;
	private int _activeVariables;
	
	private static double _infinity = Double.POSITIVE_INFINITY;
	private static double _timeLimit = 3600;
	private static boolean _mostrarSolucion = false;
	private static boolean _exportarModelo = false;
	private static boolean _entero = false;
	private static boolean _verbose = false;
	
	public RelajacionIncremental(Instancia instancia, PadCache padCache)
	{
		_instancia = instancia;
		_puntos = new ArrayList<Point>();
		_pads = padCache;
		
		try
		{
			crearModelo();
			crearObjetivo();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void sumar(List<Point> puntos)
	{
		try
		{
			_puntos.addAll(puntos);
			_start = System.currentTimeMillis();
	
			crearVariables(puntos);
			System.out.println("CV: " + (System.currentTimeMillis() - _start) / 1000.0);
			_start = System.currentTimeMillis();
			actualizarObjetivo(puntos);
			System.out.println("AO: " + (System.currentTimeMillis() - _start) / 1000.0);
			_start = System.currentTimeMillis();
			actualizarRestricciones(puntos);
			System.out.println("AR: " + (System.currentTimeMillis() - _start) / 1000.0);
			_start = System.currentTimeMillis();
			crearRestricciones(puntos);
			
			System.out.println("CR: " + (System.currentTimeMillis() - _start) / 1000.0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Solucion resolver()
	{
		try
		{
			resolverModelo();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return _solucion;
	}
	
	private void crearModelo() throws IloException
	{
		_cplex = new IloCplex();
		_cplex.setParam(IloCplex.IntParam.TimeLimit, _timeLimit);
		_solucion = null;
		_start = System.currentTimeMillis();
		_vars = new HashMap<IloNumVar, Pad>();
		_inverseVars = new HashMap<Pad, IloNumVar>();
		_constr = new HashMap<Coordinate, IloRange>();
	}

	private void crearVariables(List<Point> puntos) throws IloException
	{
		int i = 1;
		for(Point point: puntos)
		for(Semilla semilla: _instancia.getSemillas())
		{
			_pads.add(point, semilla); // No se agrega si el pad no es factible
			
			if( _pads.contains(point, semilla) )
			{
				IloNumVar variable = _entero ? _cplex.boolVar("x" + (i++)) : _cplex.numVar(0, _infinity, "x" + (i++));

				_vars.put(variable, _pads.get(point, semilla));
				_inverseVars.put(_pads.get(point, semilla), variable);
			}
		}
	}

	private void crearObjetivo() throws IloException
	{
		_obj = _cplex.linearNumExpr();
		_cplex.addMaximize(_obj);
	}

	private void actualizarObjetivo(List<Point> puntos) throws IloException
	{
		for(Point point: puntos)
		for(Semilla semilla: _instancia.getSemillas()) if( _pads.contains(point, semilla) )
		{
			Pad pad = _pads.get(point, semilla);
			_obj = _cplex.sum(_obj, _cplex.prod(pad.getValorizacion(), _inverseVars.get(pad)));
		}
	}

	private void actualizarRestricciones(List<Point> puntos) throws IloException
	{
		for(Coordinate coord: _constr.keySet())
		{
			IloRange constr = _constr.get(coord);
			
			for(Point point: puntos)
			for(Semilla semilla: _instancia.getSemillas()) if( _pads.contains(point, semilla) )
			{
				Pad pad = _pads.get(point, semilla);
				if( pad.contiene(coord) )
					_cplex.setLinearCoef(constr, _inverseVars.get(_pads.get(point, semilla)), 1);
			}
		}
	}

	private void crearRestricciones(List<Point> puntos) throws IloException
	{
		for(Point point: puntos)
		for(Semilla semilla: _instancia.getSemillas()) if( _pads.contains(point, semilla) )
		{
			Pad pad = _pads.get(point, semilla);

			for(Coordinate esquina: pad.getPerimetro().getCoordinates())
			for(Coordinate coord: _instancia.snappedNeighbors(esquina)) if( pad.contiene(coord) && !_constr.containsKey(coord) )
			{
				IloNumExpr lhs = _cplex.linearNumExpr();

				for(IloNumVar ovar: _vars.keySet()) if( _vars.get(ovar).contiene(coord) )
					lhs = _cplex.sum(lhs, ovar);
			
				IloRange constraint = _cplex.le(lhs, 1);
	
				_cplex.add(constraint);
				_constr.put(coord, constraint);
			}
		}
	}

	private void resolverModelo() throws IloException
	{
		if( _exportarModelo == true )
			_cplex.exportModel("/home/javier/Escritorio/modelo.lp");
		
		if( _verbose == false )
			_cplex.setOut(null);
		
		if( _cplex.solve() == true )
		{
			_solucion = new Solucion(_instancia);
			_objValue = _cplex.getObjValue();
			_activeVariables = 0;

			for(IloNumVar var: _vars.keySet()) if( _cplex.getValue(var) > 0.05 )
			{
				if( _mostrarSolucion == true )
					System.out.println(var + " = " + _cplex.getValue(var) + " -> " + _vars.get(var));
				
				_solucion.agregar(_vars.get(var), _cplex.getValue(var));
				_activeVariables++;
			}
			
			if( _mostrarSolucion == true )
				System.out.println("Primal objective value: " + _objValue);
		}
		
		if( _mostrarSolucion == true )
			System.out.println("Cplex status: " + _cplex.getStatus());

		_time = (System.currentTimeMillis() - _start) / 1000.0;
//		_cplex.end();
	}
	
	public List<Point> varPoints()
	{
		return _puntos;
	}
	
	public Set<Coordinate> constraintPoints()
	{
		return _constr.keySet();
	}

	public Instancia getInstancia()
	{
		return _instancia;
	}

	public PadCache getPadCache()
	{
		return _pads;
	}

	public double getObjValue()
	{
		return _objValue;
	}

	public Solucion getSolucion()
	{
		return _solucion;
	}
	
	public double getTime()
	{
		return _time;
	}

	public int getNumVariables()
	{
		return _vars.size();
	}

	public int getNumConstraints()
	{
		return _constr.size();
	}
	
	public int getActiveVariables()
	{
		return _activeVariables;
	}
	
	public static void setVerbose(boolean value)
	{
		_verbose = value;
	}
}
