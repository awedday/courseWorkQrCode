package com.example.qrcodecoursework.Models;

import com.google.gson.annotations.SerializedName;

public class Information {
    @SerializedName("idInformation")
    public int idInformation;
    @SerializedName("locationInformation")
    public String locationInformation;
    @SerializedName("distanceInformation")
    public float distanceInformation;
    @SerializedName("androidInformation")
    public String androidInformation;
    @SerializedName("employeeId")
    public int employeeId;
}
