package modelo;

public class Usuario {
	private String nombre;
	private String address;
	private int port;
	private boolean connected;

	public Usuario(String nombre, String address) {
		super();
		this.nombre = nombre;
		this.address = address;
		this.connected = true;
	}

	public Usuario(String nombre) {
		super();
		this.nombre = nombre;
		this.address = "";
		this.connected = true;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
