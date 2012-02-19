package net.erickelly.lmgt;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.ShareActionProvider;

public class QRCodeDisplayer extends Activity {
	//private final static String TAG = "QR Code";
	
	ShareActionProvider mShareActionProvider;
	File code;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr);
        
        File root = android.os.Environment.getExternalStorageDirectory();               
		File dir = new File (root.getAbsolutePath() + "/xmls");
		code = new File(dir, "qrcode.png");
				
		displayImage(code);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// Inflate your menu.
        getMenuInflater().inflate(R.menu.share, menu);
        mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.share).getActionProvider();
		mShareActionProvider.setShareIntent(createShareIntent());
    	return true;
    }
    
    private Intent createShareIntent() {
    	Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri uri = Uri.fromFile(code);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        return shareIntent;
    }

	private void displayImage(File file) {
		if(file.exists()){
		    Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
		    ImageView myImage = (ImageView) findViewById(R.id.qr_code);
		    myImage.setImageBitmap(myBitmap);
		}
	}
}