package modelo;

import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import conexion.Contenido;
import conexion.ListaContenido;
import conexion.ListaSDTO;
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
//        	System.out.println("[ServerSystem] Usuario reconectado: " + usuario.toString());
        	response.setRespuesta("registrado");
        }else {
        	if(usuario == null) { //nuevo usuario
        		Usuario u = new Usuario(nombre);
        		usuarios.put(nombre, u);
            	u.setOut(out);
//        		System.out.println("[ServerSystem] Usuario registrado: " + u.toString());
        		response.setRespuesta("registrado");
        	}else {
        		response.setRespuesta("en uso"); 
//        		System.out.println("[ServerSystem] Usuario en uso: " + nombre);
        	}
        }
        pendings.putIfAbsent(nombre, new ArrayList<>());
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
        if(receptor != null) {
        	if(receptor.isConnected()) {
        		resend = new Paquete("recibirM", mDTO);
        	}else{        		
        		almacenarMensaje(mDTO);
//        		System.out.println("[ServerSystem] Mensaje para " + nombreReceptor + " almacenado.");
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

	
	public void actualizarUsuarios(ListaContenido contenido) {
		List<Usuario> uNuevos = convertirLista(contenido);
		Iterator<Usuario> it = uNuevos.iterator();
		while(it.hasNext()) {
			Usuario u = it.next();
			usuarios.putIfAbsent(u.getNombre(), u);				
		}	
		if(usuarios != null)
			System.out.println("Usuarios registrados: "+ usuarios.toString());
		else
			System.out.println("No hay usuarios registrados");
	}
	
	public List<Usuario> convertirLista(ListaContenido listaContenido) {
	    List<Usuario> usuarios = new ArrayList<>();

	    for (Contenido c : listaContenido.getContenido()) {
	            Usuario usuario = new Usuario(((UsuarioDTO)c).getNombre());
	            usuarios.add(usuario);
	    }
	    return usuarios;
	}

	public void actualizarMensajesPendientes(ListaContenido listaContenido) {		
		pendings.clear();
		for (Contenido c : listaContenido.getContenido()) {
	        if (c instanceof MensajeDTO mensaje) {
	            String nombreUsuario = mensaje.getReceptor().getNombre();
	            pendings.computeIfAbsent(nombreUsuario, k -> new ArrayList<>()).add(mensaje);
	        }
	    }
		if(pendings != null)
			System.out.println("Mensajes almacenados: "+ pendings.toString());
		else
			System.out.println("No hay mensajes pendientes");
	}	

	public void actualizarListaDeServidores(ListaSDTO listaContenido) {
		this.setServidores(new ArrayList<PuertoDTO>(listaContenido.getServidores()));
		System.out.println("Servidores registrados: " + servidores.toString());
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

	public ListaContenido getUsuariosDTO() {
		UsuarioDTO uDTO;
		ArrayList<Contenido> listaUDTO = new ArrayList<Contenido>();
		
		Iterator<Usuario> it = usuarios.values().iterator();
		while (it.hasNext()) {
			Usuario u = it.next();
			uDTO = new UsuarioDTO(u.getNombre());
			listaUDTO.add(uDTO);
		}
		
		ListaContenido lista = new ListaContenido();
		lista.setContenido(listaUDTO);
		return lista;
	}

	public ListaContenido getMPDTO() {
	    ListaContenido lista = new ListaContenido();
	    ArrayList<Contenido> todosLosMensajes = new ArrayList<>();

	    for (List<MensajeDTO> mensajes : pendings.values()) {
	        todosLosMensajes.addAll(mensajes);
	    }

	    lista.setContenido(todosLosMensajes);
	    return lista;
	}

	public void actualizarSocketDeUsuario(UsuarioDTO usuario, ObjectOutputStream out) {
		if(usuarios.containsKey(usuario.getNombre())) {
			usuarios.get(usuario.getNombre()).setOut(out);
			System.out.println("Se actualizo el socket de "+usuario.getNombre());
		}else
			System.out.println("Fallo la actualizacion del socket de "+usuario.getNombre());

	}
}