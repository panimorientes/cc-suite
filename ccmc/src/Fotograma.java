//~--- JDK imports ------------------------------------------------------------

/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
import java.awt.image.BufferedImage;

import java.io.*;

import java.util.Date;

/**
 *
 * @author daniel
 */
public class Fotograma implements Serializable {
    public String Camara;
    public String Fecha;
    public byte[] bytesImagen;

    public Fotograma(String Camara, byte[] image) {
        this.Camara      = Camara;
        this.bytesImagen = image;
        Fecha = new Date().toString();
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
