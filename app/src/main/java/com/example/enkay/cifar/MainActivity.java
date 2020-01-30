package com.example.enkay.cifar;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


//import TensorFlow libraries
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

// Import image utilities
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.enkay.cifar.Helper.BitmapHelper;

import static android.os.Environment.getExternalStoragePublicDirectory;


public class MainActivity extends AppCompatActivity {

    private TensorFlowInferenceInterface inferenceInterface;
    FloatingActionButton floatbutton;
    ImageView image;
    TextView textView;
    String pathToFile;
    Button button;
    String text_key="mytext";

    private static final String MODEL_FILE = "file:///android_asset/frozen_model_FIVECROPSFINAL1.pb";

    private static final String INPUT_NODE = "ipnode"; // our input node
    private static final String OUTPUT_NODE = "opnode"; // our output node

    private static final int[] INPUT_SIZE = {1,32,32,3};

    static {
        System.loadLibrary("tensorflow_inference");
    }

    // helper function to find the indices of the element in an array with maximum value
    public static int argmax (float [] elems)
    {
        int bestIdx = -1;
        float max = -1000;
        for (int i = 0; i < elems.length; i++) {
            float elem = elems[i];
            if (elem > max) {
                max = elem;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatbutton = (FloatingActionButton) findViewById(R.id.floatbutton);
        button=(Button)findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.result);
        image = (ImageView) findViewById(R.id.car);

        inferenceInterface = new TensorFlowInferenceInterface();
        inferenceInterface.initializeTensorFlow(getAssets(), MODEL_FILE);
        System.out.println("model loaded successfully");
        String imageUri = "drawable://" + R.drawable.models;

        AssetManager assetManager = getAssets();
        if(Build.VERSION.SDK_INT>=23){
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }

        floatbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchPictureTakeAction();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity2();
            }
        });

    }

    public void openActivity2(){
        String name= textView.getText().toString();
        Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);
        Intent intent=new Intent(this,Main2Activity.class);
        BitmapHelper.getInstance().setBitmap(bitmap);
        intent.putExtra(text_key,name);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode ==RESULT_OK){
            if(requestCode==1){

                    try {


                        final int inputSize = 32;

                        final int destWidth = 32;
                        final int destHeight = 32;


            /*
            Mean and standard deviation of the Dataset
            Initialise them with corresponding values

            int imageMean;
            float imageStd;
             */
                        // Load the image
                        //InputStream file = assetManager.open("tnausi7.jpg");
                        //Bitmap bitmap = BitmapFactory.decodeStream(file);
                        Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);

                        Bitmap bitmap_scaled = Bitmap.createScaledBitmap(bitmap, destWidth, destHeight, false);

                        // Set the bitmap to the image view (UI) - optional

                        image.setImageBitmap(bitmap);


                        // Load class names of CIFAR 10 dataset into a string array
                        //String[] classes = {"cotton", "rice", "sugarcane","coffee","jute","millet","mustard","oilseed","turmeric","wheat"};
                        //String[] classes = {"airplane","automobile","bird", "cat", "deer", "dog", "frog "," horse", "ship", "truck"};
                        String[] classes = {"cotton", "rice", "sugarcane","Mustard","wheat"};


                        int[] intValues = new int[inputSize * inputSize]; // array to copy values from Bitmap image
                        float[] floatValues = new float[inputSize * inputSize * 3]; // float array to store image data

                        // note: Both intValues and floatValues are flattened arrays

                        //get pixel values from bitmap image and store it in intValues
                        bitmap_scaled.getPixels(intValues, 0, bitmap_scaled.getWidth(), 0, 0, bitmap_scaled.getWidth(), bitmap_scaled.getHeight());
                        for (int i = 0; i < intValues.length; ++i) {
                            final int val = intValues[i];
                /*
                preprocess image if required
                floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - imageMean) / imageStd;
                floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - imageMean) / imageStd;
                floatValues[i * 3 + 2] = ((val & 0xFF) - imageMean) / imageStd;
                */

                            // convert from 0-255 range to floating point value
                            floatValues[i * 3 + 0] = ((val >> 16) & 0xFF);
                            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF);
                            floatValues[i * 3 + 2] = (val & 0xFF);
                        }


                        //  the input size node that we declared earlier will be a parameter to reshape the tensor
                        // fill the input node with floatValues array
                        inferenceInterface.fillNodeFloat(INPUT_NODE, INPUT_SIZE, floatValues);
                        // make the inference
                        inferenceInterface.runInference(new String[]{OUTPUT_NODE});
                        // create an array filled zeros with dimension of number of output classes. In our case its 10
                        float[] result = new float[5];
                        Arrays.fill(result, 0.0f);
                        // copy the values from output node to the 'result' array
                        inferenceInterface.readNodeFloat(OUTPUT_NODE, result);
                        // find the class with highest probability
                        int class_id = argmax(result);

                        // Setting the class name in the UI
                        textView.setText(classes[class_id]);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }

    private void dispatchPictureTakeAction(){
        Intent takepic= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takepic.resolveActivity(getPackageManager())!= null){
            File photofile= null;
            photofile= createPhotoFile();

            if(photofile!=null){
                pathToFile= photofile.getAbsolutePath();
                Uri photouri= FileProvider.getUriForFile(MainActivity.this,"com.muskangoyal.CIFAR-Android-TF.fileprovider",photofile);
                takepic.putExtra(MediaStore.EXTRA_OUTPUT,photouri);
                startActivityForResult(takepic,1);
            }
        }
    }

    private File createPhotoFile(){
        String name= new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir= getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image =null;
        try{
            image=File.createTempFile(name,".jpg",storageDir);

        }catch(IOException e){
            Log.d("mylog","Excep:"+e.toString());

        }
        return image;
    }
}
