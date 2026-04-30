package heuristicas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import general.Instancia;
import general.Pad;
import general.Punto;
import general.Solucion;
import heuristicas.Grafo.Clique;
import interfaz.EntryPoint;

// Representa el proceso de resolución
public class Goloso
{
	protected Instancia _instancia;
	protected Discretizacion _discretizacion;
	protected ArrayList<Pad> _pads;
	protected Grafo _grafo;
	protected Random _random;
	
	private int _pasoHorizontal;
	private int _pasoVertical;
	
	private static int _semilla = 0;
	private static int _intentos = 100;
	private static int _factorPasoHorizontal = 20;
	private static int _factorPasoVertical = 20;
	private static boolean _verbose = false;
	private static boolean _resumen = true;

	// Constructor
	public Goloso(Instancia instancia)
	{
		_instancia = instancia;
		_pasoHorizontal = instancia.getPasoHorizontal() * _factorPasoHorizontal;
		_pasoVertical = instancia.getPasoVertical() * _factorPasoVertical;
	}
	
	// Resuelve la instancia
	public Solucion resolver()
	{
		long start = System.currentTimeMillis();
		
		construirDiscretizacion();
		generarPads();
		construirGrafo();
		Solucion ret = construirSolucion();
		
		if( _resumen == true )
			System.out.println("Goloso | " + _instancia.getArchivo() + " | " + String.format("%.2f", (System.currentTimeMillis() - start) / 1000.0) + " sec | Obj: " + String.format("%.5f", ret.valorizacion()) + " | Area: " + String.format("%.5f", ret.areaCubierta()) + " | " + _discretizacion.size() + " pts | | | | | | " + EntryPoint.args() + "\r\n");

		return ret;
	}
	
	// Construye una solución en forma golosa y semi-aleatoria
	public Solucion construirSolucion()
	{
		log("Construyendo solución ... \r\n");
		
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
			
			String best = "";
			if( mejorValor < valorizacion )
			{
				ret = actual;
				mejorValor = valorizacion;
				best = " *";
			}
			
			log("  -> Solución " + (i+1) + "/" + _intentos + " - fobj: " + valorizacion + best);
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
		log("Construyendo discretizacion ... \r\n");
		log("  -> Delta x: " + _pasoHorizontal + ", Delta y: " + _pasoVertical);

		_discretizacion = new Discretizacion(_instancia, _pasoHorizontal, _pasoVertical);

		log("  -> " + _discretizacion.size() + " puntos generados \r\n");
	}

	// Genera todos los pads factibles
	protected void generarPads()
	{
		log("Construyendo pads ... \r\n");

		_pads = _discretizacion.construirPads();
		
		log("  -> " + _pads.size() + " pads generados \r\n");
	}

	// Construye el grafo de intersecciones
	protected void construirGrafo()
	{
		_grafo = new Grafo(_pads.size());
		
		for(int i=0; i<_pads.size(); ++i)
			_grafo.setPeso(i, _pads.get(i).getValorizacion());
		
		int k = 0;
		for(Punto punto: _discretizacion.getPuntos())
		{
			HashSet<Integer> vertices = new HashSet<Integer>();
			
			for(int i=0; i<_pads.size(); ++i) if( _pads.get(i).contiene(punto) )
				vertices.add(i);
			
			_grafo.agregarClique(vertices);
			
			if( _discretizacion.size() / 20 > 0 && (++k) % (_discretizacion.size() / 20) == 0 )
				log("  -> Grafo " + Math.round(k * 100.0 / _discretizacion.size()) + "% construido" );
		}

		log("");
	}
	
	// Retorna la discretización	
	public Discretizacion getDiscretizacion()
	{
		return _discretizacion;
	}
	
	// Log
	private void log(String texto)
	{
		if( _verbose == true )
			System.out.println(texto);
	}
	
	// Configuración
	public static void setVerbose(boolean valor)
	{
		_verbose = valor;
	}
	
	public static void setSemilla(int valor)
	{
		_semilla = valor;
	}
	
	public static void setIntentos(int valor)
	{
		_intentos = valor;
	}
	
	public static void setFactorPasoHorizontal(int valor)
	{
		_factorPasoHorizontal = valor;
	}
	
	public static void setFactorPasoVertical(int valor)
	{
		_factorPasoVertical = valor;
	}
}
