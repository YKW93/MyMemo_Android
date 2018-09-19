package com.bignerdranch.android.memoserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bignerdranch.android.memoserviceapp.databaseModel.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 전체 메모 보기
        RelativeLayout allMemoLayout = (RelativeLayout) findViewById(R.id.allMemoConfirm);
        // 메모 쓰기
        RelativeLayout memoWriteLayout = (RelativeLayout) findViewById(R.id.memoWrite);
        // 즐겨찾기 한 메모 보기
        RelativeLayout bookmarkLayout = (RelativeLayout) findViewById(R.id.bookmarkConfirm);
        // 로그아웃
        RelativeLayout logoutLayout = (RelativeLayout) findViewById(R.id.logout);

        allMemoLayout.setOnClickListener(onClickListener);
        memoWriteLayout.setOnClickListener(onClickListener);
        bookmarkLayout.setOnClickListener(onClickListener);
        logoutLayout.setOnClickListener(onClickListener);




    }

    // 각각 뷰마다의 버튼 처리
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.allMemoConfirm :
                    Intent intent = new Intent(MainActivity.this, AllMemosActivity.class);
                    startActivity(intent);
                    break;
                case R.id.memoWrite :
                    Intent intent2 = new Intent(MainActivity.this, MemoWriteActivity.class);
                    startActivity(intent2);
                    break;
                case R.id.bookmarkConfirm :
                    Intent intent3 = new Intent(MainActivity.this, BookmarkActivity.class);
                    startActivity(intent3);
                    break;
                case R.id.logout :
                    finish();
                    break;
            }
        }
    };





}
