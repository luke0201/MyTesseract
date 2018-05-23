package io.github.luke0201.mytesseract;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ID_READ_PERMISSION = 100;
    public static final int REQUEST_ID_WRITE_PERMISSION = 200;

    private TessBaseAPI mTess;
    private TextView mTextView;

    private boolean mCanRead;
    private boolean mCanWrite;
    private boolean mTessInitDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.textView);

        mCanRead = false;
        mCanWrite = false;
        mTessInitDone = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTess != null) {
            mTess.end();
        }
    }

    public void onClick(View v) {
        mCanRead |= askPermission(
                REQUEST_ID_READ_PERMISSION, Manifest.permission.READ_EXTERNAL_STORAGE);
        mCanWrite |= askPermission(
                REQUEST_ID_WRITE_PERMISSION, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!mCanRead || !mCanWrite) {
            Toast.makeText(
                    getApplicationContext(),
                    "Failed to retrieve permissions!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String language = null;
        switch (v.getId()) {
            case R.id.button_equ:
                language = "equ";
                break;

            case R.id.button_eng:
                language = "eng";
                break;

            case R.id.button_kor:
                language = "kor";
                break;
        }

        tesseractInit(language);
        ocrTest(R.drawable.digits2);
    }

    private boolean askPermission(int requestId, String permissionName) {
        if (Build.VERSION.SDK_INT >= 23) {
            // check if we have permission
            int permission = ActivityCompat.checkSelfPermission(this, permissionName);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{permissionName}, requestId);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            switch (requestCode) {
                case REQUEST_ID_READ_PERMISSION:
                    mCanRead |= (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                    break;

                case REQUEST_ID_WRITE_PERMISSION:
                    mCanWrite |= (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                    break;
            }
        }
    }

    private void tesseractInit(String language) {
        if (mTessInitDone) {
            mTess.end();
            mTessInitDone = false;
        }

        String datapath = Environment.getExternalStorageDirectory() + "/tesseract/";
        File dir = new File(datapath + "tessdata/");
        Log.d("tesseractInit", "datapath: " + datapath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.d("tesseractInit", "Failed to create datapath");
                return;
            }
        }

        mTess = new TessBaseAPI();
        if (!mTess.init(datapath, language)) {
            Log.d("tesseractInit", "Failed to initialize TessBaseAPI");
            return;
        }

        mTessInitDone = true;
    }

    private String getOcrResult(Bitmap bmp) {
        mTess.setImage(bmp);
        return mTess.getUTF8Text();
    }

    private void ocrTest(int resid) {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), resid);
        String ocrResult = getOcrResult(bmp);
        mTextView.setText(ocrResult);
    }
}
