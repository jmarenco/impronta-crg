package general;

import java.util.Objects;

public class Semilla
{
	private String _nombre;
	private double _largo;
	private double _ancho;
	private double _anchoLocacion;
	private double _largoLocacion;
	private double _offsetHorizontalLocacion;
	private double _offsetVerticalLocacion;
	private double _toleranciaLocacion;
	private double _coeficiente;
	
	public Semilla()
	{
	}
	
	public Semilla(double largo, double ancho, double largoInst, double anchoInst, double tolInst, double coef)
	{
		_nombre = "";
		_ancho = ancho;
		_largo = largo;
		_anchoLocacion = anchoInst;
		_largoLocacion = largoInst;
		_toleranciaLocacion = tolInst;
		_offsetHorizontalLocacion = _ancho/2;
		_offsetVerticalLocacion = _largo/2;
		_coeficiente = coef;
	}
	
	public Semilla(String nombre, double largo, double ancho, double largoInst, double anchoInst, double tolInst, double coef)
	{
		_nombre = nombre;
		_ancho = ancho;
		_largo = largo;
		_anchoLocacion = anchoInst;
		_largoLocacion = largoInst;
		_toleranciaLocacion = tolInst;
		_offsetHorizontalLocacion = _largo/2;
		_offsetVerticalLocacion = _ancho/2;
		_coeficiente = coef;
	}
	
	public Semilla(String nombre, double largo, double ancho, double largoInst, double anchoInst, double tolInst, double offsetHorizontal, double offsetVertical, double coef)
	{
		_nombre = nombre;
		_ancho = ancho;
		_largo = largo;
		_anchoLocacion = anchoInst;
		_largoLocacion = largoInst;
		_toleranciaLocacion = tolInst;
		_offsetHorizontalLocacion = offsetHorizontal;
		_offsetVerticalLocacion = offsetVertical;
		_coeficiente = coef;
	}
	
	public String getNombre()
	{
		return _nombre;
	}
	public double getLargo()
	{
		return _largo;
	}
	public double getAncho()
	{
		return _ancho;
	}
	public double getAnchoLocacion()
	{
		return _anchoLocacion;
	}
	public double getLargoLocacion()
	{
		return _largoLocacion;
	}
	public double getToleranciaLocacion()
	{
		return _toleranciaLocacion;
	}
	public double getOffsetHorizontalLocacion()
	{
		return _offsetHorizontalLocacion;
	}
	public double getOffsetVerticalLocacion()
	{
		return _offsetVerticalLocacion;
	}
	public double getCoeficiente()
	{
		return _coeficiente;
	}
	
	public void setLargo(double _largo)
	{
		this._largo = _largo;
	}
	public void setAncho(double _ancho)
	{
		this._ancho = _ancho;
	}
	public void setAnchoLocacion(double _anchoLocacion)
	{
		this._anchoLocacion = _anchoLocacion;
	}
	public void setLargoLocacion(double _largoLocacion)
	{
		this._largoLocacion = _largoLocacion;
	}
	public void setToleranciaLocacion(double _toleranciaLocacion)
	{
		this._toleranciaLocacion = _toleranciaLocacion;
	}
	public void setOffsetHorizontalLocacion(double offset)
	{
		this._offsetHorizontalLocacion = offset;
	}
	public void setOffsetVerticalLocacion(double offset)
	{
		this._offsetVerticalLocacion = offset;
	}
	public void setCoeficiente(double _coeficiente)
	{
		this._coeficiente = _coeficiente;
	}
	
	// Valorizacion del pad si no hay OGIP
	public double getValorizacion()
	{
		double coeficiente = this.getCoeficiente() > 0 ? this.getCoeficiente() : 1.0;
		double valor = this.getLargo() * this.getAncho() / 1e6;
	
		return valor / coeficiente;
	}

	@Override public String toString()
	{
		return _nombre + " - Pad = " + _largo + " x " + _ancho + " - Locacion: " + _largoLocacion + " x " + _anchoLocacion + " +/- " + _toleranciaLocacion + " - Offset locacion: (" + _offsetHorizontalLocacion + ", " + _offsetVerticalLocacion + ") - Coeficiente obj: " + _coeficiente;
	}

	@Override
	public int hashCode() 
	{
		return Objects.hash(_ancho, _anchoLocacion, _coeficiente, _largo, _largoLocacion, _nombre,
				_offsetHorizontalLocacion, _offsetVerticalLocacion, _toleranciaLocacion);
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
		Semilla other = (Semilla) obj;
		return Double.doubleToLongBits(_ancho) == Double.doubleToLongBits(other._ancho)
				&& Double.doubleToLongBits(_anchoLocacion) == Double.doubleToLongBits(other._anchoLocacion)
				&& Double.doubleToLongBits(_coeficiente) == Double.doubleToLongBits(other._coeficiente)
				&& Double.doubleToLongBits(_largo) == Double.doubleToLongBits(other._largo)
				&& Double.doubleToLongBits(_largoLocacion) == Double.doubleToLongBits(other._largoLocacion)
				&& Objects.equals(_nombre, other._nombre)
				&& Double.doubleToLongBits(_offsetHorizontalLocacion) == Double
						.doubleToLongBits(other._offsetHorizontalLocacion)
				&& Double.doubleToLongBits(_offsetVerticalLocacion) == Double
						.doubleToLongBits(other._offsetVerticalLocacion)
				&& Double.doubleToLongBits(_toleranciaLocacion) == Double.doubleToLongBits(other._toleranciaLocacion);
	}
}
