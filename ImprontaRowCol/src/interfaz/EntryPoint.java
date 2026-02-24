package interfaz;

import colrowgen.Master;
import general.Instancia;
import general.Solucion;
import heuristicas.Goloso;

public class EntryPoint
{
	public static void main(String[] args)
	{
		Instancia.set(Instancia.Formato.French);
		Instancia instancia = new Instancia("instancias/sqr.00.xml");
		
//		for(Semilla semilla: instancia.getSemillas())
//			Viewer.show(instancia, RegionInterna.calcular(instancia, semilla));
		
//		ModeloCompleto modelo = new ModeloCompleto(instancia);
//		Solucion completa = modelo.resolver();
//		
//		Viewer.show(instancia, completa);
		
		Goloso goloso = new Goloso(instancia);
		Solucion golosa = goloso.resolver();
		
		System.out.println("Goloso: " + String.format("%.5f", golosa.areaCubierta()));
		Viewer.show(instancia, golosa);
		
		Master master = new Master(instancia, golosa.getCentros());
		master.solve();
		
		Viewer.show(instancia, master.getSolucion(), master.getPoints());
	}
}
