package general;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.Point;

// Representa una solución
public class Solucion
{
	private Instancia _instancia;
	private ArrayList<Pad> _pads;
	
	public Solucion(Instancia instancia)
	{
		_instancia = instancia;
		_pads = new ArrayList<Pad>();
	}
	
	public void agregarPad(Pad pad)
	{
		_pads.add(pad);
	}
	
	public ArrayList<Pad> getPads()
	{
		return _pads;
	}
	
	// Area cubierta por la solución, en valor absoluto y en porcentaje del yacimiento
	public double areaCubierta()
	{
		double ret = 0;
		for(Pad pad: getPads())
			ret += pad.getArea();
		
		return ret;
	}
	public double porcentajeCubierto()
	{
		double total = _instancia.getRegion().getArea();
		return total > 0 ? areaCubierta() * 100.0 / total : 0;
	}
	
	// Centros de los pads de la solución
	public List<Point> getCentros()
	{
		return _pads.stream().map(p -> p.getCentro()).collect(Collectors.toList());
	}
}
