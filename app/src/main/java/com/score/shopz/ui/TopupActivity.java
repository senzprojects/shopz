package com.score.shopz.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.score.shopz.R;

/**
 * Created by chathura on 5/13/16.
 */
public class TopupActivity extends Activity {

    private static final String TAG = TopupActivity.class.getName();

    // custom type face
    private Typeface typeface;

    // deals with NFC
    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] nfcIntentFilters;
    private String[][] nfcTechLists;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topup_layout);

        initNfc();
        initActionBar();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        // enable foreground dispatch
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, nfcIntentFilters, nfcTechLists);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        super.onPause();

        // disable foreground dispatch
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewIntent(Intent intent) {
        String action = intent.getAction();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        String s = action + "\n\n" + tag.toString();
        Log.d(TAG, "tag... " + s);

        // parse through all NDEF messages and their records and pick text type only
        Parcelable[] data = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (data != null) {
            NdefMessage message = (NdefMessage) data[0];
            String amount = new String(message.getRecords()[0].getPayload());
            Log.d(TAG, "----------------- " + amount);

            // launch pay activity
            Intent mapIntent = new Intent(this, PayActivity.class);
            mapIntent.putExtra("EXTRA", amount);
            startActivity(mapIntent);
            overridePendingTransition(R.anim.bottom_in, R.anim.stay_in);

        }
    }

    /**
     * Initialize action bar
     */
    private void initActionBar() {
        typeface = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        // Set up action bar.
        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Top Up");
        getActionBar().setBackgroundDrawable(new ColorDrawable(0xff384e77));

        // set custom font for
        //  1. action bar title
        //  2. other ui texts
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitle = (TextView) (findViewById(titleId));
        actionBarTitle.setTextColor(getResources().getColor(R.color.white));
        actionBarTitle.setTypeface(typeface);
    }

    /**
     * Initialize NFC components
     */
    private void initNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "[ERROR] No NFC supported", Toast.LENGTH_LONG).show();
        } else {
            // create an intent with tag data and deliver to this activity
            nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

            // set an intent filter for all MIME data
            IntentFilter nfcDiscoveredIntentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            try {
                nfcDiscoveredIntentFilter.addDataType("text/plain");
                nfcIntentFilters = new IntentFilter[]{nfcDiscoveredIntentFilter};
            } catch (Exception e) {
                Log.e("TagDispatch", e.toString());
            }

            // tech list
            nfcTechLists = new String[][]{new String[]{NfcF.class.getName()}};
        }
    }

}
