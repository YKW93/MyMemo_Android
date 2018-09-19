package com.bignerdranch.android.memoserviceapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bignerdranch.android.memoserviceapp.databaseModel.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;

public class SignupActivity extends AppCompatActivity {

    private EditText email_edit, pw_edit, name_edit;
    private Button signup_btn;
    private FirebaseDatabase mFirebaseDatabase; // 데이터베이스 접근 변수
    private FirebaseAuth mFirebaseAuth; // 사용자인증(정보) 접근 변수
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 각각의 데이터 초기화 및 인스턴스 얻어오기
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        email_edit = (EditText) findViewById(R.id.signupActivity_editText_email);
        pw_edit = (EditText) findViewById(R.id.signupActivity_editText_password);
        name_edit = (EditText) findViewById(R.id.signupActivity_editText_name);
        signup_btn = (Button) findViewById(R.id.signupActivity_Button_signup);

        // 회원가입하기 버튼을 클릭했을 경우 -> firebase에 사용자 정보 저장
        signup_btn.setOnClickListener(new View.OnClickListener() { //회원가입 버튼 눌렀을 경우
            @Override
            public void onClick(View view) {
                // 사용자가 입력한 정보값 가져오기
                String email = email_edit.getText().toString();
                String name = name_edit.getText().toString();
                String pw = pw_edit.getText().toString();
                // 이메일,패스워드,이름중 하나라도 입력안한 경우
                if (email.getBytes().length <= 0 || name.getBytes().length <= 0 || pw.getBytes().length <= 0) {
                    Toast.makeText(SignupActivity.this, "  이메일 또는 이름 또는 " + "\n" + "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else { // 모두다 입력 했을 경우
                    FirebaseAuth.getInstance() // 계정이 현재 존재하는지 위한 구문 , 계정이 존재할 경우 리스너가 해당 콜백메소드를 호출
                            .signInWithEmailAndPassword(email_edit.getText().toString(), pw_edit.getText().toString())
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override //계정이 존재하거나 존재하지 않을때 해당 콜백 메소드 호출
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) { // 입력한 이메일이 사용이 가능할 경우(즉 서버에 계정이 존재하지않을때)
                                        Toast.makeText(SignupActivity.this, "현재 입력하신 이메일은 사용 가능합니다.", Toast.LENGTH_SHORT).show();
                                        FirebaseAuth.getInstance() // 계정을 생성하기 위한 구문 , 생성이 완료되면 리스너가 해당 콜백메소드를 호출
                                                .createUserWithEmailAndPassword(email_edit.getText().toString(), pw_edit.getText().toString())
                                                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        Toasty.success(getApplicationContext(), "계정 생성이 완료 되었습니다.", Toast.LENGTH_LONG, true).show();
                                                        UserModel userModel = new UserModel();
                                                        userModel.setUserName(name_edit.getText().toString());
                                                        userModel.setUid(task.getResult().getUser().getUid());
                                                        mFirebaseDatabase.getReference().child("users").child(userModel.getUid()).setValue(userModel)
                                                                // 데이터베이스에 데이터가 다들어갔을때 반응하는 리스너
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        SignupActivity.this.finish();
                                                                    }
                                                                });

                                                    }
                                                });
                                    } else { // 입력한 이메일이 사용 불가능 할 경우(즉 서버에 계정이 존재할 때)
                                        Toast.makeText(SignupActivity.this, "현재 입력하신 이메일은 사용중 입니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

            }

        });
    }


}
