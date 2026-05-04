package general;

// Representa una restriccion
public class Restriccion
{
	// Datos privados
	private String _id;
	private String _ring;
	private Poligono _poligono;
	
	// Construye la region
	public Restriccion(String id, String ring, Poligono poligono)
	{
		_id = id;
		_ring = ring;
		_poligono = poligono;
	}

	// Determina si interseca el poligono especificado
	public boolean interseca(Poligono poligono)
	{
		return poligono.getVertices().stream().anyMatch(v -> _poligono.contiene(v));
	}
	
	// Obtiene un identificador
	public String identificacion()
	{
		return "ID: " + _id + ", Ring: " + _ring;
	}

	// Getter
	public Poligono getPolygon()
	{
		return _poligono;
	}
}
