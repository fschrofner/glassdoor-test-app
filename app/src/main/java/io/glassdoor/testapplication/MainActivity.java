package io.glassdoor.testapplication;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private String password = "thatismypassword";
    private String user = "GlassdoorUser";
    public String userName = "Testing";
    private String passwd = "appletree";
    protected String pass = "pass";
    private String secret = "5ebe2294ecd0e0f08eab7690d2a6ee69";
    private String key = "3c6e0b8a9c15224a8228b9a98ca1531d";
    private String url = "https://flosch.at"; //flosch.at will not accept the request but that does not matter for our cause

    private static int PERMISSION_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //else this method will be called inside the permission callback
        if(checkPermissions()){
            Log.d("MainActivity", "write to external storage permission granted");
            try {
                writePrivateDataToFileSystem();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        httpPostPrivateData();
    }

    private boolean checkPermissions(){
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(storagePermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("MainActivity", "request permission result");

        if (requestCode == PERMISSION_REQUEST_CODE) {
            Log.d("MainActivity", "request code matches permission request");
            Log.d("MainActivity", "number of permissions in result: " + permissions.length);

            for(int i=0; i<permissions.length; i++) {
                Log.d("MainActivity", "permission: " + permissions[i] + " " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

                if(permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    try {
                        writePrivateDataToFileSystem();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * Method to test the fs-changes plugin
     * @throws IOException
     */
    private void writePrivateDataToFileSystem() throws IOException {

        Log.d("MainActivity", "writing data to filesystem..");

        //write critical data to sd card
        File sdcard = Environment.getExternalStorageDirectory();
        File folder = new File(sdcard.getAbsolutePath() + File.separator + "glassdoor-test");
        File output = new File (folder.getAbsolutePath() + File.separator + "private-keys");

        if(!folder.exists()){
            folder.mkdirs();
        }

        if(output.exists()){
            output.delete();
        }

        output.createNewFile();

        FileOutputStream outputStream = new FileOutputStream(output);
        PrintWriter writer = new PrintWriter(outputStream);
        writer.println("user:" + user);
        writer.println("password:" + password);
        writer.println("secret:" + secret);

        writer.close();
        outputStream.close();

        //write data to shared prefs. can not be accessed by default
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putString("key", key).apply();
    }

    /**
     * Method to test the mitm plugin
     * @throws IOException
     */
    private void httpPostPrivateData() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("MainActivity", "sending post request");

                MediaType jsonType = MediaType.parse("application/json; charset=utf-8");

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(jsonType, "{username:" + userName + ", password:" + passwd + "}");

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                try {
                    client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

}


