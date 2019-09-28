package com.eztool.mysimpleapp.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.eztool.mysimpleapp.BuildConfig;
import com.eztool.mysimpleapp.R;
import com.eztool.mysimpleapp.adapters.CellAdapter;
import com.eztool.mysimpleapp.mutils.InterstitialUtils;
import com.eztool.mysimpleapp.mutils.PurchaseUtils;
import com.eztool.mysimpleapp.utils.Constants;
import com.eztool.mysimpleapp.utils.Util;
import com.google.android.material.snackbar.Snackbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import us.shandian.giga.util.Utility;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_PERMISSION_READ_WRITE_STORAGE = 1;
    private static final int REQ_SELECT_VIDEO = 2;
    private static final int REQ_TRIM_VIDEO = 3;
//    @BindView(R.id.selectVideoBtn)
//    Button selectVideoBtn;

    private String[] VIDEO_TRIMMERS = new String[]{
            "Telegram"
    };
    private Uri videoFile;
    private AlertDialog selectTrimmerDialog;

    @BindView(R.id.gridView)
    GridView gridView;

    public static final boolean DEBUG = !BuildConfig.BUILD_TYPE.equals("release");
    private int[] items = new int[]{0, 1, 2, 3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        InterstitialUtils.getSharedInstance().init(this);
        PurchaseUtils.getSharedInstance().init(this);
        Utility.initDownloadSetting(this);

        CellAdapter adapter = new CellAdapter(this, items);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((adapterView, view, position, l) -> {
            switch (position) {
                case 0:
                    if (Util.checkAndRequestPermissions(MainActivity.this, Constants.PERMISSION_AUDIO)) {
                        Intent contact = new Intent(MainActivity.this, AudioConverter.class);
                        startActivity(contact);
                    }
                    break;
                case 1:
                    if (!Util.checkAndRequestPermissions(MainActivity.this, Constants.PERMISSION_CUTTER)) {
                        return;
//                        Intent i = new Intent(MainActivity.this, VideoCutter.class);
//                        startActivity(i);
                    }
                    selectVideo();
                    break;
                case 2:

//                if (show_youtube) {
//                    if (Util.checkAndRequestPermissions(MainActivity.this)) {
//                        Intent intent = new Intent(MainActivity.this, YoutubeActivity.class);
//                        if (skuDetailsList != null && skuDetailsList.size() > 0)
//                            intent.putExtra("sku_detail", skuDetailsList.get(0).getOriginalJson());
//                        startActivity(intent);
//                    }
//                } else {
//                    Intent a = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=S-APP+INDIE"));
//                    startActivity(a);
//                }


                    if (Util.checkAndRequestPermissions(MainActivity.this, Constants.PERMISSION_DOWNLOAD)) {
                        Intent i = new Intent(MainActivity.this, YoutubeActivity.class);
                        startActivity(i);
                    }
                    break;
                case 3:
                    Intent j = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(j);
                    break;

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_SELECT_VIDEO) {
                if (data != null)
                    videoFile = data.getData();

                Intent intent = new Intent(this, VideoCutter.class);
                intent.putExtra(Constants.MAIN_OBJ, videoFile);
                startActivityForResult(intent, REQ_TRIM_VIDEO);
            } else if (requestCode == REQ_TRIM_VIDEO) {
                showMessage("Trim video success");
            }
        }
    }

    private void selectVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQ_SELECT_VIDEO);
    }


    private void showSelectTrimmerDialog(boolean show) {
        if (show) {
            if (selectTrimmerDialog == null) {
                selectTrimmerDialog = new AlertDialog.Builder(this)
                        .setSingleChoiceItems(VIDEO_TRIMMERS, 0, (dialogInterface, index) -> {
                            Class trimmerActivityClass = null;
                            if (index == 0) { // Telegram
                                trimmerActivityClass = VideoCutter.class;
                            }
                            Intent intent = new Intent(this, trimmerActivityClass);
                            intent.putExtra(Constants.MAIN_OBJ, videoFile);
                            startActivityForResult(intent, REQ_TRIM_VIDEO);
                            dialogInterface.dismiss();
                        })
                        .create();
            }
            selectTrimmerDialog.show();
        } else if (selectTrimmerDialog != null) {
            selectTrimmerDialog.dismiss();
        }
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean gotPermission = grantResults.length > 0;
        for (int result : grantResults) {
            gotPermission &= result == PackageManager.PERMISSION_GRANTED;
        }
        switch (requestCode) {
            case Constants.PERMISSION_DOWNLOAD:


                if (gotPermission) {
                    Intent i = new Intent(MainActivity.this, YoutubeActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Accept permission please!", Toast.LENGTH_SHORT).show();
                }
                break;

            case Constants.PERMISSION_AUDIO:

                if (gotPermission) {
                    Intent i = new Intent(MainActivity.this, AudioConverter.class);
                    startActivity(i);
                } else {
                    Toast.makeText(this, "Accept permission please!", Toast.LENGTH_SHORT).show();
                }

                break;

            case Constants.PERMISSION_CUTTER:

                if (gotPermission) {
                    selectVideo();
                } else {
                    Toast.makeText(this, "Accept permission please!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }
}
