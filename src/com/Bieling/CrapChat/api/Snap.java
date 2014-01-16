package com.Bieling.CrapChat.api;

import com.Bieling.CrapChat.Globals;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

public class Snap {

    public static enum MediaType {
        IMAGE
       ,VIDEO
       ,FRIENDREQUEST
       ,UNKNOWN
    }

    public static enum MediaStatus {
        READ,
        UNREAD
    }

    public String Id;
    public MediaType Type;
    public MediaStatus Status;
    public Date TimeSent;
    public String SenderName;
    public boolean Downloaded;
    private Snaphax hax;

    public Snap(JSONObject snapData, Snaphax snaphax) {
        hax = snaphax;

        Id = (String)snapData.get("id");
        long mType = (Long)snapData.get("m");
        Type = mType == 0 ? MediaType.IMAGE : mType == 1 ? MediaType.VIDEO :
                            mType == 3 ? MediaType.FRIENDREQUEST : MediaType.UNKNOWN;
        Status = (Long)snapData.get("st") == 1 ? MediaStatus.UNREAD : MediaStatus.READ;
        TimeSent = new Date(new Timestamp((Long)snapData.get("ts")).getTime());
        SenderName = (String)snapData.get("sn");
        Downloaded = (new File(Globals.SnapsDir, getFilename())).exists();
    }

    @Override
    public String toString() {
        return "[" + Type + "-" + Status + "] " + SenderName + " @ " + TimeSent;
    }

    public String getFilename() {
        return Id + (Type == MediaType.IMAGE ? ".jpg" : ".mp4");
    }

    public boolean Download() throws Snaphax.LoggedOutException {
        if(Downloaded || Type == MediaType.FRIENDREQUEST || Type == MediaType.UNKNOWN)
            return Downloaded;

        byte[] data = hax.Fetch(Id);
        if(data != null) {
            try{
                File file = new File(Globals.SnapsDir, getFilename());
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.close();
                file.setReadable(true, false);
                file.setWritable(true, false);

                Downloaded = true;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return Downloaded;
    }
}
