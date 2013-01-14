package itu.assignments.fluidphotoclient;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class SingleViewActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.view);
	    
	    ImageView iv = (ImageView) findViewById(R.id.imageView1);
	    iv.setImageDrawable(null);
	    
	    
	
	    // TODO Auto-generated method stub
	}

}
