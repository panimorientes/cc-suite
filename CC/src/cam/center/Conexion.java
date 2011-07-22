/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cam.center;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author daniel
 */
public class Conexion extends Activity {

    String host = "10.0.2.2";
    int puertohost = 8888;
    int puertoweb = 8888;
    int tiempo=10;
    boolean aceptado = false;
    String msj;
    Handler manejador = new Handler();
    Button volver;
    Button probar;
    TextView mensaje;
    EditText fhost;
    HashMap<String, String> camaras;
    HashMap<String, String> alertas;
    Runnable listado = new Runnable() {

        public void run() {
            mensaje.setText(msj);
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.conexion2);
        probar = (Button) findViewById(R.id.probar);

        volver = (Button) findViewById(R.id.volver);

        mensaje = (TextView) findViewById(R.id.infoconex);
        Spinner s = (Spinner) findViewById(R.id.spinner1);
            ArrayAdapter adapter = ArrayAdapter.createFromResource(
                    this, R.array.tiempos, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            s.setAdapter(adapter);
            s.getPrompt();
        volver.setOnClickListener(new OnClickListener() {

            public void onClick(View view) {
                Intent myintent = new Intent();
                myintent.putExtra("host", host);
                myintent.putExtra("puerto", puertohost);
                myintent.putExtra("aceptado", aceptado);
                myintent.putExtra("tiempo", tiempo);
                setResult(RESULT_OK, myintent);
                finish();

            }
        });

              s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                 public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                     String item = (String)parent.getItemAtPosition(pos);
                     tiempo = Integer.parseInt(item);
                 }
                 public void onNothingSelected(AdapterView<?> parent) {
                 	
                 }
             });
        
        probar.setOnClickListener(new OnClickListener() {

            public void onClick(View view) {

                fhost = (EditText) findViewById(R.id.host);
                if (!fhost.getText().toString().equals("")) {
                    host = fhost.getText().toString();
                }

                fhost = (EditText) findViewById(R.id.widget29);
                if (!fhost.getText().toString().equals("")) {
                    puertohost = Integer.parseInt(fhost.getText().toString());
                }
                conectar();
            }
        });


    }
    @Override
    public void onBackPressed() {
    	  Intent myintent = new Intent();
          myintent.putExtra("host", host);
          myintent.putExtra("puerto", puertohost);
          myintent.putExtra("aceptado", aceptado);
          myintent.putExtra("tiempo", tiempo);
          setResult(RESULT_OK, myintent);
          finish();
        super.onBackPressed();
  }
    
    public void conectar() {
        try {
            Socket s = new Socket(host, puertohost);
                        ObjectOutputStream salida = new ObjectOutputStream(s.getOutputStream());
            salida.writeObject(new String("MOB"));
            ObjectInputStream entrada = new ObjectInputStream(s.getInputStream());

            camaras = (HashMap) entrada.readObject();
            alertas = (HashMap) entrada.readObject();

            entrada.close();
            s.close();
            aceptado = true;
            msj = "Conexion correcta";

        } catch (OptionalDataException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            msj = "Conexion Erronea revise los datos";
            aceptado = false;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            msj = "Conexion Erronea revise los datos";
            aceptado = false;
        } catch (UnknownHostException ex) {
            msj = "Conexion Erronea revise los datos";
            aceptado = false;
        } catch (IOException ex) {
            msj = "Conexion Erronea revise los datos";
            aceptado = false;
        }
        manejador.post(listado);
    }
}
