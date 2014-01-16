package com.Bieling.CrapChat.api;

import android.graphics.Bitmap;
import android.os.Bundle;
import com.Bieling.CrapChat.Globals;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Snaphax {

    public class LoggedOutException extends Exception{
        public LoggedOutException() {
            super();
            Snaphax.getInstance().Logout();
        }
    }

    public SnaphaxApi Api = new SnaphaxApi();
    public String Username;
    public String Password;
    public String Auth_token;
    public Snap[] Snaps;
    public String[] Friends;
    private static Snaphax savedInstance;

    public enum LoginResponse {
        OK,
        INVALIDCREDS,
        NETWORKERROR
    }

    public Snaphax(String Username, String Password) {
        this.Username = Username;
        this.Password = Password;
        this.Auth_token = null;
        savedInstance = this;
    }

    public static Snaphax getInstance(){
        return savedInstance;
    }

    public static boolean HasInstance(){
        return savedInstance != null;
    }

    public void Logout() {
        Auth_token = null;
    }

    public boolean LoggedIn() {
        return Auth_token != null;
    }

    public Snap[] FilterFriendRequest(Snap[] snaps){
        int friendRequests = 0;
        for(Snap s : snaps)
            if(s.Type == Snap.MediaType.FRIENDREQUEST || s.Type == Snap.MediaType.UNKNOWN)
                friendRequests++;

        Snap[] filtered = new Snap[snaps.length - friendRequests];
        int i = 0;
        for(Snap s : snaps) {
            if(s.Type != Snap.MediaType.FRIENDREQUEST && s.Type != Snap.MediaType.UNKNOWN)  {
                filtered[i] = s;
                i++;
            }
        }

        return filtered;
    }

    public LoginResponse Login() {
        Logout();

        Bundle data = new Bundle();
        data.putString("username", Username);
        data.putString("password", Password);
        data.putString("timestamp", Api.GetTimestamp() + "");
        try {
            String response = Api.postCall("/bq/login", data, "m198sOkJEn37DjqZ32lpRu76xmw288xSQ9", Api.GetTimestamp() + "");
            JSONObject jsonData = (JSONObject) JSONValue.parse(response);
            Auth_token = (String)jsonData.get("auth_token"); // Will return null if login failed

            if(Auth_token != null) {
                parseSnaps((JSONArray)jsonData.get("snaps"));
                parseFriends((JSONArray)jsonData.get("friends"));
            }
            return Auth_token != null ? LoginResponse.OK : LoginResponse.INVALIDCREDS;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return LoginResponse.NETWORKERROR;
    }

    public void Sync() throws LoggedOutException {
        if(Auth_token == null)
            throw new LoggedOutException();

        Bundle data = new Bundle();
        data.putString("username", Username);
        data.putString("timestamp", Api.GetTimestamp()+"");
        try {
            String response = Api.postCall("/bq/login", data, Auth_token, Api.GetTimestamp() + "");
            JSONObject jsonData = (JSONObject) JSONValue.parse(response);
            Auth_token = (String)jsonData.get("auth_token"); // Will return null if login failed

            if(Auth_token != null)
                parseSnaps((JSONArray)jsonData.get("snaps"));
            else
                throw new LoggedOutException();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public boolean UploadImage(Bitmap image, String[] recipients) throws LoggedOutException{
        return UploadImage(image, recipients, 8);
    }

    public boolean UploadImage(Bitmap image, String[] recipients, int time) throws LoggedOutException{
        if(Auth_token == null)
            throw new LoggedOutException();
        String mediaId = (Username + UUID.randomUUID().toString()).toUpperCase();
        try {
            Bundle uploadData = new Bundle();
            uploadData.putString("username", Username);
            uploadData.putInt("type", 0);
            uploadData.putInt("timestamp", Api.GetTimestamp());
            uploadData.putString("media_id", mediaId);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 0, stream);
            uploadData.putByteArray("data", Api.Encrypt(stream.toByteArray()));
            stream.close();
            image = null;

            Api.postCall(
                    "/bq/upload", uploadData,
                    Auth_token, Api.GetTimestamp() + ""
            );

            uploadData = null;

            Bundle sendData = new Bundle();
            sendData.putString("username", Username);
            sendData.putInt("type", 0);
            sendData.putInt("timestamp", Api.GetTimestamp());
            sendData.putString("recipient", join(recipients, ","));
            sendData.putString("media_id", mediaId);
            sendData.putString("timestamp", Api.GetTimestamp() + "");
            sendData.putString("time", time + "");
            Api.postCall(
                    "/bq/send", sendData,
                    Auth_token, Api.GetTimestamp() + ""
            );
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void parseSnaps(JSONArray snapsRaw) {
        Snaps = new Snap[snapsRaw.size()];
        for (int i = 0; i < snapsRaw.size(); i++)
            Snaps[i] = new Snap((JSONObject)snapsRaw.get(i) ,this);
    }

    private void parseFriends(JSONArray friendsRaw) {
        Friends = new String[friendsRaw.size()];
        for(int i = 0; i < friendsRaw.size(); i++)
            Friends[i] = (String)((JSONObject)friendsRaw.get(i)).get("name");
    }

    private String join(String r[],String d)
    {
        if (r.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        int i;
        for(i=0;i<r.length-1;i++)
            sb.append(r[i]+d);
        return sb.toString()+r[i];
    }

    public byte[] Fetch(String id) throws LoggedOutException {
        if(Auth_token == null) {
            Api.debug("missing auth token!");
            throw new LoggedOutException();
        }

        int ts = Api.GetTimestamp();
        Bundle data = new Bundle();
        data.putString("id", id+"");
        data.putString("timestamp", ts+"");
        data.putString("username", Username);
        byte[] response = null;
        try {
            response = Api.getCall("/bq/blob", data, Auth_token, ts+"");
        } catch (IOException e) {
            //e.printStackTrace();
        }
        if(response == null) {
            Api.debug("Response == null");
            return null;
        }
        return response;
    }
}
