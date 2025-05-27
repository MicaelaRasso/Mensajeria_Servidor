package modeloCliente;

public class Contacto extends Usuario{
    private Conversacion conversacion;

    public Contacto(String nombre) {
        super(nombre);
    }

    // Getters y Setters
    public Conversacion getConversacion() {
        return conversacion;
    }

    public void setConversacion(Conversacion conversacion) {
        this.conversacion = conversacion;
    }


    @Override
    public String toString() {
        return super().toString();
    }
}