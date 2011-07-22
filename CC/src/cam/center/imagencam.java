package cam.center;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class imagencam extends Activity  {

    String camara;
    String fecha;
    String host;
    int puerto;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
              setContentView(R.layout.imagen);

        Intent intent = getIntent();
        camara = intent.getStringExtra("Camara");
        fecha = intent.getStringExtra("Fecha");
        host = intent.getStringExtra("host");
        puerto = intent.getIntExtra("puerto",8888);

        Button cn = (Button)findViewById(R.id.vl);
        Context context = getApplicationContext();
        String dir = "http://" + host + ":"+ puerto +"/" + camara + "/" + fecha + ".jpg";
        Drawable image = ImageOperations(context, dir, "image.jpg");
        ImageView imgView = new ImageView(context);
        imgView = (ImageView) findViewById(R.id.image1);

        imgView.setImageDrawable(image);
        
       cn.setOnClickListener(new OnClickListener() {

            public void onClick(View view) {
            	finish();
            }
        });



  
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
