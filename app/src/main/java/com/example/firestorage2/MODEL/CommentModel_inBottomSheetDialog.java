package com.example.firestorage2.MODEL;

public class CommentModel_inBottomSheetDialog extends com.example.firestorage2.MODEL.PostId {
    private String eContent, eUser, eAvatar;

    public CommentModel_inBottomSheetDialog() {}

    public CommentModel_inBottomSheetDialog(String eContent, String eUser, String eAvatar) {
        this.eContent = eContent;
        this.eUser = eUser;
        this.eAvatar = eAvatar;
    }

    public String geteContent() {
        return eContent;
    }

    public void seteContent(String eContent) {
        this.eContent = eContent;
    }

    public String geteUser() {
        return eUser;
    }

    public void seteUser(String eUser) {
        this.eUser = eUser;
    }

    public String geteAvatar() {
        return eAvatar;
    }

    public void seteAvatar(String eAvatar) {
        this.eAvatar = eAvatar;
    }
}
