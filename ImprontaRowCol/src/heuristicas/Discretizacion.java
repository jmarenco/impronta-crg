package heuristicas;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

import general.Instancia;
import general.Pad;
import general.Punto;
import general.Semilla;

// Representa una discretización de la región
public class Discretizacion
{
	// Instancia y región asociadas
	private Instancia _instancia;
	private Geometry _yacimiento;
	
	// Pasos de la discretización
	private int _pasoHorizontal;
	private int _pasoVertical;
	
	// Centroide y longitud al punto más distante
	private Point _centroide;
	private double _radio;
	
	// Puntos de la discretización
	private ArrayList<Punto> _puntos;
	
	// Genera la discretización
	public Discretizacion(Instancia instancia)
	{
		construir(instancia, instancia.getPasoHorizontal(), instancia.getPasoVertical());
	}
	public Discretizacion(Instancia instancia, int pasoHorizontal, int pasoVertical)
	{
		construir(instancia, pasoHorizontal, pasoVertical);
	}

	// Construye la discretización
	private void construir(Instancia instancia, int pasoHorizontal, int pasoVertical)
	{
		_instancia = instancia;
		_pasoHorizontal = pasoHorizontal;
		_pasoVertical = pasoVertical;
		_yacimiento = instancia.getRegion().getGeometry();
	
		calcularRadio();

		MultiPoint rotados = _yacimiento.getFactory().createMultiPoint(generarPuntos());
		Geometry puntos = _yacimiento.intersection(rotados);
		
		if( puntos.getClass().getName().equals( rotados.getClass().getName()) == false )
			throw new RuntimeException("Error! La intersección de la región con un MultiPoint no retornó un MultiPoint. Se recibió: " + puntos.getClass().getName() );
		
		_puntos = new ArrayList<Punto>();
		
		for(int i=0; i<puntos.getNumGeometries(); ++i)
		for(var coord: puntos.getGeometryN(i).getCoordinates())
			_puntos.add(Punto.fromCoordinate(coord));
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
		
		for(double x=_instancia.snapx(_centroide.getX()-_radio); x<_centroide.getX()+_radio; x+=_pasoHorizontal)
		for(double y=_instancia.snapy(_centroide.getY()-_radio); y<_centroide.getY()+_radio; y+=_pasoVertical)
			puntos.add(new Coordinate(x, y));
		
		Coordinate[] ret = new Coordinate[puntos.size()];
		
		for(int i=0; i<puntos.size(); ++i)
			ret[i] = puntos.get(i);
		
		return ret;	
	}
	
	// Obtiene los puntos
	public List<Punto> getPuntos()
	{
		return _puntos;
	}
	
	public int size()
	{
		return _puntos.size();
	}
	
	// Construye pads centrados en los puntos de la discretización
	public ArrayList<Pad> construirPads()
	{
		ArrayList<Pad> ret = new ArrayList<Pad>();
	
		for(Semilla s: _instancia.getSemillas())
		for(Punto p: _puntos)
		{
			Pad pad = new Pad(_instancia, s, p);
			if( _yacimiento.contains( pad.getPerimetro() ) && _yacimiento.contains( pad.getLocacion() ) && pad.factible() )
				ret.add(pad);
		}
		
		return ret;
	}
}
