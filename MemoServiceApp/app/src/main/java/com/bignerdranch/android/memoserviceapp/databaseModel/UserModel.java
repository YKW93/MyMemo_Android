package com.bignerdranch.android.memoserviceapp.databaseModel;


// 사용자들의 정보를 저장하는 객체
public class UserModel {
    private String userName; // 사용자 이름
    private String uid; // 사용자 uid(고유) 값

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
