package com.example.xk.facecard;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.microsoft.projectoxford.face.contract.Candidate;
import com.microsoft.projectoxford.face.contract.IdentifyResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import cz.msebera.android.httpclient.Header;

/**
 * Created by xk on 2018/3/1.
 */

public class RemoteHelper {
    static final String value = "745f790a22584b42b934018ba37932a4";
    static HashMap<String,HashMap<String,String>> cacheHashMap = new HashMap<>();
    static HashMap<String,Bitmap> cacheBitMap = new HashMap<>();
    public static String getPersonName(String personId, String mPersonGroupId, IdentificationActivity identificationActivity) {
        HttpClient httpclient = new DefaultHttpClient();

        try {

            HttpGet request = new HttpGet(new URI("https://westcentralus.api.cognitive.microsoft.com/face/v1.0/persongroups/"
                    +mPersonGroupId+"/persons/"+personId));
            request.setHeader("Ocp-Apim-Subscription-Key", value);


            // Request body

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
               JSONObject jsonObject = new JSONObject(EntityUtils.toString(entity));
                cacheHashMap.put(jsonObject.getString("personId"),
                        getUserDataInfo(jsonObject.getString("name"),
                                jsonObject.getJSONArray("persistedFaceIds").getString(0),
                                jsonObject.getString("userData")));

                return jsonObject.getString("name");

            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
    public static String getUserData(String personId, String mPersonGroupId, IdentificationActivity identificationActivity) {
        HttpClient httpclient = new DefaultHttpClient();

        try {

            HttpGet request = new HttpGet(new URI("https://westcentralus.api.cognitive.microsoft.com/face/v1.0/persongroups/"
                    +mPersonGroupId+"/persons/"+personId));
            request.setHeader("Ocp-Apim-Subscription-Key", value);


            // Request body

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                JSONObject jsonObject = new JSONObject(EntityUtils.toString(entity));
                return jsonObject.getString("userData");
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
    public static String getImage(String personId, String mPersonGroupId, IdentificationActivity identificationActivity) {
        HttpClient httpclient = new DefaultHttpClient();

        try {

            HttpGet request = new HttpGet(new URI("https://westcentralus.api.cognitive.microsoft.com/face/v1.0/persongroups/"
                    +mPersonGroupId+"/persons/"+personId));
            request.setHeader("Ocp-Apim-Subscription-Key", value);


            // Request body

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                JSONObject jsonObject = new JSONObject(EntityUtils.toString(entity));
                JSONArray faceID = jsonObject.getJSONArray("persistedFaceIds");
                return getImageUri(personId,mPersonGroupId, faceID.getString(0),identificationActivity);
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
    public static String getImageUri(String personId, String mPersonGroupId,String faceID, IdentificationActivity identificationActivity) {
        HttpClient httpclient = new DefaultHttpClient();

        try {
            String url = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0/persongroups/"
                    +mPersonGroupId+"/persons/"+personId+"/persistedFaces/"+faceID;
            HttpGet request = new HttpGet(new URI(url));
            request.setHeader("Ocp-Apim-Subscription-Key", value);


            // Request body

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String res = EntityUtils.toString(entity);
                JSONObject jsonObject = new JSONObject(res);
                return jsonObject.getString("userData");
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    public static void getImage(IdentifyResult identifyResult, final String mPersonGroupId, final IdentificationActivity.FaceListAdapter faceListAdapter
    ,final Handler handler) {

        List<Candidate> candidates = identifyResult.candidates;
        for (int i = 0;i < candidates.size();i++){
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
            final String pID = candidates.get(i).personId.toString();
            String uri = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0/persongroups/"
                    +mPersonGroupId+"/persons/"+pID+"/persistedFaces/"+
                    cacheHashMap.get(pID).get("faceID");
            asyncHttpClient.addHeader("Ocp-Apim-Subscription-Key", value);
            final int finalI = i;
            asyncHttpClient.get(uri, null, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(new String(responseBody));
                        String imageUri = jsonObject.getString("userData");

                        //String imageUri = getImageUri(pID,mPersonGroupId, faceID.getString(0),null);
                        Bitmap bitmap = ImageHelper.getBitmapFromURL(imageUri);
                        faceListAdapter.faceImages.set(finalI,bitmap);
                        cacheBitMap.put(pID,bitmap);
                        Message msg = handler.obtainMessage();
                        msg.arg1 = 0;
                        handler.sendMessage(msg);
                 //       faceListAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }


    }
    public static HashMap<String, String> getUserDataInfo(String name,String faceID, String userData) {
        //String name = getPersonName(personId, mPersonGroupId, identificationActivity);
       // String userData = getUserData(personId, mPersonGroupId, identificationActivity);
        HashMap<String, String> userDataInfo = new HashMap<>();
        userDataInfo.put("姓名", name);
        userDataInfo.put("faceID", faceID);
        String[] userDatas = userData.split(" ");
        boolean title = false;
        String curTitle = "";
        for(int i = 0; i < userDatas.length; i ++) {
            if(userDatas[i].contains("：")) {
                title = true;
                String[] titles = userDatas[i].split("：");
                userDataInfo.put(titles[0], "");
                curTitle = titles[0];
                for(int j = 1; j < titles.length; j ++) {
                    userDataInfo.put(curTitle, userDataInfo.get(curTitle) + titles[j] + " ");
                }
            } else if(title) {
                userDataInfo.put(curTitle, userDataInfo.get(curTitle) + userDatas[i] + " ");
            }
        }
        return userDataInfo;
    }

    public static Bitmap getBitmapByID(String personId) {
        if (cacheBitMap.get(personId)==null){
            return ImageHelper.getBitmapFromURL(getImage(personId,"1",null));
        }else {
            return cacheBitMap.get(personId);
        }
    }
}
