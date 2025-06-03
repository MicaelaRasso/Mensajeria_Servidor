package conexion;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import modelo.Servidor;
import utils.ConfigLoader;

public class ConexionServidor {
    private static String monitor_host = ConfigLoader.mHost;
    private static int monitor_port = ConfigLoader.mPort;
    private static final int PUERTO = ConfigLoader.sPort;
    private static final String IP = ConfigLoader.sHost;
    
    private ServerSocket serverSocket;

    private Socket monitorSocket;
    private ObjectOutputStream mout;
    private ObjectInputStream min;

    public ConexionServidor() {}

    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(PUERTO);
        System.out.println("Servidor escuchando en el puerto " + PUERTO);
        
        while (true) {
            Socket socket = serverSocket.accept();
            new ClientHandler(socket, this).start();
        }
    }

    public void registrarServidor() {
        try {
            monitorSocket = new Socket(monitor_host, monitor_port);
            mout = new ObjectOutputStream(monitorSocket.getOutputStream());
            mout.flush();
            min = new ObjectInputStream(monitorSocket.getInputStream());

            mout.writeObject(new Paquete("registrarS", new PuertoDTO(PUERTO, IP)));
            mout.flush();

            Paquete resp = (Paquete) min.readObject();
            if ("obtenerSR".equals(resp.getOperacion())) {
            	Servidor sys = Servidor.getInstance();
            	ListaSDTO lista = (ListaSDTO) resp.getContenido();

            	sys.setServidores(new ArrayList<>(lista.getServidores()));
                System.out.println("Otros servidores: " + sys.getServidores().toString());
            }
            
            //mout.writeObject(new Paquete("ACK",null));
            //mout.flush();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("No se pudo conectar al monitor");
            System.exit(1);
        }
    }

/*    public void actualizarServidores(Paquete paquete) {
    	Servidor sys = Servidor.getInstance();
    	ArrayList<PuertoDTO> servidores = sys.getServidores();
        for (PuertoDTO servidor : servidores) {
            
        	try (Socket socket = new Socket(servidor.getAddress(), servidor.getPuerto());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            	out.flush();

            	System.out.println("Enviando paquete a " + servidor.getAddress() + ":" + servidor.getPuerto() + " : " + paquete.getOperacion());

            	out.writeObject(paquete);
                out.flush();
                dormir(1000);
                System.out.println("Paquete enviado a " + servidor.getAddress() + ":" + servidor.getPuerto());

            } catch (Exception e) {
                System.err.println("Error al conectar con " + servidor.getAddress() + ":" + servidor.getPuerto());
                e.printStackTrace();
            }
        }
    }*/

    public void actualizarServidores(Paquete paquete) {
        Servidor sys = Servidor.getInstance();
        ArrayList<PuertoDTO> servidores = sys.getServidores();

        for (PuertoDTO servidor : servidores) {
            if (servidor.getPuerto() == PUERTO && servidor.getAddress().equals(IP)) {
                continue;
            }

            try (Socket socket = new Socket(servidor.getAddress(), servidor.getPuerto());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.flush();

                System.out.println("Enviando paquete a " + servidor.getAddress() + ":" + servidor.getPuerto() + " : " + paquete.getOperacion());

                out.writeObject(paquete);
                out.flush();
                dormir(1000);
                System.out.println("Paquete enviado a " + servidor.getAddress() + ":" + servidor.getPuerto());

            } catch (Exception e) {
                System.err.println("Error al conectar con " + servidor.getAddress() + ":" + servidor.getPuerto());
                e.printStackTrace();
            }
        }
    }

    
    private void dormir(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {}
	}

	public void terminar() throws IOException {
        if (serverSocket != null) serverSocket.close();
        if (monitorSocket != null) monitorSocket.close();
        if (mout != null) mout.close();
        if (min != null) min.close();
    }

    public static void main(String[] args) throws IOException {
        ConexionServidor s = new ConexionServidor();
        s.registrarServidor();
        s.iniciar(); 
    }
}
