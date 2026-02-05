package heuristicas;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Pad;
import general.Semilla;

// Representa una discretización de la región
public class Discretizacion
{
	// Instancia y región asociadas
	private Instancia _instancia;
	private Geometry _yacimiento;
	
	// Centroide y longitud al punto más distante
	private Point _centroide;
	private double _radio;
	
	// Puntos de la discretización
	private MultiPoint _puntos;
	
	// Genera la discretización
	public Discretizacion(Instancia instancia)
	{
		_instancia = instancia;
		_yacimiento = instancia.getRegion().getGeometry();
	
		calcularRadio();

		MultiPoint rotados = _yacimiento.getFactory().createMultiPoint(generarPuntos());
		Geometry puntos = _yacimiento.intersection(rotados);
		
		if( puntos.getClass().getName().equals( rotados.getClass().getName()) == false )
			throw new RuntimeException("Error! La intersección de la región con un MultiPoint no retornó un MultiPoint. Se recibió: " + puntos.getClass().getName() );
		
		_puntos = (MultiPoint)puntos;
	}
	
	// Calcula la distancia del centroide al punto más lejano
	public void calcularRadio()
	{
		_radio = 0;
		_centroide = _yacimiento.getCentroid();

		for(Coordinate c: _yacimiento.getCoordinates())
			_radio = Math.max(_radio,  _centroide.getCoordinate().distance(c));
	}
	
	// Genera puntos alineados con los ejes en un área que contiene a la región
	private Coordinate[] generarPuntos()
	{
		ArrayList<Coordinate> puntos = new ArrayList<Coordinate>();
		
		for(double x=_centroide.getX()-_radio; x<_centroide.getX()+_radio; x+=_instancia.getPasoHorizontal())
		for(double y=_centroide.getY()-_radio; y<_centroide.getY()+_radio; y+=_instancia.getPasoVertical())
			puntos.add(new Coordinate(x, y));
		
		Coordinate[] ret = new Coordinate[puntos.size()];
		
		for(int i=0; i<puntos.size(); ++i)
			ret[i] = puntos.get(i);
		
		return ret;	
	}
	
	// Obtiene los puntos
	public MultiPoint getPuntos()
	{
		return _puntos;
	}
	
	// Construye pads centrados en los puntos de la discretización
	public ArrayList<Pad> construirPads()
	{
		ArrayList<Pad> ret = new ArrayList<Pad>();
	
		for(Semilla s: _instancia.getSemillas())
		for(Coordinate c: _puntos.getCoordinates())
		{
			Pad pad = new Pad(_instancia, s, c);
			if( _yacimiento.contains( pad.getPerimetro() ) && _yacimiento.contains( pad.getLocacion() ) && pad.factible() )
				ret.add(pad);
		}
		
		return ret;
	}
}
