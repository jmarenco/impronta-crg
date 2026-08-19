package colrowgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import general.Instancia;

public class DualDesdePrimal extends Dual
{
	public DualDesdePrimal(Instancia instancia, Relajacion primal)
	{
		super(instancia, primal);
	}
	
	public Map<Point, Double> resolver(double timeLimit)
	{
		Map<Point, Double> ret = new HashMap<Point, Double>();
		Map<Coordinate, Double> dual = _primal.getDual();
		
		for(Coordinate c: dual.keySet())
			ret.put(_factory.createPoint(c), dual.get(c));
		
		return ret;
	}
	
	public double getTime()
	{
		return 0;
	}
	
	public double getObjValue()
	{
		return _primal.getObjValue();
	}
	
	public Set<Point> getBindingConstraints()
	{
		return null;
	}
}
