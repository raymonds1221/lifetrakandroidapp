package com.salutron.lifetrakwatchapp.util;

import android.content.Context;
import android.os.Environment;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.salutron.lifetrakwatchapp.LifeTrakApplication;
import com.salutron.lifetrakwatchapp.web.AsyncListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Janwel Ocampo on 10/6/2015.
 */
public class AmazonTransferUtility{
    private Context mContext;
    private static AmazonTransferUtility mAmazonTransferUtility;
    private CognitoCachingCredentialsProvider mCredentialsProvider;
    private AmazonS3 mAmazonS3;
    private  TransferUtility transferUtility;

    private String textName = "lifetrakData.json";
    private String textBucketName = "lifetrak-bulk-data2";
    private String textBucketNameRestore = "lifetrak-restore-data";
    private String fileName = "lifetrak";

    private static final int BUFFER = 2048;

    private AsyncListener mListener;

    private String mUUID = "";

    public static String getDataPath() {
        return Environment.getExternalStorageDirectory() + File.separator + "LifetrakData";
    }

    private AmazonTransferUtility(Context context) {
        mContext = context;
        mCredentialsProvider = new CognitoCachingCredentialsProvider(
                context, "us-east-1:83239ddc-6033-4a53-9e26-56f06bb46325",
                Regions.US_EAST_1
        );
        ClientConfiguration s3Config = new ClientConfiguration();
        s3Config.setMaxConnections(1);
        s3Config.setSocketTimeout(1000 * 60 * 3);
        s3Config.setConnectionTimeout(1000 * 60 * 3);
        mAmazonS3 = new AmazonS3Client(mCredentialsProvider, s3Config);

        transferUtility = new TransferUtility(mAmazonS3, context);

    }

    public static AmazonTransferUtility getInstance(Context context) {
        if (mAmazonTransferUtility == null)
        {
            mAmazonTransferUtility = new AmazonTransferUtility(context);
        }
        return mAmazonTransferUtility;
    }

    public AmazonTransferUtility listener(AsyncListener listener) {
        mListener = listener;
        return this;
    }



    public AmazonTransferUtility uploadFileToAmazonS3(String data, Date date){

        generateTextFileFromString(data, date);

        File jsonFile = new File(getDataPath(), textName);

        TransferObserver observer = transferUtility.upload(
                textBucketName,
                mUUID + File.separator + date.getTime() + textName ,
                jsonFile
        );
        mListener.onAsyncStart();

        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                try {
                    if (state.toString().equals("COMPLETED")) {
                        deleteFile(textName);
                        if (mListener != null) {
                            JSONObject result = new JSONObject();
                            result.put("result", state.toString());
                            mListener.onAsyncSuccess(result);
                        }
                    }
                    else if (state.toString().equals("FAILED") ||
                            state.toString().equals("UNKNOWN")
                            ){
                        mListener.onAsyncFail(id, state.toString());
                    }
                    else{
                        LifeTrakLogger.info("S3 TransferState :" + state.toString());
                    }
                }catch (JSONException e){
                    LifeTrakLogger.info(e.getLocalizedMessage());
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                if (bytesCurrent == bytesTotal){
                    LifeTrakLogger.info("Completed");
                }
                else{
                    LifeTrakLogger.info("Current bytes: " + bytesCurrent + " Of bytesTotal : " + bytesTotal);
                }
            }

            @Override
            public void onError(int id, Exception ex) {
                mListener.onAsyncFail(id,ex.getMessage());
            }
        });
        return this;
    }

    public AmazonTransferUtility setUUID(String uuid){
        mUUID = uuid;
        return this;
    }

    public AmazonTransferUtility downloadFileToAmazonS3(String key, String bucketName){
        File root = new File(getDataPath());
        if (!root.exists()) {
            root.mkdirs();
        }

        File tempFile = new File(root, key);
        TransferObserver observer = transferUtility.download(
                bucketName,     /* The bucket to download from */
                mUUID + File.separator + key,    /* The key for the object to download */
                tempFile        /* The file to download the object to */
        );

        mListener.onAsyncStart();
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                try {
                    if (state.toString().equals("COMPLETED")) {
                        if (mListener != null) {
                            JSONObject result = new JSONObject();
                            result.put("result", state.toString());
                            mListener.onAsyncSuccess(result);
                        }
                    }
                }catch (JSONException e){
                    LifeTrakLogger.info(e.getLocalizedMessage());
                }
            }
            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                if (bytesCurrent == bytesTotal){
                    LifeTrakLogger.info("Completed bytes: " + bytesCurrent + " Of bytesTotal : " + bytesTotal);
                }
                else{
                    LifeTrakLogger.info("Current bytes: " + bytesCurrent + " Of bytesTotal : " + bytesTotal);
                }
            }
            @Override
            public void onError(int id, Exception ex) {

                mListener.onAsyncFail(id,ex.getMessage());
            }
        });
        return this;
    }


    public S3Object getDownloadObject(String bucketName, String keyName){
        GetObjectRequest objReq = new GetObjectRequest(bucketName,  mUUID + File.separator + keyName);
        objReq.withGeneralProgressListener(new ProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                LifeTrakLogger.info("progressEvent :" + progressEvent.toString());
            }
        });
        return mAmazonS3.getObject(objReq);
    }

    private void generateTextFileFromString(String data, Date date){
        try
        {
            File root = new File(getDataPath());
            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxFile = new File(root, textName);
            FileWriter writer = new FileWriter(gpxFile);
            writer.append(data);
            writer.flush();
            writer.close();



        }
        catch(IOException e)
        {
            e.printStackTrace();
            e.getMessage();
        }

    }

    public static String generateRandomUUID(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }


    public File getFile(String fileName){
      return  new File(getDataPath(), fileName);
    }
    public void deleteFile(String fileName){
        new File(getDataPath() + File.separator + fileName).delete();
    }

}
