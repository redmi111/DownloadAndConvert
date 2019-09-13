package com.eztool.mysimpleapp.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.eztool.mysimpleapp.R;
import com.eztool.mysimpleapp.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.eztool.mysimpleapp.mutils.AdCloseListener;
import com.eztool.mysimpleapp.mutils.AdRewardListener;
import com.eztool.mysimpleapp.mutils.ClientConfig;
import com.eztool.mysimpleapp.mutils.InterstitialUtils;
import com.eztool.mysimpleapp.mutils.PurchaseListener;
import com.eztool.mysimpleapp.mutils.PurchaseUtils;

import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import us.shandian.giga.ui.DownloadDialog;
import us.shandian.giga.util.ACCEPT_DOWNLOAD;
import us.shandian.giga.util.ExtractorHelper;
import us.shandian.giga.util.ListHelper;

@SuppressLint("SetJavaScriptEnabled")
public class YoutubeActivity extends AppCompatActivity implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    @BindView(R.id.webview)
    WebView webView;

    @BindView(R.id.search_view)
    MaterialSearchView search_view;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tv_remain)
    TextView tv_remain;

    @BindView(R.id.fab_download)
    FloatingActionButton fab_download;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.tv_getmore)
    TextView tv_getmore;

    @BindView(R.id.ll_purchase)
    RelativeLayout ll_purchase;

    private String url_download;

    private SharedPreferences.Editor editor;
    private SharedPreferences mPrefs;
    private ClientConfig clientConfig;
    private Dialog dialog;
    private ProgressDialog progressDialog;

    private WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
            super.onProgressChanged(view, newProgress);
        }
    };

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            view.loadUrl(url);
            Log.d("tuanvn", "shouldOverrideUrlLoading");
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.d("tuanvn", "onLoadResource");

            url_download = view.getUrl();
            String temp = url_download.toLowerCase();

            if ((temp.contains("youtu.be") && temp.contains("?v="))
                    || (temp.contains("youtube.com") && temp.contains("?v="))) {
                fab_download.show();
            } else
                fab_download.hide();
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            super.onPageFinished(view, url);
            Log.d("tuanvn", "onPageFinished");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        mPrefs.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes

        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("download_remain") && tv_remain != null) {
            int download_remain = sharedPreferences.getInt(s, 0);
            tv_remain.setText(getString(R.string.download_remain, download_remain));
        }

        if (s.equals("youtube") && ll_purchase != null) {
            ll_purchase.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        ButterKnife.bind(this);
        mPrefs = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        editor = mPrefs.edit();
        clientConfig = InterstitialUtils.getSharedInstance().getClient();

        if (mPrefs.contains("youtube")) {
            ll_purchase.setVisibility(View.GONE);
        }

        initWebView();

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getResources().getString(R.string.youtube));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        search_view.setCursorDrawable(R.drawable.cursor_search);
        search_view.setHint("Search Youtube videos");

        search_view.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadWebView(query);
                search_view.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


//        int download_remain = mPrefs.getInt("download_remain", 0);
//        tv_remain.setText(getString(R.string.download_remain, download_remain));
//        tv_getmore.setOnClickListener(this);
        fab_download.setOnClickListener(this);



    }

    private void upgradePremium() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.title_dialog_purchase)
                .setMessage(R.string.message_dialog_purchase)
                .setPositiveButton("PURCHASE", (dialogInterface, i) -> {
                    PurchaseUtils.getSharedInstance().purchaseItem(this, 0, new PurchaseListener() {
                        @Override
                        public void purchaseFailed(int item) {

                        }

                        @Override
                        public void purchaseSuccess(int item) {
                            editor.putInt("no_ads", 1).apply();
                            if (clientConfig.isAccept == 1)
                                editor.putInt("youtube", 1).apply();

                            Toast.makeText(YoutubeActivity.this, R.string.restart_take_effect, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void purchaseCancel(int item) {

                        }
                    });
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {
                });

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_yt, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        search_view.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_search:
                break;
            case android.R.id.home:
                if (search_view.isSearchOpen()) {
                    search_view.closeSearch();
                } else {
                    super.onBackPressed();
                }
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else
            super.onBackPressed();
    }

    private void loadWebView(String content) {


        if (content.length() > 0) {
            if (content.toLowerCase().contains("youtu.be") || content.toLowerCase().contains("youtube.com")) {
                url_download = content;
            } else {
                url_download = "https://www.youtube.com/results?search_query=" + Uri.encode(content);
            }

            webView.loadUrl(url_download);

        } else {
            Toast.makeText(this, "invalid url.", Toast.LENGTH_SHORT).show();

        }

    }

    private void initWebView() {

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
        webView.loadUrl("https://m.youtube.com/");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.tv_getmore:
                upgradePremium();
                break;

            case R.id.fab_download:
                progressDialog = new ProgressDialog(view.getContext(), R.style.AppCompatProgressDialogStyle);
                progressDialog.setMessage("Loading...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);

                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();


                checkLinkDownload(url_download);

                break;
        }
    }

    private void showDownloadDialog(StreamInfo result, List<VideoStream> sortedVideoStreams, int selectedVideoStreamIndex
            , ACCEPT_DOWNLOAD type) {
        FragmentManager fm = getSupportFragmentManager();
        DownloadDialog downloadDialog = DownloadDialog.newInstance(result);
        downloadDialog.setVideoStreams(sortedVideoStreams);
        downloadDialog.setAudioStreams(result.getAudioStreams());
        downloadDialog.setSelectedVideoStream(selectedVideoStreamIndex);
        downloadDialog.setAcceptDownload(type);
        downloadDialog.show(fm, "downloadDialog");
        fm.executePendingTransactions();
    }

    private void showDownloadPlanDialog(StreamInfo result, List<VideoStream> sortedVideoStreams, int selectedVideoStreamIndex) {


        if (mPrefs.getInt("youtube", 0) == 1) {
            showDownloadDialog(result, sortedVideoStreams, selectedVideoStreamIndex, ACCEPT_DOWNLOAD.FULL);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);

            View root = getLayoutInflater().inflate(R.layout.layout_fab_download_dialog, null);
            builder.setTitle(R.string.title_dl_plan_dialog)
                    .setView(root)
                    .setNegativeButton("CANCEL", (dialogInterface, i) -> {
                    })
                    .setCancelable(false);

            Button btn_dl_audio_free = root.findViewById(R.id.btn_dl_audio_free);
            Button btn_dl_low_quality = root.findViewById(R.id.btn_dl_low_quality);
            Button btn_purchase_dl = root.findViewById(R.id.btn_purchase_dl);

            int download_trial = mPrefs.getInt("download_trial", 0);
            if (download_trial == 1) {
                btn_purchase_dl.setText(getResources().getString(R.string.purchase));
            } else {
                btn_purchase_dl.setText(getResources().getString(R.string.trial));
            }

            btn_dl_audio_free.setOnClickListener(view -> {

                //show Ads
                InterstitialUtils.getSharedInstance().showInterstitialAds(new AdCloseListener() {
                    @Override
                    public void onAdClose() {
                        if (dialog != null)
                            dialog.dismiss();

                        showDownloadDialog(result, sortedVideoStreams, selectedVideoStreamIndex, ACCEPT_DOWNLOAD.ONLY_AUDIO);
                    }
                });


            });

            btn_dl_low_quality.setOnClickListener(view -> {


                //show Ads
                InterstitialUtils.getSharedInstance().showRewardVideoAds(new AdRewardListener() {
                    @Override
                    public void onRewarded() {
                        if (dialog != null)
                            dialog.dismiss();

                        showDownloadDialog(result, sortedVideoStreams, selectedVideoStreamIndex, ACCEPT_DOWNLOAD.LOW_VIDEO);
                    }

                    @Override
                    public void onAdClose() {

                    }

                    @Override
                    public void onAdNotAvailable() {
                        if (dialog != null)
                            dialog.dismiss();

                        showDownloadDialog(result, sortedVideoStreams, selectedVideoStreamIndex, ACCEPT_DOWNLOAD.LOW_VIDEO);

                        Toast.makeText(YoutubeActivity.this, R.string.reward_video_unavailable, Toast.LENGTH_SHORT).show();
                    }
                });

            });

            btn_purchase_dl.setOnClickListener(view -> {

                if (download_trial == 1) {
                    PurchaseUtils.getSharedInstance().purchaseItem(YoutubeActivity.this, 0, new PurchaseListener() {
                        @Override
                        public void purchaseFailed(int item) {

                        }

                        @Override
                        public void purchaseSuccess(int item) {
                            if (dialog != null)
                                dialog.dismiss();


                            editor.putInt("no_ads", 1).apply();

                            if (clientConfig.isAccept == 1)
                                editor.putInt("youtube", 1).apply();

                            Toast.makeText(view.getContext(), R.string.restart_take_effect, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void purchaseCancel(int item) {

                        }
                    });
                } else {
                    if (dialog != null)
                        dialog.dismiss();

                    showDownloadDialog(result, sortedVideoStreams, selectedVideoStreamIndex, ACCEPT_DOWNLOAD.FULL);
                }
            });
            dialog = builder.create();
            dialog.show();
        }


    }


    private void checkLinkDownload(String url) {

        String urlExtra = url;
        if (url.contains("?list")) {
            if (url.contains("&v=")) {
                String idextra = url.split("&v=")[1];
                if (idextra.contains("&")) {
                    urlExtra = "https://m.youtube.com/watch?v=" + idextra.split("&")[0];
                } else {
                    urlExtra = "https://m.youtube.com/watch?v=" + idextra;
                }
            }

        } else if (url.contains("?v=")) {
            urlExtra = url.split("&")[0];
        } else {
            Toast.makeText(this, "Your link is invalid!", Toast.LENGTH_SHORT).show();
        }
        //https://youtu.be/7kRueqzOb6g
        Log.d("adsdk", "----- " + urlExtra);

        Disposable disposable = ExtractorHelper.getStreamInfo(0, urlExtra, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((@NonNull StreamInfo result) -> {
                    List<VideoStream> sortedVideoStreams = ListHelper.getSortedStreamVideosList(this,
                            result.getVideoStreams(),
                            result.getVideoOnlyStreams(),
                            false);
                    int selectedVideoStreamIndex = ListHelper.getDefaultResolutionIndex(this,
                            sortedVideoStreams);

                    progressDialog.dismiss();
                    showDownloadPlanDialog(result, sortedVideoStreams, selectedVideoStreamIndex);
                }, (@NonNull Throwable throwable) -> {
                    Log.d("adsdk", "errror");

                    progressDialog.dismiss();
                    Toast.makeText(this, R.string.link_error, Toast.LENGTH_SHORT).show();
                });

    }





}
