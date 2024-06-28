package com.example.loving;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class activity_report2 extends AppCompatActivity {
    private ImageButton endbtn;
    private TextView repname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report2);

        ImageView report2_1 = (ImageView)findViewById(R.id.report2_1);
        Glide.with(this).load(R.raw.report2_1).into(report2_1);

        endbtn = (ImageButton) findViewById(R.id.closebtn);
        repname = (TextView) findViewById(R.id.repname);

        Intent IntentfromCheck = getIntent();
        String name_ = IntentfromCheck.getStringExtra("name");

        repname.setText(name_+"의 최종결과");

        endbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_re = new Intent(activity_report2.this, activity_bluetooth.class);
                startActivity(intent_re);

            }
        });

    }
}