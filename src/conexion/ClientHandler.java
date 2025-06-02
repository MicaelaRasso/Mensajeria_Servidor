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
import modelo.Usuario;

public class ClientHandler extends Thread {
    private final Socket socket;
    private ObjectInputStream in;
	private ObjectOutputStream out;

    ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    private void enviarMensaje(Paquete paquete, Usuario receptor) throws SinConexionException{
        //Socket socket;
		try {
			System.out.println(receptor.toString());
			ObjectOutputStream outR = receptor.getOut();
            outR.flush();
			
        	System.out.println("Se envio: "+ paquete.toString());
        	outR.writeObject(paquete);
        	outR.flush();
		} catch (IOException e) {
			receptor.setConnected(false);
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
                    	Paquete reg = sys.registrarUsuario((UsuarioDTO) paquete.getContenido(), out);
                        out.writeObject(reg);
                        out.flush();

                        enviarPendientes(((UsuarioDTO) paquete.getContenido()).getNombre());
                        break;
                    }
                    case "agregarC": {
                        Paquete resp = sys.manejarConsulta((UsuarioDTO) paquete.getContenido());
                        out.writeObject(resp);
                        out.flush();
                        break;
                    }
                    case "enviarM": {
                        Paquete resend = sys.manejarMensaje((MensajeDTO) paquete.getContenido());
                        if (resend != null) {
                        	String nombreR = ((MensajeDTO)resend.getContenido()).getReceptor().getNombre();
                        	Usuario receptor = sys.getUsuarios().get(nombreR);
                        	
                            try {
                                enviarMensaje(resend, receptor);
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
                    case "desconectarU":{
                    	sys.desconectarUsuario((UsuarioDTO)paquete.getContenido());
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


    
    
	private void enviarPendientes(String nUsuarioReconectado) {
		Servidor sys = Servidor.getInstance();
		Usuario receptor = sys.getUsuarios().get(nUsuarioReconectado);
		
		List<MensajeDTO> mensajesPendientes = sys.entregarPendientes(nUsuarioReconectado);

		System.out.println("Cantidad de mensajes pendientes: " + mensajesPendientes.size());
		
		Iterator<MensajeDTO> it = mensajesPendientes.iterator();

		while(it.hasNext()) {
		    MensajeDTO mp = it.next();
	    	Paquete paq = new Paquete("recibirM", mp);
	        try {
				enviarMensaje(paq, receptor);
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
