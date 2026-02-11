package test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

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
}
