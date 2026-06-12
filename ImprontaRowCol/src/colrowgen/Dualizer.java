package colrowgen;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Semilla;

public class Dualizer 
{
	private Instancia _instancia;
	private Relajacion _relajacion;
	private ArrayList<Point> _nuevos;
	private PadCache _pads;
	
	private Map<Point, Double> _dualSolution;
	private Set<Point> _dualBindingConstraints;
	
	private long _start;
	private double _time;
	private double _timeLimit;
	private double _dualTime;
	private double _intersectionTime;
	private double _bfsTime;
	private long _iniciados;
	private long _explorados;
	
	private static boolean _verbose = false;
	private static boolean _mostrarPuntos = false;
	
	public Dualizer(Relajacion relajacion)
	{
		_relajacion = relajacion;
		_instancia = relajacion.getInstancia();
		_pads = relajacion.getPadCache();

		if( _mostrarPuntos == true )
			mostrarPuntos(relajacion);
	}
	
	public void ejecutar(double timeLimit)
	{
		_timeLimit = timeLimit;
		_start = System.currentTimeMillis();
		_iniciados = 0;
		_explorados = 0;
		
		log("Resolviendo dual");
		Dual dual = new Dual(_instancia, _relajacion);

		_dualSolution = dual.resolver(remainingTime());
		_dualTime = dual.getTime();
		_dualBindingConstraints = dual.getBindingConstraints();
		_nuevos = new ArrayList<Point>();
		_intersectionTime = 0;
		_bfsTime = 0;

		log("  Solucion: " + dual.getObjValue() + ", target primal: " + _relajacion.getObjValue());
		
		for(Semilla semilla: _instancia.getSemillas())
		{
			log("Semilla " + semilla.getLargo() + " x " + semilla.getAncho());
			
			SimpleBFS multiBFS = new SimpleBFS(_instancia, _dualSolution, semilla, _pads);
			multiBFS.ejecutar(remainingTime());
			
			_nuevos.addAll(multiBFS.getNuevos());
			_iniciados += multiBFS.getIniciados();
			_explorados += multiBFS.getExplorados();
			_intersectionTime += multiBFS.getIntersectionTime();
			_bfsTime += multiBFS.getBFSTime();
		}

		_time = elapsedTime();
	}
	
	private double elapsedTime()
	{
		return (System.currentTimeMillis() - _start) / 1000.0;
	}

	private double remainingTime()
	{
		return Math.max(0, _timeLimit - (System.currentTimeMillis() - _start) / 1000.0);
	}
	
	public Map<Point, Double> getDualSolution()
	{
		return _dualSolution;
	}
	
	public Set<Point> getDualBindingConstraints()
	{
		return _dualBindingConstraints;
	}
	
	public ArrayList<Point> getNuevos()
	{
		return _nuevos;
	}

	private void mostrarPuntos(Relajacion relajacion)
	{
		for(Point point: relajacion.varPoints())
			System.out.println("Relajacion VarPoint " + point);

		for(Coordinate coordinate: relajacion.constraintPoints())
			System.out.println("Relajacion ConstraintPoint " + coordinate);
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
	
	public double getIntersectionTime()
	{
		return _intersectionTime;
	}
	
	public double getBFSTime()
	{
		return _bfsTime;
	}
	
	public static void setVerbose(boolean value)
	{
		_verbose = value;
	}
}
