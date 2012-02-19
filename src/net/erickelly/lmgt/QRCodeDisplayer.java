package net.erickelly.lmgt;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class QRCodeDisplayer extends Activity {
	//private final static String TAG = "QR Code";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr);
        
        File root = android.os.Environment.getExternalStorageDirectory();               
		File dir = new File (root.getAbsolutePath() + "/xmls");
		File file = new File(dir, "qrcode.png");
		
		displayImage(file);
    }

	private void displayImage(File file) {
		if(file.exists()){
		    Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
		    ImageView myImage = (ImageView) findViewById(R.id.qr_code);
		    myImage.setImageBitmap(myBitmap);
		}
	}
}