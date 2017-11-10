package com.shahin8r.plugin;

import org.apache.cordova.*;
import org.apache.cordova.file.FileUtils;
import org.apache.cordova.file.LocalFilesystemURL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.media.*;
import android.net.Uri;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import android.database.Cursor;
import android.provider.MediaStore;
import android.content.Context;

public class VideoMetadata extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("file")) {

            String source = args.getString(0);
            final CordovaResourceApi resourceApi = webView.getResourceApi();

            Uri tmpSrc = Uri.parse(source);
            final Uri src = resourceApi.remapUri(
                    tmpSrc.getScheme() != null ? tmpSrc : Uri.fromFile(new File(source)));


//            Uri src = Uri.parse(args.getString(0));

            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();


            String realPathFromURI = getRealPathFromURI(src);
            metaRetriever.setDataSource(realPathFromURI);
            
            int width = Integer.valueOf(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int rotation = Integer.valueOf(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
            int height = Integer.valueOf(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            int duration = Integer.valueOf(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            int bitrate = Integer.valueOf(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
            String mimeType = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);

            Class webViewClass = webView.getClass();
            PluginManager pm = null;
            try {
                Method gpm = webViewClass.getMethod("getPluginManager");
                pm = (PluginManager) gpm.invoke(webView);
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            if (pm == null) {
                try {
                    Field pmf = webViewClass.getField("pluginManager");
                    pm = (PluginManager)pmf.get(webView);
                } catch (NoSuchFieldException e) {
                } catch (IllegalAccessException e) {
                }
            }
            FileUtils filePlugin = (FileUtils) pm.getPlugin("File");
            LocalFilesystemURL url = filePlugin.filesystemURLforLocalPath(realPathFromURI);
            File filename = new File(realPathFromURI);

            JSONObject metadata = new JSONObject();

            metadata.put("width", width);
            metadata.put("height", height);
            metadata.put("rotation", rotation);
            metadata.put("duration", duration);
            metadata.put("bitrate", bitrate);
            metadata.put("mime", mimeType);
            metadata.put("fullPath", realPathFromURI);
            metadata.put("localUrl", url.toString());
            metadata.put("filename", filename.getName());

            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, metadata));

            return true;

        } else {

            return false;

        }
    }

    public String getRealPathFromURI(Uri contentUri) {

        Cursor cursor = null;

        try {

            Context mContext = this.cordova.getActivity().getApplicationContext();

            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);

            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            return cursor.getString(column_index);

        } catch (Exception e) {

            //error handler

        } finally {

            if (cursor != null) {
                cursor.close();
            }

        }

        return contentUri.getPath();

    }

}