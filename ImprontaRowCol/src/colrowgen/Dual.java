package colrowgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Pad;
import general.Semilla;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

public class Dual 
{
	private Instancia _instancia;
	private Relajacion _primal;
	private PadCache _pads;
	private GeometryFactory _factory;

	private IloCplex _cplex;
	private Map<Point, IloNumVar> _vars;
	private Map<Pad, IloRange> _constr;
	private Set<Point> _usados;
	private Set<Point> _bindingConstraints = null;
	private Map<Point, Double> _solucion;
	private long _start;
	private double _time;
	private double _objValue;

	private static double _infinity = Double.POSITIVE_INFINITY;
	private static double _timeLimit = 3600;
	private static boolean _mostrarSolucion = false;
	private static boolean _exportarModelo = false;
	private static boolean _verbose = false;
	private static boolean _registrarBindings = false;

	public Dual(Instancia instancia, Relajacion primal)
	{
		_instancia = instancia;
		_primal = primal;
		_pads = primal.getPadCache();
		_factory = _instancia.getFactory();
	}
	
	public Map<Point, Double> resolver()
	{
		try
		{
			crearModelo();
			crearVariables();
			crearObjetivo();
			crearRestriccionesCubrimiento();
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
		_vars = new HashMap<Point, IloNumVar>();
		_usados = new HashSet<Point>();

		int i = 1;
		for(Coordinate coord: _primal.constraintPoints())
			_vars.put(_factory.createPoint(coord), _cplex.numVar(0, _infinity, "y" + (i++)));
	}

	private void crearObjetivo() throws IloException
	{
		IloNumExpr obj = _cplex.linearNumExpr();
		
		for(IloNumVar var: _vars.values())
			obj = _cplex.sum(obj, var);
		
		_cplex.addMinimize(obj);
	}

	private void crearRestriccionesCubrimiento() throws IloException
	{
		_constr = new HashMap<Pad, IloRange>();
		
		for(Point point: _primal.varPoints())
		for(Semilla semilla: _instancia.getSemillas())
		{
			if( _pads.contains(point, semilla) )
			{
				Pad pad = _pads.get(point, semilla);
				IloNumExpr lhs = _cplex.linearNumExpr();
				
				for(Point p: _vars.keySet()) if( pad.contiene(p) )
				{
					lhs = _cplex.sum(lhs, _vars.get(p));
					_usados.add(p);
				}
				
				IloRange constraint = _cplex.ge(lhs, pad.getValorizacion());
	
				_cplex.add(constraint);
				_constr.put(pad, constraint);
			}
		}
	}

	private void resolverModelo() throws IloException
	{
		if( _exportarModelo == true )
			_cplex.exportModel("/home/javier/Escritorio/dual.lp");
		
		if( _verbose == false )
			_cplex.setOut(null);
		
		if( _cplex.solve() == true )
		{
			_solucion = new HashMap<Point, Double>();
			
			for(Point p: _usados)
			{
				IloNumVar var = _vars.get(p);
				
				if( _cplex.getValue(var) > 0.0000001 )
				{
					if( _mostrarSolucion == true )
						System.out.println(var + " = " + _cplex.getValue(var) + " - " + p);
				
					_solucion.put(p, _cplex.getValue(var));
				}
				
			}

			if( _mostrarSolucion == true )
				System.out.println("Dual objective value: " + _cplex.getObjValue());
			
			if( _registrarBindings == true )
			{
				_bindingConstraints = new HashSet<Point>();
				for(Pad pad: _constr.keySet()) if( Math.abs(_cplex.getSlack(_constr.get(pad))) <= 0.00001 )
					_bindingConstraints.add(pad.getCentro());
//				else
//					System.out.println(_cplex.getSlack(_constr.get(pad)) + " | " + _cplex.getValue(_constr.get(pad).getExpr()) + " <- " + _constr.get(pad));

//				for(Pad pad: _constr.keySet())
//					System.out.println(_cplex.getSlack(_constr.get(pad)) + " | " + _cplex.getValue(_constr.get(pad).getExpr()) + " <- " + _constr.get(pad));
			}
		}
		
		if( _mostrarSolucion == true )
			System.out.println("Cplex status: " + _cplex.getStatus());
		
		_objValue = _cplex.getObjValue();
		_time = (System.currentTimeMillis() - _start) / 1000.0;
		_cplex.end();
	}
	
	public double getTime()
	{
		return _time;
	}
	
	public double getObjValue()
	{
		return _objValue;
	}
	
	public boolean onTarget()
	{
		return Math.abs(_objValue - _primal.getObjValue()) < 0.001;
	}
	
	public Set<Point> getBindingConstraints()
	{
		return _bindingConstraints;
	}
	
	public static void setRegistrarBindings(boolean valor)
	{
		_registrarBindings = valor;
	}
	
	public static void setVerbose(boolean value)
	{
		_verbose = value;
	}
}
