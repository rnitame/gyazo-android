package android.rnita.me.gyazo_android.Activity;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.rnita.me.gyazo_android.Service.UploadImageService;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

public class LauncherActivity extends AppCompatActivity {

    private static final String SCREENSHOTS_DIR_NAME = "Screenshots";
    private static final long RECENT_THREASHOLD_MILLIS = 60000L;
    private int PERMISSION_READ_EXTERNAL_STORAGE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isGrant = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (isGrant) {
            Uri target = findUploadableScreenshot();
            if (target != null) {
                uploadScreenshot(target);
            } else {
                startPreference();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_READ_EXTERNAL_STORAGE);
        }

        finish();
    }

    private void startPreference() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void uploadScreenshot(Uri target) {
        Intent i = new Intent(this, UploadImageService.class);
        i.setData(target);
        startService(i);
    }

    private Uri findUploadableScreenshot() {
        Uri target = findTargetFromMediaStore();
        return target;
    }

    private Uri findTargetFromMediaStore() {
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATE_TAKEN
                },
                MediaStore.Images.ImageColumns.MIME_TYPE + "=? AND " +
                        MediaStore.Images.ImageColumns.DATA + " LIKE ?",
                new String[]{
                        "image/png",
                        getScreenshotDir() + "%"
                },
                MediaStore.Images.ImageColumns._ID + " DESC");

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    if (System.currentTimeMillis() - RECENT_THREASHOLD_MILLIS < cursor.getLong(1))
                        return ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                cursor.getLong(0));
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    @NonNull
    private String getScreenshotDir() {
        File screenShotDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), SCREENSHOTS_DIR_NAME);
        return screenShotDir.getAbsolutePath();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Uri target = findUploadableScreenshot();
                if (target != null) {
                    uploadScreenshot(target);
                } else {
                    startPreference();
                }
            }
        }
    }
}
