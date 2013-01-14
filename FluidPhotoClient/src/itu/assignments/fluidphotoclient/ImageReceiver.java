package itu.assignments.fluidphotoclient;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;

import android.util.Log;

public class ImageReceiver extends Observable implements Runnable {
	
	private String responseImage;
	
	public String getResponseImage() {
		return responseImage;
	}
	

	@Override
	public void run() {
		
		try {
			ServerSocket ss = new ServerSocket(8889);
			Log.d("MESSAGE:", String.valueOf(ss.getLocalPort()));

			while(true)
			{
				Socket s = ss.accept();
				Log.d("MESSAGE:", String.valueOf(ss.getLocalPort()));
 
	            DataInputStream dataInputStream = new DataInputStream(s.getInputStream());
	            DataOutputStream dataOutputStream = new DataOutputStream(s.getOutputStream());
	
	            int imageSize = dataInputStream.readInt();
	             byte[] buffer = new byte[imageSize];
	             dataInputStream.readFully(buffer, 0, imageSize);
	                
	             Log.d("MESSAGE:", String.valueOf(buffer.length));
	                /*
	                try {
	                	final BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(buffer)); 
	                	
	          
	                } catch (IOException e) {
	                	e.printStackTrace();
	                }
	                
	                */
	                
	                
	                if(buffer.length > 0)
	                {
		                this.responseImage = String.valueOf(buffer.length);
		            	setChanged();
		            	notifyObservers();
	                }
			}
		} catch (UnknownHostException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        Log.d("MESSAGE:", "Unknown exception");
	        
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    	Log.d("MESSAGE:", "IO exception");
	        e.printStackTrace();
	    }
			Log.d("MESSAGE:", "koniec");
		}

	}


