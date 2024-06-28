package com.example.loving;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



//메인 액티비티 : 로그인 화면
//로그인 화면에서 회원가입 버튼 누르면 sign 액티비티로 넘어감.
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    // 이메일과 비밀번호
    private EditText Email;
    private EditText Password;
    private ImageButton buttonLogIn;
    private ImageButton buttonSignup;
    private static final String TAG = "Main_Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();


        Email = (EditText) findViewById(R.id.login_email);
        Password = (EditText) findViewById(R.id.login_password);


        buttonSignup = (ImageButton)findViewById(R.id.btn_signup);
        buttonLogIn = (ImageButton) findViewById(R.id.btn_login);

        //1. 로그인 버튼 눌렀을때
        //editText 로 받은 값 string 으로 변환해서 가져오기
        //2. 회원가입 버튼 눌렀을때 sign 액티비티로 연결
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, activity_sign.class);
                // 안적어도 되는데 걍 적음
                Log.d(TAG, "onClick:" + MainActivity.this);
                startActivity(intent);
            }

        });
        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Email.getText().toString().equals("") && !Password.getText().toString().equals("")) {
                    loginUser(Email.getText().toString(), Password.getText().toString());
                } else {    //토스트 메세지 사용
                    Toast.makeText(MainActivity.this, "계정과 비밀번호를 입력하세요.", Toast.LENGTH_LONG).show();
                }
            }
        });


        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    finish();
                } else {

                }
            }
        };
    }

    public void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                            firebaseAuth.addAuthStateListener(firebaseAuthListener);

                            Intent intent2 = new Intent(MainActivity.this, activity_bluetooth.class);
                            startActivity(intent2);

                        } else {
                            // 로그인 실패
                            Toast.makeText(MainActivity.this, "아이디 또는 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuthListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }
}

