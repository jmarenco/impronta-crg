package colrowgen;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

import com.vividsolutions.jts.geom.Point;

import general.Semilla;

public class DualCovering
{
	private Map<Rect,Double> _rects;
	
	public DualCovering(Map<Point,Double> dualSolution, Semilla semilla)
	{
		_rects = new HashMap<Rect,Double>();
		
		System.out.println("Construyendo dual covering");
		System.out.println();

		for(Point point: dualSolution.keySet())
			add(new Rect(point, semilla), dualSolution.get(point));

		System.out.println("Listo");
	}
	
	private void add(Rect agregado, double valor)
	{
		System.out.println("Agregando " + agregado);
		System.out.println();
		
		if( _rects.size() == 0 )
		{
			put(agregado, valor);
			return;
		}
		
		ArrayList<Rect> pendientes = new ArrayList<Rect>();
		pendientes.add(agregado);
		
		while( pendientes.size() > 0 )
		{
			Rect nuevo = pendientes.get(0);
			pendientes.remove(0);
			
			for(Rect rect: new ArrayList<Rect>(_rects.keySet()))
			{
				System.out.println("  Considerando " + rect);
				if( intersecan(rect, nuevo) )
				{
					// Elimina el rectángulo anterior
					double anterior = _rects.get(rect);
					remove(rect);
					
					// Rectangulos de "nuevo" por fuera de "rect"
					add(pendientes, nuevo.izquierda, rect.derecha, nuevo.arriba, nuevo.abajo);
					add(pendientes, Math.max(rect.izquierda, nuevo.izquierda), Math.min(rect.derecha, nuevo.derecha), rect.abajo, nuevo.abajo);
					add(pendientes, rect.derecha, nuevo.derecha, nuevo.arriba, nuevo.abajo);
					add(pendientes, Math.max(rect.izquierda, nuevo.izquierda), Math.min(rect.derecha, nuevo.derecha), rect.arriba, nuevo.arriba);
					
					// Rectangulos de "rect" por fuera de "nuevo"
					put(rect.izquierda, nuevo.derecha, rect.arriba, rect.abajo, anterior);
					put(Math.max(rect.izquierda, nuevo.izquierda), Math.min(rect.derecha, nuevo.derecha), nuevo.abajo, rect.abajo, anterior);
					put(nuevo.derecha, rect.derecha, rect.arriba, rect.abajo, anterior);
					put(Math.max(rect.izquierda, nuevo.izquierda), Math.min(rect.derecha, nuevo.derecha), nuevo.arriba, rect.arriba, anterior);
					
					// Interseccion
					put(Math.max(rect.izquierda, nuevo.izquierda), Math.min(rect.derecha, nuevo.derecha), Math.max(rect.arriba, nuevo.arriba), Math.min(rect.abajo, nuevo.abajo), anterior + valor);
				}
				else
				{
					put(nuevo, valor);
				}
				
				System.out.println("  Resultado:");
				for(Rect r: _rects.keySet())
					System.out.println("   - " + r);
				System.out.println();
			}

			System.out.println("  Pendientes:");
			for(Rect r: pendientes)
				System.out.println("   - " + r);
			System.out.println("  " + pendientes.size() + " pendientes");
			System.out.println();

			new Scanner(System.in).nextLine();
		}
	}
	
	private void add(ArrayList<Rect> pendientes, double izq, double der, double arr, double abj)
	{
		Rect nuevo = new Rect(izq, der, arr, abj);
		
		if( nuevo.izquierda < nuevo.derecha && nuevo.arriba < nuevo.abajo && !pendientes.contains(nuevo) && !existeSimilar(nuevo) )
			pendientes.add(nuevo);
	}
	
	private void put(double izq, double der, double arr, double abj, double valor)
	{
		put(new Rect(izq, der, arr, abj), valor);
	}
	
	private void put(Rect rect, double valor)
	{
		if( rect.izquierda < rect.derecha && rect.arriba < rect.abajo )
			_rects.put(rect, valor);
	}
	
	private void remove(Rect rect)
	{
		_rects.remove(rect);
	}
	
	private boolean intersecan(Rect uno, Rect otro)
	{
		return !(uno.derecha < otro.izquierda || uno.izquierda > otro.derecha || uno.arriba > otro.abajo || uno.abajo < otro.arriba);
	}
	
	private boolean existeSimilar(Rect rect)
	{
		return _rects.keySet().stream().anyMatch(r -> similares(r,rect));
	}
	
	private boolean similares(Rect uno, Rect otro)
	{
		return similares(uno.izquierda, otro.izquierda) && similares(uno.derecha, otro.derecha) && similares(uno.arriba, otro.arriba) && similares(uno.abajo, otro.abajo);
	}
	
	private boolean similares(double uno, double otro)
	{
		return Math.abs(uno - otro) < 1e-6;
	}
	
	public Set<Rect> getRectangulos()
	{
		return _rects.keySet();
	}
	
	public double get(Rect rect)
	{
		return _rects.get(rect);
	}

	public static class Rect
	{
		public double izquierda;
		public double derecha;
		public double arriba;
		public double abajo;
		
		public Rect(Point centro, Semilla semilla)
		{
			izquierda = centro.getX() - semilla.getLargo() / 2;
			derecha = centro.getX() + semilla.getLargo() / 2;
			arriba = centro.getY() - semilla.getAncho() / 2;
			abajo = centro.getY() + semilla.getAncho() / 2;

//			System.out.println("Constructing rect");
//			System.out.println("   " + centro);
//			System.out.println("   " + semilla);
//			System.out.println("   " + izquierda + " " + derecha + " " + arriba + " " + abajo);
		}
		
		public Rect(double izq, double der, double arr, double abj)
		{
			izquierda = izq;
			derecha = der;
			arriba = arr;
			abajo = abj;
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hash(abajo, arriba, derecha, izquierda);
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Rect other = (Rect) obj;
			return Double.doubleToLongBits(abajo) == Double.doubleToLongBits(other.abajo)
					&& Double.doubleToLongBits(arriba) == Double.doubleToLongBits(other.arriba)
					&& Double.doubleToLongBits(derecha) == Double.doubleToLongBits(other.derecha)
					&& Double.doubleToLongBits(izquierda) == Double.doubleToLongBits(other.izquierda);
		}
		
		@Override
		public String toString()
		{
			return "[" + izquierda + ", " + derecha + " : " + arriba + ", " + abajo + "]";
		}
	}

}
