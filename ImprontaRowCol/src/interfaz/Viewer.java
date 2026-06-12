package interfaz;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import general.Instancia;
import general.OGIP;
import general.Restriccion;
import general.Semilla;
import general.Solucion;
import general.Pad;
import general.Region;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Viewer extends JPanel
{
	private static final long serialVersionUID = 1L;

	private List<Geometry> geometries = new ArrayList<Geometry>();
	private List<Color> colors = new ArrayList<Color>();
	private List<Color> fills = new ArrayList<Color>();
	private OGIP ogip = null;
	private Map<Point, Double> dual = null;
	private Instancia instancia = null; // Solo si hay una solución dual
  
    private int _minx = Integer.MAX_VALUE;
    private int _maxx = Integer.MIN_VALUE;
    private int _miny = Integer.MAX_VALUE;
    private int _maxy = Integer.MIN_VALUE;
    private int _margen = 20;
    
    private static boolean _latex = false;

    public void addGeometry(Geometry geom)
    {
    	addGeometry(geom, Color.BLACK);
    }
    
    public void addGeometry(Geometry geom, Color color)
    {
        geometries.add(geom);
        colors.add(color);
        fills.add(null);
        
        for(Coordinate c: geom.getCoordinates())
        {
        	_minx = Math.min(_minx, (int)c.x);
        	_maxx = Math.max(_maxx, (int)c.x);
        	_miny = Math.min(_miny, (int)c.y);
        	_maxy = Math.max(_maxy, (int)c.y);
        }
    }
    
    public void addGeometry(Geometry geom, Color color, Color fill)
    {
        geometries.add(geom);
        colors.add(color);
        fills.add(fill);
        
        for(Coordinate c: geom.getCoordinates())
        {
        	_minx = Math.min(_minx, (int)c.x);
        	_maxx = Math.max(_maxx, (int)c.x);
        	_miny = Math.min(_miny, (int)c.y);
        	_maxy = Math.max(_maxy, (int)c.y);
        }
    }
    
    public void addOGIP(OGIP obj)
    {
    	ogip = obj;
    }
    
    public void addDual(Instancia instance, Map<Point, Double> dualSolution)
    {
    	instancia = instance;
    	dual = dualSolution;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        if( dual != null )
        	drawDualCovering(g2d);

        if (!geometries.isEmpty())
        {
        	for(int i=0; i<geometries.size(); ++i)
            {
        		Geometry geom = geometries.get(i);
        		g2d.setColor(colors.get(i));
        		
            	if( geom.getClass().getName().contains("Polygon") )
            		drawPolygon((Polygon)geom, g2d);

            	if( geom.getClass().getName().contains("MultiPoint") )
            		drawMultiPoint((MultiPoint)geom, g2d);

            	if( geom.getClass().getName().contains("LineString") )
            		drawLineString((LineString)geom, g2d);

            	if( geom.getClass().getName().contains(".Point") )
            		drawPoint((Point)geom, g2d);
            }
        }
        
        if( ogip != null )
        	drawOGIP(g2d);
    }

    private void drawPolygon(Polygon polygon, Graphics2D g2d)
    {
    	Coordinate[] c = polygon.getCoordinates();
    	for(int i=0; i+1<c.length; ++i)
    		g2d.drawLine(convx(c[i]), convy(c[i]), convx(c[i+1]), convy(c[i+1]));

		g2d.drawLine(convx(c[c.length-1]), convy(c[c.length-1]), convx(c[0]), convy(c[0]));
    }

    private void drawLineString(LineString polygon, Graphics2D g2d)
    {
    	Coordinate[] c = polygon.getCoordinates();
    	for(int i=0; i+1<c.length; ++i)
    		g2d.drawLine(convx(c[i]), convy(c[i]), convx(c[i+1]), convy(c[i+1]));
    }

    private void drawMultiPoint(MultiPoint points, Graphics2D g2d)
    {
    	for(Coordinate c: points.getCoordinates())
    	{
    		Ellipse2D.Double circle = new Ellipse2D.Double(convx(c), convy(c), 2, 2);
    		g2d.fill(circle);
    	}
    }

    private void drawPoint(Point point, Graphics2D g2d)
    {
 		Ellipse2D.Double circle = new Ellipse2D.Double(convx(point.getCoordinate()), convy(point.getCoordinate()), 4, 4);
   		g2d.fill(circle);
    }
    
    private void drawOGIP(Graphics2D g2d)
    {
    	double min = ogip.minMedicion();
    	double max = ogip.maxMedicion();
    	
    	if( max <= min )
    		return;
    	
    	for(Coordinate c: ogip.getPuntos())
    	{
    		int nivel = 255 - (int)((ogip.getValor(c) - min) * 255 / (max - min));
    		g2d.setColor(new Color(nivel, nivel, nivel));
    		
    		Ellipse2D.Double circle = new Ellipse2D.Double(convx(c), convy(c), 3, 3);
    		g2d.fill(circle);
    	}
    }
    
    private void drawDualCovering(Graphics2D g2d)
    {
    	for(Semilla semilla: instancia.getSemillas())
    	for(Point punto: dual.keySet())
		{
   			Pad pad = new Pad(instancia, semilla, punto.getCoordinate());
			Coordinate[] coords = pad.getPerimetro().getCoordinates();
			
			int[] x = new int[coords.length];
			int[] y = new int[coords.length];

			for(int i=0; i<coords.length; ++i)
			{
				x[i] = convx(coords[i]);
				y[i] = convy(coords[i]);
			}
			
    		double targetArea = semilla.getAncho() * semilla.getLargo()/ 1e6;
   			double valor = Math.min(targetArea, dual.get(punto));
   			int nivel = (int)(255 * (targetArea - valor) / targetArea);

   			g2d.setColor(new Color(nivel, nivel, nivel));
    		g2d.fillPolygon(x, y, x.length);
    		g2d.setColor(Color.DARK_GRAY);
    		g2d.drawPolygon(x, y, x.length);
		}
    }

    private int convx(Coordinate c)
    {
    	return _margen + ((int)c.x - _minx) * (getWidth() - 2*_margen) / (_maxx - _minx);
    }
    
    private int convy(Coordinate c)
    {
    	return -_margen + getHeight() - ((int)c.y - _miny) * (getHeight() - 2*_margen) / (_maxy - _miny);
    }
    
    private int convxo(Coordinate c)
    {
    	return (int)((c.x - _minx) * 500.0 / (_maxx - _minx));
    }

    private int convyo(Coordinate c)
    {
    	return (int)((c.y - _miny) * 500.0 / (_maxy - _miny));
    }

    public void printLatex()
    {
		System.out.println("\\begin{figure}");
		System.out.println("\\begin{center}");
		System.out.println("\\begin{adjustbox}{max width=0.7\\textwidth}");
		System.out.println("\\begin{tikzpicture}[scale=0.03]");

		if( dual != null )
        	printDualCoveringLatex();

        if (!geometries.isEmpty())
        {
        	for(int i=0; i<geometries.size(); ++i)
            {
        		Geometry geom = geometries.get(i);
        		
            	if( geom.getClass().getName().contains("Polygon") )
            		printPolygonLatex((Polygon)geom, colors.get(i), fills.get(i));

            	if( geom.getClass().getName().contains("MultiPoint") )
            		printMultiPointLatex((MultiPoint)geom, colors.get(i));

            	if( geom.getClass().getName().contains("LineString") )
            		printLineStringLatex((LineString)geom, colors.get(i));

            	if( geom.getClass().getName().contains(".Point") )
            		printPointLatex((Point)geom, colors.get(i));
            }
        }

        System.out.println("\\end{tikzpicture}");
		System.out.println("\\end{adjustbox}");
		System.out.println("\\end{center}");
		System.out.println("\\end{figure}");
    }
    
    private void printDualCoveringLatex()
    {
	}

	private void printPolygonLatex(Polygon geom, Color color, Color fill)
	{
		System.out.print("\\draw[draw=" + toLatex(color));
		
		if( fill != null)
			System.out.print(",fill=" + toLatex(fill) + ",opacity=0.3");
			
		System.out.print("] ");
		
    	Coordinate[] c = geom.getCoordinates();
    	for(int i=0; i<c.length; ++i)
    		System.out.print("(" + convxo(c[i]) + "," + convyo(c[i]) + ") -- ");
    	
    	System.out.println("cycle;");
	}

	private void printMultiPointLatex(MultiPoint geom, Color color)
	{
	}

	private void printLineStringLatex(LineString geom, Color color)
	{
	}

	private void printPointLatex(Point point, Color color)
	{
		System.out.println("\\draw[draw=" + toLatex(color) + ",fill=" + toLatex(color) +"] ("+ convxo(point.getCoordinate()) + "," + convyo(point.getCoordinate()) + ") circle (1);");
	}
	
	private String toLatex(Color color)
	{
		return "{rgb,255:red," + color.getRed() + "; green," + color.getGreen() + "; blue," + color.getBlue() + "}";
	}

	public static Viewer show(Instancia instancia)
    {
        Viewer panel = new Viewer();
        addRegion(panel, instancia);
        addRestricciones(panel, instancia);
        showFrame(instancia, panel, "Instancia");
        
        return panel;
    }
    
    public static Viewer show(Instancia instancia, Region interna)
    {
        Viewer panel = new Viewer();
        addRegion(panel, instancia);
        addRegion(panel, interna, Color.BLUE, null);
        addRestricciones(panel, instancia);
        showFrame(instancia, panel, "Instancia");
        
        return panel;
    }
    
    public static void show(Instancia instancia, Solucion solucion)
    {
    	show(instancia, solucion, null);
    }
    
    public static void show(Instancia instancia, Solucion solucion, ArrayList<Point> puntos)
    {
        Viewer panel = new Viewer();
        addRegion(panel, instancia);
        addSolucion(panel, solucion);
        addRestricciones(panel, instancia);
        addPuntos(panel, puntos);
        
        showFrame(instancia, panel, "Solución");
    }

    public static Viewer show(Instancia instancia, Map<Point, Double> dual, Semilla semilla)
    {
        Viewer panel = new Viewer();
        panel.addDual(instancia, dual);
        addRegion(panel, instancia);
        addRegion(panel, instancia.getRegionInterna(semilla), Color.BLUE, null);
        addRestricciones(panel, instancia);
        showFrame(instancia, panel, "Solución dual");

        return panel;
    }

    private static void addRegion(Viewer panel, Instancia instancia)
    {
    	addRegion(panel, instancia.getRegion(), Color.BLACK, new Color(224, 173, 122));
    }

    private static void addRegion(Viewer panel, Region region, Color color, Color fill)
    {
        for(Polygon envolvente: region.getEnvolventes())
        	panel.addGeometry(envolvente, color, fill);
        
        for(Polygon agujero: region.getAgujeros())
        	panel.addGeometry(agujero, color, fill);
    }
    
    private static void addRestricciones(Viewer panel, Instancia instancia)
    {
        for(Restriccion restriccion: instancia.getRestricciones())
        	panel.addGeometry(restriccion.getPolygon(), Color.BLACK, new Color(205, 184, 255));

       	panel.addOGIP(instancia.getOGIP());
    }

	private static void addSolucion(Viewer panel, Solucion solucion)
	{
		for(Pad pad: solucion.getPads())
		{
			int nivel = 255 - (int)(255 * solucion.getValor(pad));
			Color color = new Color(nivel, nivel, nivel);

			panel.addGeometry(pad.getPerimetro(), color, color);
		    panel.addGeometry(pad.getLocacion(), color, color);
		    panel.addGeometry(pad.getCentro(), color, color);
		}
	}
	
	private static void addPuntos(Viewer panel, ArrayList<Point> puntos)
	{
		if( puntos != null )
        {
        	for(Point point: puntos)
        		panel.addGeometry(point, Color.RED);
        }
	}
	
	public static void setLatex(boolean valor)
	{
		_latex = valor;
	}

	private static void showFrame(Instancia instancia, Viewer panel, String texto)
	{
		JFrame frame = new JFrame(texto + " - " + instancia.getArchivo());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setSize(500, 500);
        frame.setVisible(true);
        
        if( _latex == true )
        	panel.printLatex();
	}
}