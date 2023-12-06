package messenger;

import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

public class BaseDeDatos {
    
    public String rutaDeArchivo;
    
    public BaseDeDatos(String rutaDeArchivo){
        this.rutaDeArchivo = rutaDeArchivo;
    }
    
    public boolean salidaUsuario(String usuario){
        
        try{
            File fXmlFile = new File(rutaDeArchivo);
            DocumentBuilderFactory crearDB = DocumentBuilderFactory.newInstance();
            DocumentBuilder construirD = crearDB.newDocumentBuilder();
            Document doc = construirD.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            
            NodeList nLista = doc.getElementsByTagName("usuario");
            
            for (int temp = 0; temp < nLista.getLength(); temp++) {
                Node nNode = nLista.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElemento = (Element) nNode;
                    if(valorDeEtiqueta("usuario", eElemento).equals(usuario)){
                        return true;
                    }
                }
            }
            return false;
        }
        catch(Exception ex){
            System.out.println("Base de datos excepcion : salidaUsuario()");
            return false;
        }
    }
    
    public boolean checarInicio(String usuario, String contrasena){
        
        if(!salidaUsuario(usuario)){ return false; }
        
        try{
            File fXmlFile = new File(rutaDeArchivo);
            DocumentBuilderFactory crearDB = DocumentBuilderFactory.newInstance();
            DocumentBuilder construirD = crearDB.newDocumentBuilder();
            Document doc = construirD.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            
            NodeList nLista = doc.getElementsByTagName("usuario");
            
            for (int temp = 0; temp < nLista.getLength(); temp++) {
                Node nNode = nLista.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElemento = (Element) nNode;
                    if(valorDeEtiqueta("usuario", eElemento).equals(usuario) && valorDeEtiqueta("contraseña", eElemento).equals(contrasena)){
                        return true;
                    }
                }
            }
            System.out.println("Hippie");
            return false;
        }
        catch(Exception ex){
            System.out.println("Base de datos excepcion : salidaUsuario()");
            return false;
        }
    }
    
    public void agregarUsuario(String usuario, String contrasena){
        
        try {
            DocumentBuilderFactory crearDoc = DocumentBuilderFactory.newInstance();
            DocumentBuilder construirDoc = crearDoc.newDocumentBuilder();
            Document doc = construirDoc.parse(rutaDeArchivo);
 
            Node data = doc.getFirstChild();
            
            Element newpersona = doc.createElement("persona");
            Element newusuario = doc.createElement("usuario"); newusuario.setTextContent(usuario);
            Element newcontrasena = doc.createElement("constraseña"); newcontrasena.setTextContent(contrasena);
            
            newpersona.appendChild(newusuario); newpersona.appendChild(newcontrasena); data.appendChild(newpersona);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformar = transformerFactory.newTransformer();
            DOMSource buscar = new DOMSource(doc);
            StreamResult resultado = new StreamResult(new File(rutaDeArchivo));
            transformar.transform(buscar, resultado);
 
	   } 
           catch(Exception ex){
		System.out.println("Excepcion modify xml");
	   }
	}
    
    public static String valorDeEtiqueta(String sTag, Element eElemento) {
	NodeList nlLista = eElemento.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValor = (Node) nlLista.item(0);
	return nValor.getNodeValue();
  }
}
