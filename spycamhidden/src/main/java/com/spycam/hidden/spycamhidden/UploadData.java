package com.spycam.hidden.spycamhidden;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;

@SuppressWarnings("ALL")
class UploadData {

    String uploadData(String filePath, String uid, String url, String macAddress) {
        String responseString;

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        try {

            AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                    new AndroidMultiPartEntity.ProgressListener() {

                        @Override
                        public void transferred(long num) {
                            Log.d("Test", "transferred: " + num);
                        }
                    });
            File sourceFile = new File(filePath);

            // Adding file data to http body
            entity.addPart("image", new FileBody(sourceFile));

            // Extra parameters if you want to pass to server
            entity.addPart("uid",
                    new StringBody(uid));
            entity.addPart("macAddress",
                    new StringBody(macAddress));

            long totalSize = entity.getContentLength();
            Log.d("Test", "uploadData: totalSize-> " + totalSize);
            httppost.setEntity(entity);

            // Making server call
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity r_entity = response.getEntity();

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // Server response
                responseString = EntityUtils.toString(r_entity);
            } else {
                responseString = "Error occurred! Http Status Code: "
                        + statusCode;
            }

        } catch (Exception e) {
            responseString = e.toString();
        }

        return responseString;

    }
}
