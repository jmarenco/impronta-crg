package interfaz;

import colrowgen.Dualizer;
import colrowgen.PadCache;
import colrowgen.Relajacion;
import general.Instancia;
import general.Solucion;
import heuristicas.Goloso;

public class EntryPoint {

	public static void main(String[] args)
	{
		Instancia.set(Instancia.Formato.French);
		Instancia instancia = new Instancia("instancias/sqr.01.xml");
		
		Goloso goloso = new Goloso(instancia);
		Solucion solucion = goloso.resolver();
		
		Viewer.show(instancia, solucion);

		Relajacion relajacion = new Relajacion(instancia, solucion.getCentros(), new PadCache(instancia));
		Solucion modelo = relajacion.resolver();
		
		Viewer.show(instancia, modelo);
		
		Dualizer dualizer = new Dualizer(relajacion);
		dualizer.ejecutar();
	}
}
