package com.eztool.mysimpleapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Util {


    public static boolean checkAndRequestPermissions(Activity activity, int code) {
//        int contactPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS);
//        int contactWritePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CONTACTS);
        int modifyAudioPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStorage = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (modifyAudioPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (writeStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

//        if (contactPermission != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
//        }
//
//        if (contactWritePermission != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.WRITE_CONTACTS);
//        }

        if (listPermissionsNeeded.size() > 0) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[0]),
                    code);

            return false;
        }

        return true;
    }
}
