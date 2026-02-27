package colrowgen;

import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Pad;
import general.Semilla;

public class PadCache
{
	private Instancia _instancia;
	private DoubleMap<Point, Semilla, Pad> _map;
	
	public PadCache(Instancia instancia)
	{
		_instancia = instancia;
		_map = new DoubleMap<Point, Semilla, Pad>();
	}
	
	public void add(Point point)
	{
		for(Semilla semilla: _instancia.getSemillas())
			add(point, semilla);
	}
	
	public void add(Point point, Semilla semilla)
	{
		if( _map.containsKey(point, semilla) == false )
		{
			Pad pad = new Pad(_instancia, semilla, point.getCoordinate());
			
			if( pad.factible() == true )
				_map.put(point, semilla, pad);
			else
				_map.put(point, semilla, null);
		}
	}
	
	public boolean contains(Point point, Semilla semilla)
	{
		return _map.containsKey(point, semilla) && _map.get(point, semilla) != null;
	}
	
	public Pad get(Point point, Semilla semilla)
	{
		return _map.get(point, semilla);
	}
}
