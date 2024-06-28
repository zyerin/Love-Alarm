package com.example.loving;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;



import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class activity_sign extends AppCompatActivity {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = database.getReference();
    private FirebaseAuth firebaseAuth; //파이어베이스 계정에 대한 정보를 가지고 있는 객체 선언
    private EditText edit_email;
    private EditText edit_pwd;
    private EditText edit_name;
    private ImageButton buttonJoin;
    private static final String TAG = "activity_sign";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        edit_email = (EditText) findViewById(R.id.sign_email);
        edit_pwd = (EditText) findViewById(R.id.sign_passWord);
        edit_name = (EditText) findViewById(R.id.sign_name);

        final String email = edit_email.getText().toString().trim();
        final String password = edit_pwd.getText().toString().trim();
        final String name = edit_name.getText().toString().trim();

        //trim > 공백인 부분 제거하고 보여주기

        firebaseAuth = FirebaseAuth.getInstance();  //파이어베이스 접근 권한 갖기


        // 가입 버튼 누르면 신규계정 등록하기

        buttonJoin = (ImageButton) findViewById(R.id.btn_join);
        buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edit_email.getText().toString().equals("") && !edit_pwd.getText().toString().equals("")) {
                    //이메일과 비밀번호가 공백이 아닌경우
                    createUser(edit_email.getText().toString(), edit_pwd.getText().toString(), edit_name.getText().toString());
                } else {
                    //이메일과 비밀번호가 공백인 경우
                    Toast.makeText(activity_sign.this, "계정과 비밀번호를 입력하세요.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void createUser(String email, String password, String name) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 회원가입 성공시
                            table table = new table(name);
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = user.getUid();
                            databaseReference.child("zoo").child(uid).setValue(table);
                            Toast.makeText(activity_sign.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                            finish();

                        } else {
                            // 계정이 중복된 경우
                            Toast.makeText(activity_sign.this, "이미 존재하는 계정입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}


