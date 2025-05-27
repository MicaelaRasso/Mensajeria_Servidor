package modeloCliente;

import java.time.LocalDateTime;

public class Mensaje {
    private String contenido;
    private String emisor;
    private LocalDateTime fechaYHora;

    public Mensaje(String emisor, String contenido, LocalDateTime fechaYHora) {
        this.emisor = emisor;
        this.contenido = contenido;
        this.fechaYHora = fechaYHora;
    }

    // Getters
    public String getEmisor() {
        return emisor;
    }

    public String getContenido() {
        return contenido;
    }

    public LocalDateTime getFechaYHora() {
        return fechaYHora;
    }

    @Override
    public String toString() {
        return emisor + ": // " + contenido + " // " + fechaYHora;
    }

    public String paraMostrar() {
        String salto = System.lineSeparator();
        return emisor + ": " + salto + contenido + salto + fechaYHora;
    }
}