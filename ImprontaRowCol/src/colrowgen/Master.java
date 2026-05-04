package colrowgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import general.Instancia;
import general.Punto;
import general.Solucion;
import interfaz.EntryPoint;

public class Master
{
	private Instancia _instancia;
	private ArrayList<Punto> _points;
	private Map<Punto, Integer> _iterationsWithNullPrimalValue;
	private Map<Punto, Integer> _iterationsWithNullDualValue;
	private Map<Punto, Integer> _iterationsWithNullDualSlack;
	private PadCache _pads;

	private Relajacion _relajacion;
	private Dualizer _dualizer;
	private Solucion _solucion;
	
	private int _eliminados = 0;
	private int _BFSs = 0;
	private int _explorados = 0;
	
	private static double _timeLimit = 3600;
	private static boolean _verbose = true;
	private static boolean _resumen = true;
	private static int _umbralEliminacionPrimal = Integer.MAX_VALUE;
	private static int _umbralEliminacionDual = Integer.MAX_VALUE;
	private static int _umbralAnulacionPrimal = Integer.MAX_VALUE;
//	private static int _umbralAnulacionDual = Integer.MAX_VALUE;
	
	public Master(Instancia instancia, List<Punto> iniciales)
	{
		_instancia = instancia;
		_points = new ArrayList<Punto>(iniciales);
		_pads = new PadCache(instancia);
		
		if( _umbralEliminacionPrimal < Integer.MAX_VALUE || _umbralAnulacionPrimal < Integer.MAX_VALUE )
			_iterationsWithNullPrimalValue = new HashMap<Punto, Integer>();
		
		if( _umbralEliminacionDual < Integer.MAX_VALUE )
		{
			_iterationsWithNullDualValue = new HashMap<Punto, Integer>();
			_iterationsWithNullDualSlack = new HashMap<Punto, Integer>();

			Dual.setRegistrarBindings(true);
		}
	}
	
	public void solve()
	{
		int iteracion = 1;
		long start = System.currentTimeMillis();
		boolean agregados = true;

		while( agregados == true && (System.currentTimeMillis() - start) / 1000.0 <= _timeLimit)
		{
			log("It: " + (iteracion++) + " | ");
			
			_relajacion = new Relajacion(_instancia, _points, anuladosPrimales(), _pads);
			_solucion = _relajacion.resolver();
			
			log("Rel: " + String.format("%.5f", _relajacion.getObjValue()) + " | " + _relajacion.varPoints().size() + " pts | " + _relajacion.getNumVariables() + " vars | " + _relajacion.getAnuladosPrimales() + " ap | " + _relajacion.getActiveVariables() + " nz | " + String.format("%.2f", _relajacion.getTime()) + " sec | ");
			
			_dualizer = new Dualizer(_relajacion);
			_dualizer.ejecutar();

			log("Dualizer: " + _dualizer.getNuevos().size() + " new pts | " + String.format("%.2f", _dualizer.getTotalTime()) + " sec | Dual: " + String.format("%.2f", _dualizer.getDualTime()) + " sec | BFSs: " + _dualizer.getIniciosBFS() + " | Expl: " + _dualizer.getExplorados() + " | ");

			eliminarPuntos();

			int anteriores = _points.size();
			for(Punto point: _dualizer.getNuevos()) if( _points.contains(point) == false )
				_points.add(point);
			
			agregados = _points.size() > anteriores;
			_BFSs += _dualizer.getIniciosBFS();
			_explorados += _dualizer.getExplorados();

			log("New pts: " + _dualizer.getNuevos().size() + " | Total: " + String.format("%.2f", (System.currentTimeMillis() - start) / 1000.0) + " sec \r\n");
		}
		
		if( _resumen == true )
			System.out.println("\r\nMaster | " + _instancia.getArchivo() + " | " + String.format("%.2f", (System.currentTimeMillis() - start) / 1000.0) + " sec | Obj: " + String.format("%.5f", _relajacion.getObjValue()) + " | " + (iteracion-1) + " its | " + _relajacion.varPoints().size() + " pts | " + _relajacion.getNumVariables() + " pvars | " + _relajacion.getNumConstraints() + " pcons | BFSs: " + _BFSs + " | Expl: " + String.format("%.2f", _BFSs > 0 ? _explorados / (double)_BFSs : 0) + " prom | " + _eliminados + " rem | " + EntryPoint.args() + "\r\n");
	}
	
	private void eliminarPuntos()
	{
		long start = System.currentTimeMillis();
		
		// Suma 1 a todos los puntos
		for(Punto point: _points)
		{
			if( _iterationsWithNullPrimalValue != null )
				_iterationsWithNullPrimalValue.put(point, _iterationsWithNullPrimalValue.containsKey(point) ? _iterationsWithNullPrimalValue.get(point) + 1 : 1);

			if( _iterationsWithNullDualValue != null )
				_iterationsWithNullDualValue.put(point, _iterationsWithNullDualValue.containsKey(point) ? _iterationsWithNullDualValue.get(point) + 1 : 1);

			if( _iterationsWithNullDualSlack != null )
				_iterationsWithNullDualSlack.put(point, _iterationsWithNullDualSlack.containsKey(point) ? _iterationsWithNullDualSlack.get(point) + 1 : 1);
		}
		
		// Pone en cero a los puntos activos

		if( _iterationsWithNullPrimalValue != null )
		{
			for(Punto point: _solucion.getCentros())
				_iterationsWithNullPrimalValue.put(point, 0);
		}

		if( _iterationsWithNullDualValue != null )
		{
			for(Punto point: _dualizer.getActiveVariables())
				_iterationsWithNullDualValue.put(point, 0);
		}

		if( _iterationsWithNullDualSlack != null )
		{
			for(Punto point: _dualizer.getBindingConstraints())
				_iterationsWithNullDualSlack.put(point, 0);
		}
		
		// Elimina los puntos con varias iteraciones en cero
		int anterior = _points.size();
		
		if( _iterationsWithNullPrimalValue != null && _iterationsWithNullDualValue != null )
		{
			for(Punto point: _iterationsWithNullPrimalValue.keySet()) if( _iterationsWithNullDualValue.containsKey(point) && _iterationsWithNullPrimalValue.get(point) > _umbralEliminacionPrimal && (_iterationsWithNullDualValue.get(point) > _umbralEliminacionDual || _iterationsWithNullDualSlack.get(point) > _umbralEliminacionDual) )
				_points.remove(point);
		}
		else if( _iterationsWithNullPrimalValue != null )
		{
			for(Punto point: _iterationsWithNullPrimalValue.keySet()) if( _iterationsWithNullPrimalValue.get(point) > _umbralEliminacionPrimal )
				_points.remove(point);
		}
		else if( _iterationsWithNullDualValue != null )
		{
			for(Punto point: _iterationsWithNullDualValue.keySet()) if( _iterationsWithNullDualValue.get(point) > _umbralEliminacionDual )
				_points.remove(point);
		}
		
		_eliminados += anterior - _points.size();
		
		log("EP: " + (anterior - _points.size()) + " rem | " + String.format("%.2f", (System.currentTimeMillis() - start) / 1000.0) + " sec | ");
	}
	
	private List<Punto> anuladosPrimales()
	{
		List<Punto> ret = new ArrayList<Punto>();
		
		if( _iterationsWithNullPrimalValue != null )
		{
			for(Punto point: _iterationsWithNullPrimalValue.keySet()) if( _iterationsWithNullPrimalValue.get(point) > _umbralAnulacionPrimal )
				ret.add(point);
		}
		
		return ret;
	}

	public Solucion getSolucion()
	{
		return _solucion;
	}
	
	public ArrayList<Punto> getPoints()
	{
		return _points;
	}
	
	private void log(String texto)
	{
		if( _verbose == true )
			System.out.print(texto);
	}
	
	public static void setVerbose(boolean value)
	{
		_verbose = value;
	}
	
	public static void setTimeLimit(double value)
	{
		_timeLimit = value;
	}
	
	public static void setUmbralEliminacionPrimal(int umbral)
	{
		_umbralEliminacionPrimal = umbral;
	}
	
	public static void setUmbralEliminacionDual(int umbral)
	{
		_umbralEliminacionDual = umbral;
	}
	
	public static void setUmbralAnulacionPrimal(int umbral)
	{
		_umbralAnulacionPrimal = umbral;
	}
}
