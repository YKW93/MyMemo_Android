package com.bignerdranch.android.memoserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final long FINISH_INTERVAL_TIME = 2000; // 시간계산을 위한 변수
    private long backPressedTime = 0;
    private Button loginBtn;
    private Button signupBtn;
    private EditText emailEdit, pwEdit;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener; //로그인 인터페이스 리스너
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginBtn = (Button) findViewById(R.id.login_Button);
        signupBtn = (Button) findViewById(R.id.signup_Button);
        emailEdit = (EditText) findViewById(R.id.email_EditText);
        pwEdit = (EditText) findViewById(R.id.pw_EditText);

        mFirebaseAuth = FirebaseAuth.getInstance(); // 사용자 인증 인스턴스값을 가져옴
        mFirebaseAuth.signOut(); // 로그아웃

        signupBtn.setOnClickListener(this);

        //회원가입 버튼 클릭했을 경우
        signupBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)); // 회원가입 페이지로 이동
            }
        });

        //로그인 버튼 클릭했을 경우
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //이메일 또는 비밀번호를 안누르고 로그인 할경우
                if (emailEdit.getText().toString().isEmpty() || pwEdit.getText().toString().isEmpty()) {
//                    Toast.makeText(LoginActivity.this, "이메일 또는 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    Toasty.error(getApplicationContext(), "이메일 또는 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT, true).show();
                } else {
                    loginEvent(); //로그인 이벤트를 처리하는 곳
                }
            }
        });


        mAuthStateListener = new FirebaseAuth.AuthStateListener() { // 로그인 인터페이스 리스너
            // 로그인이 되었거나 로그아웃이 되었을때(상태가 변할 때마다) 호출되는 콜백메소드
            // 즉 loginEvent() 에서 아무일 없이 끝났을 경우 로그인 이벤트가 발생하고 해당 콜벡메소드로 처리
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mFirebaseAuth.getCurrentUser(); //현재 로그인한 유저
                if (user != null) { // 로그인 된 상태
//                    Toast.makeText(LoginActivity.this, "로그인 완료", Toast.LENGTH_SHORT).show();
                    Toasty.success(getApplicationContext(), "로그인 완료", Toast.LENGTH_SHORT,true).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else { // 로그아웃 된 상태
//                    Toast.makeText(LoginActivity.this, "로그아웃 완료", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    void loginEvent() { // 입력한 회원정보가 서버에 있는지 확인하기 위해 선언한 메소드
        mFirebaseAuth.signInWithEmailAndPassword(emailEdit.getText().toString(), pwEdit.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() { // 계정이 현재 존재하는지 확인하는 리스너
                    @Override // 계정이 존재하거나 존재하지 않을때 호출되는 콜백 메소드
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()) { //실패 했을때(계정이 존재하지 않을때)
//                            Toast.makeText(LoginActivity.this, "로그인 정보가 없습니다." ,Toast.LENGTH_SHORT).show();
                            Toasty.error(getApplicationContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT, true).show();
                        }
                    }
                });
    }
    @Override
    public void onStart() { // 액티비티가 화면에 뿌려지기 직전에 로그인 인터페이스 리스너를 붙여준다.
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() { // 현재 액티비티가 다른 액티비티에 가려졌을때 로그인 인터페이스 리스너를 빼준다.
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onRestart() { // 액티비티가 다시 재호출 됬을 경우
        super.onRestart();
        mFirebaseAuth.signOut();

    }

    @Override
    public void onBackPressed() { // 휴대폰에서 이전 버튼 클릭 했을 경우
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) // 2초안에 뒤로가기키를 한번더 누르면 앱종료
        {
            finishAffinity(); // 현재 액티비티 종료가 아닌 모든 액티비티를 종료시킬때 사용
        }
        else // 경고표시
        {
            backPressedTime = tempTime;
            Toasty.warning(getApplicationContext(),"뒤로가기를 한 번 더 누르면" + "\n" + "앱이 종료됩니다.", Toast.LENGTH_LONG).show();
        }

    }

    private void ShowToase(String text)
    {
        Toast toast = Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onClick(View view)
    {
        //연타방지

        switch (view.getId())
        {
            case R.id.signup_Button:
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                break;

                default:
                    break;

        }

    }

}
