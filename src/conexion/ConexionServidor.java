package conexion;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import utils.ConfigLoader;

public class ConexionServidor {
    private static String monitor_host = ConfigLoader.mHost;
    private static int monitor_port = ConfigLoader.mPort;
    private static final int PUERTO = ConfigLoader.sPort;
    private static final String IP = ConfigLoader.sHost;
    
    private ServerSocket serverSocket;
    private ArrayList<PuertoDTO> servidores = new ArrayList<>();

    // Streams y socket al monitor, ahora atributos
    private Socket monitorSocket;
    private ObjectOutputStream mout;
    private ObjectInputStream min;

    public ConexionServidor() {}

    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(PUERTO);
        System.out.println("Servidor escuchando en el puerto " + PUERTO);
        
        while (true) {
            Socket socket = serverSocket.accept();
            new ClientHandler(socket).start();
        }
    }

    public void registrarServidor() {
        try {
            monitorSocket = new Socket(monitor_host, monitor_port);
            mout = new ObjectOutputStream(monitorSocket.getOutputStream());
            mout.flush(); // importante antes de crear el input
            min = new ObjectInputStream(monitorSocket.getInputStream());

            // 1) Enviar registro
            mout.writeObject(new Paquete("registrarS", new PuertoDTO(PUERTO, IP)));
            mout.flush();

            // 2) Esperar lista de servidores
            Paquete resp = (Paquete) min.readObject();
            if ("obtenerSR".equals(resp.getOperacion())) {
                ListaSDTO lista = (ListaSDTO) resp.getContenido();
                List<PuertoDTO> otros = lista.getServidores();
                servidores.addAll(otros);
                System.out.println("Otros servidores: " + servidores.toString());
            }
            
            mout.writeObject(new Paquete("ACK",null));
            mout.flush();



        } catch (IOException | ClassNotFoundException e) {
            System.err.println("No puedo conectar al monitor");
            e.printStackTrace();
            System.exit(1);
        }
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
