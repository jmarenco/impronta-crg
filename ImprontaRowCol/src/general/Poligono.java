package general;

import java.util.ArrayList;
import java.util.List;

public class Poligono
{
	public ArrayList<Punto> _puntos;
	
	public Poligono()
	{
		_puntos = new ArrayList<Punto>();
	}
	
	public Poligono(List<Punto> puntos)
	{
		_puntos = new ArrayList<Punto>();

		for(Punto punto: puntos)
			add(punto);
	}
	
	public void add(Punto punto)
	{
		_puntos.add(punto);
	}
	
	public boolean contiene(Punto punto)
	{
	    boolean inside = false;
	    for (int i = 0, j = _puntos.size() - 1; i < _puntos.size(); j = i++)
	    {
	        if (((_puntos.get(i).gety() > punto.gety()) != (_puntos.get(j).gety() > punto.gety())) &&
	            (punto.getx() < (_puntos.get(j).getx() - _puntos.get(i).getx()) * (punto.gety() - _puntos.get(i).gety()) / (_puntos.get(j).gety() - _puntos.get(i).gety()) + _puntos.get(i).getx()))
	        {
	            inside = !inside;
	        }
	    }

	    return inside;
	}
	
	public boolean cubre(Punto punto)
	{
	    boolean inside = false;
	    for (int i = 0, j = _puntos.size() - 1; i < _puntos.size(); j = i++)
	    {
	        if (((_puntos.get(i).gety() >= punto.gety()) != (_puntos.get(j).gety() >= punto.gety())) &&
	            (punto.getx() <= (_puntos.get(j).getx() - _puntos.get(i).getx()) * (punto.gety() - _puntos.get(i).gety()) / (_puntos.get(j).gety() - _puntos.get(i).gety()) + _puntos.get(i).getx()))
	        {
	            inside = !inside;
	        }
	    }

	    return inside;
	}
	
	public ArrayList<Punto> getVertices()
	{
		return _puntos;
	}

	public double distancia(Punto q)
	{
		return _puntos.stream().mapToDouble(p -> p.distancia(q)).min().orElse(0);
	}
}
