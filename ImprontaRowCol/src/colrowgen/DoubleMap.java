package colrowgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DoubleMap<S,T,R>
{
	private Map<S, Map<T,R>> _map;
	
	public DoubleMap()
	{
		_map = new HashMap<S, Map<T,R>>();
	}
	
	public void put(S s, T t, R r)
	{
		if( _map.containsKey(s) == false )
			_map.put(s, new HashMap<T,R>());
		
		_map.get(s).put(t, r);
	}
	
	public R get(S s, T t)
	{
		if( _map.containsKey(s) == false )
			throw new RuntimeException("Key [" + s + ", " + t + "] not present in double map!");
		
		if( _map.get(s).containsKey(t) == false )
			throw new RuntimeException("Key [" + s + ", " + t + "] not present in double map!");
		
		return _map.get(s).get(t);
	}

	public boolean containsKey(S s, T t)
	{
		return _map.containsKey(s) ? _map.get(s).containsKey(t) : false;
	}
	
	public Set<R> valueSet()
	{
		Set<R> ret = new HashSet<R>();
		
		for(S s: _map.keySet())
		for(T t: _map.get(s).keySet())
			ret.add(_map.get(s).get(t));
		
		return ret;
	}
}
