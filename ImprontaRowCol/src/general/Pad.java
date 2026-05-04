package general;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Pad
{
	// Datos privados
	private Instancia _instancia;
	private Semilla _semilla;
	private Punto _centroPad;
	private Punto _centroLocacion;
	
	// Datos calculados
	private ArrayList<Punto> _vertices; // Del perimetro
	private int _izquierda;
	private int _derecha;
	private int _arriba;
	private int _abajo;
	
	// Extremos
	private Poligono _perimetro;
	private Poligono _locacion;
	
	// Constructor
	public Pad(Instancia instancia, Semilla semilla, Punto centro)
	{
		_instancia = instancia;
		_semilla = semilla;
		_centroPad = centro;

		_izquierda = (int)(_centroPad.getx() - _semilla.getLargo()/2);
		_derecha = (int)(_centroPad.getx() + _semilla.getLargo()/2);
		_arriba = (int)(_centroPad.gety() - _semilla.getAncho()/2);
		_abajo = (int)(_centroPad.gety() + _semilla.getAncho()/2);
		
		_vertices = new ArrayList<Punto>();
		_vertices.add(new Punto(_izquierda, _arriba));
		_vertices.add(new Punto(_izquierda, _abajo));
		_vertices.add(new Punto(_derecha, _abajo));
		_vertices.add(new Punto(_derecha, _arriba));
		
		// Construye los puntos del perímetro
		_perimetro = new Poligono();
		_perimetro.add(new Punto(_izquierda, _arriba));
		_perimetro.add(new Punto(_izquierda, _abajo));
		_perimetro.add(new Punto(_derecha, _abajo));
		_perimetro.add(new Punto(_derecha, _arriba));
		_perimetro.add(new Punto(_izquierda, _arriba));
		
		// Candidatos a centros de la locación
		Punto centroLocacion = new Punto((int)(_centroPad.getx() - _semilla.getLargo()/2 + _semilla.getOffsetHorizontalLocacion()), (int)(_centroPad.gety() - _semilla.getAncho()/2 + _semilla.getOffsetVerticalLocacion()));

		int tol = (int)semilla.getToleranciaLocacion();
		double raiz2 = Math.sqrt(2);

		ArrayList<Punto> centrosPosibles = new ArrayList<Punto>();
		centrosPosibles.add(new Punto(centroLocacion.getx(), centroLocacion.gety()));
		centrosPosibles.add(new Punto(centroLocacion.getx() + tol, centroLocacion.gety()));
		centrosPosibles.add(new Punto(centroLocacion.getx() + raiz2 * tol, centroLocacion.gety() + raiz2 * tol));
		centrosPosibles.add(new Punto(centroLocacion.getx(), centroLocacion.gety() + tol));
		centrosPosibles.add(new Punto(centroLocacion.getx() - raiz2 * tol, centroLocacion.gety() + raiz2 * tol));
		centrosPosibles.add(new Punto(centroLocacion.getx() - tol, centroLocacion.gety() + tol));
		centrosPosibles.add(new Punto(centroLocacion.getx() - raiz2 * tol, centroLocacion.gety() - raiz2 * tol));
		centrosPosibles.add(new Punto(centroLocacion.getx(), centroLocacion.gety() - tol));
		centrosPosibles.add(new Punto(centroLocacion.getx() + raiz2 * tol, centroLocacion.gety() - raiz2 * tol));
		centrosPosibles.add(new Punto(centroLocacion.getx(), centroLocacion.gety()));
		
		for(Punto c: centrosPosibles)
		{
			// Construye los puntos de la instalación
			_locacion = new Poligono();
			_locacion.add(new Punto((int)(c.getx() - _semilla.getLargoLocacion()/2), (int)(c.gety() - _semilla.getAnchoLocacion()/2)));
			_locacion.add(new Punto((int)(c.getx() - _semilla.getLargoLocacion()/2), (int)(c.gety() + _semilla.getAnchoLocacion()/2)));
			_locacion.add(new Punto((int)(c.getx() + _semilla.getLargoLocacion()/2), (int)(c.gety() + _semilla.getAnchoLocacion()/2)));
			_locacion.add(new Punto((int)(c.getx() + _semilla.getLargoLocacion()/2), (int)(c.gety() - _semilla.getAnchoLocacion()/2)));
			_locacion.add(new Punto((int)(c.getx() - _semilla.getLargoLocacion()/2), (int)(c.gety() - _semilla.getAnchoLocacion()/2)));
		
			_centroLocacion = c;
			
			if( factible() == true )
				break;
		}
	}
	
	// Getters de los datos
	public Punto getCentro()
	{
		return _centroPad;
	}
	public Punto getCentroLocacion()
	{
		return _centroLocacion;
	}
	public Semilla getSemilla()
	{
		return _semilla;
	}
	public List<Punto> getVertices()
	{
		return _vertices;
	}
	
	// Getters de las geometrías
	public Poligono getPerimetro()
	{
		return _perimetro;
	}
	public Poligono getLocacion()
	{
		return _locacion;
	}
	
	// Determina si el pad contiene (estrictamente) al punto
	public boolean contiene(Punto punto)
	{
		return _izquierda + 0.01 <= punto.getx() && punto.getx() <= _derecha - 0.01 && _arriba + 0.01 <= punto.gety() && punto.gety() <= _abajo - 0.01;
	}
	
	// Área del perímetro
	public double getArea()
	{
		return _semilla.getLargo() * _semilla.getAncho() / 1e6;
	}
	
	// Determina si la locacion se interseca con algún área restringida
	public boolean factible()
	{
		if( _instancia.getRegion().contiene(this.getPerimetro()) == false )
			return false;
		
		if( _instancia.getRegion().contiene(this.getLocacion()) == false )
			return false;
		
		for(Restriccion restriccion: _instancia.getRestricciones())
		{
			if( restriccion.interseca(_locacion) )
				return false;
		}
		
		return true;		
	}
	
	// Interseccion de pads
	public List<Punto> verticesInterseccion(Pad otro)
	{
		List<Punto> ret = new ArrayList<Punto>();
		
		if( this.getIzquierda() <= otro.getIzquierda() && this.getDerecha() >= otro.getIzquierda() && otro.getArriba() <= this.getArriba() && this.getArriba() <= otro.getAbajo() )
			ret.add(new Punto(otro.getIzquierda(), this.getArriba()));
		
		if( this.getIzquierda() <= otro.getIzquierda() && this.getDerecha() >= otro.getIzquierda() && otro.getArriba() <= this.getAbajo() && this.getAbajo() <= otro.getAbajo() )
			ret.add(new Punto(otro.getIzquierda(), this.getAbajo()));

		if( this.getIzquierda() <= otro.getDerecha() && this.getDerecha() >= otro.getDerecha() && otro.getArriba() <= this.getArriba() && this.getArriba() <= otro.getAbajo() )
			ret.add(new Punto(otro.getDerecha(), this.getArriba()));
		
		if( this.getIzquierda() <= otro.getDerecha() && this.getDerecha() >= otro.getDerecha() && otro.getArriba() <= this.getAbajo() && this.getAbajo() <= otro.getAbajo() )
			ret.add(new Punto(otro.getDerecha(), this.getAbajo()));
		
		if( otro.getIzquierda() <= this.getIzquierda() && otro.getDerecha() >= this.getIzquierda() && this.getArriba() <= otro.getArriba() && otro.getArriba() <= this.getAbajo() )
			ret.add(new Punto(this.getIzquierda(), otro.getArriba()));
		
		if( otro.getIzquierda() <= this.getIzquierda() && otro.getDerecha() >= this.getIzquierda() && this.getArriba() <= otro.getAbajo() && otro.getAbajo() <= this.getAbajo() )
			ret.add(new Punto(this.getIzquierda(), otro.getAbajo()));

		if( otro.getIzquierda() <= this.getDerecha() && otro.getDerecha() >= this.getDerecha() && this.getArriba() <= otro.getArriba() && otro.getArriba() <= this.getAbajo() )
			ret.add(new Punto(this.getDerecha(), otro.getArriba()));
		
		if( otro.getIzquierda() <= this.getDerecha() && otro.getDerecha() >= this.getDerecha() && this.getArriba() <= otro.getAbajo() && otro.getAbajo() <= this.getAbajo() )
			ret.add(new Punto(this.getDerecha(), otro.getAbajo()));

		return ret;
	}
	
	private double getIzquierda()
	{
		return _izquierda;
	}
	private double getDerecha()
	{
		return _derecha;
	}
	private double getArriba()
	{
		return _arriba;
	}
	private double getAbajo()
	{
		return _abajo;
	}
	
	// Valorizacion del pad
	public double getValorizacion()
	{
		double coeficiente = _semilla.getCoeficiente() > 0 ? _semilla.getCoeficiente() : 1.0;
		return getArea() / coeficiente;
	}
	
	@Override public String toString()
	{
		return _perimetro.toString();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(_centroLocacion, _centroPad, _semilla);
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
		Pad other = (Pad) obj;
		return Objects.equals(_centroLocacion, other._centroLocacion) && Objects.equals(_centroPad, other._centroPad)
				&& Objects.equals(_semilla, other._semilla);
	}
}
