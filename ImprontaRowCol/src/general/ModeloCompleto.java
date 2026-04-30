package general;

import java.util.ArrayList;

import heuristicas.Discretizacion;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import interfaz.EntryPoint;

public class ModeloCompleto
{
	private Instancia _instancia;
	private boolean _entero = true;

	private static boolean _verbose = true;
	private static boolean _resumen = true;
	private static double _timeLimit = 3600;
	
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
			cplex.setParam(IloCplex.IntParam.TimeLimit, _timeLimit);
			
			if( _verbose == false )
				cplex.setOut(null);
			
			log("Construyendo discretizacion");
			Discretizacion discretizacion = new Discretizacion(_instancia);

			log(" -> " + discretizacion.size() + " puntos\r\n\r\nConstruyendo pads");
			ArrayList<Pad> pads = discretizacion.construirPads();
			log(" -> " + pads.size() + " pads\r\n\r\nConstruyendo variables");
			
			ArrayList<IloNumVar> x = new ArrayList<IloNumVar>();
			
			for(int i=0; i<pads.size(); ++i)
				x.add(_entero ? cplex.boolVar() : cplex.numVar(0,1));
			
			log("Construyendo restricciones");
			int k = 0, constraints = 0;
			for(Pad pad: pads)
			{
				for(Punto esquina: pad.getVertices())
				for(Punto punto: _instancia.snappedNeighbors(esquina))
				{
					IloNumExpr lhs = cplex.linearNumExpr();
					
					for(int i=0; i<pads.size(); ++i) if( pads.get(i).contiene(punto) )
						lhs = cplex.sum(lhs, x.get(i));
					
					cplex.add(cplex.le(lhs, 1));
					++constraints;
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

			if( _resumen == true )
				System.out.println("\r\nComplete | " + _instancia.getArchivo() + " | " + String.format("%.2f", (System.currentTimeMillis() - inicio) / 1000.0) + " sec | Obj: " + String.format("%.5f", cplex.getObjValue()) + " | | " + discretizacion.size() + " pts | " + x.size() + " pvars | " + constraints + " pcons | | | | " + EntryPoint.args() + "\r\n");

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
	
	public static void setVerbose(boolean value)
	{
		_verbose = value;
	}
	
	public static void setTimeLimit(double value)
	{
		_timeLimit = value;
	}
}
