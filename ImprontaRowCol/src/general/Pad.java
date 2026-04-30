package general;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

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
	private Polygon _perimetro;
	private Polygon _locacion;
	
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
		Coordinate[] perimetro = new Coordinate[5];
		perimetro[0] = new Coordinate(_izquierda, _arriba);
		perimetro[1] = new Coordinate(_izquierda, _abajo);
		perimetro[2] = new Coordinate(_derecha, _abajo);
		perimetro[3] = new Coordinate(_derecha, _arriba);
		perimetro[4] = new Coordinate(_izquierda, _arriba);
		
		_perimetro = _instancia.getFactory().createPolygon(perimetro);
		
		// Candidatos a centros de la locación
		Coordinate centroLocacion = new Coordinate((int)(_centroPad.getx() - _semilla.getLargo()/2 + _semilla.getOffsetHorizontalLocacion()), (int)(_centroPad.gety() - _semilla.getAncho()/2 + _semilla.getOffsetVerticalLocacion()));

		int tol = (int)semilla.getToleranciaLocacion();
		double raiz2 = Math.sqrt(2);

		ArrayList<Coordinate> centrosPosibles = new ArrayList<Coordinate>();
		centrosPosibles.add(new Coordinate(centroLocacion.x, centroLocacion.y));
		centrosPosibles.add(new Coordinate(centroLocacion.x + tol, centroLocacion.y));
		centrosPosibles.add(new Coordinate(centroLocacion.x + raiz2 * tol, centroLocacion.y + raiz2 * tol));
		centrosPosibles.add(new Coordinate(centroLocacion.x, centroLocacion.y + tol));
		centrosPosibles.add(new Coordinate(centroLocacion.x - raiz2 * tol, centroLocacion.y + raiz2 * tol));
		centrosPosibles.add(new Coordinate(centroLocacion.x - tol, centroLocacion.y + tol));
		centrosPosibles.add(new Coordinate(centroLocacion.x - raiz2 * tol, centroLocacion.y - raiz2 * tol));
		centrosPosibles.add(new Coordinate(centroLocacion.x, centroLocacion.y - tol));
		centrosPosibles.add(new Coordinate(centroLocacion.x + raiz2 * tol, centroLocacion.y - raiz2 * tol));
		centrosPosibles.add(new Coordinate(centroLocacion.x, centroLocacion.y));
		
		for(Coordinate c: centrosPosibles)
		{
			// Construye los puntos de la instalación
			Coordinate[] locacion = new Coordinate[5];
			locacion[0] = new Coordinate((int)(c.x - _semilla.getLargoLocacion()/2), (int)(c.y - _semilla.getAnchoLocacion()/2));
			locacion[1] = new Coordinate((int)(c.x - _semilla.getLargoLocacion()/2), (int)(c.y + _semilla.getAnchoLocacion()/2));
			locacion[2] = new Coordinate((int)(c.x + _semilla.getLargoLocacion()/2), (int)(c.y + _semilla.getAnchoLocacion()/2));
			locacion[3] = new Coordinate((int)(c.x + _semilla.getLargoLocacion()/2), (int)(c.y - _semilla.getAnchoLocacion()/2));
			locacion[4] = new Coordinate((int)(c.x - _semilla.getLargoLocacion()/2), (int)(c.y - _semilla.getAnchoLocacion()/2));
		
			_locacion = _instancia.getFactory().createPolygon(locacion);
			_centroLocacion = new Punto(c.x, c.y);
			
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
	public Polygon getPerimetro()
	{
		return _perimetro;
	}
	public Polygon getLocacion()
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
	
	// Determina si la locación se interseca con el área restringida
	private boolean interseca(Restriccion restriccion)
	{
		return restriccion.interseca(_locacion);
	}
	
	// Determina si la locacion se interseca con algún área restringida
	public boolean factible()
	{
		if( _instancia.getRegion().getGeometry().contains(this.getPerimetro()) == false )
			return false;
		
		if( _instancia.getRegion().getGeometry().contains(this.getLocacion()) == false )
			return false;
		
		for(Restriccion restriccion: _instancia.getRestricciones())
		{
			if( interseca(restriccion) )
				return false;
		}
		
		return true;		
	}
	
	// Interseccion de pads
	public List<Punto> verticesInterseccion(Pad otro)
	{
		ArrayList<Punto> ret = new ArrayList<Punto>();
		for(Coordinate coord: this.getPerimetro().intersection(otro.getPerimetro()).getCoordinates())
			ret.add(Punto.fromCoordinate(coord));
		
		return ret;
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
