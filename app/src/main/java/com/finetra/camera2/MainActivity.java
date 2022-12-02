package com.finetra.camera2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {
    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceViewHolder;
    Handler mHandler;
    ImageReader mImageReader;
    CameraDevice mCameraDevice;
    CaptureRequest.Builder mPreviewBuilder;
    CameraCaptureSession mSession;
    int mDeviceRotation;
    Sensor mAccelerometer;
    Sensor mMagnetometer;
    SensorManager mSensorManager;
    DeviceOrientation deviceOrientation;
    int mDSI_height,mDSI_width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW);
        setContentView(R.layout.activity_main);
        ImageButton btnImage = findViewById(R.id.take_photo);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                takePicture();
            }
        });
        mSurfaceView = findViewById(R.id.surfaceView);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        deviceOrientation = new DeviceOrientation();

        initSurfaceView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(deviceOrientation.getEventListener(),mAccelerometer,SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(deviceOrientation.getEventListener(),mMagnetometer,SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(deviceOrientation.getEventListener());
    }
    public void initSurfaceView(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mDSI_height = displayMetrics.heightPixels;
        mDSI_width = displayMetrics.widthPixels;

        mSurfaceViewHolder = mSurfaceView.getHolder();
        mSurfaceViewHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                initCameraAndPreview();
            }
            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                if(mCameraDevice  !=null){
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });

    }
    public void initCameraAndPreview(){
        HandlerThread handlerThread = new HandlerThread("CAMERA2");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
        Handler mainHandler = new Handler(getMainLooper());
        try {
        String mCameraId = ""+ CameraCharacteristics.LENS_FACING_FRONT;
        CameraManager mCameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size largestPreviewSize = map.getOutputSizes(ImageFormat.JPEG)[0];
        Log.i("LargestSize",largestPreviewSize.getWidth()+""+largestPreviewSize.getHeight());
        setAspectRaionTexturePreview(largestPreviewSize.getHeight(),largestPreviewSize.getWidth());
        mImageReader = ImageReader.newInstance(largestPreviewSize.getWidth(),largestPreviewSize.getHeight(),ImageFormat.JPEG,/*maxImages*/7);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,mainHandler);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        mCameraManager.openCamera(mCameraId,deviceStateCallback,mHandler);
        } catch (CameraAccessException e) {
            Toast.makeText(this,"Cannot Open Camera.",Toast.LENGTH_SHORT).show();
        }


    }
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            new SaveImageTask().execute(bitmap);
        }
    };
}