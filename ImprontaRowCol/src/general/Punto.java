package general;

import java.util.Objects;

import com.vividsolutions.jts.geom.Coordinate;

public class Punto
{
	private double _x;
	private double _y;
	
	public Punto(double x, double y)
	{
		_x = x;
		_y = y;
	}
	
	public double getx()
	{
		return _x;
	}
	
	public double gety()
	{
		return _y;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(_x, _y);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Punto other = (Punto) obj;
		return Double.doubleToLongBits(_x) == Double.doubleToLongBits(other._x)
				&& Double.doubleToLongBits(_y) == Double.doubleToLongBits(other._y);
	}
	
	public static Punto fromCoordinate(Coordinate coordinate)
	{
		return new Punto(coordinate.x, coordinate.y);
	}
	
	public Coordinate asCoordinate()
	{
		return new Coordinate(_x, _y);
	}
}
