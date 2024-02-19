package com.example.qrcodecoursework;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.qrcodecoursework.Models.Employee;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginEmployee extends AppCompatActivity {

    private EditText etLogin;
    private EditText etPassword;
    private Button btnLogin;

    private ApiInterface apiInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_employee);
        etLogin = findViewById(R.id.et_login);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        apiInterface = RequestBuilder.buildRequest().create(ApiInterface.class);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String login = etLogin.getText().toString();
//                String password = etPassword.getText().toString();

                Call<ArrayList<Employee>> getEmployeeList = apiInterface.getEmployeeList();
                getEmployeeList.enqueue(new Callback<ArrayList<Employee>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Employee>> call, Response<ArrayList<Employee>> response) {
                        if (response.isSuccessful()) {
                            ArrayList<Employee> employees = response.body();
                            if (employees != null && !employees.isEmpty()) {
                                for (Employee employee : employees) {
                                    // Проверяем совпадение логина и пароля
                                    if (employee.getMail().equals(etLogin.getText().toString()) &&
                                            employee.getPassword().equals(etPassword.getText().toString())) {

                                        // Логин и пароль верны, перенаправляем на другое окно
                                        Intent intent = new Intent(LoginEmployee.this, MainActivity.class);
                                        startActivity(intent);
                                        finish(); // Закрываем текущую активность, чтобы пользователь не мог вернуться назад
                                        return; // Завершаем цикл, так как найдено совпадение
                                    }
                                }
                                // Если цикл завершился, значит ни одно совпадение не найдено
                                Log.d("Login", "Invalid credentials");
                            } else {
                                Log.d("Employee", "No employees found");
                            }
                        } else {
                            Log.e("Employee", "Failed to fetch employees. Error code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Employee>> call, Throwable t) {
                        Log.e("API Error", "Failed to fetch employees", t);

                        // Отображаем пользователю сообщение об ошибке с помощью Toast
                        Toast.makeText(LoginEmployee.this, "Failed to fetch employees. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });
    }
}