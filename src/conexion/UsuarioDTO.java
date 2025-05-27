package conexion;

public class UsuarioDTO extends ContenidoC {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String nombre;
	private PuertoDTO puerto;
	private String respuesta;

	public UsuarioDTO(UsuarioDTO emisor, String nombreC, PuertoDTO puerto) {
		super(emisor);
		this.nombre = nombreC;
		this.puerto = puerto;
		respuesta = "";
	}
	
	public UsuarioDTO(UsuarioDTO emisor, String nombre) {
		super(emisor);
		this.nombre = nombre;
		this.puerto = null;
		respuesta = "";
	}

	public UsuarioDTO(UsuarioDTO emisor) {
		super(emisor);
		this.nombre = "";
		this.puerto = null;
		respuesta = "";
	}

	public String getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}
	
	public String getNombre() {
		return nombre;
	}
	
	public PuertoDTO getPuerto() {
		return puerto;
	}
}
