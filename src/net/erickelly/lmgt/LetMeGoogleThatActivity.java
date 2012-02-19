package net.erickelly.lmgt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.http.util.ByteArrayBuffer;

public class LetMeGoogleThatActivity extends Activity implements CreateNdefMessageCallback {
	private final static String TAG = "Main Activity";
	
	private Context mContext;
	private String mShortUrl;
	private String mLongUrl;
	private String mSearch;
	
	NfcAdapter mNfcAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Set variables
        final EditText targetEditText = (EditText) findViewById(R.id.search);
        final ImageView googleImage = (ImageView) findViewById(R.id.logo);
        mContext = getApplicationContext();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        
        targetEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					mSearch = targetEditText.getText().toString();
					URL url = Urls.generateUrl(mSearch);
					mLongUrl = url.toString();
					(new ShortenerAsyncTask(LetMeGoogleThatActivity.this)).execute(new String[] { url.toString() });
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(targetEditText.getWindowToken(), 0);
		            return true;	
		        }
		        return false;
			}
        });
        
        // Register callback to set NDEF messages
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        
        // Animation
        final Animation a = AnimationUtils.loadAnimation(this, R.anim.fadein);
        a.reset();
        googleImage.startAnimation(a);
        
        if(mShortUrl != null) {
        	success(mShortUrl);
        }
    }
    
    private class ShortenerAsyncTask extends AsyncTask<String, Void, String> {
    	Context mContext;
    	ProgressDialog loading;
    	
    	ShortenerAsyncTask(Context ctx) {
    		mContext = ctx;
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		loading = new ProgressDialog(mContext);
			loading.setMessage("Fetching url");
			loading.show();
    	}
    	
		@Override
		protected String doInBackground(String... params) {
			return Urls.getShortenedUrl(params[0]);
		}
		
		protected void onPostExecute(String result) {
			Log.d(TAG, result);
			loading.dismiss();
			success(result);
		}
    }
    
    private class QRCodeAsyncTask extends AsyncTask<String, Void, Boolean> {
    	Context mContext;
    	ProgressDialog loading;
    	
    	QRCodeAsyncTask(Context ctx) {
    		mContext = ctx;
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		loading = new ProgressDialog(mContext);
			loading.setMessage("Downloading QR code");
			loading.show();
    	}
    	
		@Override
		protected Boolean doInBackground(String... params) {
			return DownloadFromUrl(params[0]);
		}
		
		protected void onPostExecute(Boolean result) {
			loading.dismiss();
			if(result) {
				Intent myIntent = new Intent(LetMeGoogleThatActivity.this, QRCodeDisplayer.class);
				LetMeGoogleThatActivity.this.startActivity(myIntent);
			} else {
				Toast toast = new Toast(mContext);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setText("Sorry, something went wrong");
				toast.show();
			}
		}
    }

    public void success(final String shorturl) {
		// Find the buttons
    	final Button share = (Button) findViewById(R.id.share); 
    	final Button preview = (Button) findViewById(R.id.preview);
    	final Button copy = (Button) findViewById(R.id.copy_link);
    	final Button qr = (Button) findViewById(R.id.qr_button);
    	final LinearLayout LL1 = (LinearLayout) findViewById(R.id.LinearLayout1);
    	final LinearLayout LL2 = (LinearLayout) findViewById(R.id.LinearLayout2);
    	
    	mShortUrl = shorturl;
    	
    	share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
		        shareIntent.putExtra(Intent.EXTRA_TEXT, shorturl);
		        shareIntent.setType("text/plain");
				startActivity(Intent.createChooser(shareIntent, "Share via"));
			}
    	});
    	preview.setOnClickListener(new OnClickListener() {
    		@Override
			public void onClick(View v) {
    			EditText search = (EditText) findViewById(R.id.search);
    			String query = search.getText().toString();
    			Intent i=new Intent(mContext, Preview.class);
    			i.putExtra("query", query);
    			startActivity(i);
			}
    	});
    	copy.setOnClickListener(new OnClickListener() {
    		@Override
			public void onClick(View v) {
    			ClipboardManager clipboard = 
    			      (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
    			 clipboard.setText(shorturl);
    			 Context context = getApplicationContext();
    			 CharSequence text = shorturl + " copied to clipboard.";
    			 int duration = Toast.LENGTH_LONG;
    			 Toast toast = Toast.makeText(context, text, duration);
    			 toast.show();
			}
    	});
    	qr.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			(new QRCodeAsyncTask(LetMeGoogleThatActivity.this)).execute(mLongUrl);
    		}
    	});
    	
    	// Unhide the buttons
    	final Animation a = AnimationUtils.loadAnimation(this, R.anim.slide_in);
    	a.setDuration(1000);
    	a.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {}
			@Override
			public void onAnimationRepeat(Animation arg0) {}
			@Override
			public void onAnimationStart(Animation arg0) {
				LL1.setVisibility(0);
				LL2.setVisibility(0);
			}
    		
    	});
        a.reset();
        LL1.startAnimation(a);
        LL2.startAnimation(a);
    }
	
	public boolean DownloadFromUrl(String EncodeUrl) {

		final String QR_API = "http://chart.apis.google.com/chart?cht=qr&chs=420x420&chl=";
		
		try {
			File root = android.os.Environment.getExternalStorageDirectory();               
			File dir = new File (root.getAbsolutePath() + "/xmls");
			if(dir.exists()==false) {
				dir.mkdirs();
			}

			URL url = new URL(QR_API + EncodeUrl);
			File file = new File(dir, "qrcode.png");

			long startTime = System.currentTimeMillis();
			Log.d("DownloadManager", "download begining");
			Log.d("DownloadManager", "download url:" + url);
			Log.d("DownloadManager", "downloading qr code");

			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(5000);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.flush();
			fos.close();
			Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
			
			return true;

		} catch (IOException e) {
			Log.d("DownloadManager", "Error: " + e);
		}
		return false;
    }

	@Override
	public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
		if(mSearch != null) {
			NdefMessage msg = new NdefMessage(
	                new NdefRecord[] { createMimeRecord(
	                        "application/net.erickelly.lmgt", mSearch.getBytes()),
	                        NdefRecord.createApplicationRecord("net.erickelly.lmgt") });
			return msg;
		}
		return null;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
	}
	
	/**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
    	Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        mSearch = new String(msg.getRecords()[0].getPayload());
    }
    
    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     */
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
    	NdefRecord mimeRecord = new NdefRecord(
    		    NdefRecord.TNF_MIME_MEDIA ,
    		    "application/net.erickelly.lmgt".getBytes(Charset.forName("US-ASCII")),
    		    new byte[0], payload);
        return mimeRecord;
    }
}