package com.whitejuke.latexpix;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.text.StringEscapeUtils;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.agog.mathdisplay.MTFontManager;
import com.agog.mathdisplay.MTMathView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int CAMERA_REQUEST_CODE = 100;
    String storagePermissions[];
    String cameraPermissions[];

    String wolframAppId = "";
    String serverIp = "";
    Uri image_uri = null;

    boolean checkOnce;
    private ImageView imageView;
    private Button selectImageButton;
    private Button convertButton;

    private Button solveButton;
    private WebView webView;

    private String wolframSend;
    private MTMathView mathView;
    private TextView resultTextView;

    private File selectedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        imageView = findViewById(R.id.imageView);
        selectImageButton = findViewById(R.id.selectImageButton);
        solveButton = findViewById(R.id.solveButton);

        convertButton = findViewById(R.id.convertButton);

        resultTextView = findViewById(R.id.resultTextView);





        imageView.setAdjustViewBounds(true);
        mathView = findViewById(R.id.mathview);
        mathView.setDisplayErrorInline(false);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        mathView.setFontSize(55f);


        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        solveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wolframSend != null) {
                    sendMathMLToWolfram(wolframSend, false);
                } else {
                    Toast.makeText(MainActivity.this, "Analyze image First", Toast.LENGTH_SHORT).show();
                }
            }
        });

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultTextView.setText("");
                if (selectedImageFile != null) {
                    convertImageToLaTeX(selectedImageFile);
                } else {
                    Toast.makeText(MainActivity.this, "Select an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private void pickFromGallery() {
        // Pick image from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        GalleryActivityResultLauncher.launch(galleryIntent);
    }

    private void convertImageToLaTeX(File imageFile) {
        checkOnce = true;
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("image/jpeg");
        RequestBody requestBody;
        try {
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg",
                            RequestBody.create(mediaType, imageFile))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Invalid file path", Toast.LENGTH_SHORT).show();
            return;
        }

        Request request = new Request.Builder()
                .url("http://"+serverIp+":8502/predict/") // API address should be set correctly
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Conversion failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    String cleanedResponseBody = removeDuplicateBackslashes(responseBody);
                    Log.d("response", "respond" + responseBody);
                    Log.d("response", "respondclean" + cleanedResponseBody);
                    Gson gson = new Gson();
                    JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);

                    // Check if the response is a JSON object
                    if (jsonElement.isJsonObject()) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        String result = jsonObject.get("result").getAsString();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Server is Closed.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String noStarString;
                                noStarString = cleanedResponseBody.replace("*","");
                                String viewString;
                                viewString = noStarString.replace("\\operatorname{lim}","\\lim");
                                Log.d("nostar", "result : " + viewString);
                                mathView.setTextAlignment(MTMathView.MTTextAlignment.KMTTextAlignmentCenter);
                                mathView.setFontSize(75f);
                                mathView.setLatex(viewString);
                                mathView.refreshDrawableState();
                                if(viewString.length()>80){
                                    mathView.setFontSize(42f);
                                    mathView.refreshDrawableState();
                                }
                                else if(viewString.length()>45){
                                    mathView.setFontSize(55f);
                                    mathView.refreshDrawableState();
                                }
                                if(mathView.getLatex()==""){
                                    Toast.makeText(MainActivity.this, "Wasnot able to analyze.", Toast.LENGTH_SHORT).show();
                                }
                                convertLatexToMathML(noStarString);
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Conversion failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    private void sendMathMLToWolfram(String mathMLString, boolean secondTry) {
        //String plainText = StringEscapeUtils.unescapeXml(mathMLString);

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        String wolframUrl = "https://api.wolframalpha.com/v2/result?appid="+wolframAppId;

        String postData = "input=" + Uri.encode(mathMLString);
        RequestBody requestBody = RequestBody.create(mediaType, postData);

        Request request = new Request.Builder()
                .url(wolframUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Request failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String result = response.body().string();

                if (response.isSuccessful()) {
                    // Parse the result to extract the relevant information

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            resultTextView.setText(result);
                            Log.d("response", "respond" + result);
                            if(secondTry){
                                resultTextView.setText("Result :\n"+result);
                            }
                            // Handle the equation result
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(checkOnce){
                                checkOnce= false;
                                resultTextView.setText("First method failed. Trying second method");
                                sendMathMLToWolfram(MathMLConverter.fixMathML(mathMLString),true);
                            }
                            Log.d("response", "respond" + result);
                            if(secondTry){
                                resultTextView.setText("Not me Wolfram says: "+  result);
                                if(result.contains("understand")){
                                    resultTextView.setText("Wolfram did'nt understand your problem.");
                                }
                            }
                        }
                    });
                }
            }
        });
    }



    private void convertLatexToMathML(String latexString) {

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        String texzillaUrl = "http://"+serverIp+":3000/convert";

        // Create a JSON object with the "latex" key and the LaTeX string as its value
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("latex", latexString);

        RequestBody requestBody = RequestBody.create(mediaType, jsonBody.toString());

        Request request = new Request.Builder()
                .url(texzillaUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Conversion failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String result = response.body().string();

                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wolframSend = result;
                            Log.d("convertLatexToMathML SEND", "result : " + result);
                            String newresult = MathMLConverter.fixMathML(result);
                            Log.d("convertLatexToMathML SEND", "result : " + newresult);

                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Log.d("response", "respond" + result);

                            Toast.makeText(MainActivity.this, "Conversion failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    private String removeDuplicateBackslashes(String text) {
        return text.replace("\\\\", "\\");
    }

    ActivityResultLauncher<Intent> GalleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                //image is picked from gallery get uri of image
                image_uri = data.getData();

                imageView.setImageURI(image_uri);
                Log.d("imageuri", "imageuri:" + image_uri);
                selectedImageFile = new File(getFileFromUri(image_uri));
            }
        }
    });

    private String getFileFromUri(Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    protected void onResume() {
        super.onResume();
    }
    private void showImagePickDialog() {
        //show dialog containing options camera and gallery to pick the image
        String options[] = {"Camera","Gallery"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set title
        builder.setTitle(("Pick Image From"));
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clicks
                if (which == 0 ){
                    //Camera Clicked
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        pickFromCamera();
                    }
                }
                else if (which == 1){
                    //Gallery clicked
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }
    private void pickFromCamera() {
        //intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri
        image_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        CameraActivityResultLauncher.launch(cameraIntent);
    }

    ActivityResultLauncher<Intent> CameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == Activity.RESULT_OK){
                //image is picked from gallery get uri of image

                imageView.setImageURI(image_uri);
                selectedImageFile = new File(getFileFromUri(image_uri));
            }
        }
    });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Handling permission cases (allowed & denied)
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                // Picking from camera
                if (grantResults.length >= 2) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        // Permission granted
                    } else {
                        Toast.makeText(this, "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Insufficient permissions granted", Toast.LENGTH_SHORT).show();
                }
                break;

            case STORAGE_REQUEST_CODE:
                // Picking from gallery
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        // Permission granted
                    } else {
                        Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Insufficient permissions granted", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                Toast.makeText(this, "Unhandled permission request code", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean checkStoragePermission(){

        boolean resultx = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return resultx;
    }
    private void requestStoragePermission(){
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission(){

        boolean resultx = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return resultx && result1;
    }
    private void requestCameraPermission(){
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, cameraPermissions,CAMERA_REQUEST_CODE);
    }
}
