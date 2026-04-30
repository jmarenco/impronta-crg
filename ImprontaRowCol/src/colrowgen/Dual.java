package colrowgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import general.Instancia;
import general.Pad;
import general.Punto;
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

	private IloCplex _cplex;
	private Map<Punto, IloNumVar> _vars;
	private Map<Pad, IloRange> _constr;
	private Set<Punto> _usados;
	private Set<Punto> _bindingConstraints = null;
	private Map<Punto, Double> _solucion;
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
	}
	
	public Map<Punto, Double> resolver()
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
		_vars = new HashMap<Punto, IloNumVar>();
		_usados = new HashSet<Punto>();

		int i = 1;
		for(Punto coord: _primal.constraintPoints())
			_vars.put(coord, _cplex.numVar(0, _infinity, "y" + (i++)));
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
		
		for(Punto point: _primal.varPoints())
		for(Semilla semilla: _instancia.getSemillas())
		{
			if( _pads.contains(point, semilla) )
			{
				Pad pad = _pads.get(point, semilla);
				IloNumExpr lhs = _cplex.linearNumExpr();
				
				for(Punto p: _vars.keySet()) if( pad.contiene(p) )
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
			_solucion = new HashMap<Punto, Double>();
			
			for(Punto p: _usados)
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
				_bindingConstraints = new HashSet<Punto>();
				
//				for(Pad pad: _constr.keySet()) if( Math.abs(_cplex.getSlack(_constr.get(pad))) <= 0.00001 )
//					_bindingConstraints.add(pad.getCentro());
				
				for(Pad pad: _constr.keySet())
				{
					boolean algunaPositiva = false;

					for(Punto esquina: pad.getVertices())
					for(Punto coord: _instancia.snappedNeighbors(esquina)) if( _vars.containsKey(coord) && _cplex.getValue(_vars.get(coord)) > 0.0001 )
						algunaPositiva = true;
					
					if( algunaPositiva == false )
						_bindingConstraints.add(pad.getCentro());
				}
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
	
	public Set<Punto> getBindingConstraints()
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
