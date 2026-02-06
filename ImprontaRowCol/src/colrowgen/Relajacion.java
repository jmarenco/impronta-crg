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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

public class Relajacion
{
	private Instancia _instancia;
	private List<Point> _puntos;
	private PadCache _pads;
	
	private IloCplex _cplex;
	private Map<IloNumVar, Pad> _vars;
	private Map<Coordinate, IloRange> _constr;
	private Solucion _solucion;
	
	private double _infinity = Double.POSITIVE_INFINITY;
	private boolean _mostrarSolucion = true;
	private boolean _entero = false;
	
	public Relajacion(Instancia instancia, List<Point> puntos, PadCache padCache)
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
		_solucion = null;
	}

	private void crearVariables() throws IloException
	{
		_vars = new HashMap<IloNumVar, Pad>();
		int i = 1;
		for(Point point: _puntos)
		for(Semilla semilla: _instancia.getSemillas())
		{
			_pads.add(point, semilla); // No se agrega si el pad no es factible
			
			if( _pads.contains(point, semilla) )
			{
				if( _entero == false )
					_vars.put(_cplex.numVar(0, _infinity, "x" + (i++)), _pads.get(point, semilla));
				else
					_vars.put(_cplex.boolVar("x" + (i++)), _pads.get(point, semilla));
			}
		}
	}

	private void crearObjetivo() throws IloException
	{
		IloNumExpr obj = _cplex.linearNumExpr();
		
		for(IloNumVar var: _vars.keySet())
			obj = _cplex.sum(obj, _cplex.prod(_vars.get(var).getArea(), var));
		
		_cplex.addMaximize(obj);
	}

	private void crearRestricciones() throws IloException
	{
		_constr = new HashMap<Coordinate, IloRange>();
		
		for(IloNumVar var: _vars.keySet())
		for(Coordinate coord: _vars.get(var).getPerimetro().getCoordinates()) if( _constr.containsKey(coord) == false )
		{
			IloNumExpr lhs = _cplex.linearNumExpr();
			lhs = _cplex.sum(lhs, var);

			for(IloNumVar ovar: _vars.keySet()) if( _vars.get(ovar).contiene(coord) )
				lhs = _cplex.sum(lhs, ovar);
			
			IloRange constraint = _cplex.le(lhs, 1);

			_cplex.add(constraint);
			_constr.put(coord, constraint);
		}
	}

	private void resolverModelo() throws IloException
	{
		_cplex.exportModel("/home/javier/Escritorio/modelo.lp");
		if( _cplex.solve() == true )
		{
			_solucion = new Solucion(_instancia);
			
			for(IloNumVar var: _vars.keySet()) if( _cplex.getValue(var) > 0.05 )
			{
				if( _mostrarSolucion == true )
					System.out.println(var + " = " + _cplex.getValue(var));
				
				_solucion.agregarPad(_vars.get(var));
			}
		}
	}
}
