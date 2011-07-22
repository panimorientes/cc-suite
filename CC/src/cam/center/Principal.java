package cam.center;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;

public class Principal extends Activity {
    /** Called when the activity is first created. */
    String host = "10.0.2.2";
    int puertohost = 8888;
    int tiempo =10;
    boolean aceptado; 
    volatile boolean matar;
    boolean notf = false;
    Handler handler = new Handler();
    ListView Lista;
    ArrayList<String>     opciones = new ArrayList<String>(); 
    HashMap<String, String> alertas = new HashMap<String, String>();
    HashMap<String, String> camaras = new HashMap<String, String>();
    Runnable Funcionar = new Runnable() {

        public void run() {
            while (matar) {
                try {
                    int i=tiempo;
                    do{
                    Thread.sleep(1000);
                    cambiarcontador(i--);
                    }while(i>0);
                    Thread.sleep(1000);
                    cambiarcontador("Recibiendo");
                    Thread.sleep(1000);
                    conectar();
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }


        }
    };
    Thread hilo = new Thread(Funcionar);
    Runnable listado = new Runnable() {

        public void run() {

            Lista.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, opciones));
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        opciones.add(0,"Listado de camaras");
        opciones.add(1,"Notificaciones OFF");
        opciones.add(2,"Servidor OFF");
        opciones.add(3,"Tiempo: -- seg");
        opciones.add(4,"Datos de conexion");

        Lista = (ListView) findViewById(R.id.Lista);
        Lista.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, opciones));

        Lista.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
              // When clicked, show a toast with the TextView text
            	if (opciones.get(position).trim().equals("Servidor OFF")){

            		opciones.remove(position);
            		opciones.add(position,"Servidor ON");
            		handler.post(listado);
            		matar = true;
                    
                	if(hilo.isAlive()){
                        
                	}else{
                	hilo = new Thread(Funcionar);
                	hilo.start();
                	}
            	}else if (opciones.get(position).trim().equals("Servidor ON")){

            		opciones.remove(position);
            		opciones.add(position,"Servidor OFF");
            		opciones.remove(3);
            		opciones.add(3,"Tiempo: -- seg");
            		handler.post(listado);
                    matar = false;
                    if(hilo.isAlive()){
                    		hilo.interrupt();
                    	
                	}
                    camaras.clear();
                    alertas.clear();
            		

            	}
            	
            	if (opciones.get(position).trim().equals("Notificaciones OFF")){

            		opciones.remove(position);
            		opciones.add(position,"Notificaciones ON");
            		handler.post(listado);
            	    notf = true;

            	}else if(opciones.get(position).trim().equals("Notificaciones ON")){
        
            		opciones.remove(position);
            		opciones.add(position,"Notificaciones OFF");
            		handler.post(listado);
            	    notf = false;
            	
            	}
            	if (opciones.get(position).trim().equals("Datos de conexion")){
            	    Intent myIntent = new Intent(view.getContext(), Conexion.class);
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivityForResult(myIntent, 0);
            	}
            	
            	if (opciones.get(position).trim().equals("Listado de camaras")){
            	    Intent myIntent = new Intent(view.getContext(), camaras.class);
                   // myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    ArrayList<String> aux = new ArrayList<String>();
                    ArrayList<String> fechas = new ArrayList<String>();
                    for(String hh:camaras.keySet()){
                    	aux.add(hh);
                    	fechas.add(camaras.get(hh));
                    }
                    myIntent.putExtra("host", host);
                    myIntent.putExtra("puertohost", puertohost);
                    myIntent.putStringArrayListExtra("camaras", aux);
                    myIntent.putStringArrayListExtra("camaras-fechas", fechas);
                    startActivity(myIntent);
            	}
            	
            	
            }
          });
    
    }
        
    protected void cambiarcontador(int i) {
		
		opciones.remove(3);
		opciones.add(3,"Tiempo: "+i+" seg");
		handler.post(listado);
    
	}
    
    protected void cambiarcontador(String i) {
		
		opciones.remove(3);
		opciones.add(3,"Tiempo: "+i+"...");
		handler.post(listado);
    
	}

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0){
        host = data.getStringExtra("host");
        puertohost = data.getIntExtra("puerto", 8888);
        tiempo =  data.getIntExtra("tiempo", 60);
        aceptado = data.getBooleanExtra("aceptado", false);

        if (aceptado) {
            matar = true;
            opciones.remove(2);
    		opciones.add(2,"Servidor ON");
    		handler.post(listado);
    		matar = true;
            
        	if(hilo.isAlive()){
                
        	}else{
        	hilo = new Thread(Funcionar);
        	hilo.start();
        	}

        } else {
        	opciones.remove(2);
    		opciones.add(2,"Servidor OFF");
    		handler.post(listado);
            matar = false;
            if(hilo.isAlive()){
            		hilo.interrupt();
            	
        	}
            camaras.clear();
            alertas.clear();
        }
        }
    }
    
    @Override
    public void onBackPressed() {
    	  matar = false;
          if(hilo.isAlive()){
          		hilo.interrupt();
          	
      	}else{
      	 
      	}

          super.onBackPressed();
    }
    @Override
    protected void onResume() {
        super.onResume();
        

    }
    
    
    
    public void conectar() {
        try {
            Socket s = new Socket(host, puertohost);
            ObjectOutputStream salida = new ObjectOutputStream(s.getOutputStream());
            salida.writeObject(new String("MOB"));
            ObjectInputStream entrada = new ObjectInputStream(s.getInputStream());
            HashMap viejas = (HashMap) alertas.clone();
            camaras = (HashMap) entrada.readObject();
            alertas = (HashMap) entrada.readObject();
            if (notf) {
                alertar(viejas);
            }
            entrada.close();
            s.close();
        } catch (OptionalDataException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
        } catch (IOException ex) {
        }
       
    }

    public void alertar(HashMap viejas) {
        Iterator iterator = viejas.keySet().iterator();
        int cont = 1;
        while (iterator.hasNext()) {
            String camara = (String) iterator.next();
            cont++;
            if (!viejas.get(camara).equals(alertas.get(camara))) {
                displayNotification("Movimiento en la camara " + camara, cont, camara, (String) alertas.get(camara));

            }

        }

    }

    public void displayNotification(String msg, int ID, String Camara, String Fecha) {

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mg = (NotificationManager) getSystemService(ns);
        int icon = R.drawable.icon2;
        CharSequence tickerText = "CamCenter alerta!!";
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        //Uri path = Uri.parse("file:///sdcard/music/cop_siren.mp3");
        //notification.sound = path;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        Context context = getApplicationContext();
        
        CharSequence contentTitle = "CamCenter";
        CharSequence contentText = msg;
        Intent notificationIntent = new Intent(this, notificacion.class);
        notificationIntent.setData((Uri.parse("custom://"+System.currentTimeMillis())));
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("Camara", Camara);
        notificationIntent.putExtra("Fecha", Fecha);
        notificationIntent.putExtra("host", host);
        notificationIntent.putExtra("puerto", puertohost);
        notificationIntent.putExtra("id", ID);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.contentIntent = contentIntent;
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        mg.notify(ID, notification);


    }
    
}