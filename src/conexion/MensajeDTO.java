package conexion;

import modeloCliente.Contacto;
import modeloCliente.Usuario;

public class MensajeDTO extends ContenidoC {
	private String mensaje;
	private Contacto receptor;
	

	public MensajeDTO(Usuario emisor, String mensaje, Contacto receptor) {
		super(emisor);
		this.mensaje = mensaje;
		this.receptor = receptor;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public Contacto getReceptor() {
		return receptor;
	}

	public void setReceptor(Contacto receptor) {
		this.receptor = receptor;
	}

}
