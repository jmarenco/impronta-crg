package test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;

import general.Instancia;

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
		Coordinate c1 = new Coordinate(10,8);
		Coordinate c2 = new Coordinate(20,8);
		Coordinate c3 = new Coordinate(10,16);
		Coordinate c4 = new Coordinate(10,16);
		Coordinate[] expected = new Coordinate[] { c1, c2, c3, c4 };

		Asserts.equal(_instancia.multisnap(new Coordinate(15,14)), expected);
	}

	@Test
	public void snappedNeighborsTest()
	{
		Coordinate c1 = new Coordinate(0,8);
		Coordinate c2 = new Coordinate(0,16);
		Coordinate c3 = new Coordinate(0,24);
		Coordinate c4 = new Coordinate(10,8);
		Coordinate c5 = new Coordinate(10,24);
		Coordinate c6 = new Coordinate(20,8);
		Coordinate c7 = new Coordinate(20,16);
		Coordinate c8 = new Coordinate(20,24);
		Coordinate[] expected = new Coordinate[] { c1, c2, c3, c4, c5, c6, c7, c8 };

		Asserts.equal(_instancia.snappedNeighbors(new Coordinate(10,16)), expected);
		Asserts.equal(_instancia.snappedNeighbors(new Coordinate(9,15)), expected);
		Asserts.equal(_instancia.snappedNeighbors(new Coordinate(9,17)), expected);
		Asserts.equal(_instancia.snappedNeighbors(new Coordinate(11,15)), expected);
		Asserts.equal(_instancia.snappedNeighbors(new Coordinate(11,17)), expected);
	}
}
