package com.philip.SimultaneousInterpretation;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;

import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;



public class ReceiverService extends Service{
    private final static String TAG = "ReceiverService";

    private String mReturnString;

    private static boolean sStop, sEnd;
    private String usrName;// params[0];
    private String lang;// params[1];

    public class LocalBinder extends Binder {
        public ReceiverService getServerInstance() {
            return ReceiverService.this;
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sStop = false;
        sEnd = false;
        usrName = "abc";
        lang = "fr";

        mReturnString = "";

        final Runnable runnable = new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                        if (!sStop) {
                            new DoBackgroundTask().execute(usrName, lang);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error occurs in loop.");
                        e.printStackTrace();
                    }
                    if (sEnd) {
                        break;
                    }
                }
            }
        };

        performOnBackgroundThread(runnable);

        return START_STICKY;
    }

    public static void pauseReceive () {
        sStop = true;
    }

    public static void startReceive () {
        sStop = false;
    }

    public static void endReceive () {
        sEnd = true;
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                    sStop = true;
                    sEnd = true;
                }
            }
        };
        t.start();
        return t;
    }

    private class DoBackgroundTask extends AsyncTask<String, String, String> {
        private final static String TAG = "DoBackgroundTask";

        @Override
        protected String doInBackground(String... params) {
            ConversationManager sender = ConversationManager.getInstance();
            String text = sender.getByUrl(lang, usrName);

            return text;

        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) return;

            Log.d(TAG, "Translate Res:" + result);
            mReturnString = result;

            if (! TextUtils.isEmpty(mReturnString)) {
                Intent i = new Intent("RECEIVED_MESSAGE");
                i.putExtra("receivedMsg", mReturnString);
                mReturnString = "";
                sendBroadcast(i);
            }

            super.onPostExecute(result);
        }
    }
}
