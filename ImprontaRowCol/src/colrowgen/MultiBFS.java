package colrowgen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Semilla;
import interfaz.Viewer;

// El nombre de la clase no es el mejor. Representa la búsqueda de puntos factibles no cubiertos
// por la solución dual asociada con una semilla particular

public class MultiBFS
{
	private Instancia _instancia;
	private Map<Point, Double> _dualSolution;
	private Semilla _semilla;
	private PadCache _pads;

	private DualCovering _covering;
	private Geometry _uncovered;
	private ArrayList<Point> _pendientes;
	private ArrayList<Point> _procesados;
	private ArrayList<Point> _nuevos;
	private Viewer _panel;

	private int _indice;
	private int _iniciados;
	private int _explorados;
	
	private static boolean _mostrarBFS = false;
	private static boolean _verbose = false;

	public MultiBFS(Instancia instancia, Map<Point, Double> dualSolution, Semilla semilla, PadCache pads)
	{
		_instancia = instancia;
		_dualSolution = dualSolution;
		_semilla = semilla;
		_pads = pads;
	}
	
	public void ejecutar()
	{
		long start = log(" - Construyendo dual covering");
		
		_covering = new DualCovering(_instancia, _dualSolution, _semilla);
		_uncovered = _covering.uncovered();

		log(" - Dual covering: ", start);

		if( _mostrarBFS == true )
			_panel = interfaz.Viewer.show(_instancia, _covering, _instancia.getRegionInterna(_semilla));
		
		_procesados = new ArrayList<Point>();
		_nuevos = new ArrayList<Point>();
		_iniciados = 0;
		_explorados = 0;
		
		start = log(" - Procesando coordenadas");
		
		for(Coordinate coord: _uncovered.getCoordinates())
			addNuevo(closestFeasible(coord, Long.MAX_VALUE));

		log(" - Proceso coordenadas: ", start);
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
		
		if( _procesados.contains(nuevo) || _pendientes.contains(nuevo) )
			return;
		
		if( _pendientes.contains(nuevo) == false && _procesados.contains(nuevo) == false && _uncovered.contains(nuevo) )
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
	
	private long log(String texto)
	{
		if( _verbose == true )
			System.out.println(texto);
		
		return System.currentTimeMillis();
	}
	
	private void log(String texto, long start)
	{
		if( _verbose == true )
			System.out.println(texto + String.format("%.2f", (System.currentTimeMillis() - start) / 1000.0) + " seg.");
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
}
