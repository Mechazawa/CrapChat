package com.Bieling.CrapChat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Globals {
    public static String SnapsDir;
    public static Bitmap PlayButton;
    public final static String Prefs_name = "CrapChat";
    public final static String[] Changelog = new String[]{
            //1
            //////////////////////////
            "First alpha release.\nPlay store release version\n" +
            "Planned features:\n" +
                "# Uploading snaps without limits\n" +
                "# Better user interface\n" +
                "# Faster downloading\n" +
                "# ??Suggestions??\n",
            //2
            //////////////////////////
            "Changelog:\n# Fixed the back button not closing the app\n# Rotating the screen to portrait adds an extra column",

            //3
            /////////////////////////
            "",

            //4
            /////////////////////////
            "Changelog:\n" +
                    "# Changed the UI a bit\n" +
                    "# App no longer automatically downloads new snaps (added a button)\n" +
                    "# Speed improvements\n" +
                    "# Videos are now marked by a play symbol (not final symbol)\n" +
                    "**NOTE**\n" +
                    "You need to clear the cache of the app (or re-install) if you want to utilise the speed improvements",

            /////////////////////////
            "Changelog:\n" +
                    "# Changed the UI a bit\n" +
                    "# App no longer automatically downloads new snaps (added a button)\n" +
                    "# Speed improvements\n" +
                    "# Videos are now marked by a play symbol (not final symbol)\n" +
                    "# Added Android 3.0+ support\n" +
                    "**NOTE**\n" +
                    "You need to clear the cache of the app (or re-install) if you want to utilise the speed improvements" ,

            ////////////////////////
            "Changelog\n" +
                    "# Fixed a bug where the thumbnails didn't show up on some devices\n" +
                    "# Fixed images looking 'stretched' in the thumbnails"
    };

    public Globals(Context context) {
        SnapsDir = context.getCacheDir().toString();
        PlayButton = BitmapFactory.decodeResource(context.getResources(),  R.drawable.video_play_btn);
    }
}
