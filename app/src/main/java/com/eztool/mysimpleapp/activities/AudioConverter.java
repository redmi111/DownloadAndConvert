package com.eztool.mysimpleapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.eztool.mysimpleapp.R;
import com.eztool.mysimpleapp.services.ConverterService;
import com.eztool.mysimpleapp.utils.Constants;
import com.eztool.mysimpleapp.utils.DataStore;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;


public class AudioConverter extends AppCompatActivity {

    public static final String INPUT_ = "input";
    public static final String OUTPUT_ = "output";
    public static final String ID_ = "id";
    public static boolean isConversionRunning = false;
    private AlertDialog alertDialog;
    private RadioGroup radioGroup;
    private EditText input, output;
    private ImageButton inputAddr, outputAddr;
    private FloatingActionButton go_convert;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_converter);

        alertDialog = dialog();

        radioGroup = findViewById(R.id.r_group);
        input = findViewById(R.id.inputAddr);
        output = findViewById(R.id.outputAddr);
        inputAddr = findViewById(R.id.inputAddrEditor);
        outputAddr = findViewById(R.id.outputAddrEditor);
//        go_convert = findViewById(R.id.conv_go);
        progressBar = findViewById(R.id.progressBar);

        radioGroup.check(R.id.mp3);
        radioGroup.setOnCheckedChangeListener((group, checkedId) ->
                Toast.makeText(this, ((RadioButton) findViewById(checkedId)).getText(), Toast.LENGTH_SHORT).show());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Audio Converter (Beta)");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!Constants.IS_CONVERSION_SUPPORTED && !AudioConverter.this.isFinishing()) {
                alertDialog.show();
                checkSupportForConversion();
            }
        }, 300);

        new Handler().postDelayed(() -> {
            Log.d("tuancon", "" +isConversionRunning);
            Log.d("tuancon", "" +progressBar.isIndeterminate());
        }, 300);


        inputAddr.setOnClickListener(v -> new ChooserDialog(AudioConverter.this)
                .withStartFile(DataStore.getInstance(getBaseContext()).getPathDownload())
                .withChosenListener((path, pathFile) -> input.setText(path))
                .withOnCancelListener(dialog -> {
                    Toast.makeText(this, "Nothing Selected :-(", Toast.LENGTH_SHORT).show();
                    dialog.cancel(); // MUST have
                })
                .build()
                .show());

        outputAddr.setOnClickListener(v -> new ChooserDialog(AudioConverter.this)
                .withFilter(true, false)
                .withStartFile(DataStore.getInstance(getBaseContext()).getPathDownload())
                .withChosenListener((path, pathFile) -> output.setText(path))
                .build()
                .show());

    }


    private void checkSupportForConversion() {


        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                if (alertDialog.isShowing())
                    alertDialog.dismiss();
                Constants.IS_CONVERSION_SUPPORTED = true;
            }

            @Override
            public void onFailure(Exception error) {
                if (alertDialog.isShowing())
                    alertDialog.dismiss();
                Constants.IS_CONVERSION_SUPPORTED = false;
                AlertDialog localDialog = new AlertDialog.Builder(AudioConverter.this)
                        .setTitle("Error")
                        .setMessage("This device currently doesn't support Audio Conversion.")
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialog, which) -> finish())
                        .create();
                if (!AudioConverter.this.isFinishing()) {
                    localDialog.show();
                }
            }
        });
    }

    private AlertDialog dialog() {
        return new AlertDialog.Builder(this)
                .setView(R.layout.dialog_progress)
                .setCancelable(false)
                .create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manu_audioconvert, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.check:
                if (Constants.IS_CONVERSION_SUPPORTED) {
                    if (!isConversionRunning) {
                        if (new File(input.getText().toString()).exists() && new File(input.getText().toString()).canRead()) {
                            if (new File(output.getText().toString()).canWrite()) {
                                Intent con_Int = new Intent(AudioConverter.this, ConverterService.class)
                                        .putExtra(INPUT_, input.getText().toString())
                                        .putExtra(OUTPUT_, output.getText().toString())
                                        .putExtra(ID_, radioGroup.getCheckedRadioButtonId());
                                AudioConverter.isConversionRunning = true;
                                progressBar.setIndeterminate(true);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(con_Int);
                                } else {
                                    startService(con_Int);
                                }
                            } else {
                                Toast.makeText(this, "Can't Write Output Directory", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Invalid input file", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Pending Conversion.. Wait For Finish", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Conversion not supported on this device", Toast.LENGTH_SHORT).show();
                }
                break;

            case android.R.id.home:
                onBackPressed();
                break;
        }


        return super.onOptionsItemSelected(item);
    }
}
