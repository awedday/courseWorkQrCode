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
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 1001;
    private double targetLatitude = 55.712477; // Заданные координаты
    private double targetLongitude = 37.476842;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "CameraExample";

    private CameraManager cameraManager;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Проверка разрешения на использование камеры
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Разрешение на использование камеры уже получено
            openCamera();
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

        SurfaceView surfaceView = findViewById(R.id.surfaceView);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                // Открываем камеру при создании Surface
                openCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                // Ничего не делаем здесь
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                // Закрываем камеру при уничтожении Surface
                closeCamera();
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


    private void openCamera() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            // Получаем ID задней (rear) камеры
            cameraId = cameraManager.getCameraIdList()[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                // Открываем камеру
                cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            // Камера успешно открыта
            cameraDevice = camera;
            createCameraPreview();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            // Камера отключена
            closeCamera();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            // Ошибка при открытии камеры
            closeCamera();
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceView surfaceView = findViewById(R.id.surfaceView);
            Surface surface = surfaceView.getHolder().getSurface();

            // Создаем CaptureRequest.Builder
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            // Создаем CameraCaptureSession
            cameraDevice.createCaptureSession(Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            // Камера успешно настроена
                            cameraCaptureSession = session;
                            updatePreview();

                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            // Ошибка при настройке камеры
                            Log.e(TAG, "Failed to configure camera capture session");
                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            return;
        }

        // Автоматическое фокусирование
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

        try {
            // Начинаем отображение предварительного просмотра
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),
                    null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
   

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBackgroundThread();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}
