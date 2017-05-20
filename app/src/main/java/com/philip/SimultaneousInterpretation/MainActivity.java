package com.philip.SimultaneousInterpretation;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends ActionBarActivity {

    protected static final int RESULT_SPEECH = 1;

    private TextView mTxtText;
    private TextView mTxtTransText;

    ReceiveMessages mMsgReceiver;

    TextToSpeech mSpeaker;


    public class ReceiveMessages extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Send out result.
            String receivedMsg = intent.getStringExtra("receivedMsg");
            mTxtTransText.setText(receivedMsg);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Config the showing up text
        mTxtText = (TextView) findViewById(R.id.txtText);
        mTxtTransText = (TextView) findViewById(R.id.txtTransText);
        ImageButton btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        // Receiver
        mMsgReceiver = new ReceiveMessages();
        IntentFilter filter = new IntentFilter();
        filter.addAction("RECEIVED_MESSAGE");
        registerReceiver(mMsgReceiver, filter);

        mSpeaker = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mSpeaker.setLanguage(Locale.FRENCH);
                }
            }
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Input speak
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en");
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    mTxtText.setText("");
                    mTxtTransText.setText("");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });

        mTxtTransText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                // Speak out loud the translated word
                if(s.length() != 0) {
                    mSpeaker.speak(s.toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent receiveServiceIntent = new Intent(this, ReceiverService.class);
        startService(receiveServiceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ReceiverService.startReceive();
    }



    @Override
    protected void onDestroy() {
        ReceiverService.endReceive();
        if (mMsgReceiver != null) {
            unregisterReceiver(mMsgReceiver);
            mMsgReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        ReceiverService.pauseReceive();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {
                    // receive result and show it
                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d("MainActivity: ", "onActivityResult: " + text.get(0));
                    mTxtText.setText(text.get(0));
                    sendMsg(text.get(0));
                }
                break;
            }
        }
    }

    private void sendMsg(String inStr) {
        // Start a separate thread to send message
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                ConversationManager sender = ConversationManager.getInstance();
                // Default english
                String text = sender.postByUrl(params[0], "en");
                return text;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result == null) return;

                Log.d("Translate Res:", result);
                mTxtText.setText(result);
            }
        }.execute(inStr);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO: add configurable language and user id
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}


