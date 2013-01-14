package itu.assignments.fluidphotobrowser;


import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.mt4j.AbstractMTApplication;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTTextField;
import org.mt4j.input.IMTInputEventListener;
import org.mt4j.input.gestureAction.DefaultDragAction;
import org.mt4j.input.gestureAction.InertiaDragAction;
import org.mt4j.input.inputData.AbstractCursorInputEvt;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.flickProcessor.FlickEvent;
import org.mt4j.input.inputProcessors.componentProcessors.flickProcessor.FlickProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeEvent;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.Direction;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.UnistrokeGesture;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.font.FontManager;
import org.mt4j.util.font.IFont;
import org.mt4j.util.math.Vector3D;



import processing.core.PConstants;
import processing.core.PImage;


public class BrowserScene extends AbstractScene {
	
	private ConnectionUtil conn;
	private MTRectangle loadedRectangle;
	private NFCUtil nfc;
	private int currentBalance = 0;
	private ArrayList<Vector3D> points;
	private boolean isGestureDefined = false;

	public BrowserScene(final AbstractMTApplication mtApplication, String name) {
		super(mtApplication, name);
		
		conn = new ConnectionUtil();
		nfc = new NFCUtil();

		//******Athenticate with NFC before start listening for images
		//Create "authenticate textblock"
		final IFont font = FontManager.getInstance().createFont(mtApplication, "arial.ttf", 20);
        final MTTextField textField = new MTTextField(mtApplication,mtApplication.width/2f, mtApplication.height/2f ,600,40,font);
        textField.unregisterAllInputProcessors();
        textField.removeAllGestureEventListeners();
        textField.setText("Use NFC device/card to authenticate...");
        this.getCanvas().addChild(textField);
        //Create "loggedin" block
        final MTTextField textFieldUser = new MTTextField(mtApplication,mtApplication.width-300, mtApplication.height-40 ,300,40,font);
        textFieldUser.unregisterAllInputProcessors();
        textFieldUser.removeAllGestureEventListeners();
        textFieldUser.setName("userTextField");
        //Create gesture definition area
        final MTRectangle rect = new MTRectangle(mtApplication, 0, 0, 300, 300);
        rect.unregisterAllInputProcessors(); 
        rect.removeAllGestureEventListeners();
        points = new ArrayList<Vector3D>();
        final UnistrokeProcessor up = new UnistrokeProcessor(getMTApplication());
        rect.registerInputProcessor(up);
        rect.addInputListener (new IMTInputEventListener() {

			@Override
			public boolean processInputEvent(MTInputEvent inEvt) {
				AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inEvt;
				Vector3D pos = new Vector3D(posEvt.getPosition().getX(),posEvt.getPosition().getY());
				points.add(pos);
				System.out.println(pos.y);
				return false;
			}
		
		});
        rect.addGestureListener(UnistrokeProcessor.class, new IGestureEventListener() { 
	       	 public boolean processGestureEvent(MTGestureEvent ge) {
	       		 UnistrokeEvent ue = (UnistrokeEvent)ge; //Cast MTGestureEvent to UnistrokeEvent
	       		 
	       		if(ue.getId()==UnistrokeEvent.GESTURE_STARTED)
	       		{
	   			     getCanvas().addChild(ue.getVisualization()); 
	   			  System.out.println("tap");
	       		}
	       		if(ue.getId() == UnistrokeEvent.GESTURE_ENDED)
	       		{
	       			UnistrokeGesture g = ue.getGesture();
	       			up.getUnistrokeUtils().getRecognizer().addTemplate(UnistrokeGesture.CUSTOMGESTURE, points,Direction.CLOCKWISE);
	       			getCanvas().removeChild(rect);
	       			
	       		}
	       		return false;
	       	 }
	       	 });
        
        //Create define gesture button
        final MTTextField textFieldButton = new MTTextField(mtApplication,mtApplication.width-180, mtApplication.height-80 ,180,40,font);
        textFieldButton.unregisterAllInputProcessors();
        textFieldButton.removeAllGestureEventListeners();
        textFieldButton.setText("Define gesture");
        textFieldButton.setName("TextButton");
        textFieldButton.setFillColor(new MTColor(123,234,92));
        textFieldButton.registerInputProcessor(new TapProcessor(mtApplication));
        textFieldButton.addGestureListener(TapProcessor.class, new IGestureEventListener() { 
            @Override 
            public boolean processGestureEvent(MTGestureEvent ge) {
            	TapEvent te = (TapEvent)ge; 
                if(te.getId() == TapEvent.GESTURE_ENDED){ 
                	getCanvas().addChild(rect);
                } 
                return false;
            } 
        });
        
        
        //Start nfc authentication thread
        Thread nfcListener = new Thread(nfc);
        nfcListener.start();
        
        //Listen for changes in nfcUtil object
        class AuthenticationObserver implements Observer {
        	@Override
        	public void update(Observable arg0, Object arg1) {
        		if(nfc.isUserAuthenticated()) {
        			startListening();
        			textFieldUser.setText("User: " + nfc.getActiveUser());
        			mtApplication.getScene("BrowserScene").getCanvas().addChild(textFieldButton);
        			mtApplication.getScene("BrowserScene").getCanvas().addChild(textFieldUser);
        		} else if(!nfc.isUserAuthenticated()) {
        			textField.setText("Unrecognized user! Please try again...");
        			System.out.println("Authentication failed!");   
        		}
        	}
        }
        AuthenticationObserver ion = new AuthenticationObserver();
        nfc.addObserver(ion);
        
        //****Listen for changes in ConnectionUtil object**************
        class ImageObserver implements Observer {
        	@Override
        	public void update(Observable arg0, Object arg1) {
        		showReceivedImage();
        	}
        }
        
        ImageObserver ioi = new ImageObserver();
        conn.addObserver(ioi);
        
        //************* REGISTER UNISTROKE GESTURE & CHECKOUT ********************
        
        //CHECKOUT
        final MTTextField textFieldCheckout = new MTTextField(mtApplication,1f, mtApplication.height/2f ,700,40, font);
		textFieldCheckout.unregisterAllInputProcessors();
		textFieldCheckout.removeAllGestureEventListeners();
		textFieldCheckout.setName("Place your card on the terminal to pay " + this.currentBalance + " PITcoins.");
        
		getCanvas().registerInputProcessor(up);
		getCanvas().addGestureListener(UnistrokeProcessor.class, new IGestureEventListener() { 
	       	 public boolean processGestureEvent(MTGestureEvent ge) {
	       		 UnistrokeEvent ue = (UnistrokeEvent)ge; //Cast MTGestureEvent to UnistrokeEvent
	       		 
	       		if(ue.getId()==UnistrokeEvent.GESTURE_STARTED)
	       		{
	   			     getCanvas().addChild(ue.getVisualization());
	       		}
	       		 if(ue.getId() == UnistrokeEvent.GESTURE_ENDED)
	       		 {
	       			 UnistrokeGesture g = ue.getGesture();
	       			 if(g == UnistrokeGesture.CUSTOMGESTURE)
	       			 {
	       				 getCanvas().removeAllChildren();
	       				 try {
	       					getCanvas().addChild(textFieldCheckout);
	       					Thread.sleep(500L);
	       					textFieldCheckout.setText("You have been charged " + currentBalance + " PITcoins. You have " + nfc.decrementBalance(currentBalance) +  " PITcoins left on your card.");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	       			 }
	       		 }
	       		 return false;
	       	 }
	       	 });		
        
	}
	
	public void showReceivedImage()
	{
		this.currentBalance = currentBalance+1;
		MTTextField textfield = (MTTextField) mtApplication.getScene("BrowserScene").getCanvas().getChildByName("userTextField");
		textfield.setText("User: " + nfc.getActiveUser() + " Balance: " + currentBalance);
		//Go from buffered image to PImage
		BufferedImage bimg = conn.getResponseImage();
		PImage img = new PImage(bimg.getWidth(),bimg.getHeight(),PConstants.ARGB);
		bimg.getRGB(0, 0, img.width, img.height, img.pixels, 0, img.width);
		img.updatePixels();
		//Set up rectangle with picture
		loadedRectangle = new MTRectangle(mtApplication, img);
		loadedRectangle.unregisterAllInputProcessors();
		loadedRectangle.removeAllGestureEventListeners();
		loadedRectangle.registerInputProcessor(new DragProcessor(mtApplication)); //Drag
		loadedRectangle.addGestureListener(DragProcessor.class, new InertiaDragAction(200, .95f, 17));
		loadedRectangle.addGestureListener(DragProcessor.class, new DefaultDragAction());
		loadedRectangle.registerInputProcessor(new FlickProcessor());
		loadedRectangle.addGestureListener(FlickProcessor.class, new IGestureEventListener() { 
		 @Override
		 public boolean processGestureEvent(MTGestureEvent ge) {
		 FlickEvent fe = (FlickEvent)ge;
		 			 if(fe.getId() == FlickEvent.GESTURE_ENDED){
				 switch (fe.getDirection()) {
				 	case EAST:
				 		System.out.println("east");
				 		conn.sendImage(new byte[]{0,12,33,1,23,21,12,34,56,78,32,21,34,56});
				 		break;
				 	case WEST:
				 		System.out.println("west");
				 		break;
				 }     
			 }
			 return false;
			 }
		});

		this.getCanvas().addChild(loadedRectangle);
	}
	
	public void startListening() {
		this.getCanvas().removeAllChildren();
		Thread pictureListener = new Thread(conn); //run picturelistener in another thread
        pictureListener.start();
	}
	
}
