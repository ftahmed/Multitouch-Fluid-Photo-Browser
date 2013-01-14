package itu.assignments.fluidphotoclient;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.Toast;

public class FluidPhotoClientActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Gallery gallery = (Gallery) findViewById(R.id.gallery);
        gallery.setAdapter(new ImageAdapter(this));
        

        gallery.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                
            	Toast.makeText(FluidPhotoClientActivity.this, "Sending photo nr: " + position, Toast.LENGTH_SHORT).show();
            	
            	try {
					Socket s = new Socket("192.168.1.35",8888);
							      
					DataOutputStream dataOutputStream = null;
					DataInputStream dataInputStream = null;
					       
					dataOutputStream = new DataOutputStream(s.getOutputStream());
					dataInputStream = new DataInputStream(s.getInputStream());
					
					Resources res = getResources();
					Drawable draw = null;
					
					switch(position) // HARDCODED IMAGE POSITION
					{
					case 0:
						draw = res.getDrawable(R.drawable.testimage);
						break;
					case 1:
						draw = res.getDrawable(R.drawable.testimage2);
						break;
					}
					
					if(draw!=null)
					{
						Bitmap bitmap = (Bitmap)((BitmapDrawable) draw).getBitmap();
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
						
						byte[] buffer = stream.toByteArray();
	
						dataOutputStream.writeInt(buffer.length);
						dataOutputStream.write(buffer,0, buffer.length);
						
						s.close();
						dataInputStream.close();
						dataOutputStream.close();
					} else {
						Toast.makeText(FluidPhotoClientActivity.this, "Error", Toast.LENGTH_SHORT).show();
					}
            	} catch (IOException e) {
            		Toast.makeText(FluidPhotoClientActivity.this, "IOException", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
            }
        });
        
        ImageReceiver ir = new ImageReceiver();
        Thread imageListener = new Thread(ir);
        imageListener.start();
        
        class ImageObserver implements Observer {
        	@Override
        	public void update(Observable arg0, Object arg1) {
        		Log.d("MESSAGE:" , "hello");
        		showIncomingImage();
        	}
        }
        ImageObserver io = 	new ImageObserver();
        ir.addObserver(io);
        
        
    }
    public void showIncomingImage() {
    	startActivity(new Intent(this, SingleViewActivity.class));
    }
    
}










