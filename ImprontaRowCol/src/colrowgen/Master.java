package colrowgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Solucion;

public class Master
{
	private Instancia _instancia;
	private ArrayList<Point> _points;
	private Map<Point, Integer> _nullIterations;
	private PadCache _pads;

	private Solucion _solucion;
	
	private static boolean _verbose = true;
	private static boolean _eliminacionPrimal = true;
	private static boolean _eliminacionDual = true;
	private static int _umbralEliminacion = 3;
	
	public Master(Instancia instancia, List<Point> iniciales)
	{
		_instancia = instancia;
		_points = new ArrayList<Point>(iniciales);
		_pads = new PadCache(instancia);
		
		if( _eliminacionPrimal || _eliminacionDual )
			_nullIterations = new HashMap<Point, Integer>();

		if( _eliminacionDual )
			Dual.setRegistrarBindings(true);
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

			if( _eliminacionPrimal || _eliminacionDual )
				eliminarPuntos(dualizer.getDualBindingConstraints());

			int anteriores = _points.size();
			for(Point point: dualizer.getNuevos()) if( _points.contains(point) == false )
				_points.add(point);
			
			agregados = _points.size() > anteriores;

			log("New pts: " + dualizer.getNuevos().size() + " | Total: " + String.format("%.2f", (System.currentTimeMillis() - start) / 1000.0) + " sec \r\n");
		}
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
		
		log("EP: " + (anterior - _points.size()) + " rem | " + String.format("%.2f", (System.currentTimeMillis() - start) / 1000.0) + " sec | ");
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
	
	public static void eliminarPuntos(boolean primal, boolean dual, int umbral)
	{
		_eliminacionPrimal = primal;
		_eliminacionDual = dual;
		_umbralEliminacion = umbral;
	}
}
