package conexion;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.List;

import excepciones.SinConexionException;
import modelo.Servidor;

public class ClientHandler extends Thread {
    private final Socket socket;
    private ObjectInputStream in;
	private ObjectOutputStream out;

    ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    private void enviarMensaje(Paquete paquete) throws SinConexionException{
        //Socket socket;
		try {

        	System.out.println("Se envio: "+ paquete.toString());
        	out.writeObject(paquete);
        	out.flush();
		} catch (IOException e) {
			throw new SinConexionException("Se perdio la conexión con el usuario: " + ((MensajeDTO)paquete.getContenido()).getReceptor());
		}
    	//System.out.println("Conectado al Proxy en " + PROXY_HOST + ":" + PROXY_PORT);
    	//System.out.println("mensaje enviado");        
    }

    @Override
    public void run() {
        Paquete paquete;
        Servidor sys = Servidor.getInstance();
        try {
        	out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

        	// Bucle de mensajes desde este cliente
            while ((paquete = (Paquete) in.readObject()) != null) {
                switch (paquete.getOperacion()) {
                    case "registrarU": {
                        Paquete reg = sys.registrarUsuario((UsuarioDTO) paquete.getContenido(), (socket.getInetAddress()).getHostAddress(), socket.getPort());
                        Paquete rta = new Paquete("ACK", null);
                        out.writeObject(rta);
                        out.flush();

                        out.writeObject(reg);
                        out.flush();

                        enviarPendientes(paquete);
                        break;
                    }
                    case "agregarC": {
                        Paquete resp = sys.manejarConsulta((UsuarioDTO) paquete.getContenido());
                        Paquete rta = new Paquete("ACK", null);
                        out.writeObject(rta);
                        out.flush();
                        out.writeObject(resp);
                        out.flush();
                        break;
                    }
                    case "enviarM": {
                        Paquete resend = sys.manejarMensaje((MensajeDTO) paquete.getContenido());
                        if (resend != null) {
                            Paquete rta = new Paquete("ACK", null);
                            out.writeObject(rta);
                            out.flush();
                            try {
                                enviarMensaje(resend);
                            } catch (SinConexionException e) {
                                sys.almacenarMensaje((MensajeDTO) paquete.getContenido());
                            }
                        }
                        break;
                    }
                    case "heartbeat": {
                        sys.actualizarHeartbeat((PuertoDTO) paquete.getContenido());
                        Paquete rta = new Paquete("ACK", null);
                        out.writeObject(rta);
                        out.flush();
                        break;
                    }
                    default: {
                        System.err.println("Operación desconocida: " + paquete.getOperacion());
                        Paquete rta = new Paquete("ACK", null);
                        out.writeObject(rta);
                        out.flush();
                        break;
                    }
                }
            }
        } catch (EOFException | SocketException e) {
            //System.out.println("Cliente desconectado: " + socket.getRemoteSocketAddress());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    
    
	private void enviarPendientes(Paquete paquete) {
		Servidor sys = Servidor.getInstance();
		UsuarioDTO uReconectado = (UsuarioDTO) ((MensajeDTO) paquete.getContenido()).getEmisor();
		String nombre = uReconectado.getNombre();

		List<MensajeDTO> mensajesPendientes = sys.entregarPendientes(nombre);
		
		Iterator<MensajeDTO> it = mensajesPendientes.iterator();

		while(it.hasNext()) {
		    MensajeDTO mp = it.next();
	    	Paquete paq = new Paquete("recibirM", mp);
	        try {
				enviarMensaje(paq);
			} catch (SinConexionException e) {
				break;
			}
		}
		
		while(it.hasNext()) {
		    MensajeDTO mp = it.next();
	        sys.almacenarMensaje(mp);
	    }		
	}
}
