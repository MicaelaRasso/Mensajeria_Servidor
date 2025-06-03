package modelo;

import java.io.ObjectOutputStream;

public class Usuario {
	private String nombre;
	private boolean connected;
	private ObjectOutputStream out;

	public Usuario(String nombre) {
		super();
		this.nombre = nombre;
		this.connected = true;
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

	public ObjectOutputStream getOut() {
		return out;
	}

	public void setOut(ObjectOutputStream out) {
		this.out = out;
	}

	@Override
	public String toString() {
		if (out != null)
			return "Usuario:" + nombre + ", connected:" + connected + ", out: " + out.toString();
		else
			return "Usuario:" + nombre + ", connected:" + connected;
	}
	
}
