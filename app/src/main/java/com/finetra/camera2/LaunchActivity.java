package com.finetra.camera2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.security.Permissions;
import java.util.Arrays;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        if (!hasPermissions(PERMISSIONS)) {

            requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        } else {
            Intent mainIntent = new Intent(LaunchActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }


    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private boolean hasPermissions(String[] permissions){
        int result;
        for(String perms: permissions){
            result = ContextCompat.checkSelfPermission(this,perms);
            if(result== PackageManager.PERMISSION_DENIED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case PERMISSIONS_REQUEST_CODE:
            if(grantResults.length>0){
                boolean cameraPermissionAccepted = grantResults[0]
                        == PackageManager.PERMISSION_GRANTED;
                boolean diskPerMissionAccepted = grantResults[1]
                        == PackageManager.PERMISSION_GRANTED;
                if(!cameraPermissionAccepted ||!diskPerMissionAccepted){
                    Log.i("TestValue","Permisssion Denied");
                    showDialogForPermission("Granted Permission for Run This App.!");

                }else{
                    Intent mainIntent = new Intent(LaunchActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            }
            break;
        }
    }

    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LaunchActivity.this);
        builder.setTitle("Alert");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                requestPermissions(PERMISSIONS,PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
           finish();
            }
        });
        builder.create().show();
    }
}
