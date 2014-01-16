package com.Bieling.CrapChat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.*;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import com.Bieling.CrapChat.api.Snap;
import com.Bieling.CrapChat.api.Snaphax;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ListingActivity extends Activity {
    private Snaphax hax;
    private Snap[] filteredSnaps;
    private EasyTracker easyTracker;
    private int downloaderInstanceCount = 0;
    private static boolean inited;
    private static int RESULT_LOAD_IMAGE = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        easyTracker = EasyTracker.getInstance(this);

        int buildNr = -1;
        String versionName = "?.?";
        try {
            PackageInfo info = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            buildNr = info.versionCode;
            versionName = info.versionName;
        } catch (Exception ignore) {ignore.printStackTrace();}

        setContentView(R.layout.main);

        if(!Snaphax.HasInstance()) {
            finish();
            return;
        }

        hax = Snaphax.getInstance();

        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int orientation = display.getOrientation();

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setNumColumns(orientation == 0 ? 3 : 4);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                File f = new File(Globals.SnapsDir, filteredSnaps[position].getFilename());
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + f.toString()), filteredSnaps[position].Type == Snap.MediaType.IMAGE ? "image/*" : "video/*");
                startActivity(intent);
            }
        });

        SharedPreferences settings = getSharedPreferences(Globals.Prefs_name, 0);
        if(settings.getInt("LastVersion", 0) < buildNr) {
            easyTracker.send(MapBuilder
                    .createEvent("ui_action",     // Event category (required)
                            "update_notify",  // Event action (required)
                            versionName,   // Event label
                            null)            // Event value
                    .build()
            );
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Version v" + versionName)
                    .setMessage(Globals.Changelog[buildNr - 1])
                    .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            settings.edit().putInt("LastVersion", buildNr).commit();
            dialog.show();
        }

        reloadList();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] d = { MediaStore.Images.Media.DATA };
            CursorLoader loader = new CursorLoader(getApplicationContext(), selectedImage, d, null, null, null);
            Cursor cursor = loader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            Intent intent = new Intent(ListingActivity.this, SendActivity.class);
            intent.putExtra("imgloc", cursor.getString(column_index));
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        inited = false;
        moveTaskToBack(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.snaplisting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                showProgress(true);
                (new SynchroniseTask()).execute(this);
                break;
            case R.id.menu_logout:
                inited = false;
                finish();
                break;
            /*case R.id.menu_settings:
                Intent intent = new Intent(ListingActivity.this, SettingsActivity.class);
                startActivity(intent);
                break; */
            case R.id.menu_send:
                Intent i = new Intent( Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void reloadList() {
        int sc = 0;
        for (Snap s : hax.Snaps)
            if(s.Downloaded)
                sc++;

        filteredSnaps = new Snap[sc];
        int i=0;
        for (Snap s : hax.Snaps)
            if(s.Downloaded) {
                filteredSnaps[i] = s;
                i++;
            }

        ((GridView) findViewById(R.id.gridview)).setAdapter(new ImageAdapter(this, filteredSnaps));
        if(downloaderInstanceCount == 0)
            showProgress(false);
    }

    private void showProgress(boolean p){
        setProgressBarIndeterminateVisibility(p);
    }

    public class SnapDownloadTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {

            try {
                hax.Snaps[(Integer)params[0]].Download();
            } catch (Snaphax.LoggedOutException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.logged_out), Toast.LENGTH_SHORT);
                inited = false;
                finish();
            }


            String mediaLocation = (new File(Globals.SnapsDir, hax.Snaps[(Integer)params[0]].getFilename())).toString();

            if(hax.Snaps[(Integer)params[0]].Downloaded)
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{mediaLocation}, null, null);

            // Make a thumbnail if it's a video
            if(hax.Snaps[(Integer)params[0]].Type == Snap.MediaType.VIDEO) {
                try {
                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(mediaLocation, MediaStore.Video.Thumbnails.MICRO_KIND);
                    thumb = Utils.overlay(thumb, Globals.PlayButton);
                    File file = new File(Globals.SnapsDir, "THUMB_"+hax.Snaps[(Integer)params[0]].Id + ".jpg");
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    thumb.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                    file.setReadable(true, false);
                    file.setWritable(true, false);
                } catch (Exception ignore) {}
            } else if(hax.Snaps[(Integer)params[0]].Type == Snap.MediaType.IMAGE) {
                try {
                    Bitmap o = BitmapFactory.decodeFile(mediaLocation);
                    int[] dimens = calculateDimens(o.getWidth(), o.getHeight(), 130);
                    Bitmap thumb = Bitmap.createScaledBitmap(o, dimens[0], dimens[1], false);
                    File file = new File(Globals.SnapsDir, "THUMB_"+hax.Snaps[(Integer)params[0]].Id + ".jpg");
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    thumb.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                    file.setReadable(true, false);
                    file.setWritable(true, false);
                    Logger.getLogger("CrapChat").log(Level.INFO, "Created " + file.toString());
                } catch (Exception ignore) {
                    ignore.printStackTrace();}
            }

            if(hax.Snaps[(Integer)params[0]].Downloaded)
                easyTracker.send(MapBuilder
                        .createEvent("server",    // Event category (required)
                                     "download",  // Event action (required)
                                     hax.Snaps[(Integer)params[0]].Type == Snap.MediaType.IMAGE ? "image" : "video",   // Event label
                                     null)        // Event value
                        .build()
                );

            return hax.Snaps[(Integer)params[0]].Downloaded;
        }

        private int[] calculateDimens(int width, int height, int max) {
            if (height == width)
                return new int[]{max,max};
            float numberwang = (float)max/(height > width ? (float)width : (float)height); // That's numberwang!
            return new int[]{Math.round(height*numberwang), Math.round(width*numberwang)};
        }

        protected void onPostExecute(final Boolean response) {
            downloaderInstanceCount--;

            //if (response)
                reloadList();
        }

        @Override
        protected void onCancelled() {
            downloaderInstanceCount--;
        }
    }

    public class SynchroniseTask extends AsyncTask<Context, Void, Void> {
        @Override
        protected Void doInBackground(Context... params) {
            try {
                hax.Sync();
            } catch (Snaphax.LoggedOutException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.logged_out), Toast.LENGTH_SHORT);
                inited = false;
                finish();
            }
            if(!inited) {
                inited = true;
                for(int i = 0; i < hax.Snaps.length; i++)
                    if((hax.Snaps[i].Type == Snap.MediaType.IMAGE || hax.Snaps[i].Type == Snap.MediaType.VIDEO)
                            && hax.Snaps[i].Status == Snap.MediaStatus.UNREAD && !hax.Snaps[i].Downloaded) {
                        downloaderInstanceCount++;
                        (new SnapDownloadTask()).execute(i, params[0]);
                        showProgress(true);
                    }
            }
            return (Void) null;
        }

        protected void onPostExecute(final Void response) {
            reloadList();
        }

        @Override
        protected void onCancelled() {

        }
    }
}