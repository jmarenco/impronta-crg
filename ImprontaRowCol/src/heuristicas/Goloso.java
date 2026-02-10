package heuristicas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Pad;
import general.Solucion;
import heuristicas.Grafo.Clique;

// Representa el proceso de resolución
public class Goloso
{
	protected Instancia _instancia;
	protected Discretizacion _discretizacion;
	protected ArrayList<Pad> _pads;
	protected Grafo _grafo;
	protected Random _random;
	
	private static int _semilla = 0;
	private static int _intentos = 100;

	// Constructor
	public Goloso(Instancia instancia)
	{
		_instancia = instancia;
	}
	
	// Resuelve la instancia
	public Solucion resolver()
	{
		construirDiscretizacion();
		generarPads();
		construirGrafo();

		return construirSolucion();
	}
	
	// Construye una solución en forma golosa y semi-aleatoria
	public Solucion construirSolucion()
	{
		System.out.println("Construyendo solución ...");
		System.out.println();
		
		Solucion ret = null;
		double mejorValor = Double.MIN_VALUE;
		_random = new Random(_semilla);
		
		for(int i=0; i<_intentos; ++i)
		{
			Solucion actual = new Solucion(_instancia);
			double valorizacion = 0;
	
			// Vértices en orden descendente de valorización
			boolean[] pendientes = todosVerdaderos(_grafo.getVertices());
			ArrayList<Vertice> vertices = obtenerVertices(pendientes);
			
			// Mientras haya vértices para seleccionar ...
			while( vertices.size() > 0 )
			{
				// Selecciona entre los mejores en cuanto a la función objetivo
				int k = _random.nextInt(Math.min(Math.max(10, vertices.size() / 10), vertices.size()));
				Vertice v = vertices.get(k);
	
				// Lo agrega a la solución
				actual.agregar(_pads.get(v.numero));
				
				// Elimina de los posibles a todos los pads que intersecan al pad seleccionado
				for(Clique clique: _grafo.getCliquesDe(v.numero))
				for(Integer vecino: clique)
					pendientes[vecino] = false;
	
				vertices = obtenerVertices(pendientes);
				valorizacion += v.valorizacion;
			}
			
			System.out.print("  -> Solución " + (i+1) + "/" + _intentos + " - fobj: " + valorizacion);

			if( mejorValor < valorizacion )
			{
				ret = actual;
				mejorValor = valorizacion;
				System.out.print(" *");
			}
			
			System.out.println();			
		}
		
		return ret;
	}
	
	// Retorna un arreglo con todos los vértices en verdadero
	public boolean[] todosVerdaderos(int n)
	{
		boolean[] ret = new boolean[n];
		
		for(int i=0; i<n; ++i)
			ret[i] = true;
		
		return ret;
	}
	
	// Representa un vértice para la construcción de la solución
	private class Vertice implements Comparable<Vertice>
	{
		public int numero;
		public double valorizacion;
		
		public Vertice(int n, double v)
		{
			numero = n;
			valorizacion = v;
		}
		
		@Override public int hashCode()
		{
			return numero % 32;
		}

		@Override public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			if (numero != ((Vertice)obj).numero) return false;
			return true;
		}

		@Override public int compareTo(Vertice otro)
		{
			return valorizacion < otro.valorizacion ? 1 : (valorizacion == otro.valorizacion ? 0 : -1);
		}
	}
	
	// Construye un arreglo auxiliar de vértices
	private ArrayList<Vertice> obtenerVertices(boolean[] habilitados)
	{
		ArrayList<Vertice> ret = new ArrayList<Vertice>(_grafo.getVertices());
		
		for(int i=0; i<_grafo.getVertices(); ++i) if( habilitados[i] )
			ret.add(new Vertice(i, _grafo.getPeso(i)));
		
		Collections.sort(ret);
		return ret;
	}
	
	// Construye la discretización
	protected void construirDiscretizacion()
	{
		System.out.println("Construyendo discretizacion ...");
		System.out.println();
		System.out.println("  -> Delta x: " + _instancia.getPasoHorizontal() + ", Delta y: " + _instancia.getPasoVertical());

		_discretizacion = new Discretizacion(_instancia);

		System.out.println("  -> " + _discretizacion.getPuntos().getCoordinates().length + " puntos generados");
		System.out.println();
	}

	// Genera todos los pads factibles
	protected void generarPads()
	{
		System.out.println("Construyendo pads ...");
		System.out.println();
		
		_pads = _discretizacion.construirPads();
		
		System.out.println("  -> " + _pads.size() + " pads generados");
		System.out.println();
	}

	// Construye el grafo de intersecciones
	protected void construirGrafo()
	{
		_grafo = new Grafo(_pads.size());
		
		for(int i=0; i<_pads.size(); ++i)
			_grafo.setPeso(i, _pads.get(i).getValorizacion());
		
		int k = 0;
		for(Coordinate c: _discretizacion.getPuntos().getCoordinates())
		{
			Point punto = _instancia.getFactory().createPoint(c);
			HashSet<Integer> vertices = new HashSet<Integer>();
			
			for(int i=0; i<_pads.size(); ++i) if( _pads.get(i).contiene(punto) )
				vertices.add(i);
			
			_grafo.agregarClique(vertices);
			
			if( (++k) % (_discretizacion.getPuntos().getNumPoints() / 20) == 0 )
				System.out.println("  -> Grafo " + Math.round(k * 100.0 / _discretizacion.getPuntos().getNumPoints()) + "% construido" );
		}

		System.out.println();
	}
	
	// Retorna la discretización	
	public Discretizacion getDiscretizacion()
	{
		return _discretizacion;
	}
}
