package net.erickelly.lmgt;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class LetMeGoogleThatActivity extends Activity {
	private final static String TAG = "Main Activity";
	
	private Context mContext;
	private String mShortUrl;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Set variables
        final EditText targetEditText = (EditText) findViewById(R.id.search);
        final ImageView googleImage = (ImageView) findViewById(R.id.logo);
        mContext = getApplicationContext();
        targetEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String text = targetEditText.getText().toString();
					URL url = generateUrl(text);
					(new MyAsyncTask(mContext)).execute(new String[] { url.toString() });
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(targetEditText.getWindowToken(), 0);
		            return true;	
		        }
		        return false;
			}
        });
        
        // Animation
        final Animation a = AnimationUtils.loadAnimation(this, R.anim.fadein);
        a.reset();
        googleImage.startAnimation(a);
        
        if(mShortUrl != null) {
        	success(mShortUrl);
        }
    }
    
    private class MyAsyncTask extends AsyncTask<String, Void, String> {

    	Context mContext;
    	
    	MyAsyncTask(Context ctx) {
    		mContext = ctx;
    	}
    	
		@Override
		protected String doInBackground(String... params) {
			return UrlShortener.getShortenedUrl(params[0]);
		}
		
		protected void onPostExecute(String result) {
			Log.d(TAG, result);
			success(result);
		}
    	
    }
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.share, menu);
	    mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.share).getActionProvider();
	    //mShareActionProvider.setShareIntent(createShareIntent());
	    return super.onCreateOptionsMenu(menu);
    }
    */

    public void success(final String shorturl) {
		// Unhide the buttons
    	final Button share = (Button) findViewById(R.id.share); 
    	final Button preview = (Button) findViewById(R.id.preview);
    	final Button copy = (Button) findViewById(R.id.copy_link);
    	
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
    			Intent i = new Intent(Intent.ACTION_VIEW);
    			i.setData(Uri.parse(shorturl));
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
				share.setVisibility(0);
				preview.setVisibility(0);
				copy.setVisibility(0);
			}
    		
    	});
        a.reset();
        share.startAnimation(a);
        preview.startAnimation(a);
        copy.startAnimation(a);
	}

	private URL generateUrl(String query) {
    	URL url = null;
    	try {
			URI uri = new URI("http", 
					"letmegooglethat.com", 
					"/", "q=" + query, null);
			url = uri.toURL();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
    }
    
}