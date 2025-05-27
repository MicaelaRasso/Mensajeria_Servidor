package conexion;

import modeloCliente.Usuario;

public class UsuarioDTO extends ContenidoC {
	private String address;
	private String respuesta;

	public UsuarioDTO(Usuario emisor, String address) {
		super(emisor);
		this.address = address;
		respuesta = "";
	}
	
	public UsuarioDTO(Usuario usuario) {
		super(usuario);
		this.address = "";
		respuesta = "";
	}
	
	

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}
	
	
}
