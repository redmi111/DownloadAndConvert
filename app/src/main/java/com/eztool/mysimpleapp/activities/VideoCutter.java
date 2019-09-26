package com.eztool.mysimpleapp.activities;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.eztool.mysimpleapp.R;
import com.eztool.mysimpleapp.utils.Constants;
import com.eztool.mysimpleapp.utils.TrimUtils;
import com.eztool.mysimpleapp.utils.Utils;
import com.google.android.material.snackbar.Snackbar;

import org.telegram.messenger.CustomVideoTimelinePlayView;

import java.io.File;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class VideoCutter extends AppCompatActivity {
    private static final int VIDEO_MIN_DURATION_MS = 3000;
    private static final int VIDEO_MAX_DURATION_MS = 60000000;
    //    private static final long VIDEO_MAX_SIZE = 10 * 1024 * 1024;
    private static final long VIDEO_MAX_SIZE = 0;
    @BindView(R.id.videoViewWrapper)
    FrameLayout videoViewWrapper;
    @BindView(R.id.videoView)
    VideoView videoView;
    @BindView(R.id.playBtn)
    ImageView playBtn;
    @BindView(R.id.trimDurAndSizeTxt)
    TextView trimDurAndSizeTxt;
    @BindView(R.id.trimDurRangeTxt)
    TextView trimDurRangeTxt;
    @BindView(R.id.timelineView)
    CustomVideoTimelinePlayView timelineView;
    @BindView(R.id.trimBtn)
    TextView trimBtn;

    private Uri videoUri;
    private File videoFile;
    private float videoDuration;
    private long estimatedDuration, trimStartTime, trimEndTime;
    private long originalSize, estimatedSize;
    private Disposable trimTask, convertTask;
    private ProgressDialog progressDialog;
    private Runnable updateProgressRunnable;
    private Timer trimDurCounterTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videocutter);
        ButterKnife.bind(this);
        initialize();
    }

    @Override
    protected void onDestroy() {
        if (trimTask != null) {
            trimTask.dispose();
        }
        if (convertTask != null) {
            convertTask.dispose();
        }
        if (updateProgressRunnable != null) {

        }
        super.onDestroy();
    }

    @OnClick(R.id.trimBtn)
    public void trimBtn() {
        //long now = System.currentTimeMillis();
        File outDir = Environment.getExternalStorageDirectory();
        /*File outDir = new File(videoFile.getParent(),"trim-"+now+".mp4");
        try {
            boolean createFileSuccess = outDir.createNewFile();
            Timber.d("Create file success: "+createFileSuccess+" - "+outDir.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            showMessage(e.getMessage());
            return;
        }*/
        Completable trimCompletable = Completable.fromAction(() -> {
            //TrimUtils.trim(videoFile,outDir,trimStartTime,trimEndTime);
            File result = TrimUtils.trimVideo(videoFile, outDir, (int) trimStartTime, (int) trimEndTime);
            Timber.d("Trim result " + result.getAbsolutePath());
        });
        trimTask = trimCompletable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    showLoading(true, "Trim in progress, please wait...");
                })
                .subscribe(() -> {
                    showLoading(false, null);
                    showMessage("Trim success");
                }, throwable -> {
                    throwable.printStackTrace();
                    showLoading(false, null);
                    showMessage(throwable.getMessage());
                });
    }

    @OnClick(R.id.videoViewWrapper)
    public void videoViewWrapper() {
        if (videoView.isPlaying()) {
            trimDurCounterTimer.cancel();
            videoView.pause();
            playBtn.setVisibility(View.VISIBLE);
        } else {
            trimDurCounterTimer = new Timer();
            trimDurCounterTimer.scheduleAtFixedRate(new TimerTask() {
                long currentTime;

                @Override
                public void run() {
                    currentTime = videoView.getCurrentPosition();
                    String trimRangeDurStr = Utils.getMinuteSeconds(currentTime) + "-" + Utils.getMinuteSeconds(trimEndTime);
                    runOnUiThread(() -> {
                        trimDurRangeTxt.setText(trimRangeDurStr);
                    });
                    if (currentTime >= trimEndTime) {
                        trimDurCounterTimer.cancel();
                        runOnUiThread(() -> {
                            videoView.pause();
                            videoView.seekTo((int) trimStartTime);
                            String trimRangeDurStr2 = Utils.getMinuteSeconds(trimStartTime) + "-" + Utils.getMinuteSeconds(trimEndTime);
                            trimDurRangeTxt.setText(trimRangeDurStr2);
                        });
                    }
                }
            }, 0, 100);
            videoView.start();
            timelineView.post(updateProgressRunnable);
            playBtn.setVisibility(View.GONE);
        }
    }

    private void initialize() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        videoUri = getIntent().getParcelableExtra(Constants.MAIN_OBJ);
        String path = Utils.getPath(this, videoUri);
        if (path != null)
            videoFile = new File(path);

        updateProgressRunnable = () -> {
            if (videoView == null || !videoView.isPlaying()) {
                timelineView.removeCallbacks(updateProgressRunnable);
            }
            updatePlayProgress();
            timelineView.postDelayed(updateProgressRunnable, 17);
        };

        initVideo();
      /*  new AlertDialog.Builder(this)
                .setMessage("Convert video ?")
                .setPositiveButton("Yes",(dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    convertInputVideo(videoFile,()->{
                        initVideo();
                    });
                })
                .setNegativeButton("No",(dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    initVideo();
                })
                .setCancelable(false)
                .show();*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_videocutter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.tick:
                trimBtn();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePlayProgress() {
        float progress = 0;
        if (videoView != null) {
            progress = videoView.getCurrentPosition() / (float) videoView.getDuration();
            if (timelineView.getVisibility() == View.VISIBLE) {
                progress -= timelineView.getLeftProgress();
                if (progress < 0) {
                    progress = 0;
                }
                progress /= (timelineView.getRightProgress() - timelineView.getLeftProgress());
                if (progress > 1) {
                    progress = 1;
                }
            }
        }
        timelineView.setProgress(progress);
    }

    private void updateVideoInfo() {
        trimStartTime = (long) Math.ceil(timelineView.getLeftProgress() * videoDuration);
        trimEndTime = (long) Math.ceil(timelineView.getRightProgress() * videoDuration);
        estimatedDuration = trimEndTime - trimStartTime;
        estimatedSize = (int) (originalSize * ((float) estimatedDuration / videoDuration));
        String videoTimeSize = String.format(Locale.US, "%s, ~%s", Utils.getMinuteSeconds(estimatedDuration), Utils.formatFileSize(estimatedSize));
        trimDurAndSizeTxt.setText(videoTimeSize);
        String trimRangeDurStr = Utils.getMinuteSeconds(trimStartTime) + "-" + Utils.getMinuteSeconds(trimEndTime);
        trimDurRangeTxt.setText(trimRangeDurStr);
    }

    private void initVideo() {
        originalSize = videoFile.length();
        videoView.setOnPreparedListener(mediaPlayer -> {
            videoDuration = mediaPlayer.getDuration();
            initVideoTimelineView();
            playBtn.setVisibility(View.VISIBLE);
            updateVideoInfo();
        });
        //videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(videoUri);
    }

    private void initVideoTimelineView() {
        if (videoDuration >= (VIDEO_MIN_DURATION_MS + 1000)) {
            float minProgressDiff = VIDEO_MIN_DURATION_MS / videoDuration;
            timelineView.setMinProgressDiff(minProgressDiff);
        }
        if (videoDuration >= (VIDEO_MAX_DURATION_MS + 1000)) {
            float maxProgressDiff = VIDEO_MAX_DURATION_MS / videoDuration;
            timelineView.setMaxProgressDiff(maxProgressDiff);
        }
        timelineView.setColor(getResources().getColor(R.color.colorAccent));
        timelineView.setMaxVideoSize(VIDEO_MAX_SIZE, originalSize);
        timelineView.setRoundFrames(false);
        timelineView.setDelegate(new CustomVideoTimelinePlayView.VideoTimelineViewDelegate() {

            @Override
            public void onLeftProgressChanged(float progress) {
                Timber.d("onLeftProgressChanged " + progress);
                if (videoView.isPlaying()) {
                    videoView.pause();
                }
                //videoView.seekTo((int) (videoDuration * progress));
                timelineView.setProgress(0);
                updateVideoInfo();
            }

            @Override
            public void onRightProgressChanged(float progress) {
                Timber.d("onRightProgressChanged " + progress);
                if (videoView.isPlaying()) {
                    videoView.pause();
                }
                //videoView.seekTo((int) (videoDuration * progress));
                timelineView.setProgress(0);
                updateVideoInfo();
            }

            @Override
            public void onPlayProgressChanged(float progress) {
                Timber.d("onPlayProgressChanged " + progress);
                videoView.seekTo((int) (videoDuration * progress));
            }

            @Override
            public void didStartDragging() {
                Timber.d("didStartDragging");
            }

            @Override
            public void didStopDragging() {
                Timber.d("didStopDragging");
                videoView.seekTo((int) (videoDuration * timelineView.getLeftProgress()));
            }
        });
        timelineView.setVideoPath(videoUri);
    }

   /* public Observable<DownloadProgress<File>> downloadFile(@NonNull final String filename) {
        return downloadService.downloadFile(filename)
                .switchMap(response -> Observable.fromEmitter(emitter -> {
                    ResponseBody body = response.body();
                    final long contentLength = body.contentLength();
                    ForwardingSource forwardingSource = new ForwardingSource(body.source()) {
                        private long totalBytesRead = 0L;

                        @Override
                        public long read(Buffer sink, long byteCount) throws IOException {
                            long bytesRead = super.read(sink, byteCount);
                            // read() returns the number of bytes read, or -1 if this source is exhausted.
                            totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                            boolean done = bytesRead == -1;
                            float progress = done ? 1f : (float) bytesRead / contentLength;
                            emitter.onNext(new DownloadProgress<>(progress));
                            return bytesRead;
                        }
                    };
                    emitter.setCancellation(body::close);
                    try {
                        File saveLocation = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsoluteFile(), filename);
                        saveLocation.getParentFile().mkdirs();
                        BufferedSink sink = Okio.buffer(Okio.sink(saveLocation));
                        sink.writeAll(forwardingSource);
                        sink.close();
                        emitter.onNext(new DownloadProgress<>(saveLocation));
                        emitter.onCompleted();
                    } catch (IOException e) {
                        // RxJava1: emitter.onError(e);
                        emitter.tryOnError(e);
                    }
                }, Emitter.BackpressureMode.LATEST));
    }*/

    private void convertInputVideo(File videoSrc, Runnable onComplete) {
        Completable convertCompletable = Completable.fromAction(() -> {
            File outDir = Environment.getExternalStorageDirectory();
            File result = TrimUtils.convertVideo(videoSrc, outDir);
            videoFile = result;
            videoUri = Uri.fromFile(result);
            Timber.d("Convert result " + result.getAbsolutePath());
        });
        convertTask = convertCompletable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    showLoading(true, "Convert in progress, please wait...");
                })
                .subscribe(() -> {
                    showLoading(false, null);
                    showMessage("Convert success");
                    onComplete.run();

                }, throwable -> {
                    throwable.printStackTrace();
                    showLoading(false, null);
                    showMessage(throwable.getMessage());
                });

       /* () -> {
            showLoading(false, null);
            showMessage("Convert success");
            onComplete.run();
        }, throwable -> {
            throwable.printStackTrace();
            showLoading(false, null);
            showMessage(throwable.getMessage());
        }*/
    }

    private void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void showLoading(boolean show, String message) {
        if (show) {
            progressDialog = ProgressDialog.show(this, "Loading", message, true, false);
        } else if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
