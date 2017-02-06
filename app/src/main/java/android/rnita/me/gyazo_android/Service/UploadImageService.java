package android.rnita.me.gyazo_android.Service;

import android.app.IntentService;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.rnita.me.gyazo_android.R;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MultipartBody;

public class UploadImageService extends IntentService {
    public static final String PREFS_GYAZO_ID = "id";
    private static final String TAG = UploadImageService.class.getSimpleName();
    private Handler mHandler;

    public UploadImageService() {
        super(TAG);
        mHandler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Uri uri = intent.getData();
        String result = null;

        try {
            Log.d(TAG, uri.toString());
            result = uploadImage(uri);
        } catch (IOException e) {
            Log.w(TAG, "Failed to upload an image retry");
        }

        if (result == null) {
            showToast(R.string.failed_upload_image, Toast.LENGTH_SHORT);
        } else {
            onSucceed(result);
        }
    }

    private void onSucceed(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData.Item clipItem = new ClipData.Item(url);
        String[] mineType = new String[1];
        mineType[0] = ClipDescription.MIMETYPE_TEXT_URILIST;
        ClipData clipData = new ClipData(
                new ClipDescription("image url", mineType),
                clipItem);
        clipboard.setPrimaryClip(clipData);
        showToast(R.string.upload_successful, Toast.LENGTH_SHORT);
    }

    private void showToast(final int res, final int length) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(UploadImageService.this, res, length).show();
            }
        });
    }

    private String uploadImage(final Uri uri) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String id = loadGyazoId();
        String type = getContentResolver().getType(uri);
        File file = getFileFromContentUri(uri);

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"id\""),
                        RequestBody.create(null, id)
                )
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"imagedata\"; filename=\"gyazo.com\""),
                        RequestBody.create(MediaType.parse(type), file)
                )
                .build();

        Request request = new Request.Builder()
                .url(getEndpointUrl())
                .post(body)
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            String url = response.body().string();

            id = response.header("X-Gyazo-Id");
            if (!TextUtils.isEmpty(id))
                saveGyazoId(id);

            return url;
        }
        return null;
    }

    private SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    private String loadGyazoId() {
        return getPrefs().getString(PREFS_GYAZO_ID, "");
    }

    private void saveGyazoId(String id) {
        getPrefs().edit().putString(PREFS_GYAZO_ID, id).apply();
    }

    public String getEndpointUrl() {
        return "http://upload.gyazo.com/upload.cgi";
    }

    @Nullable
    public File getFileFromContentUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri,
                new String[]{
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.SIZE
                }, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    if (cursor.getLong(1) > 0)
                        return new File(cursor.getString(0));
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }
}
