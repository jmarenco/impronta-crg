package colrowgen;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import general.Instancia;
import general.Punto;
import general.Semilla;

public class Dualizer 
{
	private Instancia _instancia;
	private Relajacion _relajacion;
	private ArrayList<Punto> _nuevos;
	private PadCache _pads;
	
	private Map<Punto, Double> _dualSolution;
	private Set<Punto> _dualBindingConstraints;
	private Set<Punto> _dualActiveVariables;
	
	private long _start;
	private double _time;
	private double _dualTime;
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
	
	public void ejecutar()
	{
		_start = System.currentTimeMillis();
		_iniciados = 0;
		_explorados = 0;
		
		log("Resolviendo dual");
		Dual dual = new Dual(_instancia, _relajacion);

		_dualSolution = dual.resolver();
		_dualTime = dual.getTime();
		_dualBindingConstraints = dual.getBindingConstraints();
		_dualActiveVariables = dual.getActiveVariables();
		_nuevos = new ArrayList<Punto>();

		log("  Solucion: " + dual.getObjValue() + ", target primal: " + _relajacion.getObjValue());
		
		for(Semilla semilla: _instancia.getSemillas())
		{
			log("Semilla " + semilla.getLargo() + " x " + semilla.getAncho());
			
			SimpleBFS bfs = new SimpleBFS(_instancia, _dualSolution, semilla, _pads);
			bfs.ejecutar();
			
			_nuevos.addAll(bfs.getNuevos());
			_iniciados += bfs.getIniciados();
			_explorados += bfs.getExplorados();
		}

		_time = (System.currentTimeMillis() - _start) / 1000.0;
	}
	
	public Map<Punto, Double> getDualSolution()
	{
		return _dualSolution;
	}
	
	public Set<Punto> getBindingConstraints()
	{
		return _dualBindingConstraints;
	}
	
	public Set<Punto> getActiveVariables()
	{
		return _dualActiveVariables;
	}
	
	public ArrayList<Punto> getNuevos()
	{
		return _nuevos;
	}

	private void mostrarPuntos(Relajacion relajacion)
	{
		for(Punto point: relajacion.varPoints())
			System.out.println("Relajacion VarPoint " + point);

		for(Punto coordinate: relajacion.constraintPoints())
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
	
	public static void setVerbose(boolean value)
	{
		_verbose = value;
	}
}
