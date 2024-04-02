package com.example.qrcodecoursework;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qrcodecoursework.Models.Employee;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Получение экземпляра SharedPreferences
        sharedPreferences = getSharedPreferences("LogPass", MODE_PRIVATE);

        // Получение логина и пароля из SharedPreferences
        String login = sharedPreferences.getString("login", "");
        String password = sharedPreferences.getString("password", "");

        // Инициализация интерфейса API
        ApiInterface apiInterface = RequestBuilder.buildRequest();

        // Выполнение запроса к серверу для получения данных о сотруднике
        Call<Employee> call = apiInterface.login(login, password);
        call.enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(Call<Employee> call, Response<Employee> response) {
                if (response.isSuccessful()) {
                    // Получение данных о сотруднике из ответа
                    Employee employee = response.body();
                    // Отображение данных о сотруднике на экране профиля
                    displayEmployeeData(employee);
                } else {
                    // Обработка ошибки при получении данных о сотруднике
                    // Например, отображение сообщения об ошибке
                }
            }

            @Override
            public void onFailure(Call<Employee> call, Throwable t) {
                // Обработка ошибки при выполнении запроса к серверу
                // Например, отображение сообщения об ошибке
            }
        });
    }

    // Метод для отображения данных о сотруднике на экране профиля
    private void displayEmployeeData(Employee employee) {
        // Найти представления в разметке по их идентификаторам
        TextView textViewFirstName = findViewById(R.id.textViewFirstName);
        TextView textViewSecondName = findViewById(R.id.textViewSecondName);
        TextView textViewMiddleName = findViewById(R.id.textViewMiddleName);
        TextView textViewEmail = findViewById(R.id.textViewEmail);

        TextView editTextPhone = findViewById(R.id.editTextPhone);

        // Установить значения представлений на основе данных о сотруднике
        textViewFirstName.setText(employee.getFirstName());
        textViewSecondName.setText(employee.getSecondName());
        textViewMiddleName.setText(employee.getMiddleName());
        textViewEmail.setText(employee.getMail());
        editTextPhone.setText(employee.getPhone());

        // Запретить редактирование текста в представлениях для имени, фамилии, отчества и почты
        textViewFirstName.setEnabled(false);
        textViewSecondName.setEnabled(false);
        textViewMiddleName.setEnabled(false);
        textViewEmail.setEnabled(false);
    }
}