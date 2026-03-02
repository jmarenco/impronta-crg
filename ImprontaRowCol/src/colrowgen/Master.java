package colrowgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Solucion;

public class Master
{
	private Instancia _instancia;
	private ArrayList<Point> _points;
	private Map<Point, Integer> _nonzeroIterations;
	private PadCache _pads;

	private Solucion _solucion;
	
	private static boolean _verbose = true;
	private static boolean _eliminarPuntos = false;
	private static int _umbralEliminacion = 3;
	
	public Master(Instancia instancia, List<Point> iniciales)
	{
		_instancia = instancia;
		_points = new ArrayList<Point>(iniciales);
		_pads = new PadCache(instancia);
		
		if( _eliminarPuntos == true )
			_nonzeroIterations = new HashMap<Point, Integer>();
	}
	
	public void solve()
	{
		int iteracion = 1;
		long start = System.currentTimeMillis();
		boolean agregados = true;

		while( agregados == true )
		{
			log("It: " + (iteracion++) + " | ");
			
			Relajacion relajacion = new Relajacion(_instancia, _points, _pads);
			_solucion = relajacion.resolver();
			
			log("Rel: " + String.format("%.5f", relajacion.getObjValue()) + " | " + relajacion.varPoints().size() + " pts | " + String.format("%.2f", relajacion.getTime()) + " sec | ");
			
			Dualizer dualizer = new Dualizer(relajacion);
			dualizer.ejecutar();

			log("Dualizer: " + dualizer.getNuevos().size() + " new pts | " + String.format("%.2f", dualizer.getTotalTime()) + " sec | Dual: " + String.format("%.2f", dualizer.getDualTime()) + " sec | BFSs: " + dualizer.getIniciosBFS() + " | Expl: " + dualizer.getExplorados() + " | ");

			if( _eliminarPuntos == true  )
				eliminarPuntos();

			int anteriores = _points.size();
			for(Point point: dualizer.getNuevos()) if( _points.contains(point) == false )
				_points.add(point);
			
			agregados = _points.size() > anteriores;

			log("New pts: " + dualizer.getNuevos().size() + " | Total: " + String.format("%.2f", (System.currentTimeMillis() - start) / 1000.0) + " sec \r\n");
		}
	}
	
	private void eliminarPuntos()
	{
		long start = System.currentTimeMillis();
		
		// Actualiza las iteraciones en cero
		for(Point point: _points)
			_nonzeroIterations.put(point, _nonzeroIterations.containsKey(point) ? _nonzeroIterations.get(point) + 1 : 1);

		for(Point point: _solucion.getCentros())
			_nonzeroIterations.put(point, 0);
		
		// Elimina los puntos con varias iteraciones en cero
		for(Point point: _nonzeroIterations.keySet()) if( _nonzeroIterations.get(point) > _umbralEliminacion )
			_points.remove(point);
		
		log("EP: " + _points.size() + " pts | " + String.format("%.2f", (System.currentTimeMillis() - start) / 1000.0) + " sec | ");
	}
	
	public Solucion getSolucion()
	{
		return _solucion;
	}
	
	public ArrayList<Point> getPoints()
	{
		return _points;
	}
	
	private void log(String texto)
	{
		if( _verbose == true )
			System.out.print(texto);
	}
	
	public static void eliminarPuntos(int umbral)
	{
		_eliminarPuntos = true;
		_umbralEliminacion = umbral;
	}
}
