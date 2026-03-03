package interfaz;

import colrowgen.Master;
import general.Instancia;
import general.ModeloCompleto;
import general.Solucion;
import heuristicas.Goloso;

public class EntryPoint
{
	public static String version()
	{
		return "0.10";
	}
	
	public static void main(String[] args)
	{
		ArgMap argmap = new ArgMap(args);
		procesarParametros(argmap);
		
		Instancia.set(Instancia.Formato.French);
		Instancia instancia = new Instancia(argmap.stringArg("-inst", "instancias/sqr.00.xml"));
		
		if( argmap.containsArg("-model") )
		{
			ModeloCompleto modelo = new ModeloCompleto(instancia, false);
			Solucion completa = modelo.resolver();
		
			if( argmap.containsArg("-show") )
				Viewer.show(instancia, completa);
		}

		if( argmap.containsArg("-master") )
		{
			Goloso goloso = new Goloso(instancia);
			Solucion golosa = goloso.resolver();
			
			Master master = new Master(instancia, golosa.getCentros());
			master.solve();
			
			if( argmap.containsArg("-show") )
				Viewer.show(instancia, master.getSolucion(), master.getPoints());
		}
	}
	
	private static void procesarParametros(ArgMap argmap)
	{
		if( argmap.containsArg("-help") )
		{
			System.out.println("-inst [s]		Instancia a resolver");
			System.out.println("-model			Ejecutar el modelo completo");
			System.out.println("-master			Ejecutar el master");
			System.out.println("-pde [n]		Eliminacion primal y dual de puntos con n rondas inactivos");
			System.out.println("-pe [n]			Eliminacion primal de puntos con n rondas inactivos");
			System.out.println("-de [n]			Eliminacion dual de puntos con n rondas inactivos");
			System.out.println("-ig [n]			Intentos del algoritmo goloso");
			System.out.println("-fg [n]			Factor de discretizacion del algoritmo goloso");
			System.out.println("-sg [n]			Semilla del algoritmo goloso");
			System.out.println("-time [n]		Limite de tiempo en segundos");
			System.out.println("-silent			Anula el modo verbose");
			System.out.println("-show			Muestra la solucion");
		}

		Master.eliminarPuntos(argmap.containsArg("-pe") || argmap.containsArg("-pde"), argmap.containsArg("-de") || argmap.containsArg("-pde"), Math.max(Math.max(argmap.intArg("-pe", 0), argmap.intArg("-de", 0)), argmap.intArg("-pde", 0)));
		ModeloCompleto.setTimeLimit(argmap.doubleArg("-time", 3600));
		Master.setTimeLimit(argmap.doubleArg("-time", 3600));
		ModeloCompleto.setVerbose(!argmap.containsArg("-silent"));
		Master.setVerbose(!argmap.containsArg("-silent"));
		Goloso.setSemilla(argmap.intArg("-sg", 0));
		Goloso.setIntentos(argmap.intArg("-ig", 100));
		Goloso.setFactorPasoHorizontal(argmap.intArg("-fg", 20));
		Goloso.setFactorPasoVertical(argmap.intArg("-fg", 20));
	}
}
