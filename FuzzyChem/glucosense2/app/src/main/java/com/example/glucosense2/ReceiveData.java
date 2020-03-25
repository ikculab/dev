package com.example.glucosense2;

import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ReceiveData extends AppCompatActivity {

    Bitmap bmp;
    ImageView imView2;
    TextView tView2;
    String vals, name;
    ProgressBar pro_bar;
    private DatabaseReference rDatabase, sDatabase;
    public String TAG = "GLUCOSENSE";
    private Handler mHandler = new Handler();
    int progressStatus;
    float floatStatus;
    int stat = 0;
    float stat2 = 0;
    int checker1 = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        imView2 = findViewById(R.id.pro_image);
        tView2 = findViewById(R.id.writeData);

        pro_bar = findViewById(R.id.progres_barr);


        byte[] byteArray = getIntent().getByteArrayExtra("bitt");
        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imView2.setImageBitmap(bmp);

        rDatabase = FirebaseDatabase.getInstance().getReference().child("dataValues").child("1");

        new getVals().execute();
        //progressAnimator = ObjectAnimator.ofInt(pro_bar, "progress", 20, pro_bar.getProgress());
    }

    private class getVals extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                rDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            name = ds.getKey();
//                            Log.e("names: ", name);
                        }

                        sDatabase = rDatabase.child(name);
                        sDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                vals = Objects.requireNonNull(dataSnapshot.getValue(Float.class)).toString();
                                floatStatus = dataSnapshot.getValue(Float.class);
                                progressStatus = dataSnapshot.getValue(Integer.class);

                                new Thread(new Runnable() {
                                    public void run() {
                                        while (stat2 < floatStatus) {
                                            stat2 += 0.1;
                                            checker1 += 1;
                                            if (checker1 == 10){
                                                stat += 1;
                                                checker1 = 0;
                                            }
                                            mHandler.post(new Runnable() {
                                                public void run() {
                                                    pro_bar.setProgress(stat);
                                                    settingProgressColor();
                                                    if(floatStatus<1){
                                                        tView2.setText(stat2+"/"+pro_bar.getMax());
                                                    } else{
                                                        tView2.setText(stat+"/"+pro_bar.getMax());
                                                    }

                                                }
                                            });
                                            try {
                                                // Sleep for 200 milliseconds.
                                                Thread.sleep(5);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        if (stat==100){
                                            tView2.setText("100/"+pro_bar.getMax());
                                        } else {
                                            tView2.setText(vals+"/"+pro_bar.getMax());
                                        }

                                    }
                                }).start();

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w(TAG, "Failed to read value.", databaseError.toException());
                            }
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "Failed to read value.", databaseError.toException());
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    public void settingProgressColor(){

        if (stat>=80){
            pro_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(18, 38, 52)));
        }
        else if (stat>=72) {
            pro_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(16, 41, 63)));
        }
        else if (stat>=65) {
            pro_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(15, 44, 73)));
        }
        else if (stat>=50) {
            pro_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(17, 52, 81)));
        }
        else if (stat>=40) {
            pro_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(18, 60, 90)));
        }
        else if (stat>=30) {
            pro_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(20, 68, 97)));
        }
        else if (stat>=20) {
            pro_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(27, 77, 99)));
        }
        else if (stat>=10) {
            pro_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(43, 95, 105)));
        }
        else if (stat>=6) {
            pro_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(77, 115, 114)));
        }
        else if (stat>=2) {
            pro_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(125, 145, 130)));
        }
        else if (stat>=0.5) {
            pro_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(155, 154, 134)));
        }
        else {
            pro_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(158, 152, 131)));
        }
    }


}
