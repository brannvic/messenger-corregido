package messenger;

import java.io.*;
import java.net.*;

class servidorHilo extends Thread { 
	
    public SocketServidor servidor = null;
    public Socket socket = null;
    public int ID = -1;
    public String usuario = "";
    public ObjectInputStream transmisionEntrada  =  null;
    public ObjectOutputStream transmisionSalida = null;
    public VServidor ui;

    public servidorHilo(SocketServidor _servidor, Socket _socket){  
    	super();
        servidor = _servidor;
        socket = _socket;
        ID     = socket.getPort();
        ui = _servidor.ui;
    }
    
    public void enviar(Mensaje msg){
        try {
            transmisionSalida.writeObject(msg);
            transmisionSalida.flush();
        } 
        catch (IOException ex) {
            System.out.println("Excepcion [SocketCliente : enviando(...)]");
        }
    }
    
    public int obtenerID(){  
	    return ID;
    }
   
    @SuppressWarnings("obsolencia")
	public void run(){  
    	ui.jTextArea1.append("\nServidor hilo " + ID + " compilando.");
        while (true){  
    	    try{  
                Mensaje msg = (Mensaje) transmisionEntrada.readObject();
    	    	servidor.manejar(ID, msg);
            }
            catch(Exception ioe){  
            	System.out.println(ID + " ERROR lectura: " + ioe.getMessage());
                servidor.remove(ID);
                stop();
            }
        }
    }
    
    public void open() throws IOException {  
        transmisionSalida = new ObjectOutputStream(socket.getOutputStream());
        transmisionSalida.flush();
        transmisionEntrada = new ObjectInputStream(socket.getInputStream());
    }
    
    public void close() throws IOException {  
    	if (socket != null)    socket.close();
        if (transmisionEntrada != null)  transmisionEntrada.close();
        if (transmisionSalida != null) transmisionSalida.close();
    }
}





public class SocketServidor implements Runnable {
    
    public servidorHilo clientes[];
    public ServerSocket servidor = null;
    public Thread       hilo = null;
    public int clienteConteo = 0, puerto = 13000;
    public VServidor ui;
    public BaseDeDatos db;

    public SocketServidor(VServidor frame){
       
        clientes = new servidorHilo[50];
        ui = frame;
        db = new BaseDeDatos(ui.rutaDeArchivo);
        
	try{  
	    servidor = new ServerSocket(puerto);
            puerto = servidor.getLocalPort();
	    ui.jTextArea1.append("Servidor iniciado. IP : " + InetAddress.getLocalHost() + ", Puerto : " + servidor.getLocalPort());
	    start(); 
        }
	catch(IOException ioe){  
            ui.jTextArea1.append("No se puede enlazar el puerto: " + puerto + "\nReintentando"); 
            ui.RetryStart(0);
	}
    }
    
    public SocketServidor(VServidor frame, int Puerto){
       
        clientes = new servidorHilo[50];
        ui = frame;
        puerto = Puerto;
        db = new BaseDeDatos(ui.rutaDeArchivo);
        
	try{  
	    servidor = new ServerSocket(puerto);
            puerto = servidor.getLocalPort();
	    ui.jTextArea1.append("Servidor iniciado. IP : " + InetAddress.getLocalHost() + ", Puerto : " + servidor.getLocalPort());
	    start(); 
        }
	catch(IOException ioe){  
            ui.jTextArea1.append("\nNo se puede enlazar el puerto " + puerto + ": " + ioe.getMessage()); 
	}
    }
	
    public void run(){  
	while (hilo != null){  
            try{  
		ui.jTextArea1.append("\nEsperando al cliente ..."); 
	        agregarHilo(servidor.accept()); 
	    }
	    catch(Exception ioe){ 
                ui.jTextArea1.append("\nError al aceptar servidor: \n");
                ui.RetryStart(0);
	    }
        }
    }
	
    public void start(){  
    	if (hilo == null){  
            hilo = new Thread(this); 
	    hilo.start();
	}
    }
    
    @SuppressWarnings("obsolencia")
    public void parar(){  
        if (hilo != null){  
            hilo.stop(); 
	    hilo = null;
	}
    }
    
    private int encontrarCliente(int ID){  
    	for (int i = 0; i < clienteConteo; i++){
        	if (clientes[i].obtenerID() == ID){
                    return i;
                }
	}
	return -1;
    }
	
    public synchronized void manejar(int ID, Mensaje msg){  
	if (msg.contenido.equals(".hasta luego")){
            anuncio("cerrar sesión", "servidor", msg.de);
            remove(ID); 
	}
	else{
            if(msg.tipo.equals("iniciar sesión")){
                if(encontrarHiloDeUsuario(msg.de) == null){
                    if(db.checarInicio(msg.de, msg.contenido)){
                        clientes[encontrarCliente(ID)].usuario = msg.de;
                        clientes[encontrarCliente(ID)].enviar(new Mensaje("iniciar sesión", "servidor", "verdadero", msg.de));
                        anuncio("nuevo usuario", "servedior", msg.de);
                        mandarListaDeUsuarios(msg.de);
                    }
                    else{
                        clientes[encontrarCliente(ID)].enviar(new Mensaje("iniciar sesión", "servidor", "salso", msg.de));
                    } 
                }
                else{
                    clientes[encontrarCliente(ID)].enviar(new Mensaje("iniciar sesión", "servidor", "falso", msg.de));
                }
            }
            else if(msg.tipo.equals("mensaje")){
                if(msg.para.equals("todos")){
                    anuncio("mensaje", msg.de, msg.contenido);
                }
                else{
                    encontrarHiloDeUsuario(msg.para).enviar(new Mensaje(msg.tipo, msg.de, msg.contenido, msg.para));
                    clientes[encontrarCliente(ID)].enviar(new Mensaje(msg.tipo, msg.de, msg.contenido, msg.para));
                }
            }
            else if(msg.tipo.equals("test")){
                clientes[encontrarCliente(ID)].enviar(new Mensaje("test", "servidor", "OK", msg.de));
            }
            else if(msg.tipo.equals("Registrarse")){
                if(encontrarHiloDeUsuario(msg.de) == null){
                    if(!db.salidaUsuario(msg.de)){
                        db.agregarUsuario(msg.de, msg.contenido);
                        clientes[encontrarCliente(ID)].usuario = msg.de;
                        clientes[encontrarCliente(ID)].enviar(new Mensaje("Resgistrarse", "servidor", "verdadero", msg.de));
                        clientes[encontrarCliente(ID)].enviar(new Mensaje("iniciar sesión", "servidor", "verdadero", msg.de));
                        anuncio("newuser", "SERVER", msg.de);
                        mandarListaDeUsuarios(msg.de);
                    }
                    else{
                        clientes[encontrarCliente(ID)].enviar(new Mensaje("Registrarse", "servidor", "falso", msg.de));
                    }
                }
                else{
                    clientes[encontrarCliente(ID)].enviar(new Mensaje("Registrarse", "servidor", "FALSE", msg.de));
                }
            }
            else if(msg.tipo.equals("subir_req")){
                if(msg.para.equals("todos")){
                    clientes[encontrarCliente(ID)].enviar(new Mensaje("mensaje", "servidor", "Subiendo para 'todo' esta prohibido", msg.de));
                }
                else{
                    encontrarHiloDeUsuario(msg.para).enviar(new Mensaje("subir_req", msg.de, msg.contenido, msg.para));
                }
            }
            else if(msg.tipo.equals("subir_res")){
                if(!msg.contenido.equals("NO")){
                    String IP = encontrarHiloDeUsuario(msg.de).socket.getInetAddress().getHostAddress();
                    encontrarHiloDeUsuario(msg.para).enviar(new Mensaje("subir_res", IP, msg.contenido, msg.para));
                }
                else{
                    encontrarHiloDeUsuario(msg.para).enviar(new Mensaje("subir_res", msg.de, msg.contenido, msg.para));
                }
            }
	}
    }
    
    public void anuncio(String type, String sender, String content){
        Mensaje msg = new Mensaje(type, sender, content, "todos");
        for(int i = 0; i < clienteConteo; i++){
            clientes[i].enviar(msg);
        }
    }
    
    public void mandarListaDeUsuarios(String toWhom){
        for(int i = 0; i < clienteConteo; i++){
            encontrarHiloDeUsuario(toWhom).enviar(new Mensaje("nuevo usuario", "sevidor", clientes[i].usuario, toWhom));
        }
    }
    
    public servidorHilo encontrarHiloDeUsuario(String usr){
        for(int i = 0; i < clienteConteo; i++){
            if(clientes[i].usuario.equals(usr)){
                return clientes[i];
            }
        }
        return null;
    }
	
    @SuppressWarnings("obsolencia")
    public synchronized void remove(int ID){  
    int pos = encontrarCliente(ID);
        if (pos >= 0){  
            servidorHilo toTerminate = clientes[pos];
            ui.jTextArea1.append("\nQuitando el cliente del hilo  " + ID + " a " + pos);
	    if (pos < clienteConteo-1){
                for (int i = pos+1; i < clienteConteo; i++){
                    clientes[i-1] = clientes[i];
	        }
	    }
	    clienteConteo--;
	    try{  
	      	toTerminate.close(); 
	    }
	    catch(IOException ioe){  
	      	ui.jTextArea1.append("\nError cerrando el hilo: " + ioe); 
	    }
	    toTerminate.stop(); 
	}
    }
    
    private void agregarHilo(Socket socket){  
	if (clienteConteo < clientes.length){  
            ui.jTextArea1.append("\nCliente acceptado: " + socket);
	    clientes[clienteConteo] = new servidorHilo(this, socket);
	    try{  
	      	clientes[clienteConteo].open(); 
	        clientes[clienteConteo].start();  
	        clienteConteo++; 
	    }
	    catch(IOException ioe){  
	      	ui.jTextArea1.append("\nError abriendo el hilo: " + ioe); 
	    } 
	}
	else{
            ui.jTextArea1.append("\nCliente rechazado: maximo " + clientes.length + " alcanzado.");
	}
    }
}
