package interfaz;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import colrowgen.RectangularDualCovering;
import general.Instancia;
import general.OGIP;
import general.Restriccion;
import general.Semilla;
import general.Solucion;
import general.Pad;

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
	private OGIP ogip = null;
  
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
    
    public void addOGIP(OGIP obj)
    {
    	ogip = obj;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

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

    private int convx(Coordinate c)
    {
    	return _margen + ((int)c.x - _minx) * (getWidth() - 2*_margen) / (_maxx - _minx);
    }
    
    private int convy(Coordinate c)
    {
    	return -_margen + getHeight() - ((int)c.y - _miny) * (getHeight() - 2*_margen) / (_maxy - _miny);
    }
    
    public static void show(Instancia instancia)
    {
        Viewer panel = new Viewer();
        addEnvelope(panel, instancia);
        addRestricciones(panel, instancia);
        showFrame(instancia, panel, "Instancia");
    }
    
    public static void show(Instancia instancia, Solucion solucion)
    {
        Viewer panel = new Viewer();
        addEnvelope(panel, instancia);
        addSolucion(panel, solucion);
        addRestricciones(panel, instancia);
        showFrame(instancia, panel, "Solución");
    }

    public static void show(Instancia instancia, Map<Point, Double> dual, Semilla semilla)
    {
        Viewer panel = new Viewer();
        addEnvelope(panel, instancia);
        addDual(panel, instancia, dual, semilla);
        addRestricciones(panel, instancia);
        showFrame(instancia, panel, "Solución dual");
    }

    public static void show(Instancia instancia, RectangularDualCovering covering, Semilla semilla)
    {
        Viewer panel = new Viewer();
        addEnvelope(panel, instancia);
        addDualCovering(panel, instancia, covering, semilla);
        addRestricciones(panel, instancia);
        showFrame(instancia, panel, "Dual covering");
    }

    private static void addEnvelope(Viewer panel, Instancia instancia)
    {
        for(Polygon envolvente: instancia.getRegion().getEnvolventes())
        	panel.addGeometry(envolvente);
        
        for(Polygon agujero: instancia.getRegion().getAgujeros())
        	panel.addGeometry(agujero);

//       	panel.addGeometry(solver.getDiscretizacion().getPuntos());
    }
    
    private static void addRestricciones(Viewer panel, Instancia instancia)
    {
        for(Restriccion restriccion: instancia.getRestricciones())
        	panel.addGeometry(restriccion.getPolygon());
        
//       	panel.addOGIP(instancia.getOGIP());
    }

	private static void addSolucion(Viewer panel, Solucion solucion)
	{
		for(Pad pad: solucion.getPads())
		{
			int nivel = 255 - (int)(255 * solucion.getValor(pad));
			Color color = new Color(nivel, nivel, nivel);

			panel.addGeometry(pad.getPerimetro(), color);
		    panel.addGeometry(pad.getLocacion(), color);
		    panel.addGeometry(pad.getCentro(), color);
		}
	}
	
	private static void addDual(Viewer panel, Instancia instancia, Map<Point, Double> dual, Semilla semilla)
	{
    	for(Point punto: dual.keySet()) if( dual.get(punto) > 0 )
    	{
			int nivel = 255 - (int)(255 * ((semilla.getLargo() * semilla.getAncho() / 1e6) - dual.get(punto)));
			Color color = new Color(nivel, nivel, nivel);
    		
    		panel.addGeometry(new Pad(instancia, semilla, punto.getCoordinate()).getPerimetro(), color);
    	}
	}
	
	private static void addDualCovering(Viewer panel, Instancia instancia, RectangularDualCovering covering, Semilla semilla)
	{
		for(RectangularDualCovering.Rect rect: covering.getRectangulos())
		{
			double cx = (rect.izquierda + rect.derecha) / 2;
			double cy = (rect.arriba + rect.abajo) / 2;
			
			Pad pad = new Pad(instancia, semilla, new Coordinate(cx,cy));
			double targetArea = pad.getArea();

			int nivel = 255 - (int)(255 * (targetArea - covering.get(rect)));
			Color color = new Color(nivel, nivel, nivel);
    		
			panel.addGeometry(pad.getPerimetro(), color);
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