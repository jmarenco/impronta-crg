package colrowgen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import general.Instancia;
import general.Pad;
import general.Region;
import general.Semilla;
import interfaz.Viewer;

// El nombre de la clase no es el mejor. Representa la búsqueda de puntos factibles no cubiertos
// por la solución dual asociada con una semilla particular

public class SimpleBFS
{
	private Instancia _instancia;
	private Map<Point, Double> _dualSolution;
	private Semilla _semilla;
	private PadCache _pads;
	private Region _interna;

	private ArrayList<Point> _pendientes;
	private ArrayList<Point> _procesados;
	private ArrayList<Point> _nuevos;
	private Viewer _panel;

	private int _indice;
	private int _iniciados;
	private int _explorados;
	
	private static boolean _mostrarBFS = false;
	private static boolean _verbose = false;

	public SimpleBFS(Instancia instancia, Map<Point, Double> dualSolution, Semilla semilla, PadCache pads)
	{
		_instancia = instancia;
		_dualSolution = dualSolution;
		_semilla = semilla;
		_pads = pads;
		_interna = instancia.getRegionInterna(semilla);
	}
	
	public void ejecutar()
	{
		if( _mostrarBFS == true )
			_panel = interfaz.Viewer.show(_instancia, _dualSolution, _semilla);

		_procesados = new ArrayList<Point>();
		_nuevos = new ArrayList<Point>();
		_iniciados = 0;
		_explorados = 0;
		
		log(" - Procesando coordenadas");
		
//		for(Point centro: _dualSolution.keySet())
//		{
//			Polygon nuevo = new Pad(_instancia, _semilla, centro.getCoordinate()).getPerimetro();
//			for(Coordinate coord: nuevo.getCoordinates())
//				addNuevo(closestFeasible(coord, Long.MAX_VALUE));
//		}
//
//		for(Coordinate coord: _interna.getCoordinates())
//			addNuevo(closestFeasible(coord, Long.MAX_VALUE));
		
		for(Coordinate coord: relevantCoordinates())
			addNuevo(closestFeasible(coord, Long.MAX_VALUE));
	}
	
	private Point closestFeasible(Coordinate start, long pointsLimit)
	{
		_iniciados += 1;
		_pendientes = new ArrayList<Point>();
		_indice = 0;

		add(toPoint(start), Color.MAGENTA);

		for(Coordinate vecino: _instancia.snappedNeighbors(start))
			addPendiente(toPoint(vecino), 0, 0);

		while( _indice < _pendientes.size() )
		{
			Point actual = _pendientes.get(_indice);
			_pads.add(actual, _semilla);

			if( _pads.contains(actual, _semilla) )
			{
				add(actual, Color.GREEN);
				_explorados += _pendientes.size();

				return actual;
			}
			
			addPendiente(actual, _instancia.getPasoHorizontal(), 0);
			addPendiente(actual, -_instancia.getPasoHorizontal(), 0);
			addPendiente(actual, 0, _instancia.getPasoVertical());
			addPendiente(actual, 0, -_instancia.getPasoVertical());
			
			if( _pendientes.size() > pointsLimit )
				return null;
			
			_indice += 1;
			_procesados.add(actual);
		}

		_explorados += _pendientes.size();
		return null;
	}
	
	private void addNuevo(Point nuevo)
	{
		if( nuevo != null && _nuevos.contains(nuevo) == false )
			_nuevos.add(nuevo);
	}
	
	private void addPendiente(Point actual, int offsetx, int offsety)
	{
		Point nuevo = toPoint(new Coordinate(actual.getX() + offsetx, actual.getY() + offsety));
		
		if( !_interna.cubre(nuevo) )
			add(actual, Color.RED);

		if( !_interna.cubre(nuevo) || _procesados.contains(nuevo) || _pendientes.contains(nuevo) )
			return;
		
		if( _pendientes.contains(nuevo) == false && _procesados.contains(nuevo) == false && cubierto(nuevo) == false )
		{
			_pendientes.add(nuevo);
			add(actual, Color.BLUE);
		}
		else
		{
			add(actual, Color.RED);
		}
	}
	
	private Point toPoint(Coordinate coord)
	{
		return _instancia.getFactory().createPoint(coord);
	}
	
	private void add(Geometry geom, Color color)
	{
		if( _panel != null )
			_panel.addGeometry(geom, color);
	}
	
	private boolean cubierto(Point punto)
	{
		return _dualSolution.keySet().stream().filter(c -> incluye(c,punto)).mapToDouble(c -> _dualSolution.get(c)).sum() >= _semilla.getValorizacion();
	}
	
	private boolean incluye(Point centro, Point punto)
	{
		return punto.getX() > centro.getX() - _semilla.getLargo() / 2 &&
				punto.getX() < centro.getX() + _semilla.getLargo() / 2 &&
				punto.getY() > centro.getY() - _semilla.getAncho() / 2 &&
				punto.getY() < centro.getY() + _semilla.getAncho() / 2;
	}
	
	private Set<Coordinate> relevantCoordinates()
	{
		ArrayList<Pad> pads = new ArrayList<Pad>();
		Set<Coordinate> ret = new HashSet<Coordinate>();
		
		for(Point centro: _dualSolution.keySet())
			pads.add(new Pad(_instancia, _semilla, centro.getCoordinate()));
		
		for(Pad pad: pads)
		for(Coordinate c: pad.getPerimetro().getCoordinates())
			ret.add(c);
		
		for(Pad primero: pads)
		for(Pad segundo: pads)
		for(Coordinate c: primero.getPerimetro().intersection(segundo.getPerimetro()).getCoordinates())
			ret.add(c);
		
		ret.addAll(_interna.getCoordinates());
		return ret;
	}
	
	private void log(String texto)
	{
		if( _verbose == true )
			System.out.println(texto);
	}

	public ArrayList<Point> getNuevos()
	{
		return _nuevos;
	}
	
	public int getIniciados()
	{
		return _iniciados;
	}
	
	public int getExplorados()
	{
		return _explorados;
	}
	
	public static void setMostrarBFS(boolean valor)
	{
		_mostrarBFS = valor;
	}
}
