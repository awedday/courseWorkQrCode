package com.example.qrcodecoursework.Models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class History {
    @SerializedName("idHistory")
    public int idHistory;
    @SerializedName("dateStartHistory")
    public Date dateStartHistory;
    @SerializedName("dateFinishHistory")
    public Date dateFinishHistory;
    @SerializedName("employeeId")
    public int employeeId;
}
