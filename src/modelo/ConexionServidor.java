package modelo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import conexion.MensajeDTO;
import conexion.Paquete;
import conexion.PuertoDTO;
import conexion.UsuarioDTO;
import modeloCliente.Usuario;
import utils.ConfigLoader;

public class ConexionServidor {
    private static String PROXY_HOST = ConfigLoader.pHost;
    private static int PROXY_PORT = ConfigLoader.pPort;
	private static final int puerto = ConfigLoader.sPort;
	private static final String IP = ConfigLoader.sHost;
    private ServerSocket serverSocket;
    private ArrayList<PuertoDTO> servidores = new ArrayList<PuertoDTO>();

    public ConexionServidor(){
    }

    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(puerto);
        System.out.println("Servidor escuchando en el puerto " + puerto);

        while (true) {
            Socket socket = serverSocket.accept();
            new ClientHandler(socket).start();
        }
    }
    
    private void registrarServidor() throws IOException {
        Socket socket = new Socket(PROXY_HOST, PROXY_PORT);
        
    	ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
    	ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
    	
    	PuertoDTO pDTO =  new PuertoDTO(puerto, IP);
    	Paquete paqueteRegistroServidor = new Paquete("", pDTO);
    	servidores.add(pDTO);
    	out.writeObject(paqueteRegistroServidor);
    	out.flush();
    	
    	Paquete respuesta;
		try {
			respuesta = (Paquete) in.readObject();
		
			if(respuesta.getContenido() != null) {
        		System.out.println("Conectado al Proxy en " + PROXY_HOST + ":" + PROXY_PORT);
    			this.iniciar();
    		}else {
        		System.out.println("No se ha podido registrar el servidor en el proxy");
    			socket.close();
        	}
			
		} catch (ClassNotFoundException | IOException e) {
			socket.close();
			e.printStackTrace();
		}
    }
    

    public void terminar() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;
        private ObjectInputStream in;
    	private ObjectOutputStream out;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        private void enviarMensaje(Paquete paquete, String address){
            Socket socket;
			try {
				socket = new Socket(PROXY_HOST, PROXY_PORT);
	        	ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

	        	out.writeObject(paquete);
				
			} catch (IOException e) {
				//puedoLanzarExcepcion
			}
        	//System.out.println("Conectado al Proxy en " + PROXY_HOST + ":" + PROXY_PORT);
        	//System.out.println("mensaje enviado");        
        }

        @Override
        public void run() {
            //try {
            	in = new ObjectInputStream(socket.getInputStream());
            	out = new ObjectOutputStream(socket.getOutputStream());

                Paquete paquete;
                Servidor sys = Servidor.getInstance();

                while ((paquete = (Paquete) in.readObject()) != null) {
                	try {
                	switch (paquete.getOperacion()) {
                    	case "registro":
                    		Paquete reg = sys.registrarUsuario((UsuarioDTO)paquete.getContenido());
                    		out.writeObject("ACK");
                    		out.writeObject(reg);
                    		
                    		enviarPendientes(paquete);

                    		break;
                    	case "consulta":
                    		Paquete resp = sys.manejarConsulta((UsuarioDTO) paquete.getContenido());
                    		out.writeObject("ACK");
                    		out.writeObject(resp);
                    		break;
                    	case "mensaje":
                    		Paquete resend = sys.manejarMensaje((MensajeDTO)paquete.getContenido());
                    		if(resend != null) {
                    			MensajeDTO mDTO = (MensajeDTO) resend.getContenido();
                    			String address = mDTO.getReceptor().getIP();
                        		out.writeObject("ACK");
                    			enviarMensaje(resend,address);
                    		}else {
                    			sys.almacenarMensaje((MensajeDTO) paquete.getContenido());
                    		}
                    		
/*                    		
                    		Request r = sys.createResponse("Respuesta", "respuesta");
                    		out.println("ACK");
                    		out.println(JsonConverter.toJson(r));
                    		Request resend = sys.manejarMensaje(req);
                    		if(resend != null) {                    			
                    			enviarMensaje(resend,resend.getReceptor().getAddress());
                    			out.println("ACK");
                    		}else {
                    			sys.almacenarMensaje(req);
                    		}*/
                    		break;
                    	case "heartbeat":
                    		sys.actualizarHeartbeat((PuertoDTO)paquete.getContenido());
                    		out.writeObject("ACK");
                    		break;
                    	default:
                    		System.err.println("Operación desconocida: " + paquete.getOperacion());
                    		out.writeObject("ACK");
                    	}
                    }catch(IOException e) {
                    	
                    }
                }
        }

	private void enviarPendientes(Paquete paquete) {
		Servidor sys = Servidor.getInstance();

		
		Usuario uReconectado = ((MensajeDTO) paquete.getContenido()).getEmisor();
		String nombre = uReconectado.getNombre();
		Usuario uEnSistema = sys.getUsuarios().get(nombre);
		String address = uEnSistema.getIP();

		List<MensajeDTO> mensajesPendientes = sys.entregarPendientes(nombre);
		
		Iterator<MensajeDTO> it = mensajesPendientes.iterator();

		while(it.hasNext()) {
		    MensajeDTO mp = it.next();
		    try {
		        enviarMensaje(mp, address);
		    } catch (IOException e) {
		        System.out.println("El usuario se desconecto en medio del envío de sus mensajes pendientes");
		        // Reencolo el mensaje fallido y los demás
		        sys.almacenarMensaje(mp); // el actual
		        while(it.hasNext()) {
		            sys.almacenarMensaje(it.next());
		        }
		        break; // salgo del while, no sigo enviando
		    }
		}

		
	}
   }
    public static void main(String[] args) throws IOException {
		ConexionServidor s = new ConexionServidor();
		s.registrarServidor();
        System.out.println("[SERVIDOR] Arrancando servidor en puerto " + puerto);
    }
}