package com.eztool.mysimpleapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;

import com.eztool.mysimpleapp.BuildConfig;
import com.eztool.mysimpleapp.R;
import com.eztool.mysimpleapp.adapters.CellAdapter;
import com.eztool.mysimpleapp.mutils.InterstitialUtils;
import com.eztool.mysimpleapp.mutils.PurchaseUtils;
import com.eztool.mysimpleapp.utils.Constants;
import com.eztool.mysimpleapp.utils.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import us.shandian.giga.util.Utility;

public class MainActivity extends AppCompatActivity {

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
                    if (Util.checkAndRequestPermissions(MainActivity.this)) {
                        Intent contact = new Intent(MainActivity.this, AudioConverter.class);
                        startActivity(contact);
                    }
                    break;
                case 1:
//                if (Util.checkAndRequestPermissions(MainActivity.this)) {
//                    Intent i = new Intent(MainActivity.this, RingdroidSelectActivity2.class);
//                    startActivity(i);
//                }
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


                    if (Util.checkAndRequestPermissions(MainActivity.this)) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.PERMISSION) {
            boolean gotPermission = grantResults.length > 0;

            for (int result : grantResults) {
                gotPermission &= result == PackageManager.PERMISSION_GRANTED;
            }

            if (gotPermission) {
                Intent i = new Intent(MainActivity.this, YoutubeActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(this, "Accept permission please!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
