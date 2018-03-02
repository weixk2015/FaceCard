package com.example.xk.facecard;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by xk on 2018/3/1.
 */

public class RemoteHelper {
    static final String value = "745f790a22584b42b934018ba37932a4";

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
}
