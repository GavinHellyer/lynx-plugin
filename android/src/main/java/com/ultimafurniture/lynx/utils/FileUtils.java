package com.ultimafurniture.lynx.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FileUtils {

    private static final String TAG = "FileUtils";

    private final String logFileName = "logs.txt";
    private String logFileNameCurrent = "";
    private String fileName = "logs.txt";
    // it's in KB
    private final long MAX_FILE_SIZE_LOG = 2;
    // private String fileDirectory =
    // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    // .getPath();
    private static String fileDirectory = "";

    private FileWriter writer;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    // the file will be created in Android>data>app package >files>documents
    // this will not required any runtime permission for storage
    public static void init(Context context) {
        fileDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath();
    }

    public void writeToFile(String fileName, String string) {

        try {
            writer = new FileWriter(fileDirectory + "/" + fileName);
            writer.write(string);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendToFile(String fileName, String string) {

        try {
            writer = new FileWriter(fileDirectory + "/" + fileName, true);
            writer.write(string);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String string) {

        try {
            Log.i(TAG, string);
            writer = new FileWriter(getLogFileName(), true);
            writer.write(string + "\t" +
                    dateFormat.format(Calendar.getInstance().getTime()) + "\n");
            writer.close();

            Log.i(TAG, "writing to logs.txt " + getLogFileName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLogFileName() {
        try {
            logFileNameCurrent = (fileDirectory + "/logs" + "_" +
                    Calendar.getInstance().get(Calendar.YEAR) +
                    Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + ".txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return logFileNameCurrent;
    }

    public long fileSize(String fileName) {
        File file = new File(fileName);
        return file.length() / 1024;
    }

    // todo
    // must check storage permission before calling this method
    public void exportFile(Context context, String fileName) {

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(context, "Storage permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        File fileLocation = new File(fileDirectory, fileName);
        // Uri uri = FileProvider.getUriForFile()
    }
}
