package com.example.qrcodecoursework;

import com.example.qrcodecoursework.Models.Employee;
import com.example.qrcodecoursework.Models.History;
import com.example.qrcodecoursework.Models.Information;
import com.example.qrcodecoursework.Models.Qr;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("Employees/login")
    Call<Employee> login(@Query("login") String login, @Query("password") String password);

    @GET("Employees")
    Call<ArrayList<Employee>> getEmployeeList();
    @GET("Qrs")
    Call<ArrayList<Qr>> getQRList();
    @POST("Information")
    Call<Void> addInformation(@Body Information information);

    @POST("Histories")
    Call<Void> addHistory(@Body History history);

    @PUT("Histories/{id}")
    Call<Void> updateHistory(@Path("id") int id, @Body History history);

    @GET("Histories/{employeeId}")
    Call<ArrayList<History>> getHistoryForEmployee(@Path("employeeId") int employeeId);

    @GET("Histories/latest/{employeeId}")
    Call<History> getLatestHistory(@Path("employeeId") int employeeId);

}

