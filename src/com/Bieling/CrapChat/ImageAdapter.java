package com.Bieling.CrapChat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.Bieling.CrapChat.api.Snap;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private Snap[] snaps;

    public ImageAdapter(Context c, Snap[] snaps) {
        mContext = c;
        this.snaps = snaps;
    }

    public int getCount() {
        return snaps.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(-1, 300));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            int p = 2;
            imageView.setPadding(p, p, p, p);
        } else {
            imageView = (ImageView) convertView;
        }

        File f1 = (new File(Globals.SnapsDir, "THUMB_" + snaps[position].Id + ".jpg"));
        f1.setReadable(true,false);
        File f2 = (new File(Globals.SnapsDir, snaps[position].Id + ".jpg"));
        Bitmap b = BitmapFactory.decodeFile((f1.exists() && f1.canRead() ? f1 : f2).toString());
        if(b==null && f2.exists()) {
            Logger.getLogger("CrapChat").log(Level.INFO, "Can't open " + f1.getName() + ": using alternative file.");
            try {
                b = BitmapFactory.decodeFile(f2.toString());
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            Logger.getLogger("CrapChat").log(Level.SEVERE, "Can't open " + f1.getName() + ": alternative file is also not avalible.");
        }

        imageView.setImageBitmap(b);
        //imageView.setImageURI(Uri.parse("file://" + (f1.exists() ? f1 : f2).()));
        return imageView;
    }
}