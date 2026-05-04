package colrowgen;

import general.Instancia;
import general.Pad;
import general.Punto;
import general.Semilla;
import general.Solucion;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Relajacion
{
	private Instancia _instancia;
	private List<Punto> _puntos;
	private PadCache _pads;
	
	private IloCplex _cplex;
	private Map<IloNumVar, Pad> _vars;
	private Map<Punto, IloRange> _constr;
	private Solucion _solucion;
	private double _objValue;
	private long _start;
	private double _time;
	private int _numVariables;
	private int _activeVariables;
	
	private static double _infinity = Double.POSITIVE_INFINITY;
	private static double _timeLimit = 3600;
	private static boolean _mostrarSolucion = false;
	private static boolean _exportarModelo = false;
	private static boolean _entero = false;
	private static boolean _verbose = false;
	
	public Relajacion(Instancia instancia, List<Punto> puntos, PadCache padCache)
	{
		_instancia = instancia;
		_puntos = puntos;
		_pads = padCache;
	}
	
	public Solucion resolver()
	{
		try
		{
			crearModelo();
			crearVariables();
			crearObjetivo();
			crearRestricciones();
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
	}

	private void crearVariables() throws IloException
	{
		_vars = new HashMap<IloNumVar, Pad>();
		_numVariables = 0;
		
		int i = 1;
		for(Punto point: _puntos)
		for(Semilla semilla: _instancia.getSemillas())
		{
			_pads.add(point, semilla); // No se agrega si el pad no es factible
			
			if( _pads.contains(point, semilla) )
			{
				if( _entero == false )
					_vars.put(_cplex.numVar(0, _infinity, "x" + (i++)), _pads.get(point, semilla));
				else
					_vars.put(_cplex.boolVar("x" + (i++)), _pads.get(point, semilla));
				
				_numVariables++;
			}
		}
	}

	private void crearObjetivo() throws IloException
	{
		IloNumExpr obj = _cplex.linearNumExpr();
		
		for(IloNumVar var: _vars.keySet())
			obj = _cplex.sum(obj, _cplex.prod(_vars.get(var).getValorizacion(), var));
		
		_cplex.addMaximize(obj);
	}

	private void crearRestricciones() throws IloException
	{
		_constr = new HashMap<Punto, IloRange>();
		
		for(IloNumVar var: _vars.keySet())
		for(Punto esquina: _vars.get(var).getVertices())
		for(Punto coord: _instancia.snappedNeighbors(esquina)) if( _vars.get(var).contiene(coord) && !_constr.containsKey(coord) )
		{
			IloNumExpr lhs = _cplex.linearNumExpr();

			for(IloNumVar ovar: _vars.keySet()) if( _vars.get(ovar).contiene(coord) )
				lhs = _cplex.sum(lhs, ovar);
			
			IloRange constraint = _cplex.le(lhs, 1);

			_cplex.add(constraint);
			_constr.put(coord, constraint);
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
		_cplex.end();
	}
	
	public List<Punto> varPoints()
	{
		return _puntos;
	}
	
	public Set<Punto> constraintPoints()
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
		return _numVariables;
	}
	
	public int getActiveVariables()
	{
		return _activeVariables;
	}

	public int getNumConstraints()
	{
		return _constr.size();
	}
	
	public static void setVerbose(boolean value)
	{
		_verbose = value;
	}
}
