package colrowgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	private ArrayList<Point> _puntos;
	private PadCache _pads;
	private double _target;

	private IloCplex _cplex;
	private ArrayList<IloNumVar> _vars;
	private ArrayList<IloNumVar> _slacks;
	private ArrayList<Boolean> _usada;
	private Map<Pad, IloRange> _constr;
	private Map<Point, Double> _solucion;
	private long _start;
	private double _time;

	private double _infinity = Double.POSITIVE_INFINITY;
	private boolean _mostrarSolucion = false;
	private boolean _exportarModelo = false;
	private boolean _verbose = false;

	public Dual(Instancia instancia, ArrayList<Point> puntos, PadCache padCache, double target)
	{
		_instancia = instancia;
		_puntos = puntos;
		_pads = padCache;
		_target = target;
	}
	
	public Map<Point, Double> resolver()
	{
		try
		{
			crearModelo();
			crearVariables();
			crearObjetivo();
			crearRestriccionesCubrimiento();
			crearRestriccionTarget();
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
		_solucion = null;
		_start = System.currentTimeMillis();
	}

	private void crearVariables() throws IloException
	{
		_vars = new ArrayList<IloNumVar>();
		_slacks = new ArrayList<IloNumVar>();
		_usada = new ArrayList<Boolean>();
		
		for(int i=0; i<_puntos.size(); ++i)
		{
			_vars.add(_cplex.numVar(0, _infinity, "y" + (i+1)));
			_slacks.add(_cplex.numVar(0, _infinity, "v" + (i+1)));
			_usada.add(false);
		}
	}

	private void crearObjetivo() throws IloException
	{
		IloNumExpr obj = _cplex.linearNumExpr();
		
		for(IloNumVar var: _slacks)
			obj = _cplex.sum(obj, var);
		
		_cplex.addMinimize(obj);
	}

	private void crearRestriccionesCubrimiento() throws IloException
	{
		_constr = new HashMap<Pad, IloRange>();
		
		for(int i=0; i<_puntos.size(); ++i)
		for(Semilla semilla: _instancia.getSemillas())
		{
			Point point = _puntos.get(i);
			_pads.add(point, semilla);
			
			if( _pads.contains(point, semilla) )
			{
				Pad pad = _pads.get(point, semilla);
				IloNumExpr lhs = _cplex.linearNumExpr();
				
				for(int j=0; j<_puntos.size(); ++j) if( pad.contiene(_puntos.get(j)) )
				{
					lhs = _cplex.sum(lhs, _vars.get(j));
					_usada.set(j, true);
				}
				
				lhs = _cplex.sum(lhs, _slacks.get(i));
				IloRange constraint = _cplex.ge(lhs, pad.getArea());
	
				_cplex.add(constraint);
				_constr.put(pad, constraint);
			}
		}
	}
	
	private void crearRestriccionTarget() throws IloException
	{
		IloNumExpr lhs = _cplex.linearNumExpr();
				
		for(IloNumVar var: _vars)
			lhs = _cplex.sum(lhs, var);
		
		_cplex.add(_cplex.eq(lhs, _target));
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
			
			for(int i=0; i<_puntos.size(); ++i) if( _usada.get(i) == true )
			{
				IloNumVar var = _vars.get(i);
				
				if( _cplex.getValue(var) > 0.0000001 )
				{
					if( _mostrarSolucion == true )
						System.out.println(var + " = " + _cplex.getValue(var) + " - " + _puntos.get(i));
				
					_solucion.put(_puntos.get(i), _cplex.getValue(var));
				}
			}
			
			if( _mostrarSolucion == true )
				System.out.println("Dual objective value: " + _cplex.getObjValue());
		}
		
		if( _mostrarSolucion == true )
			System.out.println("Cplex status: " + _cplex.getStatus());
		
		_time = (System.currentTimeMillis() - _start) / 1000.0;
		_cplex.end();
	}
	
	public double getTime()
	{
		return _time;
	}
}
