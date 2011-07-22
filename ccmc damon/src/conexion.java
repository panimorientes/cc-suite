
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author daniel
 */
class conexion extends Thread {

    private Socket soc = null;
    private String archivo = null;
    private Fotograma fo = null;
    private Fotograma ne = null;
    private HashMap alertas = null;
    private HashMap Camaras = null;
    private ObjectInputStream in = null;

    public conexion(Socket sok, String Archi, HashMap Alertas, HashMap Camaras,ObjectInputStream in) throws IOException {
        this.soc = sok;
        this.archivo = Archi;
        this.alertas = Alertas;
        this.Camaras = Camaras;
        this.in = in;
    }
    
        public conexion(Socket sok, String Archi, HashMap Alertas, HashMap Camaras) throws IOException {
        this.soc = sok;
        this.archivo = Archi;
        this.alertas = Alertas;
        this.Camaras = Camaras;
        this.in = new ObjectInputStream(this.soc.getInputStream());
    }

    @Override
    public void run() {
        {
            try {

                FileWriter fstream = new FileWriter(archivo, true);
                BufferedWriter out = new BufferedWriter(fstream);

                try {
                    fo = (Fotograma) in.readObject();
                    out.write("<a href=" + fo.Camara + "/index.html>" + fo.Camara + "</a><br>");
                    System.out.println("se agrego la camara " + fo.Camara + " al index");
                    Camaras.put(fo.Camara, fo.Fecha);
                    out.close();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(conexion.class.getName()).log(Level.SEVERE, null, ex);
                }


            } catch (IOException ex) {
                Logger.getLogger(conexion.class.getName()).log(Level.SEVERE, null, ex);
            }

            boolean flag = true;

            try {

                fo = (Fotograma) in.readObject();
                while (flag) {
                    if (fo != null) {

                        File file = new File(fo.Camara);
                        ne = fo;
                        if (file.exists()) {
                            while (flag) {
                                fo = (Fotograma) in.readObject();
                                ComprarImagen(fo, ne);
                                GuardarImagen(fo);
                                CambiarIndex(fo);
                                ne = fo;
                            }
                        } else {
                            System.out.println("no existe la carpeta, creandola");

                            boolean gg = file.mkdir();
                            if (gg) {
                                System.out.println("Carpeta " + fo.Camara + " creada");
                            } else {
                                System.out.println("Carpeta " + fo.Camara + " no creada");
                            }
                        }

                        System.out.println(fo.Fecha);
                    }
                }

                in.close();
                soc.close();
            } catch (ClassNotFoundException ex) {
                //Logger.getLogger(conexion.class.getName()).log(Level.SEVERE, null, ex);
                Camaras.remove(ne.Camara);
                System.out.println("Se cayo la conexion con la camara");
            } catch (IOException ex) {
                Camaras.remove(ne.Camara);

                //Logger.getLogger(conexion.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Se cayo la conexion con la camara");
            }
        }

    }

    public void CambiarIndex(Fotograma ftgm) throws IOException {
        String sourceUrlString = ftgm.Camara + "/index.html";
        File file = new File(sourceUrlString);

        if (file.exists()) {
            Source source = new Source(new URL("file:" + sourceUrlString));
            OutputDocument outputDocument = new OutputDocument(source);

            outputDocument.replace(source.getElementById("imagen"),
                    "<img id=\"imagen\" src=\"" + ftgm.Fecha + ".jpg" + "\" alt=\"Angry face\" />");

            outputDocument.replace(source.getElementById("fecha"),
                    "<h3 id=\"fecha\">" + ftgm.Fecha + "</h3>");

            Writer out = new FileWriter(sourceUrlString);

            outputDocument.writeTo(out);
            out.close();
        } else {
            FileOutputStream fos;

            fos = new FileOutputStream(sourceUrlString);

            fos.flush();
            new PrintStream(fos).println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"es\" lang=\"es\" dir=\"ltr\">\n" + "<head>\n"
                    + "<title>Cam center</title>\n"
                    + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n"
                    + "<meta name=\"keywords\" content=\"camaras web\"/>\n" + "<head>\n" + "<body>\n" + "<img id=\"imagen\" src=\""
                    + ftgm.Fecha + ".jpg" + "\" />\n" + "<br>" + "<h5 id=\"fecha\">" + ftgm.Fecha + "</h5>" + "<a href=\"../index.html\">Volver</a>" + "</body>\n" + "</html>\n");
            System.out.println("se creo el index de la camara " + ftgm.Camara);
        }
    }

    public void GuardarImagen(Fotograma ftgm) throws IOException {
        byte[] bytesImagen = ftgm.bytesImagen;
        ByteArrayInputStream entradaImagen = new ByteArrayInputStream(bytesImagen);
        BufferedImage bufferedImage = ImageIO.read(entradaImagen);
        String nombreFichero = ftgm.Camara + "/" + ftgm.Fecha;
        // FileOutputStream     out           = new FileOutputStream(nombreFichero+".png");
        ImageCompare.saveJPG(bufferedImage, nombreFichero + ".jpg");
        //   ImageIO.write((RenderedImage) bufferedImage, "png", out);
    }

    private void ComprarImagen(Fotograma fo, Fotograma ne) throws IOException {
        byte[] bytesImagen1 = fo.bytesImagen;
        ByteArrayInputStream entradaImagen1 = new ByteArrayInputStream(bytesImagen1);
        BufferedImage bufferedImage1 = ImageIO.read(entradaImagen1);

        byte[] bytesImagen2 = ne.bytesImagen;
        ByteArrayInputStream entradaImagen2 = new ByteArrayInputStream(bytesImagen2);
        BufferedImage bufferedImage2 = ImageIO.read(entradaImagen2);

        ImageCompare ic = new ImageCompare(bufferedImage1,bufferedImage2);

        ic.setParameters(8, 6, 2, 10);
        // Display some indication of the differences in the image.
        //ic.setDebugMode(2);
        // Compare.
        ic.compare();
        // Display if these images are considered a match according to our parameters.
        //System.out.println("Match: " + ic.match());
        // If its not a match then write a file to show changed regions.
        if (!ic.match()) {
            alertas.put(fo.Camara, fo.Fecha);
            //  System.out.print("moviento "+fo.Camara +" a las "+ fo.Fecha);
            ImageCompare.saveJPG(ic.getChangeIndicator(), fo.Camara + "/" + fo.Fecha + "changes.jpg");
            System.out.println(alertas.toString());
        }
            Camaras.put(fo.Camara, fo.Fecha);

    }
}
