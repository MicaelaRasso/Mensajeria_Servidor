package conexion;

import java.util.ArrayList;

public class ListaSDTO extends Contenido {
	private ArrayList<PuertoDTO> servidores;

	public ListaSDTO() {
		this.servidores = new ArrayList<PuertoDTO>();
	}

	public ArrayList<PuertoDTO> getServidores() {
		return servidores;
	}

	public void setServidores(ArrayList<PuertoDTO> servidores) {
		this.servidores = servidores;
	}

}
