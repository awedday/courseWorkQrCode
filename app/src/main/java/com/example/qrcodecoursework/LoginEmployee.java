package com.example.qrcodecoursework;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

        apiInterface = RequestBuilder.buildRequest();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            Intent intent = new Intent(LoginEmployee.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = etLogin.getText().toString();
                String password = etPassword.getText().toString();

                // Отправляем запрос на сервер для аутентификации
                Call<Employee> call = apiInterface.login(login, password);
                call.enqueue(new Callback<Employee>() {
                    @Override
                    public void onResponse(Call<Employee> call, Response<Employee> response) {
                        if (response.isSuccessful()) {
                            // Аутентификация успешна
                            Employee employee = response.body();
                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();

                            SharedPreferences loginsharedPreferences = getSharedPreferences("LogPass", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editorlogin = loginsharedPreferences.edit();
                            editorlogin.putString("login", login); // Сохраняем логин
                            editorlogin.putString("password", password); // Сохраняем пароль
                            editorlogin.apply();

                            // Переходим на новое окно
                            Intent intent = new Intent(LoginEmployee.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Закрываем текущее окно
                        } else {
                            // Ошибка аутентификации
                            Toast.makeText(LoginEmployee.this, "Incorrect login or password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Employee> call, Throwable t) {
                        // Ошибка при выполнении запроса
                        Toast.makeText(LoginEmployee.this, "Failed to authenticate. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}