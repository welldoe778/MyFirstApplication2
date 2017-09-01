package com.mocoo.hang.rtprinter.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2015/6/3.
 */
public class SaveMediaFileUtil {

    private static final String TAG = "SaveMediaFileUtil";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static final String DIR_CAPTURE = "Capture";
    public static final String DIR_SAVED = "Saved";

    private SaveMediaFileUtil()
    {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isExternalStorageWritable() {

        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Create a file Uri for saving an image or video
     */
    public static Uri getOutputMediaFileUri(Context context, String dirName, int type) {
        return Uri.fromFile(getOutputMediaFile(context, dirName, type));
    }

    /**
     * Create a File for saving an image or video
     */
    public static File getOutputMediaFile(Context context, String dirName, int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir =
                new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), dirName);
        LogUtils.d(TAG, "mediaStorageDir getAbsolutePath = " + mediaStorageDir.getAbsolutePath());
        // a standard location for saving pictures and videos which are
        // associated with your application. If your application is uninstalled,
        // any files saved in this location are removed. Security is not
        // enforced for files in this location and other applications may read,
        // change and delete them.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                LogUtils.d("RT_Printer", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

}
