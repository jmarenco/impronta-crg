package general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class RegionInterna
{
	private Instancia _instancia;
	private Semilla _semilla;
	private Region _interna;
	private GeometryFactory _factory;
	
	public RegionInterna(Instancia instancia, Semilla semilla)
	{
		_instancia = instancia;
		_semilla = semilla;
		_factory = instancia.getFactory();
	}
	
	public static Region calcular(Instancia instancia, Semilla semilla)
	{
		return new RegionInterna(instancia, semilla).calcular();
	}
	
	public Region calcular()
	{
		_interna = new Region();
		
		for(Polygon polygon: _instancia.getRegion().getEnvolventes())
			_interna.agregarEnvolvente(reducido(polygon));
		
		for(Polygon polygon: _instancia.getRegion().getAgujeros())
			_interna.agregarEnvolvente(aumentado(polygon));
		
		return _interna;
	}
	
	private Polygon reducido(Polygon original)
	{
		Coordinate[] vertices = original.getCoordinates();
		Coordinate[] nuevos = new Coordinate[vertices.length];
		
		for(int i=0; i<vertices.length; ++i)
		{
			Coordinate vertice = vertices[i];
			
			List<Point> todas = new ArrayList<Point>();
			
			todas.add(_factory.createPoint(new Coordinate(vertice.x - _semilla.getLargo()/2, vertice.y - _semilla.getAncho()/2)));
			todas.add(_factory.createPoint(new Coordinate(vertice.x - _semilla.getLargo()/2, vertice.y + _semilla.getAncho()/2)));
			todas.add(_factory.createPoint(new Coordinate(vertice.x + _semilla.getLargo()/2, vertice.y - _semilla.getAncho()/2)));
			todas.add(_factory.createPoint(new Coordinate(vertice.x + _semilla.getLargo()/2, vertice.y + _semilla.getAncho()/2)));
			
			todas = todas.stream().filter(p -> original.contains(p)).collect(Collectors.toList());
			Collections.sort(todas, (p,q) -> (int)Math.signum(original.distance(q) - original.distance(p)));
			
			nuevos[i] = todas.get(0).getCoordinate();
		}
		
		return _factory.createPolygon(nuevos);
	}
	
	private Polygon aumentado(Polygon original)
	{
		Coordinate[] vertices = original.getCoordinates();
		Coordinate[] nuevos = new Coordinate[vertices.length];
		
		for(int i=0; i<vertices.length; ++i)
		{
			Coordinate vertice = vertices[i];
			
			List<Point> todas = new ArrayList<Point>();
			
			todas.add(_factory.createPoint(new Coordinate(vertice.x - _semilla.getLargo()/2, vertice.y - _semilla.getAncho()/2)));
			todas.add(_factory.createPoint(new Coordinate(vertice.x - _semilla.getLargo()/2, vertice.y + _semilla.getAncho()/2)));
			todas.add(_factory.createPoint(new Coordinate(vertice.x + _semilla.getLargo()/2, vertice.y - _semilla.getAncho()/2)));
			todas.add(_factory.createPoint(new Coordinate(vertice.x + _semilla.getLargo()/2, vertice.y + _semilla.getAncho()/2)));
			
			todas = todas.stream().filter(p -> !original.contains(p)).collect(Collectors.toList());
			Collections.sort(todas, (p,q) -> (int)Math.signum(original.distance(q) - original.distance(p)));
			
			nuevos[i] = todas.get(0).getCoordinate();
		}
		
		return _factory.createPolygon(nuevos);
	}
}
