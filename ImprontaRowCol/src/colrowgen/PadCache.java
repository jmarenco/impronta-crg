package colrowgen;

import general.Instancia;
import general.Pad;
import general.Punto;
import general.Semilla;

public class PadCache
{
	private Instancia _instancia;
	private DoubleMap<Punto, Semilla, Pad> _map;
	
	public PadCache(Instancia instancia)
	{
		_instancia = instancia;
		_map = new DoubleMap<Punto, Semilla, Pad>();
	}
	
	public void add(Punto point)
	{
		for(Semilla semilla: _instancia.getSemillas())
			add(point, semilla);
	}
	
	public void add(Punto point, Semilla semilla)
	{
		if( _map.containsKey(point, semilla) == false )
		{
			Pad pad = new Pad(_instancia, semilla, point);
			
			if( pad.factible() == true )
				_map.put(point, semilla, pad);
			else
				_map.put(point, semilla, null);
		}
	}
	
	public boolean contains(Punto point, Semilla semilla)
	{
		return _map.containsKey(point, semilla) && _map.get(point, semilla) != null;
	}
	
	public Pad get(Punto point, Semilla semilla)
	{
		return _map.get(point, semilla);
	}
}
