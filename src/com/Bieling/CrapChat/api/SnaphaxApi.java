package com.Bieling.CrapChat.api;

import android.os.Bundle;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SnaphaxApi {

    private final boolean debug = true;
    private final String Pattern = "0001110111101110001111010101111011010001001110011000110001000110";
    private final String Secret = "iEk21fuwZApXlz93750dmW22pw389dPwOk";
    private final String static_token = "m198sOkJEn37DjqZ32lpRu76xmw288xSQ9";
    private final String baseurl = "https://feelinsonice-hrd.appspot.com";
    private final String user_agent = "BananaPhone 9001(iPad; iPhone OS 6.0; en_US)";

    public enum MediaType {
        VIDEO,
        IMAGE
    }

    public void debug(Object o) {
        if (debug)
            Logger.getLogger("CrapChat").log(Level.INFO, o.toString());
    }

    public boolean IsValidBlobHeader(byte[] header) {
        return (header[0] == 0 && // mp4
                        header[0] ==0) ||
               (header[0] ==0xFF && // jpg
                        header[1] ==0xD8);
    }

    public byte[] Encrypt(byte[] data) {
        try {
            return AESEncrypt.encrypt(data);
        } catch(Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public byte[] Decrypt(byte[] data) {
        try {
            return AESEncrypt.decrypt(data);
        } catch(Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private static void copy(InputStream inputstream, OutputStream outputstream)
            throws IOException
    {
        byte abyte0[] = new byte[4096];
        do
        {
            int i = inputstream.read(abyte0);
            if (i != -1)
            {
                outputstream.write(abyte0, 0, i);
            } else
            {
                return;
            }
        } while (true);
    }

    @SuppressWarnings("deprecation")
    public byte[] getCall(String endpoint, Bundle post_data, String param1, String param2) throws IOException{
        byte abyte0[];
        post_data.putString("req_token", Hash(param1, param2));
        HttpURLConnection httpurlconnection;
        BufferedInputStream bufferedinputstream;
        httpurlconnection = (HttpURLConnection)(new URL(baseurl + endpoint + "?" + encode(post_data))).openConnection();
        InputStream inputstream = httpurlconnection.getInputStream();
        bufferedinputstream = new BufferedInputStream(inputstream, 4096);
        ByteArrayOutputStream bytearrayoutputstream;
        BufferedOutputStream bufferedoutputstream;
        bytearrayoutputstream = new ByteArrayOutputStream();
        bufferedoutputstream = new BufferedOutputStream(bytearrayoutputstream, 4096);
        copy(bufferedinputstream, bufferedoutputstream);
        bufferedoutputstream.flush();
        abyte0 = bytearrayoutputstream.toByteArray();
        debug("Pre  = " + abyte0.length);
        abyte0 = Decrypt(abyte0);
        debug("Post = " + abyte0.length);
        bufferedinputstream.close();
        bufferedoutputstream.close();
        return abyte0;
    }

    /*public String postCall(String endpoint, Bundle post_data, String param1, String param2) throws Exception{
        post_data.putString("req_token", Hash(param1, param2));

        URL url = new URL(baseurl + endpoint);
        debug(url.toString());

        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", user_agent);
        con.setUseCaches(false);
        con.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(encode(post_data));

        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        debug(response.toString());

        return response.toString();
    }       */

    public String postCall(String endpoint, Bundle post_data, String param1, String param2) throws Exception {
        post_data.putString("req_token", Hash(param1, param2));
        URL url = new URL(baseurl + endpoint);

        HttpPost httppost = new HttpPost();
        httppost.setURI(url.toURI());
        debug(url);

        if (post_data.containsKey("data"))
            httppost.setEntity(createMultipartEntity(post_data));
        else
            httppost.setEntity(new UrlEncodedFormEntity(paramsToList(post_data)));

        BasicHttpParams basichttpparams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(basichttpparams, '\u2710');
        HttpConnectionParams.setSoTimeout(basichttpparams, 60000);
        HttpResponse httpresponse = (new DefaultHttpClient(basichttpparams)).execute(httppost);
        HttpEntity httpentity = httpresponse.getEntity();
        debug(httpresponse.getStatusLine());
        if (httpentity != null)
        {
            String s = EntityUtils.toString(httpentity);
            debug(s);
            return s;
        }
        throw new Exception("No entity returned or something");
    }

    private static List paramsToList(Bundle bundle)
    {
        //TODO: Cleanup
        ArrayList arraylist = new ArrayList(bundle.size());
        Iterator iterator = bundle.keySet().iterator();
        do
        {
            if (!iterator.hasNext())
                break;
            String s = (String)iterator.next();
            Object obj = bundle.get(s);
            if (obj != null)
            {
                String s1 = obj.toString();
                arraylist.add(new BasicNameValuePair(s, s1));
            }
        } while (true);
        return arraylist;
    }

    private static MultipartEntity createMultipartEntity(Bundle bundle) throws UnsupportedEncodingException
    {
        //TODO: Cleanup
        MultipartEntity multipartentity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (Iterator iterator = bundle.keySet().iterator(); iterator.hasNext();)
        {
            String s = (String)iterator.next();
            Object obj = bundle.get(s);
            try{
                if (s.equals("data"))
                    multipartentity.addPart(s,  new ByteArrayBody(bundle.getByteArray(s), "data"));
                else
                    multipartentity.addPart(s, new StringBody(obj.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return multipartentity;
    }

    /*public String postCall(String endpoint, Bundle post_data, String param1, String param2, byte[] data, MediaType type) throws Exception {
        post_data.putString("req_token", Hash(param1, param2));

        File uploadable = new File(Globals.SnapsDir, "CACHE_" + GetTimestamp() + ".jpg");

        String boundary = "*****";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        URL url = new URL(baseurl + endpoint);
        debug(url.toString());

        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", user_agent);
        con.setUseCaches(false);
        con.setDoOutput(true);
        if(data != null && data.length > 0) {
            con.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setDoInput(true);

            uploadable.createNewFile();
            FileOutputStream fos = new FileOutputStream(uploadable);
            fos.write(data);
            fos.close();
            uploadable.setReadable(true, false);
            uploadable.setWritable(true, false);
        }

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(encode(post_data));

        if(data != null && data.length > 0) {
            debug("Aw shit we upload naow");
            wr.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
            wr.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"file\"" + lineEnd);
            wr.writeBytes(lineEnd);


            // create a buffer of maximum size
            int bytesRead, bytesAvailable, bufferSize;
            int maxBufferSize = 1*1024*1024;

            debug(uploadable.exists());
            FileInputStream fileInputStream = new FileInputStream(uploadable);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            // read file and write it into form...

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0)
            {
                wr.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...

            wr.writeBytes(lineEnd);
            wr.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // close streams
            fileInputStream.close();
            debug("Done uploading");
        }

        wr.flush();
        wr.close();

        //debug(con.getResponseCode() + ": " + con.getResponseMessage());

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        debug(response.toString());

        if(uploadable.exists())
            uploadable.delete();

        return response.toString();
    }                        */



    @SuppressWarnings("deprecation")
    public String encode(Bundle data) {
        String encoded = "";
        for(String k : data.keySet())
            encoded += "&" + URLEncoder.encode(k) + "=" + URLEncoder.encode(data.get(k).toString());
        encoded = encoded.replaceFirst("&", "");
        debug(encoded);
        return encoded;
    }

    public int GetTimestamp() {
        return Math.round(System.currentTimeMillis() / 1000L);
    }

    public String Hash(String s, String s1){
        try {
            StringBuilder stringbuilder = new StringBuilder();
            String s3 = stringbuilder.append(Secret).append(s).toString();
            StringBuilder stringbuilder1 = (new StringBuilder()).append(s1);
            String s5 = stringbuilder1.append(Secret).toString();
            MessageDigest messagedigest = MessageDigest.getInstance("SHA-256");
            messagedigest.update(s3.getBytes("UTF-8"));
            String s7 = toHex(messagedigest.digest());
            messagedigest.update(s5.getBytes("UTF-8"));
            String s9= toHex(messagedigest.digest());
            String s10 = "";
            int i = 0;
            do {
                int j = Pattern.length();
                if (i < j) {
                    char c1 = Pattern.charAt(i) == '0' ? s7.charAt(i) : s9.charAt(i);
                    s10 = (new StringBuilder()).append(s10).append(c1).toString();
                    i++;
                } else {
                    return s10;
                }
            } while (true);
        } catch (Exception ignore){
            return s;
        }
    }

    private String toHex(byte abyte0[])
    {
        Object aobj[] = new Object[1];
        BigInteger biginteger = new BigInteger(1, abyte0);
        aobj[0] = biginteger;
        return String.format("%064x", aobj);
    }

}
