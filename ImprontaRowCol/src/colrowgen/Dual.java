package colrowgen;

import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import general.Instancia;

public abstract class Dual
{
	protected Instancia _instancia;
	protected Relajacion _primal;
	protected GeometryFactory _factory;

	public Dual(Instancia instancia, Relajacion primal)
	{
		_instancia = instancia;
		_primal = primal;
		_factory = _instancia.getFactory();
	}
	
	public abstract Map<Point, Double> resolver(double timeLimit);
	public abstract double getTime();
	public abstract double getObjValue();
	public abstract Set<Point> getBindingConstraints();
}
