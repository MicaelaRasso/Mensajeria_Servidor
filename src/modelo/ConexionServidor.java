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
import excepciones.SinConexionException;
import modeloCliente.Usuario;
import utils.ConfigLoader;

public class ConexionServidor {
    private static String monitor_host = ConfigLoader.mHost;
    private static int monitor_port = ConfigLoader.mPort;
	private static final int PUERTO = ConfigLoader.sPort;
	private static final String IP = ConfigLoader.sHost;
    private ServerSocket serverSocket;
    private ArrayList<PuertoDTO> servidores = new ArrayList<PuertoDTO>();

    public ConexionServidor(){
    }

    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(PUERTO);
        System.out.println("Servidor escuchando en el puerto " + PUERTO);

        while (true) {
            Socket socket = serverSocket.accept();
            new ClientHandler(socket).start();
        }
    }
    
    private void registrarServidor() throws IOException {
        try {
	    	Socket socket = new Socket(monitor_host, monitor_port);
	        
	    	ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
	    	ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
	    	
	    	PuertoDTO pDTO =  new PuertoDTO(PUERTO, IP);
	    	Paquete paqueteRegistroServidor = new Paquete("", pDTO);
	    	servidores.add(pDTO);
	    	out.writeObject(paqueteRegistroServidor);
	    	out.flush();
	    	
	    	Paquete respuesta;
			try {
				respuesta = (Paquete) in.readObject();
			
				if(respuesta.getContenido() != null) {
	        		System.out.println("Conectado al Proxy en " + monitor_host + ":" + monitor_port);
	    			this.iniciar();
	    		}else {
	        		System.out.println("No se ha podido registrar el servidor en el proxy");
	    			socket.close();
	        	}
				
			} catch (ClassNotFoundException | IOException e) {
				socket.close();
				e.printStackTrace();
			}
		}catch(Exception e){
			System.err.println("No se pudo establecer conexion con el monitor");
			System.exit(0);
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
        
        private void enviarMensaje(Paquete paquete, String address) throws SinConexionException{
            Socket socket;
			try {
				socket = new Socket(monitor_host, monitor_port);
	        	ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

	        	out.writeObject(paquete);
				
			} catch (IOException e) {
				throw new SinConexionException("Se perdio la conexión con el usuario: " + ((MensajeDTO)paquete.getContenido()).getReceptor());
			}
        	//System.out.println("Conectado al Proxy en " + PROXY_HOST + ":" + PROXY_PORT);
        	//System.out.println("mensaje enviado");        
        }

        @Override
        public void run() {
            //try {
            	try {
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
	                    			try {
										enviarMensaje(resend,address);
									} catch (SinConexionException e) {
		                    			sys.almacenarMensaje((MensajeDTO) paquete.getContenido());
									}
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
						}catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						
						}
	                }
            	}catch (IOException | ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
		    	Paquete paq = new Paquete("recibirM", mp);
		        try {
					enviarMensaje(paq, address);
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
    
    public static void main(String[] args) throws IOException {
		ConexionServidor s = new ConexionServidor();
		s.registrarServidor();
        System.out.println("[SERVIDOR] Arrancando servidor en puerto " + PUERTO);
    }
}