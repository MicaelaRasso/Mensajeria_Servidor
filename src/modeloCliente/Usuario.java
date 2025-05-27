package modeloCliente;

public class Usuario {
    private String nombre;
    private String IP;
    private boolean isConnected = false;

    public Usuario() {
        this.nombre = "";
        this.IP = "";
    }

    public Usuario(String nombre, String IP) {
        this.nombre = nombre;
        this.IP = IP;
    }
    
    public Usuario(String nombre) {
        this.nombre = nombre;
        this.IP = "";
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    @Override
    public String toString() {
        return nombre + " (" + IP + ")";
    }

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
}