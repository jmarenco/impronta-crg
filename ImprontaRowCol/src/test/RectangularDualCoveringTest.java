package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import colrowgen.RectangularDualCovering;
import general.Instancia;
import general.Semilla;

public class RectangularDualCoveringTest
{
	private Instancia _instancia;
	private GeometryFactory _factory;
	private Map<Point,Double> _dual;
	private RectangularDualCovering _covering;
	private Set<RectangularDualCovering.Rect> _rectangulos;
	
	@Before
	public void inicializar()
	{
		_instancia = new Instancia();
		_instancia.agregarSemilla(new Semilla(100, 40, 10, 4, 0, 1)); // 100 x 40
		_factory = new GeometryFactory();
		_dual = new HashMap<Point,Double>();
	}

	@Test
	public void unRectanguloTest()
	{
		put(50, 20, 0.75);
		createCovering();
		
		assertEquals(1, _rectangulos.size());
		assertContains(0, 100, 0, 40, 0.75);
	}

	@Test
	public void dosRectangulosDisjuntosTest()
	{
		put(50, 20, 0.75);
		put(150, 10, 0.8);
		createCovering();
		
		assertEquals(2, _rectangulos.size());
		assertContains(0, 100, 0, 40, 0.75);
		assertContains(100, 200, -10, 30, 0.8);
	}

	@Test
	public void dosRectangulosAlineadosHorizontalmenteTest()
	{
		put(50, 20, 0.3);
		put(120, 20, 0.4);
		createCovering();
		
		assertEquals(3, _rectangulos.size());
		assertContains(0, 70, 0, 40, 0.3);
		assertContains(70, 100, 0, 40, 0.7);
		assertContains(100, 170, 0, 40, 0.4);
	}

	@Test
	public void dosRectangulosAlineadosVerticalmenteTest()
	{
		put(50, 20, 0.3);
		put(50, 45, 0.4);
		createCovering();
		
		assertEquals(3, _rectangulos.size());
		assertContains(0, 100, 0, 25, 0.3);
		assertContains(0, 100, 25, 40, 0.7);
		assertContains(0, 100, 40, 65, 0.4);
	}

	@Test
	public void dosRectangulosSolapadosArribaDerechaTest()
	{
		put(50, 20, 0.3);
		put(90, 50, 0.4);
		createCovering();
		
		assertEquals(5, _rectangulos.size());
		assertContains(0, 40, 0, 40, 0.3);
		assertContains(40, 100, 0, 30, 0.3);
		assertContains(40, 100, 30, 40, 0.7);
		assertContains(40, 100, 40, 70, 0.4);
		assertContains(100, 140, 30, 70, 0.4);
	}

	@Test
	public void dosRectangulosSolapadosAbajoDerechaTest()
	{
		put(50, 20, 0.3);
		put(80, 0, 0.4);
		createCovering();
		
		assertEquals(5, _rectangulos.size());
		assertContains(0, 30, 0, 40, 0.3);
		assertContains(30, 100, 20, 40, 0.3);
		assertContains(30, 100, 0, 20, 0.7);
		assertContains(30, 100, -20, 0, 0.4);
		assertContains(100, 130, -20, 20, 0.4);
	}

	@Test
	public void tresRectangulosSinInterseccionComunTest()
	{
		put(50, 20, 0.3);
		put(90, 50, 0.4);
		put(80, 0, 0.2);
		createCovering();
		
		System.out.println("************");
		for(RectangularDualCovering.Rect r: _rectangulos)
			System.out.println(r);
		
		assertEquals(9, _rectangulos.size());
		assertContains(0, 30, 0, 40, 0.3);
		assertContains(30, 40, 20, 40, 0.3);
		assertContains(40, 100, 20, 30, 0.3);
		assertContains(40, 100, 30, 40, 0.7);
		assertContains(40, 100, 40, 70, 0.4);
		assertContains(100, 140, 30, 70, 0.4);
		assertContains(30, 100, 0, 20, 0.5);
		assertContains(30, 100, -20, 0, 0.2);
		assertContains(100, 130, -20, 20, 0.2);
	}
	
	private void put(double x, double y, double valor)
	{
		_dual.put(_factory.createPoint(new Coordinate(x,y)), valor);
	}
	
	private void createCovering()
	{
		_covering = new RectangularDualCovering(_dual, _instancia.getSemillas().get(0));
		_rectangulos = _covering.getRectangulos();
	}
	
	private void assertContains(double izquierda, double derecha, double arriba, double abajo, double valor)
	{
		for(RectangularDualCovering.Rect rect: _rectangulos)
		{
			if( rect.izquierda == izquierda && rect.derecha == derecha && rect.arriba == arriba && rect.abajo == abajo )
			{
				assertEquals(_covering.get(rect), valor, 0.001);
				return;
			}
		}
		
		fail("No contiene el rectangulo [" + izquierda + ", " + derecha + " : " + arriba + ", " + abajo + "]");
	}
}
