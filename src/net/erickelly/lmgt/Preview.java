package net.erickelly.lmgt;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class Preview extends Activity {
	private static final String TAG = "Preview";
	
	String search;
	
	TextView steps;
	ImageView cursor;
	EditText searchbar;
	Button searchbutton;
	
	Context mContext;
	NfcAdapter mNfcAdapter;
	PendingIntent mNfcPendingIntent;
	IntentFilter[] mWriteTagFilters;
    IntentFilter[] mNdefExchangeFilters;
	Handler handle;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview);
		
		// Get search text
		search = getIntent().getStringExtra("query");
		Log.d(TAG, "Search: " + search);
		
		// Get references
		cursor = (ImageView) findViewById(R.id.cursor);
		steps = (TextView) findViewById(R.id.instructions);
		searchbar = (EditText) findViewById(R.id.search);
		searchbutton = (Button) findViewById(R.id.search_button);
		handle = new Handler();
		mContext = getApplicationContext();
		
		// Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Handle all our received NFC intents in this activiy
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        
        // Intent filters for reading a note from a tag or exchanging over p2p.
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("text/plain");
        } catch (MalformedMimeTypeException e) { }
        mNdefExchangeFilters = new IntentFilter[] { ndefDetected };
		
		// Start animation
		final Animation a = AnimationUtils.loadAnimation(this, R.anim.move_cursor);
        a.reset();
        a.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				stepTwo();
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {}

			@Override
			public void onAnimationStart(Animation arg0) {
				stepOne();
			}
        	
        });
        cursor.startAnimation(a);
	}
	
	private class TypingThread extends Thread {
		
		String complete;
		
		TypingThread(String completedSearch) {
			complete = completedSearch;
		}
		
		@Override
		public void run() {
			int length = complete.length();
			for(int i = 1; i <= length; i++) {
				final int j = i;
				handle.post(new Runnable() {
					public void run() {
						searchbar.setText(complete.subSequence(0, j));
					}
				});
				try {
					int sleeptime = 100 + (int)(Math.random() * ((300 - 100) + 1));
					Thread.sleep(sleeptime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
			handle.post(new Runnable() {
				@Override
				public void run() {
					stepThree();
				}
			});
		}
	}

	public void stepOne() {
		steps.setText(R.string.step1);
	}
	
	public void stepTwo() {
		steps.setText(R.string.step2);
		new TypingThread(search).start();
	}
	
	public void stepThree() {
		// Start animation
		final Animation a = AnimationUtils.loadAnimation(this, R.anim.cursor_search);
        a.reset();
        a.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				stepFour();
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {}

			@Override
			public void onAnimationStart(Animation arg0) {
				steps.setText(R.string.step3);
			}
        	
        });
        cursor.startAnimation(a);
	}
	
	public void stepFour() {
		steps.setText(R.string.step4);
		// Wait
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				Intent i = new Intent(Intent.ACTION_VIEW);
				String url = Urls.generateGoogleSearch(search).toString();
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		}).start();
	}
	
	/**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
    	NdefMessage[] messages = getNdefMessages(getIntent());
        byte[] payload = messages[0].getRecords()[0].getPayload();
        // record 0 contains the MIME type, record 1 is the AAR, if present
        Intent i=new Intent(mContext, Preview.class);
		i.putExtra("query", payload);
		setIntent(i);
		Log.d(TAG, "processing intent");
    }
	
    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }
    
	@Override
	public void onResume() {
		super.onResume();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
	}
    
    NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {
                    record
                });
                msgs = new NdefMessage[] {
                    msg
                };
            }
        } else {
            Log.d(TAG, "Unknown intent.");
            finish();
        }
        return msgs;
    }
}
