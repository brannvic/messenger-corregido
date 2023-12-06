package messenger;

import interfaz.VCliente;
import java.awt.HeadlessException;
import java.io.*;
import java.net.*;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class Conector implements Runnable{
    
    public int puerto;
    public String ip;
    public Socket socket;
    public VCliente ui;
    public ObjectInputStream entrada;
    public ObjectOutputStream salida;
    public Historial hist;
    
    public Conector(VCliente frame) throws IOException{
        ui = frame; 
        this.ip = ui.ip; 
        this.puerto = ui.puerto;
        socket = new Socket(InetAddress.getByName(ip), puerto);
            
        salida = new ObjectOutputStream(socket.getOutputStream());
        salida.flush();
        entrada = new ObjectInputStream(socket.getInputStream());
        
        hist = ui.hist;
    }

    @Override
    public void run() {
        boolean keepRunning = true;
        while(keepRunning){
            try {
                Messenger msg = (Messenger) entrada.readObject();
                System.out.println("Entrando : "+msg.toString());
                
                if(msg.tipo.equals("mensaje")){
                    if(msg.para.equals(ui.usuario)){
                        ui.jTextArea1.append("["+msg.de +" > yo] : " + msg.contenido + "\n");
                    }
                    else{
                        ui.jTextArea1.append("["+ msg.de +" > "+ msg.para +"] : " + msg.contenido + "\n");
                    }
                                            
                    if(!msg.contenido.equals(".Hasta luego") && !msg.de.equals(ui.usuario)){
                        String msgTime = (new Date()).toString();
                        
                        try{
                            hist.agregarMensaje(msg, msgTime);
                            DefaultTableModel table = (DefaultTableModel) ui.historialFrame.jTable1.getModel();
                            table.addRow(new Object[]{msg.de, msg.contenido, "yo", msgTime});
                        }
                        catch(Exception ex){}  
                    }
                }
                else if(msg.tipo.equals("Inicio de sesión")){
                    if(msg.contenido.equals("Verdadero")){
                        ui.jButton2.setEnabled(false); 
                        ui.jButton3.setEnabled(false);                        
                        ui.jButton4.setEnabled(true); 
                        ui.jButton5.setEnabled(true);
                        ui.jTextArea1.append("[servidor > yo] : Inicio de sesión correcto\n");
                        ui.jTextField3.setEnabled(false); 
                        ui.jPasswordField1.setEnabled(false);
                    }
                    else{
                        ui.jTextArea1.append("[servidor > yo] : Inicio de sesión fallido\n");
                    }
                }
                else if(msg.tipo.equals("test")){
                    ui.jButton1.setEnabled(false);
                    ui.jButton2.setEnabled(true); 
                    ui.jButton3.setEnabled(true);
                    ui.jTextField3.setEnabled(true); 
                    ui.jPasswordField1.setEnabled(true);
                    ui.jTextField1.setEditable(false);
                    ui.jTextField2.setEditable(false);
                    ui.jButton7.setEnabled(true);
                }
                else if(msg.tipo.equals("Nuevo usuario")){
                    if(!msg.contenido.equals(ui.usuario)){
                        boolean exists = false;
                        for(int i = 0; i < ui.model.getSize(); i++){
                            if(ui.model.getElementAt(i).equals(msg.contenido)){
                                exists = true; break;
                            }
                        }
                        if(!exists){ ui.model.addElement(msg.contenido); }
                    }
                }
                else if(msg.tipo.equals("signup")){
                    if(msg.contenido.equals("TRUE")){
                        ui.jButton2.setEnabled(false); 
                        ui.jButton3.setEnabled(false);
                        ui.jButton4.setEnabled(true); 
                        ui.jButton5.setEnabled(true);
                        ui.jTextArea1.append("[servidor > yo] : Registro correcto\n");
                    }
                    else{
                        ui.jTextArea1.append("[servidor > yo] : Registro fallido\n");
                    }
                }
                else if(msg.tipo.equals("Cerrar sesión")){
                    if(msg.contenido.equals(ui.usuario)){
                        ui.jTextArea1.append("["+ msg.de +" > yo] : Hasta luego\n");
                        ui.jButton1.setEnabled(true); 
                        ui.jButton4.setEnabled(false); 
                        ui.jTextField1.setEditable(true); 
                        ui.jTextField2.setEditable(true);
                        
                        for(int i = 1; i < ui.model.size(); i++){
                            ui.model.removeElementAt(i);
                        }
                        
                        ui.clienteThread.stop();
                    }
                    else{
                        ui.model.removeElement(msg.contenido);
                        ui.jTextArea1.append("["+ msg.de +" > todos] : "+ msg.contenido +" han cerrado sesión\n");
                    }
                }
                else if(msg.tipo.equals("subir_req")){
                    
                    if(JOptionPane.showConfirmDialog(ui, ("Aceptar '"+msg.contenido+"' desde "+msg.de+" ?")) == 0){
                        
                        JFileChooser jf = new JFileChooser();
                        jf.setSelectedFile(new File(msg.contenido));
                        int returnVal = jf.showSaveDialog(ui);
                       
                        String saveTo = jf.getSelectedFile().getPath();
                        if(saveTo != null && returnVal == JFileChooser.APPROVE_OPTION){
                            Descarga dwn = new Descarga(saveTo, ui);
                            Thread t = new Thread(dwn);
                            t.start();
                            send(new Messenger("subir_res", ui.usuario, (""+dwn.puerto), msg.de));
                        }
                        else{
                            send(new Messenger("subir_res", ui.usuario, "NO", msg.de));
                        }
                    }
                    else{
                        send(new Messenger("subir_res", ui.usuario, "NO", msg.de));
                    }
                }
                else if(msg.tipo.equals("subir_res")){
                    if(!msg.contenido.equals("NO")){
                        int puerto  = Integer.parseInt(msg.contenido);
                        String addr = msg.de;
                        
                        ui.jButton5.setEnabled(false); 
                        ui.jButton6.setEnabled(false);
                        Subida upl = new Subida(addr, puerto, ui.archivo, ui);
                        Thread t = new Thread(upl);
                        t.start();
                    }
                    else{
                        ui.jTextArea1.append("[servidor > yo] : "+msg.de+" Subida de archivo rechazada\n");
                    }
                }
                else{
                    ui.jTextArea1.append("[servidor > yo] : Tipo de mensaje desconocido\n");
                }
            }
            catch(HeadlessException ex) {
                keepRunning = false;
                ui.jTextArea1.append("[Aplicacion > yo] : Coneccion fallida\n");
                ui.jButton1.setEnabled(true); 
                ui.jTextField1.setEditable(true); 
                ui.jTextField2.setEditable(true);
                ui.jButton4.setEnabled(false); 
                ui.jButton5.setEnabled(false); 
                ui.jButton5.setEnabled(false);
                
                for(int i = 1; i < ui.model.size(); i++){
                    ui.model.removeElementAt(i);
                }
                
                ui.clienteThread.stop();
                
                System.out.println("Excepcion SocketClient run()");} catch (IOException ex) {
                    keepRunning = false;
                    ui.jTextArea1.append("[Aplicacion > yo] : Coneccion fallida\n");
                    ui.jButton1.setEnabled(true); 
                    ui.jTextField1.setEditable(true); 
                    ui.jTextField2.setEditable(true);
                    ui.jButton4.setEnabled(false); 
                    ui.jButton5.setEnabled(false); 
                    ui.jButton5.setEnabled(false);
                    
                    for(int i = 1; i < ui.model.size(); i++){
                        ui.model.removeElementAt(i);
                    }
                    
                    ui.clienteThread.stop();
                    
                    System.out.println("Exception SocketClient run()");
            } catch (ClassNotFoundException ex) {
                keepRunning = false;
                ui.jTextArea1.append("[Aplicacion > yo] : Coneccion fallida\n");
                ui.jButton1.setEnabled(true); 
                ui.jTextField1.setEditable(true); 
                ui.jTextField2.setEditable(true);
                ui.jButton4.setEnabled(false); 
                ui.jButton5.setEnabled(false); 
                ui.jButton5.setEnabled(false);
                
                for(int i = 1; i < ui.model.size(); i++){
                    ui.model.removeElementAt(i);
                }
                
                ui.clienteThread.stop();
                
                System.out.println("Excepcion SocketClient run()");
            } catch (NumberFormatException ex) {
                keepRunning = false;
                ui.jTextArea1.append("[Aplicacion > yo] : Coneccion fallida\n");
                ui.jButton1.setEnabled(true); 
                ui.jTextField1.setEditable(true); 
                ui.jTextField2.setEditable(true);
                ui.jButton4.setEnabled(false); 
                ui.jButton5.setEnabled(false); 
                ui.jButton5.setEnabled(false);
                
                for(int i = 1; i < ui.model.size(); i++){
                    ui.model.removeElementAt(i);
                }
                
                ui.clienteThread.stop();
                
                System.out.println("Excepcion SocketClient run()");
            }
        }
    }
    
    public void send(Messenger msg){
        try {
            salida.writeObject(msg);
            salida.flush();
            System.out.println("Saliente : "+msg.toString());
            
            if(msg.tipo.equals("mensaje") && !msg.contenido.equals(".hasta luego")){
                String msgTime = (new Date()).toString();
                try{
                    hist.agregarMensaje(msg, msgTime);               
                    DefaultTableModel table = (DefaultTableModel) ui.historialFrame.jTable1.getModel();
                    table.addRow(new Object[]{"yo", msg.contenido, msg.para, msgTime});
                }
                catch(Exception ex){}
            }
        } 
        catch (IOException ex) {
            System.out.println("Excepcion SocketClient enviar()");
        }
    }
    
    public void closeThread(Thread t){
        t = null;
    }
}
