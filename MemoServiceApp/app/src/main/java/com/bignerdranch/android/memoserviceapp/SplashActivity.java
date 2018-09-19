package com.bignerdranch.android.memoserviceapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class SplashActivity extends AppCompatActivity {

    private LinearLayout mLinearLayout;
    private FirebaseRemoteConfig mFirebaseRemoteConfig; // 앱 제어를 위한 필요 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 휴대폰 최 상단에 있는 window 창 제거
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mLinearLayout = (LinearLayout)findViewById(R.id.activity_splash);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        /* Firebase와 RemoteConfig를 하기위한 작업. (운영자가 접근 통제하기 위한 구문) */
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.xml);

        mFirebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            mFirebaseRemoteConfig.activateFetched();
                        } else {

                        }
                        displayMessage();
                    }
                });

    }
    void displayMessage() { // 운영자의 메시지를 보여주게 하기 위한 메소드
        String splash_background = mFirebaseRemoteConfig.getString("splash_background");
        Boolean caps = mFirebaseRemoteConfig.getBoolean("splash_message_caps");
        String splash_message = mFirebaseRemoteConfig.getString("splash_message");

        mLinearLayout.setBackgroundColor(Color.parseColor(splash_background));

        if (caps) { // Firebase에서 설정한 경고문자가 있을 경우 앱을 실행하지않고 경고 메시지를 다이얼로그로 띄움
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(splash_message).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            builder.create().show();
        } else {
            startActivity(new Intent(this, LoginActivity.class)); //Firebase에서 경고문자가 없을 경우 다음 액티비티 시작
            finish();
        }
    }
}
