package conexion;

import modeloCliente.Usuario;

public class ContenidoC extends Contenido {
	private Usuario emisor;

	public ContenidoC(Usuario emisor) {
		this.setEmisor(emisor);
	}

	public Usuario getEmisor() {
		return emisor;
	}

	public void setEmisor(Usuario emisor) {
		this.emisor = emisor;
	}

}
