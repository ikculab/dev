package com.example.tflitetry;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView result_tv;
    Button btnCamera, btnGallery, btnClassify;

    private static final int PICK_IMAGE = 1;
    Uri imageUri;
    Bitmap bitmap, outBitmap;
    private static final String modelFile = "model.tflite";
    public Interpreter tflite;
    Interpreter.Options opt = new Interpreter.Options();
    float[][][][] input;
    float r, g, b;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamera = findViewById(R.id.btnCamera);
        imageView = findViewById(R.id.imageView);
        btnGallery = findViewById(R.id.btnGallery);
        btnClassify = findViewById(R.id.btnClassify);
        result_tv = findViewById(R.id.result_tv);


        try {
            opt.setUseNNAPI(true);
            tflite = new Interpreter(loadModelFile(), opt); // load tflite model
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void btnClassifyClicked(View v){
		// since we have 10 classes in our model, our output will be [1,10] array
        float[][] out = new float[1][10];

        bitmapToInputArray(); // converting bitmap to array

        tflite.run(input, out);

		// adding threshold to output, true predicted will be 1 and the others will be 0
        for(int i = 0; i < 10; i++) {
            Log.e("number", String.valueOf(out[0][i]));
            if (out[0][i] <= 0.5) {
                out[0][i] = 0;
            } else {
                out[0][i] = 1;
            }
        }
		
		// in order to take the label from labels list, we are taking the index of the predicted class' label
        int c =0;
        for(int s=0; s<10; s++){
            if(out[0][s]==0){
                c = c + 1;
            }
            else{
                break;
            }
        }
        List<String> labels = Arrays.asList("T-shirt/top", "Trouser!", "Pullover", "Dress", "Coat", "Sandal", "Shirt", "Sneaker", "Bag", "Ankle boot");

		// writing the label onto textview
        result_tv.setText(labels.get(c));
    }

	// we will convert the bitmap into 1 * w * h * 1 array (since we are dealing with gray images pixel size is 1) 
    private void bitmapToInputArray() {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        input = new float[1][w][h][1];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = bitmap.getPixel(y, x);
				
                r = Color.red(pixel);;
                g = Color.green(pixel);
                b = Color.blue(pixel);
				
                input[0][x][y][0] = ((r * 0.3f) + (g * 0.59f) + (b * 0.11f)) ;
            }
        }
    }
	

    public void btnGalleryClicked(View v){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }
    public void btnCameraClicked(View v){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }

	// this part is for loading tflie model
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==0 && resultCode == RESULT_OK){
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }

        if (requestCode==PICK_IMAGE && resultCode == RESULT_OK){
            assert data != null;
            imageUri = data.getData();
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
