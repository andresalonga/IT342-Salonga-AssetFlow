package edu.cit.salonga.assetflow.features.auth.dto;

public class GoogleUserInfo {
    private String sub;
    private String name;
    private String email;
    private String picture;

    public String getSub() {
        return sub;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPicture() {
        return picture;
    }
}
