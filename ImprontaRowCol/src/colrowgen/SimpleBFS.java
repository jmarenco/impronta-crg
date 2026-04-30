package colrowgen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import general.Instancia;
import general.Pad;
import general.Punto;
import general.Region;
import general.Semilla;
import interfaz.Viewer;

// El nombre de la clase no es el mejor. Representa la búsqueda de puntos factibles no cubiertos
// por la solución dual asociada con una semilla particular

public class SimpleBFS
{
	private Instancia _instancia;
	private Map<Punto, Double> _dualSolution;
	private Semilla _semilla;
	private PadCache _pads;
	private Region _interna;

	private ArrayList<Punto> _pendientes;
	private ArrayList<Punto> _procesados;
	private ArrayList<Punto> _nuevos;
	private Viewer _panel;

	private int _indice;
	private int _iniciados;
	private int _explorados;
	
	private static boolean _mostrarBFS = false;
	private static boolean _verbose = false;

	public SimpleBFS(Instancia instancia, Map<Punto, Double> dualSolution, Semilla semilla, PadCache pads)
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

		_procesados = new ArrayList<Punto>();
		_nuevos = new ArrayList<Punto>();
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
		
		for(Punto coord: relevantCoordinates())
			addNuevo(closestFeasible(coord, Long.MAX_VALUE));
	}
	
	private Punto closestFeasible(Punto start, long pointsLimit)
	{
		_iniciados += 1;
		_pendientes = new ArrayList<Punto>();
		_indice = 0;

		add(start, Color.MAGENTA);

		for(Punto vecino: _instancia.snappedNeighbors(start))
			addPendiente(vecino, 0, 0);

		while( _indice < _pendientes.size() )
		{
			Punto actual = _pendientes.get(_indice);
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
	
	private void addNuevo(Punto nuevo)
	{
		if( nuevo != null && _nuevos.contains(nuevo) == false )
			_nuevos.add(nuevo);
	}
	
	private void addPendiente(Punto actual, int offsetx, int offsety)
	{
		Punto nuevo = new Punto(actual.getx() + offsetx, actual.gety() + offsety);
		
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
	
	private void add(Punto punto, Color color)
	{
		if( _panel != null )
			_panel.addGeometry(_instancia.getFactory().createPoint(punto.asCoordinate()), color);
	}
	
	private boolean cubierto(Punto punto)
	{
		return _dualSolution.keySet().stream().filter(c -> incluye(c,punto)).mapToDouble(c -> _dualSolution.get(c)).sum() >= _semilla.getValorizacion();
	}
	
	private boolean incluye(Punto centro, Punto punto)
	{
		return punto.getx() > centro.getx() - _semilla.getLargo() / 2 &&
				punto.getx() < centro.getx() + _semilla.getLargo() / 2 &&
				punto.gety() > centro.gety() - _semilla.getAncho() / 2 &&
				punto.gety() < centro.gety() + _semilla.getAncho() / 2;
	}
	
	private Set<Punto> relevantCoordinates()
	{
		ArrayList<Pad> pads = new ArrayList<Pad>();
		Set<Punto> ret = new HashSet<Punto>();
		
		for(Punto centro: _dualSolution.keySet())
			pads.add(new Pad(_instancia, _semilla, centro));
		
		for(Pad pad: pads)
		for(Punto c: pad.getVertices())
			ret.add(c);
		
		for(Pad primero: pads)
		for(Pad segundo: pads)
		for(Punto c: primero.verticesInterseccion(segundo))
			ret.add(c);
		
		ret.addAll(_interna.getVertices());
		return ret;
	}
	
	private void log(String texto)
	{
		if( _verbose == true )
			System.out.println(texto);
	}

	public ArrayList<Punto> getNuevos()
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
