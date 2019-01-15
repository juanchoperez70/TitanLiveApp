package com.bpt.tipi.streaming.model;

import java.io.Serializable;

public class TitanUserDTO implements Serializable {
    private String login;
    private String name;
    private String lastname;
    private String cameraId;

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    public String getCameraId() {
        return cameraId;
    }
    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }
}
