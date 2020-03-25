package com.example.glucosense2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Bilgilendirme extends AppCompatActivity {

    ImageView arrowsImage;
    Button nextBtn, finBtn;
    TextView bilgi1, bilgi2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bilgilendirme);

        arrowsImage = findViewById(R.id.arrows);
        nextBtn = findViewById(R.id.nextBtn);
        finBtn = findViewById(R.id.finBtn);
        bilgi1 = findViewById(R.id.bilgi1);
        bilgi2 = findViewById(R.id.bilgi2);
    }

    public void nextBtnClicked(View v){
        arrowsImage.setImageResource(R.drawable.arrow2);
        finBtn.setVisibility(View.VISIBLE);
        bilgi1.setVisibility(View.INVISIBLE);
        bilgi2.setVisibility(View.VISIBLE);
    }

    public void finBtnClicked(View v){
        this.finish();
    }

}
