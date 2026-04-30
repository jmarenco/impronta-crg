package test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;

import general.Instancia;
import general.Punto;

public class SnapTest
{
	private Instancia _instancia;
	
	@Before
	public void inicializar()
	{
		_instancia = new Instancia();
		_instancia.setPasoHorizontal(10);
		_instancia.setPasoVertical(8);
	}

	@Test
	public void snapIndividualTest()
	{
		assertEquals(30, _instancia.snapx(29));
		assertEquals(30, _instancia.snapx(30));
		assertEquals(30, _instancia.snapx(31));
		assertEquals(24, _instancia.snapy(23));
		assertEquals(24, _instancia.snapy(24));
		assertEquals(24, _instancia.snapy(25));
	}

	@Test
	public void multiSnapTest()
	{
		Punto c1 = new Punto(10,8);
		Punto c2 = new Punto(20,8);
		Punto c3 = new Punto(10,16);
		Punto c4 = new Punto(10,16);
		Punto[] expected = new Punto[] { c1, c2, c3, c4 };

		Asserts.equal(_instancia.multisnap(new Punto(15,14)), expected);
	}

	@Test
	public void snappedNeighborsTest()
	{
		Punto c1 = new Punto(0,8);
		Punto c2 = new Punto(0,16);
		Punto c3 = new Punto(0,24);
		Punto c4 = new Punto(10,8);
		Punto c5 = new Punto(10,24);
		Punto c6 = new Punto(20,8);
		Punto c7 = new Punto(20,16);
		Punto c8 = new Punto(20,24);
		Punto[] expected = new Punto[] { c1, c2, c3, c4, c5, c6, c7, c8 };

		Asserts.equal(_instancia.snappedNeighbors(new Punto(10,16)), expected);
		Asserts.equal(_instancia.snappedNeighbors(new Punto(9,15)), expected);
		Asserts.equal(_instancia.snappedNeighbors(new Punto(9,17)), expected);
		Asserts.equal(_instancia.snappedNeighbors(new Punto(11,15)), expected);
		Asserts.equal(_instancia.snappedNeighbors(new Punto(11,17)), expected);
	}
}
