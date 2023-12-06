package messenger;

import interfaz.VCliente;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Subida implements Runnable{

    public String ip;
    public int puerto;
    public Socket socket;
    public FileInputStream entrada;
    public OutputStream salida;
    public File archivo;
    public VCliente ui;
    
    public Subida(String ip, int puerto, File rutaDeArchivo, VCliente frame){
        super();
        try {
            archivo = rutaDeArchivo; ui = frame;
            socket = new Socket(InetAddress.getByName(ip), puerto);
            salida = socket.getOutputStream();
            entrada = new FileInputStream(rutaDeArchivo);
        } 
        catch (Exception ex) {
            System.out.println("Excepcion [Subiendo : subiendo(...)]");
        }
    }
    
    @Override
    public void run() {
        try {       
            byte[] buffer = new byte[1024];
            int count;
            
            while((count = entrada.read(buffer)) >= 0){
                salida.write(buffer, 0, count);
            }
            salida.flush();
            
            ui.jTextArea1.append("[Aplicacion > yo] : Subida de archivo completada\n");
            ui.jButton5.setEnabled(true); ui.jButton6.setEnabled(true);
            ui.jTextField5.setVisible(true);
            
            if(entrada != null){ entrada.close(); }
            if(salida != null){ salida.close(); }
            if(socket != null){ socket.close(); }
        }
        catch (Exception ex) {
            System.out.println("Excepcion [Subiendo : buscando()]");
            ex.printStackTrace();
        }
    }

}