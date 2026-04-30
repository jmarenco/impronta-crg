package general;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

// Representa una instancia del problema
public class Instancia
{
	private Region _region;
	private int _pasoHorizontal;
	private int _pasoVertical;
	private ArrayList<Semilla> _semillas;
	private ArrayList<Restriccion> _restricciones;
	private Map<Semilla,Region> _internas;
	private GeometryFactory _factory;
	private String _archivo = "";
	
	public enum Formato { Nada, French, US };
	private static Formato _formato = Formato.Nada;
	private static boolean _verbose = false;
	
	// Constructor por defecto
	public Instancia()
	{
		_semillas = new ArrayList<Semilla>();
		_restricciones = new ArrayList<Restriccion>();
		_factory = new GeometryFactory();
		_internas = new HashMap<Semilla,Region>();
	}
	
	// Connstruye una instancia a partir de un archivo .xml
	public Instancia(String archivoXml)
	{
		_archivo = archivoXml;
		_region = new Region();
		_semillas = new ArrayList<Semilla>();
		_restricciones = new ArrayList<Restriccion>();
		_factory = new GeometryFactory();
		_internas = new HashMap<Semilla,Region>();
		
		log("Leyendo instancia ... \r\n");

		try
		{
			File f = new File(archivoXml);
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringElementContentWhitespace(true);
			factory.setIgnoringComments(true);
			    
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document documento = builder.parse(f);
			removeWhitespaceNodes(documento);
			
			if( documento.getChildNodes().getLength() == 0 )
				throw new RuntimeException("Error! El nodo raíz del archivo .xml no tiene hijos!");
				
			Node primero = documento.getChildNodes().item(0);
			
			if( primero.getNodeName() != "Parámetros" )
				throw new RuntimeException("Error! Se esperaba un nodo de tipo 'Parámetros' como el nodo principal del archivo .xml, y se obtuvo un nodo de tipo '" + documento.getNodeName() + "'");
			
			NodeList hijos = primero.getChildNodes();
			for(int i = 0; i < hijos.getLength(); i++)
			{
				Node nodo = hijos.item(i);
				
				// Compatibilidad hacia atrás (hasta la versión 0.69)
				if( nodo.getNodeName() == "Área" )
					obtenerArea(nodo);

				// A partir de la versión 0.70
				if( nodo.getNodeName() == "Áreas" )
					obtenerAreas(nodo);

				if( nodo.getNodeName() == "Semillas" )
					obtenerSemillas(nodo);

				if( nodo.getNodeName() == "Restricciones" )
					obtenerRestricciones(nodo);

				if( nodo.getNodeName() == "Esfuerzo_horizontal_mínimo" )
					obtenerPasos(nodo);
			}
			
			// Calcula los pasos de la discretización, si corresponde
			validarPasos();
	    }
	    catch (Exception e)
	    {
	    	System.out.println("No se pudo leer el archivo .xml!");
	    	System.out.println(e.getMessage());
	    	e.printStackTrace();
	    }
	}
	
	// Muestra el contenido del archivo .xml
	@SuppressWarnings("unused")
	private void recorrer(Node nodo)
	{
		if( nodo != null )
		{
			NamedNodeMap atributos = nodo.getAttributes();
			
			if( atributos != null )
			{
				for(int i = 0; i < atributos.getLength(); ++i)
				{
					Node atributo = atributos.item(i);
					log("  Atributo " + atributo.getNodeName() + ": " + atributo.getNodeValue());
				}
			}

			System.out.println(nodo.getNodeName() + " - " + nodo.getNodeValue());
			NodeList hijos = nodo.getChildNodes();
			
			for(int i = 0; i < hijos.getLength(); i++)
				recorrer( hijos.item(i) );
		}
	}
	
	// Elimina los nodos de texto y espacios en blanco
	private void removeWhitespaceNodes(Node e)
	{
		NodeList children = e.getChildNodes();
		for (int i = children.getLength() - 1; i >= 0; i--)
		{
			Node child = children.item(i);
			if (child instanceof Text && ((Text) child).getData().trim().length() == 0)
			{
				e.removeChild(child);
			}
			else if (child instanceof Node)
			{
				removeWhitespaceNodes((Node) child);
			}
		}
	}
	
	// Auxiliar: Convierte un String a double usando la especificación de formato, si corresponde
	private double toDouble(String s) throws ParseException
	{
		NumberFormat nf = null;
		
		if( _formato == Formato.French && s.contains("."))
			throw new ParseException("No se pudo convertir " + s + " a un valor numerico.", 0);
		
		if( _formato == Formato.US && s.contains(","))
			throw new ParseException("No se pudo convertir " + s + " a un valor numerico.", 0);

		if( _formato == Formato.French )
			nf = NumberFormat.getInstance(Locale.FRENCH);
		else if( _formato == Formato.US )
			nf = NumberFormat.getInstance(Locale.US);

		return nf != null ? nf.parse(s).doubleValue() : Double.parseDouble(s);
	}	
	
	// Obtiene varias áreas del archivo .xml
	private void obtenerAreas(Node nodo)
	{
		try
		{
			NodeList hijos = nodo.getChildNodes();
			for(int i = 0; i < hijos.getLength(); i++)
			{
				if( hijos.item(i).getNodeName() == "Área")
					obtenerArea(hijos.item(i));
			}
		}
	    catch (Exception e)
	    {
	    	System.out.println("No se pudieron leer los datos de las areas!");
	    	System.out.println(e.getMessage());
	    	e.printStackTrace();
	    }
	}
	
	// Obtiene el área del archivo .xml
	private void obtenerArea(Node nodo)
	{
		try
		{
			String yacimiento = nodo.getAttributes().getNamedItem("Capa").getNodeValue();
			String id = nodo.getAttributes().getNamedItem("ID").getNodeValue();
			
			log("Yacimiento: " + yacimiento + " - ID: " + id + "\r\n");
			
			NodeList hijos = nodo.getChildNodes();

			if( hijos.getLength() > 0 && hijos.item(0).getNodeName() == "Vértice" )
			{
				// Compatibilidad hacia atras, con el formato anterior a la versión 0.70 
				leerPoligono(hijos, true);
			}
			else
			{
				// A partir de la versión 0.70, lista de envolventes y de agujeros
				for(int i = 0; i < hijos.getLength(); i++)
				{
					if( hijos.item(i).getNodeName() == "Envolvente")
						leerPoligono(hijos.item(i).getChildNodes(), true);
					
					if( hijos.item(i).getNodeName() == "Agujero")
						leerPoligono(hijos.item(i).getChildNodes(), false);
				}
			}
		}
	    catch (Exception e)
	    {
	    	System.out.println("No se pudieron leer los datos del yacimiento!");
	    	System.out.println(e.getMessage());
	    	e.printStackTrace();
	    }
	}
	
	// Agrega una envolvente o un agujero a la región
	private void leerPoligono(NodeList hijos, boolean envolvente) throws ParseException
	{
		Coordinate[] coords = new Coordinate[hijos.getLength()];
		
		for(int i = 0; i < hijos.getLength(); i++)
		{
			String x = hijos.item(i).getAttributes().getNamedItem("X").getNodeValue();
			String y = hijos.item(i).getAttributes().getNamedItem("Y").getNodeValue();
			
			log("  -> x = " + toDouble(x) + ", y = " + toDouble(y) + (envolvente ? " (+)" : " (-)"));
			coords[i] = new Coordinate(toDouble(x), toDouble(y));
		}
		
		if( envolvente == true )
			_region.agregarEnvolvente(_factory.createPolygon(coords));
		else
			_region.agregarAgujero(_factory.createPolygon(coords));

		log("");
	}
	
	// Obtiene las semillas del archivo .xml
	private void obtenerSemillas(Node nodo)
	{
		try
		{
			log("Leyendo semillas \r\n");
			
			NodeList hijos = nodo.getChildNodes();
			for(int i = 0; i < hijos.getLength(); i++)
			{
				Node semilla = hijos.item(i);
				NodeList valores = semilla.getChildNodes();
				
				String nombre = semilla.getAttributes().getNamedItem("ID").getNodeValue();
				String coeficiente = semilla.getAttributes().getNamedItem("Coeficiente").getNodeValue();
				String largoPad = "0";
				String anchoPad = "0";
				String anchoLocacion = "0";
				String largoLocacion = "0";
				String offsetHorizontal = "";
				String offsetVertical = "";
				String toleranciaLocacion = "0";

				for(int j = 0; j < valores.getLength(); j++)
				{
					Node valor = valores.item(j);
					if( valor.getNodeName() == "PAD" )
					{
						largoPad = valor.getAttributes().getNamedItem("Largo").getNodeValue(); 
						anchoPad = valor.getAttributes().getNamedItem("Ancho").getNodeValue(); 
					}

					if( valor.getNodeName() == "LOCACION" )
					{
						largoLocacion = valor.getAttributes().getNamedItem("Largo").getNodeValue(); 
						anchoLocacion = valor.getAttributes().getNamedItem("Ancho").getNodeValue(); 
						toleranciaLocacion = valor.getAttributes().getNamedItem("Tolerancia").getNodeValue(); 
						
						try
						{
							// Cruzados a partir de la versión 0.69
							offsetHorizontal = valor.getAttributes().getNamedItem("Y").getNodeValue();
							offsetVertical = valor.getAttributes().getNamedItem("X").getNodeValue();
						}
						catch(Exception e)
						{
							offsetHorizontal = "";
							offsetVertical = "";
						}
					}
				}
				
				Semilla nueva = new Semilla(nombre, toDouble(largoPad), toDouble(anchoPad), toDouble(largoLocacion), toDouble(anchoLocacion), toDouble(toleranciaLocacion), toDouble(coeficiente));
				if( offsetHorizontal.length() > 0 && offsetVertical.length() > 0 )
				{
					nueva.setOffsetHorizontalLocacion(toDouble(offsetHorizontal));
					nueva.setOffsetVerticalLocacion(toDouble(offsetVertical));
				}
				
				log("  -> " + nueva);
				_semillas.add(nueva);
			}

			log("");
		}
	    catch (Exception e)
	    {
	    	System.out.println("No se pudieron leer los datos de las semillas!");
	    	System.out.println(e.getMessage());
	    	e.printStackTrace();
	    }
	}
	
	// Calcula valores por defecto para la discretización
	private void validarPasos()
	{
		if( _pasoHorizontal == 0 )
			throw new RuntimeException("El paso horizontal no puede ser cero!");

		if( _pasoVertical == 0 )
			throw new RuntimeException("El paso vertical no puede ser cero!");
	}
	
	// Obtiene las restricciones del archivo .xml
	private void obtenerRestricciones(Node nodo)
	{
		try
		{
			log("Leyendo Restricciones \r\n");
			
			String id = "";
			String ring = "";
			
			NodeList hijos = nodo.getChildNodes();
			for(int i = 0; i < hijos.getLength(); i++)
			{
				try
				{
					Node restriccion = hijos.item(i);
					NodeList puntos = restriccion.getChildNodes();
					Coordinate[] coords = new Coordinate[puntos.getLength()];
	
					id = restriccion.getAttributes().getNamedItem("ID").getNodeValue();
					ring = restriccion.getAttributes().getNamedItem("Ring").getNodeValue();
	
					for(int j = 0; j < puntos.getLength(); j++)
					{
						String x = puntos.item(j).getAttributes().getNamedItem("X").getNodeValue();
						String y = puntos.item(j).getAttributes().getNamedItem("Y").getNodeValue();
						
						coords[j] = new Coordinate(toDouble(x), toDouble(y));
					}
					
					log("  -> Restriccion: ID " + id + ", Ring " + ring + " = " + coords.length + " puntos");
					_restricciones.add(new Restriccion(id, ring, _factory.createPolygon(coords)));
				}
				catch(Exception e)
				{
					System.out.println();
					System.out.println("  -> Restriccion: ID " + id + ", Ring " + ring + " - No se pudo generar la restricción!");
					System.out.println("  -> Error: " + e.getMessage());
					System.out.println();
				}
				
				id = "";
				ring = "";
			}

			log("");
		}
	    catch (Exception e)
	    {
	    	System.out.println("No se pudieron leer las restricciones!");
	    	System.out.println(e.getMessage());
	    	e.printStackTrace();
	    }
	}	
	
	// Obtiene el área del archivo .xml
	private void obtenerPasos(Node nodo)
	{
		try
		{
			String pasoHorizontal = nodo.getAttributes().getNamedItem("Nx").getNodeValue();
			String pasoVertical = nodo.getAttributes().getNamedItem("Ny").getNodeValue();
			
			_pasoHorizontal = (int)toDouble(pasoHorizontal);
			_pasoVertical = (int)toDouble(pasoVertical);
			
			log("Parametros de la optimizacion \r\n");
			log("  -> Delta x: " + _pasoHorizontal + ", Delta y: " + _pasoVertical + " (input) \r\n");
		}
	    catch (Exception e)
	    {
	    	System.out.println("No se pudieron leer los parametros!");
	    	System.out.println(e.getMessage());
	    	e.printStackTrace();
	    }
	}	
	
	// Setters
	public void setRegion(Region region)
	{
		if( region == null )
			throw new IllegalArgumentException();
			
		_region = region;
	}
	public void setPasoHorizontal(int paso)
	{
		if( paso <= 0 )
			throw new IllegalArgumentException();

		_pasoHorizontal = paso;
	}
	public void setPasoVertical(int paso)
	{
		if( paso <= 0 )
			throw new IllegalArgumentException();

		_pasoVertical = paso;
	}
	public void agregarSemilla(Semilla s)
	{
		_semillas.add(s);
	}
	
	// Getters
	public Region getRegion()
	{
		return _region;
	}
	public ArrayList<Semilla> getSemillas()
	{
		return _semillas;
	}
	public ArrayList<Restriccion> getRestricciones()
	{
		return _restricciones;
	}
	public int getPasoHorizontal()
	{
		return _pasoHorizontal;
	}
	public int getPasoVertical()
	{
		return _pasoVertical;
	}
	public String getArchivo()
	{
		return _archivo;
	}
	
	// Manejo de la grilla discretizada
	public int snapx(double x)
	{
		return _pasoHorizontal * (int)(x / (double)_pasoHorizontal + 0.5);
	}
	public int snapy(double y)
	{
		return _pasoVertical * (int)(y / (double)_pasoVertical + 0.5);
	}
	public ArrayList<Punto> multisnap(Punto point)
	{
		ArrayList<Punto> ret = new ArrayList<Punto>();
		
		int bx = _pasoHorizontal * (int)(point.getx() / (double)_pasoHorizontal);
		int by = _pasoVertical * (int)(point.gety() / (double)_pasoVertical);

		ret.add(new Punto(bx, by));
		ret.add(new Punto(bx + _pasoHorizontal, by));
		ret.add(new Punto(bx, by + _pasoVertical));
		ret.add(new Punto(bx + _pasoHorizontal, by + _pasoVertical));
		
		return ret;
	}
	public ArrayList<Punto> snappedNeighbors(Punto point)
	{
		ArrayList<Punto> ret = new ArrayList<Punto>();
		
		int bx = _pasoHorizontal * (int)(snapx(point.getx()) / (double)_pasoHorizontal);
		int by = _pasoVertical * (int)(snapy(point.gety()) / (double)_pasoVertical);

		ret.add(new Punto(bx, by));
		ret.add(new Punto(bx - _pasoHorizontal, by));
		ret.add(new Punto(bx - _pasoHorizontal, by - _pasoVertical));
		ret.add(new Punto(bx, by - _pasoVertical));
		ret.add(new Punto(bx + _pasoHorizontal, by - _pasoVertical));
		ret.add(new Punto(bx + _pasoHorizontal, by));
		ret.add(new Punto(bx + _pasoHorizontal, by + _pasoVertical));
		ret.add(new Punto(bx, by + _pasoVertical));
		ret.add(new Punto(bx - _pasoHorizontal, by + _pasoVertical));
		
		return ret;
	}
	
	// Regiones internas
	public Region getRegionInterna(Semilla semilla)
	{
		if( _internas.containsKey(semilla) == false )
			_internas.put(semilla, RegionInterna.calcular(this, semilla));
		
		return _internas.get(semilla);
	}
	
	// Log
	private void log(String texto)
	{
		if( _verbose == true )
			System.out.println(texto);
	}
	
	// Configuración
	public static void set(Formato formato)
	{
		_formato = formato;
	}
	
	public static void setVerbose(boolean valor)
	{
		_verbose = valor;
	}
	
	// Obtiene un constructor de geometrías
	public GeometryFactory getFactory()
	{
		return _region.getFactory();
	}
}
