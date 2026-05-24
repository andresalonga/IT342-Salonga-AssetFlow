package edu.cit.salonga.assetflow.features.auth.dto;

public class UpdateProfileRequest {
    private String name;
    private String email;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
