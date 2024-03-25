package com.example.qrcodecoursework.Models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class History {
    @SerializedName("idHistory")
    public int idHistory;
    @SerializedName("dateStartHistory")
    public String  dateStartHistory;
    @SerializedName("dateFinishHistory")
    public String  dateFinishHistory;
    @SerializedName("employeeId")
    public int employeeId;

    public int getIdHistory() {
        return idHistory;
    }

    public void setIdHistory(int idHistory) {
        this.idHistory = idHistory;
    }

    public String  getDateStartHistory() {
        return dateStartHistory;
    }

    public void setDateStartHistory(String  dateStartHistory) {
        this.dateStartHistory = dateStartHistory;
    }

    public String  getDateFinishHistory() {
        return dateFinishHistory;
    }

    public void setDateFinishHistory(String  dateFinishHistory) {
        this.dateFinishHistory = dateFinishHistory;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }


}


