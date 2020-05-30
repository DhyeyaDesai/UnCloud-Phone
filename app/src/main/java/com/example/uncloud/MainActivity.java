package com.example.uncloud;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {


    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/UnCloud", "/FilesToSync.txt");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
        }if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String address = info.getMacAddress();
    }

    public void syncClick(View view) throws IOException {
        String[] loadText = Load(file);
        String text = "";
        for (int i = 0; i < loadText.length; i++) {
            text = loadText[i] + System.getProperty("line.separator");
            syncFiles(text.replace("ftp://a8-3e-0e-61-3f-db:2221", "").trim());
        }

        Toast.makeText(getApplicationContext(), "Done.", Toast.LENGTH_SHORT).show();
    }

    public void syncFiles(String text) throws IOException {

        Log.i("HASH", "syncFiles: " + text);
        String fileName = text.substring(text.lastIndexOf('/') + 1, text.length());
        String filePath = text.substring(0, text.lastIndexOf('/'));
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + text);
        try {
            if(file.isFile()) {
                Save("/UnCloud/SyncFilesHashes/", "HASH" + fileName + ".txt", fileToMD5(Environment.getExternalStorageDirectory().getAbsolutePath() + text));
            }
            else{
                File[] files = file.listFiles();
                String strfiles = "";
                for (File file1:files) {

                    Log.i("HASH", "syncFiles: " + file1.getAbsolutePath().replace("/storage/emulated/0/", "/"));
                    strfiles = strfiles + file1 + "\n";
                    syncFiles(file1.getAbsolutePath().replace("/storage/emulated/0/", "/"));
                }
                Save("/UnCloud/FoldersToSync/", fileName + ".txt", strfiles.replace("/storage/emulated/0","ftp://a8-3e-0e-61-3f-db:2221")+"");
            }
        } catch (IOException e) { }

    }

    public static String fileToMD5(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private static String convertHashToString(byte[] md5Bytes) {
        String returnVal = "";
        for (int i = 0; i < md5Bytes.length; i++) {
            returnVal += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1);
        }
        return returnVal.toLowerCase();
    }

    public static String[] Load(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String test;
        int anzahl = 0;
        try {
            while ((test = br.readLine()) != null) {
                anzahl++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fis.getChannel().position(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] array = new String[anzahl];

        String line;
        int i = 0;
        try {
            while ((line = br.readLine()) != null) {
                array[i] = line;
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }

    public static void Save(String path, String fileName, String data) throws IOException {
        try{
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + path, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.getBytes());
            fos.close();

        }
        catch (Exception e){}
    }

    public static void Save(File file, String[] data) throws IOException {


        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            try {
                for (int i = 0; i < data.length; i++) {
                    fos.write(data[i].getBytes());
                    if (i < data.length - 1) {
                        fos.write("\n".getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
}