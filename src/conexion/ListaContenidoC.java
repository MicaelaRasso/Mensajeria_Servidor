package conexion;

import java.util.ArrayList;

public class ListaContenidoC extends Contenido {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<ContenidoC> contenidoC;
	
	public ListaContenidoC() {
		this.contenidoC = new ArrayList<ContenidoC>();
	}

	public ArrayList<ContenidoC> getContenidoC() {
		return contenidoC;
	}
	
	public void setContenido(ArrayList<ContenidoC> contenidoC) {
		this.contenidoC = contenidoC;
	}

}
