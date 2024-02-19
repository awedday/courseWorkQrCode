package com.example.qrcodecoursework;

import com.example.qrcodecoursework.Models.Employee;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("Employees/login")
    Call<Employee> login(@Query("login") String login, @Query("password") String password);

    @GET("Employees")
    Call<ArrayList<Employee>> getEmployeeList();

}

