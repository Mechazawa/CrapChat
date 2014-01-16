package com.Bieling.CrapChat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import com.Bieling.CrapChat.api.Snaphax;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SendActivity extends Activity {
    private Object sendable;
    private enum fileType {
        IMAGE, VIDEO
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String loc = getIntent().getExtras().getString("imgloc");
        File f = new File(loc);
        Bitmap bitmap = BitmapFactory.decodeFile(f.toString());
        (new SendTask()).execute(bitmap, new String[]{"GameGrep"});
    }

    public class SendTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            try {
                Snaphax.getInstance().UploadImage((Bitmap)params[0], (String[])params[1]);
            } catch (Snaphax.LoggedOutException e) {
                e.printStackTrace();
            }
            return (Void) null;
        }

        protected void onPostExecute(final Void response) {

        }

        @Override
        protected void onCancelled() {

        }
    }
}