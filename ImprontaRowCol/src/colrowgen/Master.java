package colrowgen;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Solucion;

public class Master
{
	private Instancia _instancia;
	private ArrayList<Point> _points;
	private PadCache _pads;

	private Solucion _solucion;
	
	private static boolean _verbose = true;
	
	public Master(Instancia instancia, List<Point> iniciales)
	{
		_instancia = instancia;
		_points = new ArrayList<Point>(iniciales);
		_pads = new PadCache(instancia);
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

			int anteriores = _points.size();
			for(Point point: dualizer.getNuevos()) if( _points.contains(point) == false )
				_points.add(point);
			
			agregados = _points.size() > anteriores;

			log("New pts: " + dualizer.getNuevos().size() + " | Total: " + String.format("%.2f", (System.currentTimeMillis() - start) / 1000.0) + " sec \r\n");
		}
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
}
