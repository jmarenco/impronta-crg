package colrowgen;

import java.util.ArrayList;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Semilla;

public class Dualizer 
{
	private Instancia _instancia;
	private ArrayList<Point> _puntos;
	private ArrayList<Point> _nuevos;
	private PadCache _pads;
	private double _target;
	
	private Map<Point, Double> _dualSolution;
	
	private long _start;
	private double _time;
	private double _dualTime;
	private long _iniciados;
	private long _explorados;
	
	private static boolean _verbose = false;
	private static boolean _mostrarPuntos = false;
	
	public Dualizer(Relajacion relajacion)
	{
		_instancia = relajacion.getInstancia();
		_pads = relajacion.getPadCache();
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
		_start = System.currentTimeMillis();
		_iniciados = 0;
		_explorados = 0;
		
		log("Resolviendo dual");
		Dual dual = new Dual(_instancia, _puntos, _pads, _target);

		_dualSolution = dual.resolver();
		_dualTime = dual.getTime();
		_nuevos = new ArrayList<Point>();
		
		for(Semilla semilla: _instancia.getSemillas())
		{
			MultiBFS multiBFS = new MultiBFS(_instancia, _dualSolution, semilla, _pads);
			multiBFS.ejecutar();
			
			_nuevos.addAll(multiBFS.getNuevos());
			_iniciados += multiBFS.getIniciados();
			_explorados += multiBFS.getExplorados();
		}
		
		_time = (System.currentTimeMillis() - _start) / 1000.0;
	}
	
	public Map<Point, Double> getDualSolution()
	{
		return _dualSolution;
	}
	
	public ArrayList<Point> getNuevos()
	{
		return _nuevos;
	}

	private void mostrarPuntos(Relajacion relajacion)
	{
		for(Point point: relajacion.varPoints())
			System.out.println("VarPoint " + point);

		for(Coordinate coordinate: relajacion.constraintPoints())
			System.out.println("ConstraintPoint " + coordinate);
	}
	
	private void log(String texto)
	{
		if( _verbose == true )
			System.out.println(texto);
	}
	
	public double getTotalTime()
	{
		return _time;
	}
	
	public double getDualTime()
	{
		return _dualTime;
	}
	
	public long getIniciosBFS()
	{
		return _iniciados;
	}
	
	public long getExplorados()
	{
		return _explorados;
	}
}
