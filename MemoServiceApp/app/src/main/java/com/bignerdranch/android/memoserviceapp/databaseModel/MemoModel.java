package com.bignerdranch.android.memoserviceapp.databaseModel;

import java.io.Serializable;


// 각 메모의 데이터를 저장하는 객체
public class MemoModel implements Serializable {
    public String imageUrl; // 이미지 위치
    public String title; // 제목
    public String content; // 내용
    public String imagename; // 이미지이름
    public String ModifyDate; // 수정시간
    public int Bookmark; //즐겨찾기
}
