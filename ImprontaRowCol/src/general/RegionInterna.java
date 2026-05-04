package general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RegionInterna
{
	private Instancia _instancia;
	private Semilla _semilla;
	private Region _interna;
	
	public RegionInterna(Instancia instancia, Semilla semilla)
	{
		_instancia = instancia;
		_semilla = semilla;
	}
	
	public static Region calcular(Instancia instancia, Semilla semilla)
	{
		return new RegionInterna(instancia, semilla).calcular();
	}
	
	public Region calcular()
	{
		_interna = new Region();
		
		for(Poligono polygon: _instancia.getRegion().getEnvolventes())
			_interna.agregarEnvolvente(reducido(polygon));
		
		for(Poligono polygon: _instancia.getRegion().getAgujeros())
			_interna.agregarEnvolvente(aumentado(polygon));
		
		return _interna;
	}
	
	private Poligono reducido(Poligono original)
	{
		List<Punto> vertices = original.getVertices();
		List<Punto> nuevos = new ArrayList<Punto>();
		
		for(Punto vertice: vertices)
		{
			List<Punto> todas = new ArrayList<Punto>();
			
			todas.add(new Punto(vertice.getx() - _semilla.getLargo()/2, vertice.gety() - _semilla.getAncho()/2));
			todas.add(new Punto(vertice.getx() - _semilla.getLargo()/2, vertice.gety() + _semilla.getAncho()/2));
			todas.add(new Punto(vertice.getx() + _semilla.getLargo()/2, vertice.gety() - _semilla.getAncho()/2));
			todas.add(new Punto(vertice.getx() + _semilla.getLargo()/2, vertice.gety() + _semilla.getAncho()/2));
			
			todas = todas.stream().filter(p -> original.contiene(p)).collect(Collectors.toList());
			Collections.sort(todas, (p,q) -> (int)Math.signum(original.distancia(q) - original.distancia(p)));
			
			nuevos.add(todas.get(0));
		}
		
		return new Poligono(nuevos);
	}
	
	private Poligono aumentado(Poligono original)
	{
		List<Punto> vertices = original.getVertices();
		List<Punto> nuevos = new ArrayList<Punto>();
		
		for(Punto vertice: vertices)
		{
			List<Punto> todas = new ArrayList<Punto>();
			
			todas.add(new Punto(vertice.getx() - _semilla.getLargo()/2, vertice.gety() - _semilla.getAncho()/2));
			todas.add(new Punto(vertice.getx() - _semilla.getLargo()/2, vertice.gety() + _semilla.getAncho()/2));
			todas.add(new Punto(vertice.getx() + _semilla.getLargo()/2, vertice.gety() - _semilla.getAncho()/2));
			todas.add(new Punto(vertice.getx() + _semilla.getLargo()/2, vertice.gety() + _semilla.getAncho()/2));
			
			todas = todas.stream().filter(p -> !original.contiene(p)).collect(Collectors.toList());
			Collections.sort(todas, (p,q) -> (int)Math.signum(original.distancia(q) - original.distancia(p)));
			
			nuevos.add(todas.get(0));
		}
		
		return new Poligono(nuevos);
	}
}
