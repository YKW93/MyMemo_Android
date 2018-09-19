package com.bignerdranch.android.memoserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.memoserviceapp.databaseModel.MemoModel;
import com.bignerdranch.android.memoserviceapp.databaseModel.UserModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.sackcentury.shinebuttonlib.ShineButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AllMemosActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView nameTextView, emailTextView;
    private FirebaseAuth mFirebaseAuth; // 사용자 정보에 관한 변수
    private FirebaseDatabase mFirebaseDatabase; // 데이터베이스 접근을 위한 변수
    private RecyclerView mRecyclerView; // 리사이클러뷰 변수
    private List<MemoModel> mMemoModels = new ArrayList<>(); // 메모객체 저장을 위한 변수
    private List<String> uidLists = new ArrayList<>(); // 각 메모마다 내용들이 데이터베이스에 저장되어있는데 이 메모내용들을 가리키는 uid값을 저장
    private FirebaseStorage mFirebaseStorage; // 저장소공간을 사용하기 위한 변수


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allmemos);


        mFirebaseAuth = FirebaseAuth.getInstance(); // mFirebaseAuth 는 현재 로그인한 사용자의 정보를 가지고있음.
        mFirebaseDatabase = FirebaseDatabase.getInstance(); // mFirebaseDatabase 는 현재 사용되고 있는 데이터베이스의 정보를 가지고 있음.
        mFirebaseStorage = FirebaseStorage.getInstance(); // mFirebaseStorage 는 현재 사용되고 있는 저장소의 정보를 가지고 있음.

        final String userUid= mFirebaseAuth.getCurrentUser().getUid(); // 현재 로그인한 사용자 uid 가져오기

        mRecyclerView = (RecyclerView)findViewById(R.id.recycleview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // 리사이클러뷰의 레이아웃타입을 기본으로 설정
        final RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(); // 리사이클러뷰 어댑터 객체 선언
        mRecyclerView.setAdapter(recyclerViewAdapter); // 리사이클러뷰 어댑터 적용

        // 데이터 베이스에 저장된 메모내용들을 가져오는 작업
        mFirebaseDatabase.getReference().child("users").child(userUid).child("memo").addValueEventListener( //addValueEventListener 이벤트는
                // 내가 설정한 경로의 전체 내용에 대한 변경사항을 읽고 항상 수신 대기 한다.
                new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { // 데이터가 변경 될때마다 호출되는 콜백메소드
                mMemoModels.clear(); // 객체 비우기
                uidLists.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 메모의 갯수만큼 반복문 호출
                    MemoModel user = snapshot.getValue(MemoModel.class); // 데이터베이스에 있는 각 메모(기록)들을 user변수에 저장
                    String uidKey = snapshot.getKey();
                    mMemoModels.add(user); // 선언해놓은 메모객체에 데이터베이스에서 가져온 데이터들 저장
                    uidLists.add(uidKey); // 각 메모의 uid 값 저장


                }
                recyclerViewAdapter.notifyDataSetChanged(); // 리사이클러뷰 갱신
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        // 툴바 사용 구문
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 플로팅 버튼 선언 및 클릭 리스너 처리
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(AllMemosActivity.this, MemoWriteActivity.class); // 메모(기록)하는 곳으로 이동
                startActivity(intent);
            }
        });

        // drawrlaytou(햄버거 버튼) 셋팅
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.getHeaderView(0);

        nameTextView = (TextView)view.findViewById(R.id.header_name_textView);
        emailTextView = (TextView)view.findViewById(R.id.header_email_textView);
        // 현재 로그인한 사용자의 uid값을 가져오기 위한 리스너
        mFirebaseDatabase.getReference().child("users").child(userUid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) { // 해당값 가져올 경우 콜백 메소드 호출됨.
                        UserModel user = dataSnapshot.getValue(UserModel.class);
                        nameTextView.setText(user.getUserName()); // 사용자 ID를 가져와서 UI에 뿌려준다.
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
        emailTextView.setText(mFirebaseAuth.getCurrentUser().getEmail()); // 사용자 이메일 ui로 보여줌


    }

    // Recyclerview 어탭터 (리사이클러뷰의 모든것을 관여함)
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { // 리스트에서 하나의 아이템을 그려줌(내가 선언한 xml로 그려줌)
            // xml 아이템 뷰 적용하는곳
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item,parent,false);
            return new CustomViewHolder(view);
        }

        // onCreateViewHolder 에서 그린 뷰에 접근해서 데이터를 삽입해서 ui에 보여준다.
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            final String userUid= mFirebaseAuth.getCurrentUser().getUid(); // 현재 로그인한 사용자 uid 가져오기

            ((CustomViewHolder)holder).titleTextView.setText(mMemoModels.get(position).title);
            ((CustomViewHolder)holder).contentTextView.setText(mMemoModels.get(position).content);
            Glide.with(holder.itemView.getContext()).load(mMemoModels.get(position).imageUrl).into(((CustomViewHolder)holder).imageView);
            ((CustomViewHolder)holder).dateTextView.setText("마지막 수정 날짜 : " + mMemoModels.get(position).ModifyDate);

            // 메모(데이터베이스에 저장되있는 메모값)에서 즐겨찾기 변수값 가져오기
            mFirebaseDatabase.getReference().child("users").child(userUid).child("memo").
                    child(uidLists.get(position)).addListenerForSingleValueEvent(new ValueEventListener() { //addListenerForSingleValueEvent 이벤트는 한번만 호출되고
                // 한번 로드된 후 자주 변경되지 않을 경우에 사용
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    MemoModel user = dataSnapshot.getValue(MemoModel.class);
                    if (user.Bookmark == 0) { // 사용자가 이전에 즐겨찾기 버튼을 안눌러놨을 경우
                        ((CustomViewHolder)holder).favoritButton.setChecked(false);
                    } else if (user.Bookmark == 1){ // 사용자가 이전에 즐겨찾기 버튼을 눌러놨을 경우
                        ((CustomViewHolder)holder).favoritButton.setChecked(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // 리스트아이템중 어떤 무언가를 클릭했을때.(즉 메모(한개)를 클릭했을 경우)
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MemoModel memo = mMemoModels.get(position);
                    String uidKey = uidLists.get(position);
                    Intent intent = new Intent(AllMemosActivity.this, MemoWriteActivity.class);
                    intent.putExtra("memoObject", memo); // 현재 클릭한 아이템(메모내용)의 정보를 넘겨주기위해 인텐트에 담음
                    intent.putExtra("uidKey", uidKey); // 현재 클릭한 아이템(메모객체)의 키값을 넘겨주기위해 인텐트에 담음.
                    startActivity(intent);

                }
            });

            //휴지통(삭제) 버튼 클릭했을 경우
            ((CustomViewHolder)holder).deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delete_Memo(position);
                }
            });

            //즐겨찾기 버튼 클릭했을 경우
            ((CustomViewHolder)holder).favoritButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    // 메모(데이터베이스에 저장되있는 메모값)에서 즐겨찾기 변수값 가져오기
                    mFirebaseDatabase.getReference().child("users").child(userUid).child("memo").
                            child(uidLists.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {  //addListenerForSingleValueEvent 이벤트는 한번만 호출되고
                        // 한번 로드된 후 자주 변경되지 않을 경우에 사용
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    MemoModel user = dataSnapshot.getValue(MemoModel.class);
                                    if (user.Bookmark == 0) { // 즐겨찾기 활성화(값을 1으로 변경)

                                        Map<String, Object> taskMap = new HashMap<>();
                                        taskMap.put("Bookmark", 1);
                                        // 데이터베이스로 내가 설정한 값을 보내 변경 시킨다.
                                        mFirebaseDatabase.getReference().child("users").child(userUid).child("memo").child(uidLists.get(position)).updateChildren(taskMap);
                                    } else if (user.Bookmark == 1){ // 즐겨찾기 비활성화(값을 0으로 변경)


                                        Map<String, Object> taskMap1 = new HashMap<>();
                                        taskMap1.put("Bookmark", 0);
                                        // 데이터베이스로 내가 설정한 값을 보내 변경 시킨다.
                                        mFirebaseDatabase.getReference().child("users").child(userUid).child("memo").child(uidLists.get(position)).updateChildren(taskMap1);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            }
                    );
                }
            });
        }

        // 총 아이템의 사이즈(여기에서는 메모의 총 개수)
        @Override
        public int getItemCount() {
            return mMemoModels.size();
        }

        // 내가 그린(xml) 리스트 하나의 아이템을 참조해서 초기화 시킨다.
        private class CustomViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView titleTextView;
            TextView contentTextView;
            ImageView deleteButton;
            TextView dateTextView;
            ShineButton favoritButton;

            public CustomViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.item_image);
                titleTextView = (TextView) view.findViewById(R.id.item_title);
                contentTextView = (TextView) view.findViewById(R.id.item_content);
                deleteButton = (ImageView) view.findViewById(R.id.item_delete);
                dateTextView = (TextView) view.findViewById(R.id.item_date);
                favoritButton = (ShineButton) view.findViewById(R.id.favorit_btn);
            }
        }
    }

    // 메모 삭제 메소드
    private void delete_Memo(int position) {
        //저장소에 있는 해당 메모사진 삭제
        mFirebaseStorage.getReference().child(mFirebaseAuth.getCurrentUser().getUid()).child(mMemoModels.get(position).imagename).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "저장소 삭제가 완료되었습니다." , Toast.LENGTH_SHORT).show();
                    }
                });
        // 데이터베이스에 있는 해당 메모 삭제
        mFirebaseDatabase.getReference().child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("memo").child(uidLists.get(position)).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "데이터 베이스 삭제가 완료되었습니다." , Toast.LENGTH_SHORT).show();
                    }
                });

    }



    @Override
    public void onBackPressed() { // 이전버튼클릭시 실행되는 콜백 메소드
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_write) { // 메모작성 화면 전환
            Intent intent = new Intent(AllMemosActivity.this, MemoWriteActivity.class);
            startActivity(intent);
        }

        else if (id == R.id.nav_logout) { // 로그아웃 할 경우
            mFirebaseAuth.signOut(); //로그인한 사용자 로그아웃. (mFirebaseAuth 는 현재 로그인한 사용자의 정보를 가지고있음.)
            finish();
            Intent intent = new Intent(AllMemosActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START); //drawablelayout 화면 닫기
        return true;
    }

}

