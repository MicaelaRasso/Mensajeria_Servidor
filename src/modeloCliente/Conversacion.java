package modeloCliente;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Conversacion {
    private Contacto contacto;
    private ArrayList<Mensaje> mensajes = new ArrayList<>();
    private boolean tieneNotificacion = false;

    public Conversacion(Contacto contacto) {
        this.contacto = contacto;
    }

    // Métodos
    public boolean tieneNotificacion() {
        return tieneNotificacion;
    }

    public void setNotificacion(boolean notificacion) {
        this.tieneNotificacion = notificacion;
    }

    public void recibirMensaje(String contenido, LocalDateTime fechaYHora, Contacto c) {
        mensajes.add(new Mensaje(c.getNombre(), contenido, fechaYHora));
        System.out.println("Mensaje recibido de " + c.getNombre() + ": " + contenido);
    }

    public void agregarMensaje(String mensaje, LocalDateTime fechaYHora, Usuario u) {
        mensajes.add(new Mensaje(u.getNombre(), mensaje, fechaYHora));
        System.out.println("Mensaje enviado a " + u.getNombre() + ": " + mensaje);
    }

    // Getters
    public ArrayList<Mensaje> getMensajes() {
        return mensajes;
    }

    public Contacto getContacto() {
        return contacto;
    }
}