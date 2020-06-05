package com.letschat.users;

public class UsersData {
    private String name, phone, uId;

    public UsersData(String name, String phone,String uId) {
        this.name = name;
        this.phone = phone;
        this.uId = uId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getuId() {
        return uId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }
}
