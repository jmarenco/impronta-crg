package colrowgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Pad;
import general.Semilla;
import general.Solucion;
import heuristicas.Discretizacion;
import interfaz.EntryPoint;

public class Master
{
	private Instancia _instancia;
	private ArrayList<Point> _points;
	private Map<Point, Integer> _nullIterations;
	private PadCache _pads;

	private Relajacion _relajacion;
	private Dualizer _dualizer;
	private Solucion _solucion;
	
	private int _eliminados = 0;
	private int _BFSs = 0;
	private int _explorados = 0;
	private long _start;
	
	private static double _timeLimit = 3600;
	private static boolean _verbose = true;
	private static boolean _resumen = true;
	private static boolean _discretizacionInicial = false;
	private static boolean _eliminacionPrimal = true;
	private static boolean _eliminacionDual = true;
	private static int _umbralEliminacion = 3;
	
	public Master(Instancia instancia, List<Point> iniciales)
	{
		_instancia = instancia;
		_points = new ArrayList<Point>(iniciales);
		_pads = new PadCache(instancia);
		
		if( _discretizacionInicial )
			agregarIniciales();
		
		if( _eliminacionPrimal || _eliminacionDual )
			_nullIterations = new HashMap<Point, Integer>();

		if( _eliminacionDual )
			Dual.setRegistrarBindings(true);
	}
	
	public void solve()
	{
		int iteracion = 1;
		_start = System.currentTimeMillis();
		boolean agregados = true;

		_relajacion = new Relajacion(_instancia, _points, _pads);

		while( agregados == true && elapsedTime() <= _timeLimit)
		{
			log("It: " + (iteracion++) + " | ");
			
			_solucion = _relajacion.resolver(remainingTime());
			
			log("Rel: " + String.format("%.5f", _relajacion.getObjValue()) + " | " + _relajacion.varPoints().size() + " pts | " /*+ _relajacion.getActiveVariables() + " nz | "*/ + String.format("%.2f", _relajacion.getTime()) + " sec | ");

			_dualizer = new Dualizer(_relajacion);
			_dualizer.ejecutar(remainingTime());

			log("Dualizer: " + _dualizer.getNuevos().size() + " new pts | " + String.format("%.2f", _dualizer.getTotalTime()) + " sec | Dual: " + String.format("%.2f", _dualizer.getDualTime()) + " sec | BFSs: " + _dualizer.getIniciosBFS() + " | Expl: " + _dualizer.getExplorados() + " | "); // + "Int: " + String.format("%.2f", _dualizer.getIntersectionTime()) + " sec | BFS: " + String.format("%.2f", _dualizer.getBFSTime()) + " sec | ");

			if( _eliminacionPrimal || _eliminacionDual )
				eliminarPuntos(_dualizer.getDualBindingConstraints());

			int anteriores = _points.size();
			for(Point point: _dualizer.getNuevos()) if( _points.contains(point) == false )
				_points.add(point);
			
			agregados = _points.size() > anteriores;
			_BFSs += _dualizer.getIniciosBFS();
			_explorados += _dualizer.getExplorados();

			log("New pts: " + _dualizer.getNuevos().size() + " | Total: " + String.format("%.2f", elapsedTime()) + " sec \r\n");
		}
		
		if( _resumen == true )
			System.out.println("\r\nv" + EntryPoint.version() + " | Master | " + _instancia.getArchivo() + " | " + String.format("%.2f", elapsedTime()) + " sec | Obj: " + String.format("%.5f", _relajacion.getObjValue()) + " | " + (iteracion-1) + " its | " + _relajacion.varPoints().size() + " pts | " + _relajacion.getNumVariables() + " pvars | " + _relajacion.getNumConstraints() + " pcons | BFSs: " + _BFSs + " | Expl: " + String.format("%.2f", _BFSs > 0 ? _explorados / (double)_BFSs : 0) + " prom | " + _eliminados + " rem | " + EntryPoint.args() + "\r\n");
	}
	
	private void eliminarPuntos(Set<Point> dualBindingConstraints)
	{
		long start = System.currentTimeMillis();
		
		// Actualiza las iteraciones en cero
		for(Point point: _points)
			_nullIterations.put(point, _nullIterations.containsKey(point) ? _nullIterations.get(point) + 1 : 1);

		if( _eliminacionPrimal == true )
		{
			for(Point point: _solucion.getCentros())
				_nullIterations.put(point, 0);
		}

		if( _eliminacionDual == true )
		{
			for(Point point: dualBindingConstraints)
				_nullIterations.put(point, 0);
		}
		
		// Elimina los puntos con varias iteraciones en cero
		int anterior = _points.size();
		for(Point point: _nullIterations.keySet()) if( _nullIterations.get(point) > _umbralEliminacion )
			_points.remove(point);
		
		_eliminados += anterior - _points.size();
		
		log("EP: " + (anterior - _points.size()) + " rem | " + String.format("%.2f", (System.currentTimeMillis() - start) / 1000.0) + " sec | ");
	}
	
	private void agregarIniciales()
	{
		Semilla semilla = _instancia.getSemillas().get(0);
		Discretizacion discretizacion = new Discretizacion(_instancia, (int)(semilla.getLargo()/2), (int)(semilla.getAncho()/2));

		for(Pad pad: discretizacion.construirPads()) if( _points.contains(pad.getCentro()) == false )
			_points.add(pad.getCentro());
	}

	private double elapsedTime()
	{
		return (System.currentTimeMillis() - _start) / 1000.0;
	}

	private double remainingTime()
	{
		return Math.max(0, _timeLimit - (System.currentTimeMillis() - _start) / 1000.0);
	}
	
	public Solucion getSolucion()
	{
		return _solucion;
	}
	
	public ArrayList<Point> getPoints()
	{
		return _points;
	}
	
	public List<Pad> getPads()
	{
		return _pads.getPads();
	}
	
	private void log(String texto)
	{
		if( _verbose == true )
			System.out.print(texto);
	}
	
	public static void eliminarPuntos(boolean primal, boolean dual, int umbral)
	{
		_eliminacionPrimal = primal;
		_eliminacionDual = dual;
		_umbralEliminacion = umbral;
	}
	
	public static void setVerbose(boolean value)
	{
		_verbose = value;
	}
	
	public static void setTimeLimit(double value)
	{
		_timeLimit = value;
	}
}
