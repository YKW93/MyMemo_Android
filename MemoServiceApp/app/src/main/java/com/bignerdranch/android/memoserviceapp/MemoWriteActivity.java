package com.bignerdranch.android.memoserviceapp;

import android.Manifest;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bignerdranch.android.memoserviceapp.databaseModel.MemoModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.roger.catloadinglibrary.CatLoadingView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class MemoWriteActivity extends AppCompatActivity {

    private static final int GALLERY_CODE = 10; // 갤러리 확인 값
    private CatLoadingView mView; // 다이얼로그 로딩 뷰 변수
    private FirebaseStorage mFirebaseStorage; // 저장소 접근 변수
    private FirebaseAuth mFirebaseAuth; // 사용자 인증(정보) 접근 변수
    private FirebaseDatabase mFirebaseDatabase; // 데이터베이스 접근 변수
    private ImageView photoImageView;
    private EditText titleEditText, contentTextView;
    private String imagePath; // 이미지 경로
    private int state = 0; // 추가인지 수정인지 알기위한 변수
    private String uidKey; // 각 메모내용마다의 uid key값
    private Map<String, Object> childUpdate = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);

        mView = new CatLoadingView(); // 다이얼로그 로딩 뷰 초기화

        photoImageView = (ImageView) findViewById(R.id.photo_ImageView); // 사진이 들어가는 이미지뷰 초기화
        titleEditText = (EditText) findViewById(R.id.memoActivity_editText_title); // 제목 텍스트뷰 초기화
        contentTextView = (EditText) findViewById(R.id.memoActivity_editText_content); // 내용 텍스트뷰 초기화

        mFirebaseStorage = FirebaseStorage.getInstance(); // 저장소 인스턴스값 가져오기
        mFirebaseAuth = FirebaseAuth.getInstance(); // 사용자 인증(정보) 인스턴스값 가져오기
        mFirebaseDatabase = FirebaseDatabase.getInstance(); // 데이터베이스 인스턴스 값 가져오기

        /*권한*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }

        // 메모 쓰기가 아닌 수정일 경우에 기존 메모정보 가져오기
        Intent intent = getIntent();
        MemoModel memo = (MemoModel) intent.getSerializableExtra("memoObject");

        if (memo != null) {
            uidKey = intent.getExtras().getString("uidKey"); // 리스트에서 아이템을 클릭했을때 그해당 아이템(메모내용) 키값을 가져온다.
            titleEditText.setText(memo.title);
            contentTextView.setText(memo.content);
            imagePath = memo.imageUrl;
            Glide.with(getApplicationContext()).load(imagePath).into(photoImageView);
            state = 1;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save: // 저장버튼 클릭했을 경우
                upload(imagePath); // 데이터베이스에 메모 내용들을 저장 , 수정 하는 메소드
                return true;
            case R.id.action_photo: //갤러리에서 사진 불러오기
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);  // 갤러리 실행
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 액션바 그려주기
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.memo_toolbar, menu);
        return true;
    }

    // 갤러리창에서 해당 액티비티 화면으로 돌아왔을 경우 호출되는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode ==  RESULT_OK) {
            if (requestCode == GALLERY_CODE) { // 이미지를 가져왔을 경우
//            System.out.println(data.getData());
//            System.out.println(getPath(data.getData()));
                imagePath = getPath(data.getData()); // 내가 선택한 이미지 경로를 가져옴
                File file = new File(imagePath);
//            photoImageView.setImageURI(Uri.fromFile(file)); //이미지뷰에 내가 갤러리에서 선택한 이미지를 올림
                Glide.with(getApplicationContext()).load(imagePath).into(photoImageView);
            }
        } else { // 사용자가 갤러리에서 이미지를 안가져왔을 경우
            Toasty.error(MemoWriteActivity.this, "선택된 이미지가 없습니다.", Toast.LENGTH_SHORT).show();

        }

    }

    // 이미지 경로를 가져오기 위한 함수
    // 원래 onActivityResult 에서 data.getData() 해서 가져올수 있어야되는데 구글에서 지원을 안해줌.. 그래서 따로 만들어줘야됨.
    // data.getData() 로 가져오는 경로는 파일을 가져올수가 없고 getPath(data.getData()) 로 가져오는 경로는 파일을 가져올 수 있음.
    // System.out.prinln() 을 통해서 두개의 차이를 확인해보면 쉽게 알 수 있음.
    public String getPath(Uri uri) {
        String [] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this,uri,proj,null,null,null);

        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();
        return cursor.getString(index);
    }

    // 데이터베이스에 해당 메모 정보 업로드
    private void upload(String uri) {


        //현재 시간 가져오기
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String formatDate = df.format(c.getTime());

        // 내 저장소 위치지정
        StorageReference storageRef = mFirebaseStorage.getReferenceFromUrl("gs://memoserviceapp.appspot.com");
        if (uri != null) {

            //이미지 파일 경로
            final Uri file = Uri.fromFile(new File(uri));
            // 사용자마다 가지고 있는 uid를 통해서 폴더 생성후 그뒤에 파일 추가
            StorageReference riversRef = storageRef.child(mFirebaseAuth.getCurrentUser().getUid()+"/"+file.getLastPathSegment());
            // 파일 업로드
            UploadTask uploadTask = riversRef.putFile(file);

            mView.show(getSupportFragmentManager(), "");
            mView.setCancelable(false);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) { // 업로드 실패 했을때 (여기서 업로드 실패란 즉 사진을 업데이트안하고 글씨만 수정했을때 작업할수있음.)
                    childUpdate.put("users/"+mFirebaseAuth.getCurrentUser().getUid()+"/memo/"+uidKey+"/title", titleEditText.getText().toString());
                    childUpdate.put("users/"+mFirebaseAuth.getCurrentUser().getUid()+"/memo/"+uidKey+"/content", contentTextView.getText().toString());
                    childUpdate.put("users/"+mFirebaseAuth.getCurrentUser().getUid()+"/memo/"+uidKey+"/ModifyDate", formatDate);
                    mFirebaseDatabase.getReference().updateChildren(childUpdate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
//                                    Toasty.success(MemoWriteActivity.this, "성공1", Toast.LENGTH_SHORT).show();

                                    MemoWriteActivity.this.finish();
                                }
                            });
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { //업로드 성공 했을때
//                    Toast.makeText(getApplicationContext(), "업로드 성공", Toast.LENGTH_SHORT).show();
                    Uri downloadUrl = taskSnapshot.getDownloadUrl(); //사진 저장 경로
                    if (state == 0) { // 리스트에 메모를 추가할때
//                    Toast.makeText(getApplicationContext(), downloadUrl.toString(), Toast.LENGTH_SHORT).show();
                        MemoModel memo = new MemoModel(); // 메모 모델 객체 초기화
                        // 메모 객체에 데이터 셋팅
                        memo.imageUrl = downloadUrl.toString();
                        memo.title = titleEditText.getText().toString();
                        memo.content = contentTextView.getText().toString();
                        memo.imagename = file.getLastPathSegment();
                        memo.ModifyDate = formatDate;
                        memo.Bookmark = 0;
                        mFirebaseDatabase.getReference().child("users").child(mFirebaseAuth.getCurrentUser().getUid()).child("memo").push().setValue(memo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() { // 데이터베이스에 데이터 셋팅을 위한 구문
                                    @Override
                                    public void onSuccess(Void aVoid) { // 데이터베이스에 데이터 셋팅이 성공했을 경우
//                                        Toasty.success(MemoWriteActivity.this, "성공2", Toast.LENGTH_SHORT).show();

                                        MemoWriteActivity.this.finish(); // 해당 액티비티 종료
                                        mView.dismiss(); // 다이얼로그 로딩 뷰 종료
                                    }
                                });
                    } else { // 메모 수정할 경우의 조건문
                        // 사용자가 변경한 데이터를 데이터베이스에 업데이트
                        childUpdate.put("users/"+mFirebaseAuth.getCurrentUser().getUid()+"/memo/"+uidKey+"/imageUrl", downloadUrl.toString());
                        childUpdate.put("users/"+mFirebaseAuth.getCurrentUser().getUid()+"/memo/"+uidKey+"/title", titleEditText.getText().toString());
                        childUpdate.put("users/"+mFirebaseAuth.getCurrentUser().getUid()+"/memo/"+uidKey+"/content", contentTextView.getText().toString());
                        childUpdate.put("users/"+mFirebaseAuth.getCurrentUser().getUid()+"/memo/"+uidKey+"/ModifyDate", formatDate);
                        mFirebaseDatabase.getReference().updateChildren(childUpdate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
//                                        Toasty.success(MemoWriteActivity.this, "성공3", Toast.LENGTH_SHORT).show();

                                        MemoWriteActivity.this.finish();
                                    }
                                });
                    }

                }
            });

        } else { // 이미지를 선택하지 않고 저장버튼을 눌렀을 경우
            Toasty.error(MemoWriteActivity.this, "이미지를 추가해주세요.", Toast.LENGTH_SHORT).show();
        }
    }
}
