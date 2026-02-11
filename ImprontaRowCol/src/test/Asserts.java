package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

public class Asserts
{
	public static <T> void equal(ArrayList<T> obtained, T[] expected)
	{
		assertEquals(obtained.size(), expected.length);
		
		for(T t: expected)
			assertTrue("No contiene " + t, obtained.contains(t));
	}
}
