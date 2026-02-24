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
	
	// Instancia y semilla
	private Instancia _instancia;
	private Semilla _semilla;
	
	// Determina si se usa la región interna
	private static boolean _usarRegionInterna = true;

	// Constructor
	public DualCovering(Instancia instancia, Map<Point,Double> dualSolution, Semilla semilla)
	{
		_instancia = instancia;
		_semilla = semilla;
		
		for(Point point: dualSolution.keySet())
		{
			Polygon nuevo = new Pad(instancia, semilla, point.getCoordinate()).getPerimetro();
			Geometry agregar = new Pad(instancia, semilla, point.getCoordinate()).getPerimetro();
			
			for(Geometry existente: new ArrayList<Geometry>(_areas.keySet()))
			{
				Geometry intersection = existente.intersection(nuevo);
				if( intersection.getArea() > 0 )
				{
					put(existente.difference(nuevo), _areas.get(existente));
					put(intersection, _areas.get(existente) + dualSolution.get(point));
				
					_areas.remove(existente);
				}
				
				agregar = agregar.difference(existente);
			}
			
			if( agregar.isEmpty() == false )
				put(agregar, dualSolution.get(point));
		}
	}
	
	private void put(Geometry geometry, double valor)
	{
		for(int i=0; i<geometry.getNumGeometries(); ++i)
		{
			Geometry individual = geometry.getGeometryN(i);

			if( !individual.isEmpty() && individual.getArea() > 0 )
				_areas.put(individual, valor);
		}
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
	
	public Geometry uncovered()
	{
		Geometry ret = _usarRegionInterna ? _instancia.getRegionInterna(_semilla).getGeometry() : _instancia.getRegion().getGeometry();
		
		for(Geometry geom: this.getAreas())
			ret = ret.difference(geom);
		
		return ret;
	}
}
