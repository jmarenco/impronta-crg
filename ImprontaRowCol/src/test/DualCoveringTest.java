package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import colrowgen.DualCovering;
import general.Instancia;
import general.Region;
import general.Semilla;

public class DualCoveringTest
{
	private Instancia _instancia;
	private GeometryFactory _factory;
	private Map<Point,Double> _dual;
	private DualCovering _covering;
	private Set<Geometry> _areas;
	
	@Before
	public void inicializar()
	{
		_factory = new GeometryFactory();
		_instancia = new Instancia();
		_instancia.setRegion(new Region());
		_instancia.getRegion().agregarEnvolvente(_factory.createPolygon(new Coordinate[] { new Coordinate(0,0), new Coordinate(0,1), new Coordinate(1,1), new Coordinate(0,0) } ));
		_instancia.agregarSemilla(new Semilla(100, 40, 10, 4, 0, 1)); // 100 x 40
		_dual = new HashMap<Point,Double>();
	}

	@Test
	public void unRectanguloTest()
	{
		put(50, 20, 0.75);
		createCovering();
		
		assertEquals(1, _areas.size());
		assertContainsRectangle(0.75, 0, 100, 0, 40);
	}

	@Test
	public void dosRectangulosDisjuntosTest()
	{
		put(50, 20, 0.75);
		put(150, 10, 0.8);
		createCovering();
		
		assertEquals(2, _areas.size());
		assertContainsRectangle(0.75, 0, 100, 0, 40);
		assertContainsRectangle(0.8, 100, 200, -10, 30);
	}

	@Test
	public void dosRectangulosAlineadosHorizontalmenteTest()
	{
		put(50, 20, 0.3);
		put(120, 20, 0.4);
		createCovering();
		
		assertEquals(3, _areas.size());
		assertContainsRectangle(0.3, 0, 70, 0, 40);
		assertContainsRectangle(0.7, 70, 100, 0, 40);
		assertContainsRectangle(0.4, 100, 170, 0, 40);
	}

	@Test
	public void dosRectangulosAlineadosVerticalmenteTest()
	{
		put(50, 20, 0.3);
		put(50, 45, 0.4);
		createCovering();
		
		assertEquals(3, _areas.size());
		assertContainsRectangle(0.3, 0, 100, 0, 25);
		assertContainsRectangle(0.7, 0, 100, 25, 40);
		assertContainsRectangle(0.4, 0, 100, 40, 65);
	}

	@Test
	public void dosRectangulosSolapadosArribaDerechaTest()
	{
		put(50, 20, 0.3);
		put(90, 50, 0.4);
		createCovering();
		
		assertEquals(3, _areas.size());
		assertContains(0.3, 0, 0, 100, 0, 100, 30, 40, 30, 40, 40, 0, 40);
		assertContainsRectangle(0.7, 40, 100, 30, 40);
		assertContains(0.4, 40, 40, 100, 40, 100, 30, 140, 30, 140, 70, 40, 70);
	}

	@Test
	public void dosRectangulosSolapadosAbajoDerechaTest()
	{
		put(50, 20, 0.3);
		put(80, 0, 0.4);
		createCovering();
		
		assertEquals(3, _areas.size());
		assertContains(0.3, 0, 0, 30, 0, 30, 20, 100, 20, 100, 40, 0, 40);
		assertContainsRectangle(0.7, 30, 100, 0, 20);
		assertContains(0.4, 30, 0, 30, -20, 130, -20, 130, 20, 100, 20, 100, 0);
	}

	@Test
	public void tresRectangulosSinInterseccionComunTest()
	{
		put(50, 20, 0.3);
		put(90, 50, 0.4);
		put(80, 0, 0.2);
		createCovering();
		
		assertEquals(5, _areas.size());
		assertContains(0.3, 0, 0, 30, 0, 30, 20, 100, 20, 100, 30, 40, 30, 40, 40, 0, 40);
		assertContainsRectangle(0.7, 40, 100, 30, 40);
		assertContainsRectangle(0.5, 30, 100, 0, 20);
		assertContains(0.4, 40, 40, 100, 40, 100, 30, 140, 30, 140, 70, 40, 70);
		assertContains(0.2, 30, 0, 30, -20, 130, -20, 130, 20, 100, 20, 100, 0);
	}

	@Test
	public void tresRectangulosConInterseccionComunTest()
	{
		put(50, 20, 0.3);
		put(70, 40, 0.4);
		put(90, 30, 0.2);
		createCovering();
		
		assertEquals(7, _areas.size());
		assertContains(0.3, 0, 0, 100, 0, 100, 10, 40, 10, 40, 20, 20, 20, 20, 40, 0, 40);
		assertContainsRectangle(0.7, 20, 40, 20, 40);
		assertContainsRectangle(0.5, 40, 100, 10, 20);
		assertContainsRectangle(0.9, 40, 100, 20, 40);
		assertContains(0.4, 20, 40, 40, 40, 40, 50, 120, 50, 120, 60, 20, 60);
		assertContains(0.6, 40, 40, 100, 40, 100, 20, 120, 20, 120, 50, 40, 50);
		assertContains(0.2, 100, 10, 140, 10, 140, 50, 120, 50, 120, 20, 100, 20);
	}
	
	private void put(double x, double y, double valor)
	{
		_dual.put(_factory.createPoint(new Coordinate(x,y)), valor);
	}
	
	private void createCovering()
	{
		_covering = new DualCovering(_instancia, _dual, _instancia.getSemillas().get(0));
		_areas = _covering.getAreas();
	}
	
	private void assertContainsRectangle(double valor, double izquierda, double derecha, double arriba, double abajo)
	{
		assertContains(valor, izquierda, abajo, derecha, abajo, derecha, arriba, izquierda, arriba);
	}
	
	private void assertContains(double valor, double ...x)
	{
		Coordinate[] coords = new Coordinate[x.length/2+1];
		
		for(int i=0; i<x.length; i+=2)
			coords[i/2] = new Coordinate(x[i], x[i+1]);
		
		coords[coords.length-1] = new Coordinate(x[0], x[1]);
		
		Polygon polygon = _factory.createPolygon(coords);
		assertTrue(_areas.stream().anyMatch(a -> a.equals(polygon)));
		
		double value = _areas.stream().filter(a -> a.equals(polygon)).mapToDouble(a -> _covering.get(a)).sum();
		assertEquals(valor, value, 0.0001);
	}
}
