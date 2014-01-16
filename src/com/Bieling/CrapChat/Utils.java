package com.Bieling.CrapChat;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class Utils {
    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        //Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmp1);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmp1;
    }
}
