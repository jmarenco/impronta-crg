package colrowgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import general.Instancia;

public class Dualizer 
{
	private Instancia _instancia;
	private ArrayList<Point> _puntos;
	private PadCache _pads;
	private double _target;
	
	public Dualizer(Relajacion relajacion)
	{
		_instancia = relajacion.getInstancia();
		_pads = relajacion.getPadCache();;
		_target = relajacion.getObjValue();

		_puntos = new ArrayList<Point>();

		for(Point point: relajacion.varPoints())
			_puntos.add(point);

		for(Coordinate coordinate: relajacion.constraintPoints())
			_puntos.add(_instancia.getFactory().createPoint(coordinate));
	}
	
	public void ejecutar()
	{
		Dual dual = new Dual(_instancia, _puntos, _pads, _target);
		Map<Point, Double> solucion = dual.resolver();
	}
}
