package com.example.qrcodecoursework;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.qrcodecoursework.Models.Employee;
import com.example.qrcodecoursework.Models.History;
import com.example.qrcodecoursework.Models.Information;
import com.example.qrcodecoursework.Models.Qr;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

        public class MainActivity extends AppCompatActivity {
            private boolean isDataDisplayed = false;
            private int id;
            private String startDate;
            private String androidId;
            private double userLatitude;
            private double userLongitude;
            private double distance;
            private static final int REQUEST_CODE_PERMISSION = 1001;
            private double targetLatitude = 55.712477; // Заданные координаты
            private double targetLongitude = 37.476842;
            private boolean isDataProcessed = false;


            private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
            private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
            private static final String TAG = "CameraExample";

            private SurfaceView surfaceView;
            private CameraSource cameraSource;


            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);



                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_REQUEST_CODE);
                } else {

                }
                FloatingActionButton Bt_exit = findViewById(R.id.exit);
                Bt_exit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                        builder.setTitle("Выход из аккаунта")
                                .setMessage("Вы уверены, что хотите выйти из аккаунта?");

                        builder.setPositiveButton("Выйти", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("isLoggedIn", false);
                                        editor.apply();
                                        Intent intent = new Intent(MainActivity.this, LoginEmployee.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();            }
                });

                FloatingActionButton Bt_profile  = findViewById(R.id.profile);
                Bt_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(intent);

                    }
                });
                surfaceView = findViewById(R.id.surfaceView);
                BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

                cameraSource = new CameraSource.Builder(this, barcodeDetector)
                        .setAutoFocusEnabled(true)
                        .build();

                surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(@NonNull SurfaceHolder holder) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            try {
                                cameraSource.start(surfaceView.getHolder());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                        }
                    }

                    @Override
                    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

                    }

                    @Override
                    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                        cameraSource.stop();
                    }
                });

                barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                    @Override
                    public void release() {

                    }

                    @Override
                    public void receiveDetections(Detector.Detections<Barcode> detections) {
                        final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                        if (barcodes.size() != 0 && !isDataProcessed) {
                            String qrCodeText = barcodes.valueAt(0).displayValue;

                            compareQRCodeWithAPI(qrCodeText);

                            isDataProcessed = true; //Устанавливаем флаг, что данные уже обработаны
                        }
                    }
                });

            }

            private void compareQRCodeWithAPI(String qrCodeText) {
                ApiInterface apiInterface = RequestBuilder.buildRequest();
                Call<ArrayList<Qr>> call = apiInterface.getQRList();

                call.enqueue(new Callback<ArrayList<Qr>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Qr>> call, Response<ArrayList<Qr>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            boolean isMatchFound = false;
                            for (Qr qr : response.body()) {
                                if (qr.getTextQr().equals(qrCodeText)) {
                                    isMatchFound = true;
                                    break;
                                }
                            }
                            if (isMatchFound) {
                                androidId = PhoneInfoHelper.getAndroidId(getApplicationContext());
                                Log.d(TAG, "Android ID: " + androidId);
                                getLocation();
                                if (checkLocationPermission()) {
                                    getLocation();
                                } else {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
                                }
                                FindEmployeeId();
                                if (!isDataDisplayed) {
                                    // Если нет, создаем новую запись в истории
                                    isDataDisplayed = true;
                                }
                                Toast.makeText(MainActivity.this, "QR-код совпадает с данными из API", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "QR-код не совпадает с данными из API", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Ошибка при получении данных из API", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Qr>> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Ошибка при выполнении запроса к API", Toast.LENGTH_SHORT).show();
                    }
                });
            }


            private void FindEmployeeId() {
                SharedPreferences sharedPreferences = getSharedPreferences("LogPass", MODE_PRIVATE);
                String login = sharedPreferences.getString("login", "");
                String password = sharedPreferences.getString("password", "");
                ApiInterface apiInterface = RequestBuilder.buildRequest();

                // Проверка, что логин и пароль не пустые
                Call<Employee> call = apiInterface.login(login, password);
                call.enqueue(new Callback<Employee>() {
                    @Override
                    public void onResponse(Call<Employee> call, Response<Employee> response) {
                        if (response.isSuccessful()) {
                            // Получение данных о сотруднике из ответа
                            Employee employee = response.body();
                            id = employee.getId();
                            Log.d("Employee ID", "ID сотрудника: " + id);
                            sendInformationToAPI(id, userLatitude, userLongitude, distance);
                        } else {
                            Log.e("Employee ID", "Не удалось получить ID сотрудника");
                        }
                    }

                    @Override
                    public void onFailure(Call<Employee> call, Throwable t) {
                        Log.e("Employee ID", "Ошибка при выполнении запроса к API: " + t.getMessage());
                    }
                });
            }

            private boolean checkLocationPermission() {
                return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            }

            private void getLocation() {
                GPSTracker gpsTracker = new GPSTracker(this);
                if (gpsTracker.canGetLocation()) {
                    userLatitude = gpsTracker.getLatitude();
                    userLongitude = gpsTracker.getLongitude();

                    Log.d("Location", "User Location - Latitude: " + userLatitude + ", Longitude: " + userLongitude);

                    // Сравнение с заданной точкой

                    distance = gpsTracker.distanceBetween(userLatitude, userLongitude, targetLatitude, targetLongitude);
                    Log.d("Distance", "Distance to target point: " + distance + " meters");
                } else {
                    // Если не удалось получить местоположение
                    Log.e("Location", "Unable to get user location");
                }
            }

            @Override
            public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                if (requestCode == REQUEST_CODE_PERMISSION) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getLocation();
                    }
                }
            }

            private void sendInformationToAPI(int id, double userLatitude, double userLongitude, double distance) {
                // Создать объект модели Information и заполнить данными
                Information information = new Information();
                information.locationInformation = userLatitude + ", " + userLongitude; // Форматируйте координаты как необходимо
                information.distanceInformation = (float)distance; // Переведите расстояние в int, если это требуется
                information.androidInformation = androidId;
                information.employeeId = id; // Используйте id сотрудника

                // Отправить данные на сервер
                ApiInterface apiInterface = RequestBuilder.buildRequest();
                Call<Void> call = apiInterface.addInformation(information); // Предположим, что у вас есть метод sendInformation в вашем интерфейсе API
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Данные успешно отправлены на сервер", Toast.LENGTH_SHORT).show();
                            checkAndUpdateHistory(information.employeeId);
                        } else {
                            Toast.makeText(MainActivity.this, "Ошибка при отправке данных на сервер", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Ошибка при выполнении запроса к API", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            private void createNewHistoryRecord(int employeeId) {
                // Получаем текущее время
                Date currentDate = new Date();
                // Форматируем дату в строку в нужном формате для MSSQL
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = sdf.format(currentDate);
                // Создаем новый объект History и заполняем поля
                History history = new History();
                history.setEmployeeId(employeeId);
                history.setDateStartHistory(formattedDate); // Устанавливаем стартовую дату
                // Отправляем запрос на создание новой записи в таблице History
                ApiInterface apiInterface = RequestBuilder.buildRequest();
                Call<Void> call = apiInterface.addHistory(history); // Предположим, что у вас есть метод addHistory в вашем интерфейсе API
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Запись в истории успешно создана", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Ошибка при создании записи в истории", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Ошибка при выполнении запроса к API", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            private void checkAndUpdateHistory(int employeeId) {
                ApiInterface apiInterface = RequestBuilder.buildRequest();
                Call<History> call = apiInterface.getLatestHistory(employeeId);
                call.enqueue(new Callback<History>() {
                    @Override
                    public void onResponse(Call<History> call, Response<History> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            History latestHistory = response.body();
                            if (latestHistory.getDateFinishHistory() == null) {
                                // Последняя запись имеет пустую дату окончания, обновляем ее
                                getStartDateFromHistory(employeeId);
                                updateHistoryRecord(employeeId, latestHistory.getIdHistory(), latestHistory.getDateStartHistory());
                            } else {
                                // Последняя запись заполнена, создаем новую запись
                                createNewHistoryRecord(employeeId);
                            }
                        } else {
                            // Нет записей в истории, создаем новую запись
                            createNewHistoryRecord(employeeId);
                        }
                    }

                    @Override
                    public void onFailure(Call<History> call, Throwable t) {
                        // Ошибка при получении данных из API
                        Toast.makeText(MainActivity.this, "Ошибка при выполнении запроса к API", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            private void updateHistoryRecord(int employeeId, int historyId, String startDate) {
                // Получаем текущее время для финишной даты
                Date currentDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = sdf.format(currentDate);
                // Создаем объект History и заполняем поля
                History history = new History();
                history.setIdHistory(historyId);
                history.setEmployeeId(employeeId);
                history.setDateStartHistory(startDate); // Используем стартовую дату из предыдущей записи
                history.setDateFinishHistory(formattedDate); // Устанавливаем текущую дату как финишную
                // Отправляем запрос на обновление записи в таблице History
                ApiInterface apiInterface = RequestBuilder.buildRequest();
                Call<Void> call = apiInterface.updateHistory(historyId,history);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Запись в истории успешно обновлена", Toast.LENGTH_SHORT).show();
                            // После успешного обновления записи в таблице History, создаем новую запись

                        } else {
                            Toast.makeText(MainActivity.this, "Ошибка при обновлении записи в истории", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Ошибка при выполнении запроса к API", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            private void getStartDateFromHistory(int employeeId) {
                ApiInterface apiInterface = RequestBuilder.buildRequest();
                Call<History> call = apiInterface.getLatestHistory(employeeId);

                call.enqueue(new Callback<History>() {
                    @Override
                    public void onResponse(Call<History> call, Response<History> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            History latestHistory = response.body();
                            startDate = latestHistory.getDateStartHistory();
                        } else {
                            Toast.makeText(MainActivity.this, "Не удалось получить стартовую дату из истории", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<History> call, Throwable t) {
                        // Ошибка при выполнении запроса к API
                        Toast.makeText(MainActivity.this, "Ошибка при выполнении запроса к API", Toast.LENGTH_SHORT).show();
                    }
                });
            }


        }
