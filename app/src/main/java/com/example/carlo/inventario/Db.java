package com.example.carlo.inventario;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by carlo on 28/02/2018.
 */

public class Db {

    public static String internalDir;
    public static String externalDir;
    public static final int READ_BLOCK_SIZE = 100;

    private static final int PERMISSION_WRITE_REQUEST_CODE = 1;
    private static final int PERMISSION_READ_REQUEST_CODE = 2;


    public static void checkExternalMedia() {

        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

    }

    public static void writeToExternalMedia(Context context, String filename, String data){

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

        File root = android.os.Environment.getExternalStorageDirectory();


        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        File dir = new File (externalDir);
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        dir.mkdirs();
        File file = new File(dir, filename);

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.print(data);
            pw.flush();
            pw.close();
            f.close();
            MediaScannerConnection.scanFile(context, new String[] {file.getAbsolutePath()}, new String[] {"text/plain"}, null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static void writeToInternalMedia(Context context, String filename, String data){
        try {


            FileOutputStream fileout= context.openFileOutput(internalDir+filename, MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(data);
            outputWriter.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFromInternalMedia(Context context, String filename){

        String s="";

        try {
            FileInputStream fileIn=context.openFileInput (filename);
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[READ_BLOCK_SIZE];

            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return s;
    }


    public static File[] getInternalFiles(Context context){

        File directory = new File(internalDir);

        return context.getFilesDir().listFiles();
    }

    public static void moveExternalFiles (Context context ){

        MediaScannerConnection.scanFile(context, new String[] {externalDir}, new String[] {"text/plain"}, null);
        File dir=new File(externalDir);


        File[] files =dir.listFiles();

        for(int i=0; i<files.length; ++i ){

            if (files[i].getName().equals("RESULTADO.TXT"))
                continue;

            String newFileName = files[i].getName().replaceAll("[^\\d]", "") + "_0";

            String data= readExternalFile(context, files[i].getName());


            writeToInternalMedia(context, newFileName, data);
//            context.deleteFile(externalDir+files[i].getName());
        }


    }


    public static String readExternalFile(Context context, String filename){

        StringBuffer fileContent = new StringBuffer("");


        try {
            FileInputStream fis;

            File file = new File(externalDir, filename);

            fis = new FileInputStream(file);

            byte[] buffer = new byte[1024];

            int n;
            while ((n = fis.read(buffer)) !=-1)
            {
                fileContent.append(new String(buffer, 0, n));
            }

        }

        catch (Exception e){
            e.printStackTrace();

        }

        return fileContent.toString();
    }

}

