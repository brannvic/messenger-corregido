package messenger;

import java.io.Serializable;

public class Mensaje implements Serializable{
    
    private static final long serialVersionUID = 1L;
    public String tipo, de, contenido, para;
    
    public Mensaje(String tipo, String de, String contenido, String para){
        this.tipo = tipo; this.de = de; this.contenido = contenido; this.para = para;
    }
    
    @Override
    public String toString(){
        return "{tipo='"+tipo+"', de='"+de+"', content='"+contenido+"', para='"+para+"'}";
    }
}
