package com.example.glucosense2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    ImageView imageView, firstView;
    CropImageView cropImageView;
    Bitmap bitmap;
    Uri imageUri;
    android.graphics.Matrix matrix = new android.graphics.Matrix();
    Float scale = 1f;
    ScaleGestureDetector SGD;

    private static final int PICK_IMAGE = 1;
    Button btnCrop, btnSave, btnSend;
    FloatingActionButton fab_camera, fab_gallery;
    int a, b, t;
    float[][][][] input;
    List<Float> labels;
    List<String> RGBvalues;
    private DatabaseReference mDatabase;
    private static final String TAG = "Anasayfa";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab_camera = findViewById(R.id.fab_camera);
        fab_gallery = findViewById(R.id.fab_gallery);

        imageView = findViewById(R.id.imageView);
        cropImageView = findViewById(R.id.cropImageView);
        cropImageView.setFixedAspectRatio(false);

        firstView = findViewById(R.id.firstView);

        btnCrop = findViewById(R.id.btnCrop);
        btnCrop.setVisibility(View.INVISIBLE);

        btnSave = findViewById(R.id.btnSave);
        btnSave.setVisibility(View.INVISIBLE);

        btnSend = findViewById(R.id.btnSend);
        btnSend.setVisibility(View.INVISIBLE);


        labels = new ArrayList<>();
        RGBvalues = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("rgb11values22");

        SGD = new ScaleGestureDetector(this,new ScaleListener());
        bilgilendirme();
    }

    public void bilgilendirme(){
        Intent bilgInt = new Intent(MainActivity.this, Bilgilendirme.class);
        startActivity(bilgInt);
    }

    public void btnGalleryClicked(View v){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        imageView.setVisibility(View.VISIBLE);
    }
    public void btnCameraClicked(View v){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
        imageView.setVisibility(View.VISIBLE);
    }
    public void btnCropClicked(View v){
        btnSend.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        cropImageView.setVisibility(View.VISIBLE);
        firstCrop();
    }
    public void btnSaveClicked(View v){
        Save();
        btnSave.setVisibility(View.INVISIBLE);
        btnSend.setVisibility(View.VISIBLE);
        btnCrop.setVisibility(View.VISIBLE);
    }
    public void btnSendClicked(View v){Send();}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==0 && resultCode == RESULT_OK){
            bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            btnCrop.setVisibility(View.VISIBLE);
            a=5;
            btnSend.setVisibility(View.VISIBLE);
            firstView.setVisibility(View.INVISIBLE);
        }

        if (requestCode==PICK_IMAGE && resultCode == RESULT_OK){
            imageUri = data.getData();
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                imageView.setImageBitmap(bitmap);
                btnCrop.setVisibility(View.VISIBLE);
                b=5;
                btnSend.setVisibility(View.VISIBLE);
                firstView.setVisibility(View.INVISIBLE);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void firstCrop(){
        imageView.setDrawingCacheEnabled(true);
        imageView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        imageView.layout(0, 0, imageView.getMeasuredWidth(), imageView.getMeasuredHeight());
        imageView.buildDrawingCache(true);

        try {
            // Bitmap imageBitmap = Bitmap.createBitmap(imageView.getDrawingCache());
            imageView.setDrawingCacheEnabled(false);
            cropImageView.setImageBitmap(bitmap);
            btnSave.setVisibility(View.VISIBLE);
            btnCrop.setVisibility(View.INVISIBLE);
        } catch (Exception e){
            Toast.makeText(this, "Image is too large or corrupted!", Toast.LENGTH_LONG).show();
        }
    }

    public void Save(){

        bitmap = cropImageView.getCroppedImage();
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
        cropImageView.setVisibility(View.INVISIBLE);

    }

    public void Send(){
        /* rgb values will be send to firebase in here */
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();

        getRGB(bitmap);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);

        Log.e("rgb values: ", String.valueOf(labels));

        mDatabase.setValue(labels).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "RGB values are sent.", Toast.LENGTH_LONG).show();
                }
            }
        });

        byte[] byteArray = bStream.toByteArray();

        Intent intentt = new Intent(MainActivity.this, ReceiveData.class);
        intentt.putExtra("bitt", byteArray);
        startActivity(intentt);

    }

    public void getRGB(Bitmap bitt){
        int w = bitt.getWidth();
        int h = bitt.getHeight();
        float r = 0;
        float g = 0;
        float b = 0;

        input = new float[1][w][h][3];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int pixel = bitt.getPixel(x, y);

                input[0][x][y][0] = Color.red(pixel);
                r = r + input[0][x][y][0];

                input[0][x][y][1] = Color.green(pixel);
                g = g + input[0][x][y][1];

                input[0][x][y][2] = Color.blue(pixel);
                b = b + input[0][x][y][2];
            }
        }
        r = r / (w * h);
        g = g / (w * h);
        b = b / (w * h);

        labels = Arrays.asList(r, g, b);
        RGBvalues = Arrays.asList(String.valueOf(r), String.valueOf(g), String.valueOf(b));
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale = scale * detector.getScaleFactor();
            scale = Math.max(0.1f,Math.min(scale,5f));
            matrix.setScale(scale, scale);
            imageView.setImageMatrix(matrix);
            return true;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        SGD.onTouchEvent(event);
        return true;
    }

}
