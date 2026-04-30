package interfaz;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import general.Instancia;
import general.Restriccion;
import general.Semilla;
import general.Solucion;
import general.Pad;
import general.Region;
import general.Punto;

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
	private Map<Punto, Double> dual = null;
	private Instancia instancia = null; // Solo si hay una solución dual
  
    private int _minx = Integer.MAX_VALUE;
    private int _maxx = Integer.MIN_VALUE;
    private int _miny = Integer.MAX_VALUE;
    private int _maxy = Integer.MIN_VALUE;
    private int _margen = 20;

    public void addGeometry(Geometry geom)
    {
    	addGeometry(geom, Color.BLACK);
    }
    
    public void addGeometry(Geometry geom, Color color)
    {
        geometries.add(geom);
        colors.add(color);
        
        for(Coordinate c: geom.getCoordinates())
        {
        	_minx = Math.min(_minx, (int)c.x);
        	_maxx = Math.max(_maxx, (int)c.x);
        	_miny = Math.min(_miny, (int)c.y);
        	_maxy = Math.max(_maxy, (int)c.y);
        }
    }
    
    public void addDual(Instancia instance, Map<Punto, Double> dualSolution)
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
    
    private void drawDualCovering(Graphics2D g2d)
    {
    	for(Semilla semilla: instancia.getSemillas())
    	for(Punto punto: dual.keySet())
		{
   			Pad pad = new Pad(instancia, semilla, punto);
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
        addRegion(panel, interna, Color.BLUE);
        addRestricciones(panel, instancia);
        showFrame(instancia, panel, "Instancia");
        
        return panel;
    }
    
    public static void show(Instancia instancia, Solucion solucion)
    {
    	show(instancia, solucion, null);
    }
    
    public static void show(Instancia instancia, Solucion solucion, ArrayList<Punto> puntos)
    {
        Viewer panel = new Viewer();
        addRegion(panel, instancia);
        addSolucion(panel, solucion);
        addRestricciones(panel, instancia);
        addPuntos(panel, instancia, puntos);

//        addRegion(panel, instancia.getRegionInterna(instancia.getSemillas().get(0)), Color.BLUE);
//        
//        for(Coordinate c: instancia.getRegionInterna(instancia.getSemillas().get(0)).getCoordinates())
//        	System.out.println("Region interna: " + c);
        
        showFrame(instancia, panel, "Solución");
    }

    public static Viewer show(Instancia instancia, Map<Punto, Double> dual, Semilla semilla)
    {
        Viewer panel = new Viewer();
        panel.addDual(instancia, dual);
        addRegion(panel, instancia);
        addRegion(panel, instancia.getRegionInterna(semilla), Color.BLUE);
        addRestricciones(panel, instancia);
        showFrame(instancia, panel, "Solución dual");

        return panel;
    }

    private static void addRegion(Viewer panel, Instancia instancia)
    {
    	addRegion(panel, instancia.getRegion(), Color.BLACK);
    }

    private static void addRegion(Viewer panel, Region region, Color color)
    {
        for(Polygon envolvente: region.getEnvolventes())
        	panel.addGeometry(envolvente, color);
        
        for(Polygon agujero: region.getAgujeros())
        	panel.addGeometry(agujero, color);
    }
    
    private static void addRestricciones(Viewer panel, Instancia instancia)
    {
        for(Restriccion restriccion: instancia.getRestricciones())
        	panel.addGeometry(restriccion.getPolygon());
    }

	private static void addSolucion(Viewer panel, Solucion solucion)
	{
		for(Pad pad: solucion.getPads())
		{
			int nivel = 255 - (int)(255 * solucion.getValor(pad));
			Color color = new Color(nivel, nivel, nivel);

			panel.addGeometry(pad.getPerimetro(), color);
		    panel.addGeometry(pad.getLocacion(), color);
		    panel.addGeometry(solucion.getInstancia().getFactory().createPoint(pad.getCentro().asCoordinate()), color);
		}
	}
	
	private static void addPuntos(Viewer panel, Instancia instancia, ArrayList<Punto> puntos)
	{
		if( puntos != null )
        {
        	for(Punto point: puntos)
        		panel.addGeometry(instancia.getFactory().createPoint(point.asCoordinate()), Color.RED);
        }
	}

	private static void showFrame(Instancia instancia, Viewer panel, String texto)
	{
		JFrame frame = new JFrame(texto + " - " + instancia.getArchivo());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setSize(500, 500);
        frame.setVisible(true);
	}
}