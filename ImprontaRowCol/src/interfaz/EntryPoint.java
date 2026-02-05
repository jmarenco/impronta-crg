package interfaz;

import general.Instancia;
import general.Solucion;
import heuristicas.Goloso;

public class EntryPoint {

	public static void main(String[] args)
	{
		Instancia.set(Instancia.Formato.French);
		Instancia instancia = new Instancia("instancias/Entrada_v2.xml");
		
		Goloso goloso = new Goloso(instancia);
		Solucion solucion = goloso.resolver();
		
		Viewer.show(instancia, solucion);
	}
}
