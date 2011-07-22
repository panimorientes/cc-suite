package cam.center;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.w3c.dom.ls.LSSerializer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class camaras extends Activity  {
    String host = "10.0.2.2";
    int puertohost = 8888;
	
	ArrayList<String> camaras = new ArrayList<String>();
	ArrayList<String> camarasf = new ArrayList<String>();
	ArrayList<String> listacamaras = new ArrayList<String>();
	 ListView s;

	
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.camaras);
	        
	        Intent intent = getIntent();
	        camaras = intent.getStringArrayListExtra("camaras");
	        camarasf = intent.getStringArrayListExtra("camaras-fechas");
	        host = intent.getStringExtra("host");
	        puertohost = intent.getIntExtra("puertohost", 8888);
	        
	        listacamaras.addAll(camaras);
	        listacamaras.add("Volver");
	        ListView s = (ListView)findViewById(R.id.camaraslista);
	        s.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listacamaras));
	        
	        s.setOnItemClickListener(new OnItemClickListener() {
	            public void onItemClick(AdapterView<?> parent, View view,
	                int position, long id) {
	          
	            	if ((listacamaras.get(position)).trim().equals("Volver")){
	            		finish();
	            	}else{
	            	    Intent myIntent2 = new Intent(view.getContext(), imagencam.class);
	                    //myIntent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	                    myIntent2.putExtra("Camara", camaras.get(position));
	                    myIntent2.putExtra("Fecha", camarasf.get(position) );
	                    myIntent2.putExtra("host", host);
	                    myIntent2.putExtra("puerto", puertohost);
	                    startActivityForResult(myIntent2, 0);
	            		
	            	}
	        
	            }
	 
	 

});
	 }
	 
}