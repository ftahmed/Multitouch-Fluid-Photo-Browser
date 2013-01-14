package itu.assignments.fluidphotobrowser;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;

import javax.imageio.ImageIO;


public class ConnectionUtil extends Observable implements Runnable {
	
	private BufferedImage responseImage;
	
	public BufferedImage getResponseImage() {
		return responseImage;
	}
	
	@Override
	public void run() {
		
		try {
	        Boolean end = false;
	        ServerSocket ss = new ServerSocket(8888);
	        System.out.println("Waiting for Image...");
	        while(!end)
	        {
	                Socket s = ss.accept();
	                
	                DataInputStream dataInputStream = new DataInputStream(s.getInputStream());
	                DataOutputStream dataOutputStream = new DataOutputStream(s.getOutputStream());
	
	                System.out.println("Received input from: " +s.getInetAddress());
	                int imageSize = dataInputStream.readInt();
	                byte[] buffer = new byte[imageSize];
	                dataInputStream.readFully(buffer, 0, imageSize);
	                System.out.println("Image size: " + buffer.length + " bytes");

	                try {
	                	final BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(buffer)); 
	                	this.responseImage = bufferedImage;
	                	setChanged();
	                	notifyObservers();
	                } catch (IOException e) {
	                	e.printStackTrace();
	                }
	                s.close();
	        }
	         ss.close();
	        } catch (UnknownHostException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	     }
	}
	
	public void sendImage(byte[] buffer)
	{
		try {
			Socket s = new Socket("192.168.1.34",8889);
					      
			DataOutputStream dataOutputStream = null;
			DataInputStream dataInputStream = null;
			       
			dataOutputStream = new DataOutputStream(s.getOutputStream());
			dataInputStream = new DataInputStream(s.getInputStream());

			dataOutputStream.writeInt(buffer.length);
			dataOutputStream.write(buffer,0, buffer.length);
			
			s.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	

}
