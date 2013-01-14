package itu.assignments.fluidphotobrowser;

import org.mt4j.MTApplication;

public class FluidPhotoStart extends MTApplication {

	@Override
	public void startUp() {
		
		addScene(new BrowserScene(this,"BrowserScene"));

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initialize();

	}

}
