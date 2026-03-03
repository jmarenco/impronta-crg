package general;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Point;

// Representa una solución
public class Solucion
{
	private Instancia _instancia;
	private Map<Pad, Double> _pads;
	
	public Solucion(Instancia instancia)
	{
		_instancia = instancia;
		_pads = new HashMap<Pad, Double>();
	}
	
	public void agregar(Pad pad)
	{
		_pads.put(pad, 1.0);
	}
	
	public void agregar(Pad pad, double valor)
	{
		_pads.put(pad, valor);
	}
	
	public Set<Pad> getPads()
	{
		return _pads.keySet();
	}
	
	public double getValor(Pad pad)
	{
		return _pads.containsKey(pad) ? _pads.get(pad) : 0;
	}
	
	// Area cubierta por la solución, en valor absoluto y en porcentaje del yacimiento
	public double areaCubierta()
	{
		return getPads().stream().mapToDouble(p -> p.getArea()).sum();
	}
	public double porcentajeCubierto()
	{
		double total = _instancia.getRegion().getArea();
		return total > 0 ? areaCubierta() * 100.0 / total : 0;
	}
	
	// Valorizacion total
	public double valorizacion()
	{
		return getPads().stream().mapToDouble(p -> p.getValorizacion()).sum();
	}
	
	// Centros de los pads de la solución
	public List<Point> getCentros()
	{
		return _pads.keySet().stream().map(p -> p.getCentro()).collect(Collectors.toList());
	}
}
