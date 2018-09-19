package com.bignerdranch.android.memoserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.memoserviceapp.databaseModel.MemoModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;


public class BookmarkActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth; // 사용자 인증(정보)에 필요한 변수
    private FirebaseDatabase mFirebaseDatabase; // 데이터베이스에 접근하기위해 필요한 변수
    private FirebaseStorage mFirebaseStorage; // 저장소에 접근하기 위해 필요한 변수
    private RecyclerView mRecyclerView; // 리사이클러뷰 변수


    private List<MemoModel> mMemoModels = new ArrayList<>(); // 메모를 저장하기 위한 임시변수
    private List<String> uidLists = new ArrayList<>(); // 각 메모마다 내용들이 데이터베이스에 저장되어있는데 이 메모내용들을 가리키는 uid값을 저장


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        mFirebaseAuth = FirebaseAuth.getInstance(); // mFirebaseAuth 는 현재 로그인한 사용자의 정보를 가지고있음.
        mFirebaseDatabase = FirebaseDatabase.getInstance(); // mFirebaseDatabase 는 현재 사용되고 있는 데이터베이스의 정보를 가지고 있음.
        mFirebaseStorage = FirebaseStorage.getInstance(); // mFirebaseStorage 는 현재 사용되고 있는 저장소의 정보를 가지고 있음.

        mRecyclerView = (RecyclerView)findViewById(R.id.recycleview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // 리사이클러뷰의 레이아웃타입을 기본으로 설정
        final RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(); // 리사이클러뷰 어댑터 객체 선언
        mRecyclerView.setAdapter(recyclerViewAdapter); // 리사이클러뷰 어댑터 적용


        final String userUid= mFirebaseAuth.getCurrentUser().getUid(); // 현재 로그인한 사용자 uid 가져오기

        // 데이터 베이스에 저장된 메모내용들을 가져오는 작업
        mFirebaseDatabase.getReference().child("users").child(userUid).child("memo").addValueEventListener( //addValueEventListener 이벤트는
                // 내가 설정한 경로의 전체 내용에 대한 변경사항을 읽고 항상 수신 대기 한다.
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mMemoModels.clear(); // 객체 비우기
                        uidLists.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) { // 사용자의 총 메모의 개수만큼 반복문
                            MemoModel user = snapshot.getValue(MemoModel.class);

                            if (user.Bookmark == 1) { // 즐겨찾기가 된 메모인지 검사하는 조건문
                                String uidKey = snapshot.getKey();
                                mMemoModels.add(user);
                                uidLists.add(uidKey); // 각 메모의 uid 값 저장
                            }
                        }
                        recyclerViewAdapter.notifyDataSetChanged(); // 리사이클러뷰 갱신

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { // 실패했을 경우 호출되는 콜백 메소드

                    }
                });
    }

    // 리사이클러뷰에 관한 모든 것(어댑터)
    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        //내가 그려준 리스트 아이템을 화면에 그려줌 (이때 CustomViewHolder를 참조해서 그림)
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item2, parent, false);
            return new CustomViewHolder(view);
        }

        // 리스트 아이템에 있는 뷰에 값들을 삽입
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            ((CustomViewHolder)holder).titleTextView.setText(mMemoModels.get(position).title);
            ((CustomViewHolder)holder).contentTextView.setText(mMemoModels.get(position).content);
            Glide.with(holder.itemView.getContext()).load(mMemoModels.get(position).imageUrl).into(((CustomViewHolder)holder).imageView);
            ((CustomViewHolder)holder).dateTextView.setText("마지막 수정 날짜 : " + mMemoModels.get(position).ModifyDate);


            // 리스트아이템중 어떤 무언가를 클릭했을때.(즉 메모(한개)를 클릭했을 경우)
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MemoModel memo = mMemoModels.get(position);
                    String uidKey = uidLists.get(position);
                    Intent intent = new Intent(BookmarkActivity.this, MemoWriteActivity.class);
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
        }


        @Override
        public int getItemCount() {
            return mMemoModels.size();
        }
    }

    // onCreateViewHolder 에서 그려논 xml 값에 있는 뷰들을 가져와서 초기화
    private class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView contentTextView;
        ImageView deleteButton;
        TextView dateTextView;

        public CustomViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.item_image);
            titleTextView = (TextView) view.findViewById(R.id.item_title);
            contentTextView = (TextView) view.findViewById(R.id.item_content);
            deleteButton = (ImageView) view.findViewById(R.id.item_delete);
            dateTextView = (TextView) view.findViewById(R.id.item_date);
        }
    }

    private void delete_Memo(int position) { // 메모삭제 메소드
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
}
