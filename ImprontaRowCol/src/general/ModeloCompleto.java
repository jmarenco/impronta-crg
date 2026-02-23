package general;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import heuristicas.Discretizacion;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class ModeloCompleto
{
	private Instancia _instancia;
	private boolean _verbose = true;
	
	public ModeloCompleto(Instancia instancia)
	{
		_instancia = instancia;
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
				x.add(cplex.boolVar());
			
			log("Construyendo restricciones");
			int k = 0;
			for(Coordinate c: discretizacion.getPuntos().getCoordinates())
			{
				Point punto = _instancia.getFactory().createPoint(c);
				
				IloNumExpr lhs = cplex.linearNumExpr();
				
				for(int i=0; i<pads.size(); ++i) if( pads.get(i).contiene(punto) )
					lhs = cplex.sum(lhs, x.get(i));
				
				cplex.add(cplex.le(lhs, 1));
				++k;
				
				if( k % 1000 == 0 )
					log(" -> " + k + "/" + discretizacion.getPuntos().getCoordinates().length + " restricciones");
			}
			
			log("\r\nConstruyendo objetivo");
			IloNumExpr obj = cplex.linearNumExpr();
			for(int i=0; i<pads.size(); ++i)
				obj = cplex.sum(obj, cplex.prod(pads.get(i).getValorizacion(), x.get(i)));
			
			cplex.addMaximize(obj);
			
			log("\r\nResolviendo el modelo");
			cplex.solve();
			
			for(int i=0; i<pads.size(); ++i) if( cplex.getValue(x.get(i)) > 0.9 )
				ret.agregar(pads.get(i));
			
			cplex.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		log("Tiempo total: " + String.format("%.2f", (System.currentTimeMillis() - inicio) / 1000.0) + " seg.");
		return ret;
	}
	
	private void log(String texto)
	{
		if( _verbose == true )
			System.out.println(texto);
	}
}
