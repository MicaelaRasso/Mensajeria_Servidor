package modelo;

import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import conexion.MensajeDTO;
import conexion.Paquete;
import conexion.PuertoDTO;
import conexion.UsuarioDTO;

public class Servidor {
/**
 * Singleton que mantiene el estado del servidor:
 * - Usuarios registrados (nombre → IP)
 * - Mensajes pendientes para cada receptor
 * - Último heartbeat de cada servidor (si quisieras llevarlo)
 */
   private static final Servidor INSTANCE = new Servidor();

    private final Map<String,Usuario> usuarios  = new ConcurrentHashMap<>();
    private final Map<String,List<MensajeDTO>> pendings = new ConcurrentHashMap<>();

    private ArrayList<PuertoDTO> servidores = new ArrayList<>();
    

    private Servidor() {}

    public static Servidor getInstance() {
        return INSTANCE;
    }


    public synchronized Paquete registrarUsuario(UsuarioDTO uDTO, ObjectOutputStream out) {
    	String nombre = uDTO.getNombre();
        Usuario usuario = usuarios.get(nombre);
        UsuarioDTO response = new UsuarioDTO(nombre);

        if(usuario != null && !usuario.isConnected()) { //reconectado
        	usuario.setConnected(true);
        	usuario.setOut(out);
        	System.out.println("[ServerSystem] Usuario reconectado: " + usuario.toString());
        	response.setRespuesta("registrado");
        }else {
        	if(usuario == null) { //nuevo usuario
        		Usuario u = new Usuario(nombre);
        		usuarios.put(nombre, u);
            	u.setOut(out);
        		System.out.println("[ServerSystem] Usuario registrado: " + u.toString());
        		response.setRespuesta("registrado");
        	}else {
        		response.setRespuesta("en uso"); 
        		System.out.println("[ServerSystem] Usuario en uso: " + nombre);
        	}
        }
        pendings.putIfAbsent(nombre, new ArrayList<>());
        //System.out.println(usuarios.toString());
        //System.out.println(pendings.toString());
        return new Paquete("registrarUR", response);
    }

    /** Atiende una consulta de existencia de usuario */
    public Paquete manejarConsulta(UsuarioDTO uDTO) { 
    	String buscado = uDTO.getNombre();
    	
    	UsuarioDTO uDTO1 = new UsuarioDTO(buscado);
        if (usuarios.containsKey(buscado)) {
            uDTO1.setRespuesta("encontrado");
        } else {
        	uDTO1.setRespuesta("no existe"); 
        }
    	Paquete paq = new Paquete("agregarCR", uDTO1);
    	return paq;
    }

    /** Almacena un mensaje pendiente */
    public synchronized Paquete manejarMensaje(MensajeDTO mDTO) {
        String nombreReceptor = mDTO.getReceptor().getNombre();
        Paquete resend = null;

        Usuario receptor = usuarios.get(nombreReceptor);
        //System.out.println(receptor.isConnected());
        if(receptor != null) {
        	if(receptor.isConnected()) {
        		resend = new Paquete("recibirM", mDTO);
        	}else{        		
        		almacenarMensaje(mDTO);
        		System.out.println("[ServerSystem] Mensaje para " + nombreReceptor + " almacenado.");
        	}
        }
        return resend;
    }


	public void almacenarMensaje(MensajeDTO mDTO) {
        
		UsuarioDTO receptorDTO = (UsuarioDTO) mDTO.getReceptor();
		Usuario receptor = usuarios.get(receptorDTO.getNombre());
		if(!receptor.isConnected()) {
			pendings.get(receptor.getNombre()).add(mDTO);
		}
			
	}

    /** Devuelve y limpia la lista de pendientes de un usuario */
    public synchronized List<MensajeDTO> entregarPendientes(String usuario) {
	    List<MensajeDTO> mensajes = new ArrayList<>(pendings.get(usuario));
	    pendings.get(usuario).clear();
	    return mensajes;
    }

    /** Actualiza el timestamp de un heartbeat (solo logging) */
    public void actualizarHeartbeat(PuertoDTO pDTO) {
        System.out.println("[ServerSystem] Heartbeat recibido para " + pDTO.getPuerto() + " a las " + LocalDateTime.now());
    }

	public void desconectarUsuario(UsuarioDTO contenido) {
		usuarios.get(contenido.getNombre()).setConnected(false);
	}

	public Map<String, Usuario> getUsuarios() {
		return usuarios;
	}

	public void setServidores(ArrayList<PuertoDTO> servidores) {
		this.servidores = servidores;
	}

	public ArrayList<PuertoDTO> getServidores() {
		return servidores;
	}
	
	
	
}