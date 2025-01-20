package com.example.passwordmanagerv2;

public class Password {
    private String siteName;
    private String username;
    private String password;

    public Password(String siteName, String username, String password) {
        this.siteName = siteName;
        this.username = username;
        this.password = password;
    }

    // Getters and setters
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}