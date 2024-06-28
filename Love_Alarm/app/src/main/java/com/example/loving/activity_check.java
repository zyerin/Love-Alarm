package com.example.loving;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class activity_check extends AppCompatActivity {
    private ImageButton clickbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);


        Intent IntentfromBT = getIntent();
        Integer count = IntentfromBT.getIntExtra("count", 0);
        String name_ = IntentfromBT.getStringExtra("name");


        clickbtn = (ImageButton) findViewById(R.id.clickbtn);
        clickbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count>0) {
                    Intent intent_rep1 = new Intent(activity_check.this, activity_report1.class);
                    intent_rep1.putExtra("count", count);
                    intent_rep1.putExtra("name",name_);
                    startActivity(intent_rep1);
                }else{
                    Intent intent_rep2 = new Intent(activity_check.this, activity_report2.class);
                    intent_rep2.putExtra("name",name_);
                    startActivity(intent_rep2);
                }
            }

        });
    }
}