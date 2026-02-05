package interfaz;

import general.Instancia;

public class EntryPoint {

	public static void main(String[] args)
	{
		Instancia.set(Instancia.Formato.French);
		Instancia instancia = new Instancia("instancias/Entrada_v2.xml");
		Viewer.show(instancia);
	}
}
