//~--- JDK imports ------------------------------------------------------------

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;



import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CamCenter extends JPanel implements Runnable {

    private String Nombre = "Camara";
    public String conexion = "localhost";
    Fotograma fotograma = null;
    HttpURLConnection huc = null;
    private BufferedImage image = null;
    private int intentos = 1000;
    public String jpgURL = "";    // "http://81.92.254.3/axis-cgi/mjpg/video.cgi"; links de prueba
    public String mjpgURL = "";    // "http://87.101.127.24/axis-cgi/mjpg/video.cgi"; links de prueba
    private boolean useMJPGStream = true;
    private boolean terminado = false;
    boolean sockete = true;
    boolean sockete2 = true;
    JLabel picLabel = new JLabel("No hay conexion");
    JPanel panel = new JPanel();
    private boolean motion = true;
    private boolean silentmode = false;
    JFrame frame = new JFrame("Display image");
    Font f = new Font("Courier", Font.PLAIN, 10);
    public boolean connected = false;
    DataInputStream dis;
    ObjectOutputStream oos;
    OutputStream os;
    Component parent;
    Socket s;
    int puerto;

    /** Creates a new instance of AxisCamera */
    public CamCenter(String b, String URL, JFrame fr, String conex, int pu, boolean sm) {

        puerto = pu;
        conexion = conex;
        mjpgURL = URL;
        jpgURL = URL;
        frame = fr;
        Nombre = b;
        silentmode = sm;
        if(!silentmode){
        panel.add(picLabel);
        frame.add(panel);
        }

    }

    public void Mostrar(JFrame frame, BufferedImage NombreArchivo) {
        Graphics g = NombreArchivo.getGraphics();

        g.setFont(f);
        g.drawString(Nombre, 5, 10);
        picLabel.setText("");
        picLabel.setIcon(new ImageIcon(NombreArchivo));
        picLabel.invalidate();
        picLabel.validate();

        if (!frame.isShowing()) {
            System.out.println("cerrado");
            terminado = true;
            frame.dispose();
        }
    }

    public void connect() {
        try {
            URL u = new URL(useMJPGStream
                    ? mjpgURL
                    : jpgURL);

            huc = (HttpURLConnection) u.openConnection();

            // System.out.println(huc.getContentType());
            if ((null == huc.getContentType())) {
                intentos++;
                System.out.println("No Hay conexion con " + mjpgURL + " .Volviendo a intentar en " + intentos / 1000 + " segs");
                Thread.sleep(1000);
                //connect();
            } else {
                System.out.println("conexion con " + mjpgURL + " Mostrando imagenes");

                InputStream is = huc.getInputStream();

                connected = true;

                BufferedInputStream bis = new BufferedInputStream(is);

                dis = new DataInputStream(bis);
                readStream();
            }
        } catch (InterruptedException ex) {
        } catch (IOException e) {    // incase no connection exists wait and try again, instead of printing the error
        }

    }

    public void disconnect() {

        if (connected) {
            try {
                dis.close();
                connected = false;

                if (!sockete) {
                    oos.close();
                    os.close();
                    s.close();
                }

                System.out.println("Cerrando conexion con" + mjpgURL);
            } catch (IOException ex) {
                System.out.println("Problemas al cerrar las conexiones");
            }
        }

    }

    @Override
    public void paint(Graphics g) {    // used to set the image on the panel
        if (image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }

    public void readStream() {    // the basic method to continuously read the stream
        try {
            if (useMJPGStream) {
                while (!terminado) {
                    readMJPGStream();

                    int type = (image.getType() == 0)
                            ? BufferedImage.TYPE_INT_ARGB
                            : image.getType();
                    if(!silentmode){
                    Mostrar(frame, resizeImage(image, type));
                    }
                }
            } else {
                while (!terminado) {
                    connect();
                    readJPG();
                    parent.repaint();
                    disconnect();
                }
            }
        } catch (Exception e) {
            System.out.println("Error leyendo el contenido");
            disconnect();
        }
    }

    public void readMJPGStream() {    // preprocess the mjpg stream to remove the mjpg encapsulation
        if (motion) {
            readLine(4, dis);    // discard the first 3 lines
            readJPG();
            readLine(1, dis);    // discard the last two lines
        } else {
            readLine(3, dis);    // discard the first 3 lines
            readJPG();
            readLine(2, dis);    // discard the last two lines
        }
    }

    public void readJPG() {      // read the embedded jpeg image
        try {
            JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(dis);

            image = decoder.decodeAsBufferedImage();

            if (sockete) {
                try {
                    s = new Socket(conexion, puerto);

                    os = s.getOutputStream();
                    oos = new ObjectOutputStream(os);
                    oos.writeObject(new String("SIG"));
                    sockete2 = true;
                    sockete = false;

                    System.out.println("Conexion Satisfactoria con el servidor en " + conexion + " por el puerto " + puerto);
                } catch (UnknownHostException ex) {
                    // System.out.println("No se puede establecer la conexion con " + conexion + " por el puerto " + puerto);
                    sockete2 = false;
                } catch (IOException ex) {
                    //   System.out.println("No se puede establecer la conexion con " + conexion + " por el puerto " + puerto);
                    sockete2 = false;
                }

            }


            if (sockete2) {
                ByteArrayOutputStream salidaImagen = new ByteArrayOutputStream();
                int type = (image.getType() == 0)
                        ? BufferedImage.TYPE_INT_ARGB
                        : image.getType();
                ImageIO.write(resizeImage(image, type), "png", salidaImagen);

                byte[] bytesImagen = salidaImagen.toByteArray();

                fotograma = new Fotograma(Nombre, bytesImagen);
                oos.writeObject(fotograma);
            } else {
                sockete = true;
            }

            // ImageIO.write((RenderedImage) image, "jpg", new File(String.format("imagen%d",d))); // sirve para crear el archivo
            // System.out.println("se guarda la foto");
        } catch (IOException ex) {

            if (sockete2 == true) {
                sockete = true;
            }


        } catch (com.sun.image.codec.jpeg.ImageFormatException e) {
            System.out.println("Formato Invalido de MJPE");
            if (motion == false) {
                motion = true;
                System.out.println("cambiando de modo a Formato Motion");
            } else {
                System.out.println("cambiando de modo a Formato no Motion");
                motion = false;
            }





        }
    }

    public void readLine(int n, DataInputStream dis) {    // used to strip out the header lines
        for (int i = 0; i < n; i++) {
            readLine(dis);
        }
    }

    public void readLine(DataInputStream dis) {
        try {
            boolean end = false;
            String lineEnd = "\n";    // asegura el final de linea
            byte[] lineEndBytes = lineEnd.getBytes();
            byte[] byteBuf = new byte[lineEndBytes.length];

            while (!end) {
                dis.read(byteBuf, 0, lineEndBytes.length);

                String t = new String(byteBuf);

                // System.out.print(t); //imprime lo q se elimina
                if (t.equals(lineEnd)) {
                    end = true;
                }
            }
        } catch (Exception e) {
            System.out.println("Error Eliminando linea");
            // disconnect();
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int type) {
        BufferedImage resizedImage = new BufferedImage(320, 240, type);
        Graphics g = resizedImage.getGraphics();

        g.drawImage(originalImage, 0, 0, 320, 240, null);
        g.dispose();

        return resizedImage;
    }

    @Override
    public void run() {


        if (silentmode) {
            do {
                try {
                    connect();
                    disconnect();
                    Thread.sleep(intentos);
                    intentos = intentos * 2;
                } catch (InterruptedException ex) {
                    // Logger.getLogger(CamCenter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } while (!terminado && intentos < 180000);
        } else {
            do {
                try {
                    connect();
                    disconnect();
                    Thread.sleep(intentos);
                    intentos = intentos * 2;
                } catch (InterruptedException ex) {
                    // Logger.getLogger(CamCenter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } while (frame.isShowing() && !terminado && intentos < 180000);
        }

    }

    public static JFrame Init(int cantidad) {
        JFrame frame = new JFrame("CamCenter v0.1");

        switch (cantidad) {
            case 1:
                frame.getContentPane().setLayout(new GridLayout(0, 1));
                frame.setSize(330 * cantidad, 280);

                break;

            case 2:
                frame.getContentPane().setLayout(new GridLayout(0, 2));
                frame.setSize(330 * cantidad, 280);

                break;

            case 3:
                frame.setSize(330 * cantidad, 280);
                frame.getContentPane().setLayout(new GridLayout(0, 3));

                break;

            case 4:
                frame.getContentPane().setLayout(new GridLayout(2, 3));
                frame.setSize(330 * 2, 560);

                break;

            case 6:
            case 5:
                frame.setSize(330 * 3, 560);
                frame.getContentPane().setLayout(new GridLayout(2, 3));

                break;

            case 7:
            case 8:
            case 9:
                frame.getContentPane().setLayout(new GridLayout(3, 3));
                frame.setSize(330 * 3, 280 * 3);

                break;
        }

        frame.setVisible(true);

        return frame;
    }

    public static HashMap lector(String path) {
        HashMap Camaras = new HashMap();

        try {
            int pos = 0;
            File file = new File(path);
            BufferedReader reader = null;
            String text = null;

            reader = new BufferedReader(new FileReader(file));

            while ((text = reader.readLine()) != null) {
                String[] words = text.split(" ");
                int i = 0;

                while (i < words.length) {
                    camara Cam = new camara(words[i++], words[i++]);

                    Camaras.put(pos++, Cam);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Error Revise la ruta del archivo");
            System.out.println("java -jar -h [ruta del archivo]");
            System.exit(0);
        } catch (IOException ex) {
        }

        return Camaras;
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        if (args.length > 0) {

            int puerto2 = 8888;
            String conexion2 = "localhost";
            String file = "";
            boolean silentmode = false;

            for (int i = 0; i < args.length; i++) {

                if (args[i].charAt(0) == '-') {
                    switch (args[i].charAt(1)) {
                        case 'p':
                            puerto2 = Integer.parseInt(args[++i]);
                            break;

                        case 'h':
                            conexion2 = args[++i];
                            break;

                        case 'f':
                            file = args[++i];
                            break;

                        case 's':
                            silentmode = true;
                            break;

                    }
                }
            }

            if (file.equals("")) {
                System.out.println("Revise la ruta del archivo");
                System.exit(0);
            }

            HashMap cams = CamCenter.lector(file);
            JFrame frame = null;
            if (!silentmode) {
                frame = Init(cams.size());
            }
            System.out.println(cams.size());

            Iterator k = cams.values().iterator();

            while (k.hasNext()) {
                camara c = (camara) k.next();
                CamCenter axPanel = new CamCenter(c.Nombre, c.URL, frame, conexion2, puerto2, silentmode);

                new Thread(axPanel).start();
            }
        } else {
            System.out.println("Revise la ruta del archivo");
        }
    }
}
