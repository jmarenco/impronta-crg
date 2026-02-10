package general;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class Pad
{
	// Datos privados
	private Instancia _instancia;
	private OGIP _ogip;
	private Semilla _semilla;
	private Coordinate _centroPad;
	private Coordinate _centroLocacion;
	
	// Extremos
	private Polygon _perimetro;
	private Polygon _locacion;
	
	// Constructor
	public Pad(Instancia instancia, Semilla semilla, Coordinate centro)
	{
		_instancia = instancia;
		_ogip = _instancia.getOGIP();
		_semilla = semilla;
		_centroPad = centro;
		
		// Construye los puntos del perímetro
		Coordinate[] perimetro = new Coordinate[5];
		perimetro[0] = new Coordinate((int)(_centroPad.x - _semilla.getLargo()/2), (int)(_centroPad.y - _semilla.getAncho()/2));
		perimetro[1] = new Coordinate((int)(_centroPad.x - _semilla.getLargo()/2), (int)(_centroPad.y + _semilla.getAncho()/2));
		perimetro[2] = new Coordinate((int)(_centroPad.x + _semilla.getLargo()/2), (int)(_centroPad.y + _semilla.getAncho()/2));
		perimetro[3] = new Coordinate((int)(_centroPad.x + _semilla.getLargo()/2), (int)(_centroPad.y - _semilla.getAncho()/2));
		perimetro[4] = new Coordinate((int)(_centroPad.x - _semilla.getLargo()/2), (int)(_centroPad.y - _semilla.getAncho()/2));
		
		_perimetro = _instancia.getFactory().createPolygon(perimetro);
		
		// Candidatos a centros de la locación
		Coordinate centroLocacion = new Coordinate((int)(_centroPad.x - _semilla.getLargo()/2 + _semilla.getOffsetHorizontalLocacion()), (int)(_centroPad.y - _semilla.getAncho()/2 + _semilla.getOffsetVerticalLocacion()));

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
			_centroLocacion = c;
			
			if( factible() == true )
				break;
		}
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
	public Point getCentro()
	{
		return _instancia.getFactory().createPoint(_centroPad);
	}
	public Point getCentroLocacion()
	{
		return _instancia.getFactory().createPoint(_centroLocacion);
	}
	public Semilla getSemilla()
	{
		return _semilla;
	}
	
	// Determina si el pad contiene al punto
	public boolean contiene(Point punto)
	{
		return _perimetro.contains(punto);
	}
	public boolean contiene(Coordinate coordinate)
	{
		return contiene(_instancia.getFactory().createPoint(coordinate));
	}
	
	// Determina si los pads se intersecan
	public boolean interseca(Pad otro)
	{
		return _perimetro.intersects(otro._perimetro);
	}
	
	// Área del perímetro
	public double getArea()
	{
		return _perimetro.getArea() / 1e6;
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
	
	// Valorizacion del pad
	public double getValorizacion()
	{
		double coeficiente = _semilla.getCoeficiente() > 0 ? _semilla.getCoeficiente() : 1.0;
		double valor = _ogip == null ? getArea() : _ogip.valor(_perimetro);

		return valor / coeficiente;
	}
}
