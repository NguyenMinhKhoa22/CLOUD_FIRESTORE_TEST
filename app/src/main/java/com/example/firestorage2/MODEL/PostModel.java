package com.example.firestorage2.MODEL;

public class PostModel extends com.example.firestorage2.MODEL.PostId {
    private String eTitle, eDescription, eImgPost, eTime, eUser;

    public PostModel() {}

    public PostModel(String eTitle, String eDescription, String eImgPost, String eTime, String eUser) {
        this.eTitle = eTitle;
        this.eDescription = eDescription;
        this.eImgPost = eImgPost;
        this.eTime = eTime;
        this.eUser = eUser;
    }

    public String geteTitle() {
        return eTitle;
    }

    public void seteTitle(String eTitle) {
        this.eTitle = eTitle;
    }

    public String geteDescription() {
        return eDescription;
    }

    public void seteDescription(String eDescription) {
        this.eDescription = eDescription;
    }

    public String geteImgPost() {
        return eImgPost;
    }

    public void seteImgPost(String eImgPost) {
        this.eImgPost = eImgPost;
    }

    public String geteTime() {
        return eTime;
    }

    public void seteTime(String eTime) {
        this.eTime = eTime;
    }

    public String geteUser() {
        return eUser;
    }

    public void seteUser(String eUser) {
        this.eUser = eUser;
    }
}
