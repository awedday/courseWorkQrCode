package com.example.qrcodecoursework.Models;

import com.google.gson.annotations.SerializedName;

public class Qr {
    @SerializedName("idQr")
    public int idQr;

    @SerializedName("textQr")
    public String textQr;

    public Qr(int idQr, String textQr) {
        this.idQr = idQr;
        this.textQr = textQr;
    }

    public int getIdQr() {
        return idQr;
    }

    public void setIdQr(int idQr) {
        this.idQr = idQr;
    }

    public String getTextQr() {
        return textQr;
    }

    public void setTextQr(String textQr) {
        this.textQr = textQr;
    }
}
