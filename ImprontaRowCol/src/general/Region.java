package general;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Region
{
	private ArrayList<Poligono> _envolventes;
	private ArrayList<Poligono> _agujeros;
	private ArrayList<Punto> _vertices; // Cache
	
	public Region()
	{
		_envolventes = new ArrayList<Poligono>();
		_agujeros = new ArrayList<Poligono>();
	}
	
	public void agregarEnvolvente(Poligono polygon)
	{
		_envolventes.add(polygon);
	}

	public void agregarAgujero(Poligono polygon)
	{
		_agujeros.add(polygon);
	}
	
	public ArrayList<Poligono> getEnvolventes()
	{
		return _envolventes;
	}
	
	public ArrayList<Poligono> getAgujeros()
	{
		return _agujeros;
	}

	// Determina si el punto está en el interior de la región
	public boolean incluye(Punto punto)
	{
		return _envolventes.stream().anyMatch(e -> e.contiene(punto)) && _agujeros.stream().allMatch(a -> !a.contiene(punto));
	}

	// Determina si el punto está en el interior o en el borde de la región
	public boolean cubre(Punto punto)
	{
		return _envolventes.stream().anyMatch(e -> e.cubre(punto)) && _agujeros.stream().allMatch(a -> !a.cubre(punto));
	}
	
	// Determina si contiene a los vértices del poligono
	public boolean contiene(Poligono poligono)
	{
		return poligono.getVertices().stream().allMatch(v -> this.incluye(v));
	}

	// Vertices de la region
	public List<Punto> getVertices()
	{
		if( _vertices == null )
		{
			Set<Punto> set = new HashSet<Punto>();
			
			for(Poligono polygon: _envolventes)
			for(Punto c: polygon.getVertices())
				set.add(c);
			
			for(Poligono polygon: _agujeros)
			for(Punto c: polygon.getVertices())
				set.add(c);
			
			_vertices = new ArrayList<Punto>();
			for(Punto c: set)
				_vertices.add(c);
		}

		return _vertices;
	}
}
