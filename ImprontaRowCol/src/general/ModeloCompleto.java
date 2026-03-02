package general;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import heuristicas.Discretizacion;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import interfaz.Viewer;

public class ModeloCompleto
{
	private Instancia _instancia;
	private boolean _entero = true;
	private boolean _verbose = true;
	
	public ModeloCompleto(Instancia instancia)
	{
		_instancia = instancia;
	}
	
	public ModeloCompleto(Instancia instancia, boolean entero)
	{
		_instancia = instancia;
		_entero = entero;
	}
	
	public Solucion resolver()
	{
		Solucion ret = new Solucion(_instancia);
		long inicio = System.currentTimeMillis();
		
		try
		{
			IloCplex cplex = new IloCplex();
			
			log("Construyendo discretizacion");
			Discretizacion discretizacion = new Discretizacion(_instancia);

			log(" -> " + discretizacion.getPuntos().getCoordinates().length + " puntos\r\n\r\nConstruyendo pads");
			ArrayList<Pad> pads = discretizacion.construirPads();
			log(" -> " + pads.size() + " pads\r\n\r\nConstruyendo variables");
			
			ArrayList<IloNumVar> x = new ArrayList<IloNumVar>();
			
			for(int i=0; i<pads.size(); ++i)
				x.add(_entero ? cplex.boolVar() : cplex.numVar(0,1));
			
			log("Construyendo restricciones");
			int k = 0;
			for(Pad pad: pads)
			{
				for(Coordinate esquina: pad.getPerimetro().getCoordinates())
				for(Coordinate c: _instancia.snappedNeighbors(esquina))
				{
					Point punto = _instancia.getFactory().createPoint(c);
					
					IloNumExpr lhs = cplex.linearNumExpr();
					
					for(int i=0; i<pads.size(); ++i) if( pads.get(i).contiene(punto) )
						lhs = cplex.sum(lhs, x.get(i));
					
					cplex.add(cplex.le(lhs, 1));
				}
				
				++k;
				
				if( k % 1000 == 0 )
					log(" -> " + k + "/" + pads.size() + " pads procesados");
			}
			
			log("\r\nConstruyendo objetivo");
			IloNumExpr obj = cplex.linearNumExpr();
			for(int i=0; i<pads.size(); ++i)
				obj = cplex.sum(obj, cplex.prod(pads.get(i).getValorizacion(), x.get(i)));
			
			cplex.addMaximize(obj);
			
			log("\r\nResolviendo el modelo");
			cplex.solve();
			
			for(int i=0; i<pads.size(); ++i) if( cplex.getValue(x.get(i)) > 0.01 )
				ret.agregar(pads.get(i), cplex.getValue(x.get(i)));
			
			log("Tiempo total: " + String.format("%.2f", (System.currentTimeMillis() - inicio) / 1000.0) + " seg. \r\n");
			log("Solución óptima: " + String.format("%.5f", cplex.getObjValue()));
			
			for(Pad pad: ret.getPads())
				log(" - " + pad.getCentro() + " = " + ret.getValor(pad));
			
			ArrayList<Point> centros = new ArrayList<Point>();
			for(Pad pad: pads)
				centros.add(pad.getCentro());
			
			Viewer.show(_instancia, ret, centros);
			cplex.close();
			log("");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return ret;
	}
	
	private void log(String texto)
	{
		if( _verbose == true )
			System.out.println(texto);
	}
}
