package colrowgen;

import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import general.Instancia;
import general.Pad;
import general.Semilla;

public class DualCovering
{
	// Resultado final
	private Map<Geometry,Double> _areas = new HashMap<Geometry,Double>();
	
	// Semilla
	private Semilla _semilla;

	// Constructor
	public DualCovering(Instancia instancia, Map<Point,Double> dualSolution, Semilla semilla)
	{
		_semilla = semilla;
		
		System.out.println("Construyendo dual covering");
		for(Point point: dualSolution.keySet())
		{
			Polygon nuevo = new Pad(instancia, semilla, point.getCoordinate()).getPerimetro();
			Geometry agregar = new Pad(instancia, semilla, point.getCoordinate()).getPerimetro();
			
			System.out.print(" - Agregando: " + nuevo + " con valor " + dualSolution.get(point));
			
			for(Geometry existente: new ArrayList<Geometry>(_areas.keySet()))
			{
				if( nuevo.intersects(existente) )
				{
					put(existente.difference(nuevo), _areas.get(existente));
					put(existente.intersection(nuevo), _areas.get(existente) + dualSolution.get(point));
				
					_areas.remove(existente);
				}
				
				agregar = agregar.difference(existente);
			}
			
			if( agregar.isEmpty() == false )
				put(agregar, dualSolution.get(point));
			
			System.out.println(", ahora son " + _areas.size() + " areas");
		}
	}
	
	private void put(Geometry geometry, double valor)
	{
		if( !geometry.isEmpty() && geometry.getArea() > 0 )
			_areas.put(geometry, valor);
	}
	
	public Set<Geometry> getAreas()
	{
		return _areas.keySet();
	}
	
	public double get(Geometry geom)
	{
		return _areas.get(geom);
	}
	
	public Semilla getSemilla()
	{
		return _semilla;
	}
}
