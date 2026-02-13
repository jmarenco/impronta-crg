package colrowgen;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

import com.vividsolutions.jts.geom.Point;

import general.Semilla;

@Deprecated
public class RectangularDualCovering
{
	// Resultado final
	private Map<Rect,Double> _rects = new HashMap<Rect,Double>();
	
	// Auxiliares para la construcción
	ArrayList<Double> valores = new ArrayList<Double>();
	ArrayList<Rect> rectangulos = new ArrayList<Rect>();

	public RectangularDualCovering(Map<Point,Double> dualSolution, Semilla semilla)
	{
		for(Point point: dualSolution.keySet())
		{
			ArrayList<Rect> nuevos = new ArrayList<Rect>();
			nuevos.add(new Rect(point, semilla));
			
			for(int i=0; i<rectangulos.size(); ++i)
			{
				Rect rect = rectangulos.get(i);
				double anterior = valores.get(i);
				
				for(Rect nuevo: new ArrayList<Rect>(nuevos)) if( intersecan(rect, nuevo) )
				{
					// Elimina los dos rectángulos
					rectangulos.remove(i);
					valores.remove(i);
					nuevos.remove(nuevo);
						
					// Rectangulos de "nuevo" por fuera de "rect"
					add(nuevos, nuevo.izquierda, rect.izquierda, nuevo.arriba, nuevo.abajo);
					add(nuevos, Math.max(rect.izquierda, nuevo.izquierda), Math.min(rect.derecha, nuevo.derecha), rect.abajo, nuevo.abajo);
					add(nuevos, rect.derecha, nuevo.derecha, nuevo.arriba, nuevo.abajo);
					add(nuevos, Math.max(rect.izquierda, nuevo.izquierda), Math.min(rect.derecha, nuevo.derecha), nuevo.arriba, rect.arriba);
						
					// Rectangulos de "rect" por fuera de "nuevo"
					put(rect.izquierda, nuevo.izquierda, rect.arriba, rect.abajo, anterior);
					put(Math.max(rect.izquierda, nuevo.izquierda), Math.min(rect.derecha, nuevo.derecha), nuevo.abajo, rect.abajo, anterior);
					put(nuevo.derecha, rect.derecha, rect.arriba, rect.abajo, anterior);
					put(Math.max(rect.izquierda, nuevo.izquierda), Math.min(rect.derecha, nuevo.derecha), rect.arriba, nuevo.arriba, anterior);
						
					// Interseccion
					put(Math.max(rect.izquierda, nuevo.izquierda), Math.min(rect.derecha, nuevo.derecha), Math.max(rect.arriba, nuevo.arriba), Math.min(rect.abajo, nuevo.abajo), anterior + dualSolution.get(point));
				}
			}
			
			for(Rect nuevo: nuevos)
			{
				rectangulos.add(nuevo);
				valores.add(dualSolution.get(point));
			}
		}
	}
	
	private void add(ArrayList<Rect> nuevos, double izq, double der, double arr, double abj)
	{
		Rect nuevo = new Rect(izq, der, arr, abj);
		
		if( nuevo.izquierda < nuevo.derecha && nuevo.arriba < nuevo.abajo && !nuevos.contains(nuevo) && !existeSimilar(nuevo) )
			nuevos.add(nuevo);
	}
	
	private void put(double izq, double der, double arr, double abj, double valor)
	{
		if( izq < der && arr < abj )
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
