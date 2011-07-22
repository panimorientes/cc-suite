/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cam.center;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author daniel
 */
public class notificacion extends Activity {

    EditText inputUrl;
    String camara;
    String fecha;
    String host;
    int puerto;
    int id;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
              setContentView(R.layout.view3);
        //  inputUrl.setSingleLine();
        //   inputUrl.setTextSize(11);
        //parametros de entrada
        Intent intent = getIntent();
        camara = intent.getStringExtra("Camara");
        fecha = intent.getStringExtra("Fecha");
        host = intent.getStringExtra("host");
        puerto = intent.getIntExtra("puerto",8888);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        id = intent.getIntExtra("id", 0);
        mNotificationManager.cancel(id);


        Context context = getApplicationContext();
        //  Editable ed = inputUrl.getText();
        Drawable image = ImageOperations(context, "http://" + host + ":"+ puerto +"/" + camara + "/" + fecha + "changes.jpg", "image.jpg");
        ImageView imgView = new ImageView(context);
        imgView = (ImageView) findViewById(R.id.changes);
       // imgView.invalidate();
        imgView.setImageDrawable(image);


        context = getApplicationContext();
        //   ed = inputUrl.getText();
        image = ImageOperations(context, "http://" + host + ":"+puerto+"/" + camara + "/" + fecha + ".jpg", "image.jpg");
        imgView = new ImageView(context);
        imgView = (ImageView) findViewById(R.id.image1);
       // imgView.invalidate();
        imgView.setImageDrawable(image);
  
    }
    
    

    private Drawable ImageOperations(Context ctx, String url, String saveFilename) {
        try {
            InputStream is = (InputStream) this.fetch(url);
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        } catch (MalformedURLException e) {
          //  e.printStackTrace();
            return null;
        } catch (IOException e) {
         //   e.printStackTrace();
            return null;
        }
    }

    public Object fetch(String address) throws MalformedURLException, IOException {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }
}
