package com.example.qrcodecoursework;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private boolean isDataDisplayed = false;

    private static final int REQUEST_CODE_PERMISSION = 1001;
    private double targetLatitude = 55.712477; // Заданные координаты
    private double targetLongitude = 37.476842;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final String TAG = "CameraExample";

//    private CameraManager cameraManager;
//    private String cameraId;
//    private CameraDevice cameraDevice;
//    private CameraCaptureSession cameraCaptureSession;
//    private CaptureRequest.Builder captureRequestBuilder;
//
//    private HandlerThread backgroundThread;
//    private Handler backgroundHandler;
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

        String androidId = PhoneInfoHelper.getAndroidId(getApplicationContext());
        Log.d(TAG, "Android ID: " + androidId);
        // Проверка разрешения на доступ к IMEI
        getLocation();
        if (checkLocationPermission()) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
        }
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
                if (barcodes.size() != 0) {
                    String qrCodeText = barcodes.valueAt(0).displayValue;
                    // Выводим текст QR-кода в лог
                    Log.d("QRCodeReaderActivity", qrCodeText);

                    // Действия с текстом из QR-кода
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Ваш код для обработки текста из QR-кода здесь
                            // Например, вывод на экран или обработка каких-либо действий
                            Toast.makeText(MainActivity.this, qrCodeText, Toast.LENGTH_SHORT).show();
                        }
                    });
                    isDataDisplayed = true; // Устанавливаем флаг, что данные уже выведены

                }
            }
        });

    }



    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void getLocation() {
        // Получение местоположения пользователя
        GPSTracker gpsTracker = new GPSTracker(this);
        if (gpsTracker.canGetLocation()) {
            double userLatitude = gpsTracker.getLatitude();
            double userLongitude = gpsTracker.getLongitude();

            Log.d("Location", "User Location - Latitude: " + userLatitude + ", Longitude: " + userLongitude);

            // Сравнение с заданной точкой
            double distance = distanceBetween(userLatitude, userLongitude, targetLatitude, targetLongitude);
            Log.d("Distance", "Distance to target point: " + distance + " meters");
        } else {
            // Если не удалось получить местоположение
            Log.e("Location", "Unable to get user location");
        }
    }

    private double distanceBetween(double lat1, double lon1, double lat2, double lon2) {
        // Расчет расстояния между двумя координатами (в метрах)
        double R = 6371000; // Радиус Земли в метрах
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
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

    
}
