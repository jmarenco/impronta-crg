package colrowgen;

import java.util.ArrayList;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import general.Instancia;

public class Dualizer 
{
	private Instancia _instancia;
	private ArrayList<Point> _puntos;
	private PadCache _pads;
	private double _target;
	
	private Map<Point, Double> _dualSolution;
	
	private static boolean _mostrarPuntos = false;
	
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

		if( _mostrarPuntos == true )
			mostrarPuntos(relajacion);
	}
	
	public void ejecutar()
	{
		Dual dual = new Dual(_instancia, _puntos, _pads, _target);
		_dualSolution = dual.resolver();
	}
	
	public Map<Point, Double> getDualSolution()
	{
		return _dualSolution;
	}

	private void mostrarPuntos(Relajacion relajacion)
	{
		for(Point point: relajacion.varPoints())
			System.out.println("VarPoint " + point);

		for(Coordinate coordinate: relajacion.constraintPoints())
			System.out.println("ConstraintPoint " + coordinate);
	}
}
