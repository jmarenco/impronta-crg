package interfaz;

import colrowgen.Master;
import general.Instancia;
import general.ModeloCompleto;
import general.Solucion;
import heuristicas.Goloso;

public class EntryPoint
{
	private static String _version = "0.13";
	private static ArgMap _args;
	
	public static void main(String[] args)
	{
		_args = new ArgMap(args);
		procesarParametros();
		
		Instancia.set(Instancia.Formato.French);
		Instancia instancia = new Instancia(_args.stringArg("-inst", "instancias/sqr.00.xml"));
		
		if( _args.containsArg("-model") )
		{
			ModeloCompleto modelo = new ModeloCompleto(instancia, false);
			Solucion completa = modelo.resolver();
		
			if( _args.containsArg("-show") )
				Viewer.show(instancia, completa);
		}

		if( _args.containsArg("-master") )
		{
			Goloso goloso = new Goloso(instancia);
			Solucion golosa = goloso.resolver();
			
			Master master = new Master(instancia, golosa.getCentros());
			master.solve();
			
			if( _args.containsArg("-show") )
				Viewer.show(instancia, master.getSolucion(), master.getPoints());
		}
	}
	
	private static void procesarParametros()
	{
		if( _args.containsArg("-help") )
		{
			System.out.println("-inst [s]		Instancia a resolver");
			System.out.println("-model			Ejecutar el modelo completo");
			System.out.println("-master			Ejecutar el master");
//			System.out.println("-pde [n]		Eliminacion primal y dual de puntos con n rondas inactivos");
//			System.out.println("-pe [n]			Eliminacion primal de puntos con n rondas inactivos");
//			System.out.println("-de [n]			Eliminacion dual de puntos con n rondas inactivos");
			System.out.println("-ig [n]			Intentos del algoritmo goloso");
			System.out.println("-fg [n]			Factor de discretizacion del algoritmo goloso");
			System.out.println("-sg [n]			Semilla del algoritmo goloso");
			System.out.println("-time [n]		Limite de tiempo en segundos");
			System.out.println("-silent			Anula el modo verbose");
			System.out.println("-show			Muestra la solucion");
		}

		ModeloCompleto.setTimeLimit(_args.doubleArg("-time", 3600));
		Master.setTimeLimit(_args.doubleArg("-time", 3600));
		ModeloCompleto.setVerbose(!_args.containsArg("-silent"));
		Master.setVerbose(!_args.containsArg("-silent"));
		Goloso.setSemilla(_args.intArg("-sg", 0));
		Goloso.setIntentos(_args.intArg("-ig", 100));
		Goloso.setFactorPasoHorizontal(_args.intArg("-fg", 20));
		Goloso.setFactorPasoVertical(_args.intArg("-fg", 20));
	}

	public static String version()
	{
		return _version;
	}
	
	public static ArgMap args()
	{
		return _args;
	}
}
