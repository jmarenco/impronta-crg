package interfaz;

import colrowgen.Dualizer;
import colrowgen.Master;
import colrowgen.SimpleBFS;
import general.Instancia;
import general.ModeloCompleto;
import general.Solucion;
import heuristicas.Goloso;

public class EntryPoint
{
	private static String _version = "0.15";
	private static ArgMap _args;
	
	public static void main(String[] args)
	{
		_args = new ArgMap(args);
		procesarParametros();

		if( _args.containsArg("-scale") )
			Instancia.setScale(_args.intArg("-scale", 1));

		Instancia.set(Instancia.Formato.French);
		Instancia instancia = new Instancia(_args.stringArg("-inst", "instancias/sqr.00.xml"));
		
		if( _args.containsArg("-step") )
			instancia.setPasos(_args.intArg("-step", 1), _args.intArg("-step", 1));
		
		if( _args.containsArg("-showinst") )
			Viewer.show(instancia, null);
		
		if( _args.containsArg("-model") )
		{
			ModeloCompleto modelo = new ModeloCompleto(instancia, _args.containsArg("-intmodel"));
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
			
			if( _args.containsArg("-posth") )
			{
				ModeloCompleto modelo = new ModeloCompleto(instancia, master.getPads(), true);
				Solucion heuristica = modelo.resolver();

				if( _args.containsArg("-show") )
					Viewer.show(instancia, heuristica);
			}
		}
	}
	
	private static void procesarParametros()
	{
		if( _args.containsArg("-help") )
		{
			System.out.println("UFO RowColGen - v" + _version);
			System.out.println("-inst [s]		Instancia a resolver");
			System.out.println("-scale [n]		Factor de escala para la instancia");
			System.out.println("-step [n]		Paso de la discretizacion");
			System.out.println("-model			Ejecutar el modelo completo");
			System.out.println("-master			Ejecutar el master");
			System.out.println("-posth			Heuristica con el modelo post-master");
			System.out.println("-pde [n]		Eliminacion primal y dual de puntos con n rondas inactivos");
			System.out.println("-pe [n]			Eliminacion primal de puntos con n rondas inactivos");
			System.out.println("-de [n]			Eliminacion dual de puntos con n rondas inactivos");
			System.out.println("-dual [s]		Dual solver [model|primal]");
			System.out.println("-udi [n]		Umbral dual inicial");
			System.out.println("-ig [n]			Intentos del algoritmo goloso");
			System.out.println("-fg [n]			Factor de discretizacion del algoritmo goloso");
			System.out.println("-sg [n]			Semilla del algoritmo goloso");
			System.out.println("-time [n]		Limite de tiempo en segundos");
			System.out.println("-silent			Anula el modo verbose");
			System.out.println("-show			Muestra la solucion");
		}

		Master.eliminarPuntos(_args.containsArg("-pe") || _args.containsArg("-pde"), _args.containsArg("-de") || _args.containsArg("-pde"), Math.max(Math.max(_args.intArg("-pe", 0), _args.intArg("-de", 0)), _args.intArg("-pde", 0)));
		ModeloCompleto.setTimeLimit(_args.doubleArg("-time", 3600));
		Master.setTimeLimit(_args.doubleArg("-time", 3600));
		Master.setUmbralDualInicial(_args.doubleArg("-udi", 0));
		ModeloCompleto.setVerbose(!_args.containsArg("-silent"));
		Master.setVerbose(!_args.containsArg("-silent"));
		Goloso.setSemilla(_args.intArg("-sg", 0));
		Goloso.setIntentos(_args.intArg("-ig", 100));
		Goloso.setFactorPasoHorizontal(_args.intArg("-fg", 20));
		Goloso.setFactorPasoVertical(_args.intArg("-fg", 20));
		SimpleBFS.setMostrarBFS(_args.containsArg("-showbfs"));
		Dualizer.setDualSolver(_args.stringArg("-dual", "model"));
		Viewer.setLatex(_args.containsArg("-latex"));
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
