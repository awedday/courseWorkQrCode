package com.example.qrcodecoursework.Models;

import com.google.gson.annotations.SerializedName;

public class Employee {
    @SerializedName("idEmployee")
    public int id;
    @SerializedName("firstNameEmployee")
    public String firstName;
    @SerializedName("secondNameEmployee")
    public String secondName;
    @SerializedName("middleNameEmployee")
    public String middleName;
    @SerializedName("mailEmployee")
    public String mail;
    @SerializedName("passwordEmployee")
    public String password;
    @SerializedName("phoneEmployee")
    public String phone;

    public Employee(int id, String firstName, String secondName, String middleName, String mail, String password, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.secondName = secondName;
        this.middleName = middleName;
        this.mail = mail;
        this.password = password;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
