package colrowgen;

import java.util.ArrayList;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Semilla;

public class Dualizer 
{
	private Instancia _instancia;
	private ArrayList<Point> _puntos;
	private ArrayList<Point> _nuevos;
	private PadCache _pads;
	private double _target;
	
	private Map<Point, Double> _dualSolution;
	private long _start;
	private double _time;
	private double _dualTime;
	
	private static boolean _mostrarPuntos = false;
	
	public Dualizer(Relajacion relajacion)
	{
		_instancia = relajacion.getInstancia();
		_pads = relajacion.getPadCache();
		_target = relajacion.getObjValue();
		_puntos = new ArrayList<Point>();

		for(Point point: relajacion.varPoints())
			_puntos.add(point);

		for(Coordinate coordinate: relajacion.constraintPoints())
			_puntos.add(_instancia.getFactory().createPoint(coordinate));

		if( _mostrarPuntos == true )
			mostrarPuntos(relajacion);
	}
	
	public void ejecutar()
	{
		_start = System.currentTimeMillis();
		
		Dual dual = new Dual(_instancia, _puntos, _pads, _target);

		_dualSolution = dual.resolver();
		_dualTime = dual.getTime();
		_nuevos = new ArrayList<Point>();
		
		for(Semilla semilla: _instancia.getSemillas())
		{
			DualCovering covering = new DualCovering(_instancia, _dualSolution, semilla);
			Geometry uncovered = covering.uncovered();
			
//			System.out.println("Calculando puntos no cubiertos para " + uncovered);
			
			for(Coordinate coord: uncovered.getCoordinates())
				add(closestFeasible(uncovered, coord, semilla));
		}
		
		_time = (System.currentTimeMillis() - _start) / 1000.0;
	}
	
	public Map<Point, Double> getDualSolution()
	{
		return _dualSolution;
	}
	
	public ArrayList<Point> getNuevos()
	{
		return _nuevos;
	}
	
	private void add(Point nuevo)
	{
		if( nuevo != null && _nuevos.contains(nuevo) == false )
		{
			_nuevos.add(nuevo);
//			System.out.println(" - Agregando " + nuevo);
		}
	}

	private void mostrarPuntos(Relajacion relajacion)
	{
		for(Point point: relajacion.varPoints())
			System.out.println("VarPoint " + point);

		for(Coordinate coordinate: relajacion.constraintPoints())
			System.out.println("ConstraintPoint " + coordinate);
	}
	
	private Point closestFeasible(Geometry uncovered, Coordinate start, Semilla semilla)
	{
//		System.out.println("Comenzando BFS desde " + start);
		ArrayList<Point> pendientes = new ArrayList<Point>();
		for(Coordinate vecino: _instancia.snappedNeighbors(start)) if( uncovered.contains(toPoint(vecino)) )
		{
			pendientes.add(toPoint(vecino));
//			System.out.println(" - Agregado " + vecino + " a pendientes");
		}

		int i = 0;
		while( i < pendientes.size() )
		{
			Point actual = pendientes.get(i);
//			System.out.println(" - Inicio iteration " + i + ", actual = " + actual);
			_pads.add(actual, semilla);

//			System.out.println(" - _pads.contains(actual, semilla) = " + _pads.contains(actual, semilla));
			if( _pads.contains(actual, semilla) )
				return actual;
			
			add(uncovered, pendientes, actual, _instancia.getPasoHorizontal(), 0);
			add(uncovered, pendientes, actual, -_instancia.getPasoHorizontal(), 0);
			add(uncovered, pendientes, actual, 0, _instancia.getPasoVertical());
			add(uncovered, pendientes, actual, 0, -_instancia.getPasoVertical());
			
			++i;
		}

		return null;
	}
	
	private void add(Geometry uncovered, ArrayList<Point> points, Point point, int offsetx, int offsety)
	{
		Point nuevo = toPoint(new Coordinate(point.getX() + offsetx, point.getY() + offsety));

		if( points.contains(nuevo) == false && uncovered.contains(nuevo) )
		{
			points.add(nuevo);
//			System.out.println(" - Added " + nuevo + " to pendientes");
		}
	}
	
	private Point toPoint(Coordinate coord)
	{
		return _instancia.getFactory().createPoint(coord);
	}
	
	public double getTotalTime()
	{
		return _time;
	}
	
	public double getDualTime()
	{
		return _dualTime;
	}
}
