import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
   
    
    public static void main(String[] args) throws Exception {

      int puerto = 8888;
      if (args.length > 0) {
          if (args[0].equals("-p")){
                puerto = Integer.parseInt(args[1]);
          }
      }
      File file = new File("index.html");
      if(file.exists()){
          file.delete();
       }     
        file.createNewFile();
        SimpleWebServer webserver = new SimpleWebServer(puerto);
    }

}

