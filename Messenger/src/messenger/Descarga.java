package messenger;

import interfaz.VCliente;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Descarga implements Runnable{
    
    public ServerSocket servidor;
    public Socket socket;
    public int puerto;
    public String guardar = "";
    public InputStream entrada;
    public FileOutputStream salida;
    public VCliente ui;
    
    public Descarga(String guardar, VCliente ui){
        try {
            servidor = new ServerSocket(0);
            puerto = servidor.getLocalPort();
            this.guardar = guardar;
            this.ui = ui;
        } 
        catch (IOException ex) {
            System.out.println("Excepcion [Descargando: Descargando(...)]");
        }
    }

    @Override
    public void run() {
        try {
            socket = servidor.accept();
            System.out.println("Descargando : "+socket.getRemoteSocketAddress());
            
            entrada = socket.getInputStream();
            salida = new FileOutputStream(guardar);
            
            byte[] buffer = new byte[1024];
            int count;
            
            while((count = entrada.read(buffer)) >= 0){
                salida.write(buffer, 0, count);
            }
            
            salida.flush();
            
            ui.jTextArea1.append("[Aplicación > Yo] : Descarga completa\n");
            
            if(salida != null){ salida.close(); }
            if(entrada != null){ entrada.close(); }
            if(socket != null){ socket.close(); }
        } 
        catch (Exception ex) {
            System.out.println("Excepcion [Descarga : buscando(...)]");
        }
    }
}