package com.example.loving;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class activity_report1 extends AppCompatActivity {
    private ImageButton endbtn;
    private TextView counting;
    private TextView repname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report1);

        ImageView report1 = (ImageView)findViewById(R.id.report1);
        Glide.with(this).load(R.raw.report1_2).into(report1);

        endbtn = (ImageButton) findViewById(R.id.closebtn);
        repname = (TextView) findViewById(R.id.repname);
        counting = (TextView) findViewById(R.id.counting);

        Intent IntentfromCheck = getIntent();
        Integer count = IntentfromCheck.getIntExtra("count", 0);
        String name_ = IntentfromCheck.getStringExtra("name");

        repname.setText(name_+"의 최종결과");
        counting.setText("당신이 설렌 횟수는 "+count+"회 입니다.");

        endbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_re = new Intent(activity_report1.this, activity_bluetooth.class);
                startActivity(intent_re);

            }
        });

    }
}