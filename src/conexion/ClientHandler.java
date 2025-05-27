package conexion;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
			//socket = new Socket(this.socket.getInetAddress(), this.socket.getPort());
        	//ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

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

       /* try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {*/
            
        try {
            out.flush();  // tras crear el stream de salida
            	// Bucle de mensajes desde este cliente
            while ((paquete = (Paquete) in.readObject()) != null) {
                switch (paquete.getOperacion()) {
                    case "registro": {
                        Paquete reg = sys.registrarUsuario((UsuarioDTO) paquete.getContenido(), (socket.getInetAddress()).getHostAddress(), socket.getPort());
                        out.writeObject("ACK");
                        out.flush();
                        out.writeObject(reg);
                        out.flush();

                        enviarPendientes(paquete);
                        break;
                    }
                    case "consulta": {
                        Paquete resp = sys.manejarConsulta((UsuarioDTO) paquete.getContenido());
                        out.writeObject("ACK");
                        out.flush();
                        out.writeObject(resp);
                        out.flush();
                        break;
                    }
                    case "mensaje": {
                        Paquete resend = sys.manejarMensaje((MensajeDTO) paquete.getContenido());
                        if (resend != null) {
                            MensajeDTO mDTO = (MensajeDTO) resend.getContenido();
                            out.writeObject("ACK");
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
                        out.writeObject("ACK");
                        out.flush();
                        break;
                    }
                    default: {
                        System.err.println("Operación desconocida: " + paquete.getOperacion());
                        out.writeObject("ACK");
                        out.flush();
                        break;
                    }
                }
            }
       /* } catch (EOFException | SocketException eof) {
            System.out.println("Cliente desconectó: " + socket.getRemoteSocketAddress());
        */} catch (Exception ex) {
            ex.printStackTrace();
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
