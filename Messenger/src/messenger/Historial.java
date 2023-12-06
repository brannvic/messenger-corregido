package messenger;

import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import interfaz.VHistorial;
import javax.swing.table.DefaultTableModel;

public class Historial {
    
    public String rutaDeArchivo;
    
    public Historial(String rutadeArchivo){
        this.rutaDeArchivo = rutadeArchivo;
    }
    
    public void agregarMensaje(Messenger msg, String tiempo){
        
        try {
            DocumentBuilderFactory crearDoc = DocumentBuilderFactory.newInstance();
            DocumentBuilder construirDoc = crearDoc.newDocumentBuilder();
            Document doc = construirDoc.parse(rutaDeArchivo);
 
            Node data = doc.getFirstChild();
            
            Element mensaje = doc.createElement("mensaje");
            Element _de = doc.createElement("de"); _de.setTextContent(msg.de);
            Element _contenido = doc.createElement("contenido"); _contenido.setTextContent(msg.contenido);
            Element _para = doc.createElement("para"); _para.setTextContent(msg.para);
            Element _tiempo = doc.createElement("tiempo"); _tiempo.setTextContent(tiempo);
            
            mensaje.appendChild(_de); mensaje.appendChild(_contenido); mensaje.appendChild(_para); mensaje.appendChild(_tiempo);
            data.appendChild(mensaje);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformar = transformerFactory.newTransformer();
            DOMSource buscar = new DOMSource(doc);
            StreamResult resultado = new StreamResult(new File(rutaDeArchivo));
            transformar.transform(buscar, resultado);
 
	   } 
           catch(Exception ex){
		System.out.println("Excepcion modificacion en xml");
	   }
	}
   
    public void tablaArchivos(VHistorial frame){
      
        DefaultTableModel model = (DefaultTableModel) frame.jTable1.getModel();
    
        try{
            File fXmlFile = new File(rutaDeArchivo);
            DocumentBuilderFactory crearDB = DocumentBuilderFactory.newInstance();
            DocumentBuilder construirD = crearDB.newDocumentBuilder();
            Document doc = construirD.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            
            NodeList nLista = doc.getElementsByTagName("mensaje");
            
            for (int temp = 0; temp < nLista.getLength(); temp++) {
                Node nNode = nLista.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    model.addRow(new Object[]{valorDeEtiqueta("de", eElement), valorDeEtiqueta("contenido", eElement), valorDeEtiqueta("para", eElement), valorDeEtiqueta("tiempo", eElement)});
                }
            }
        }
        catch(Exception ex){
            System.out.println("Agregar Excepcion");
        }
    }
    
    public static String valorDeEtiqueta(String sTag, Element eElement) {
	NodeList nlLista = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValor = (Node) nlLista.item(0);
	return nValor.getNodeValue();
  }
}
