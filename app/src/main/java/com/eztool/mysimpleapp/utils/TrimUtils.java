package com.eztool.mysimpleapp.utils;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import org.telegram.messenger.VideoEditInfo;
import org.telegram.messenger.VideoTrimUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class TrimUtils {

    public static File trimVideo(File src, File dst, int startTime, int endTime) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "TRIM_" + timeStamp + ".mp4";
        if (!dst.exists())
            dst.mkdirs();

        File outputFile = new File(dst, fileName);
        if (outputFile.exists()) {
            outputFile.delete();

        }
        outputFile.createNewFile();

        return VideoTrimUtils.trimVideo(src, outputFile, startTime, endTime);
    }

    public static File convertVideo(File src, File dst) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "CONVERT_" + timeStamp + ".mp4";
        //final String filePath = dst + fileName;
        File outputFile = new File(dst, fileName);
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(src.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int videoIndex = VideoTrimUtils.findTrack(mex, false);
        MediaFormat mf = mex.getTrackFormat(videoIndex);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(src.getAbsolutePath());
        int originalWidth = mf.getInteger(MediaFormat.KEY_WIDTH);
        int originalHeight = mf.getInteger(MediaFormat.KEY_HEIGHT);
        int resultWidth = 0;
        int resultHeight = 0;
        String bitrateStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        int originalBitrate = bitrateStr == null ? -1 : Integer.parseInt(bitrateStr);
        int bitrate = originalBitrate;
        int originalFrameRate = mf.getInteger(MediaFormat.KEY_FRAME_RATE);
        int frameRate = originalFrameRate > 15 ? 15 : originalFrameRate;
        long videoDuration = mf.getLong(MediaFormat.KEY_DURATION);
        mmr.release();
        mex.release();
        if (bitrate > 900000) {
            bitrate = 900000;
        }
        int selectedCompression = VideoEditInfo.COMPRESS_360;
        int compressionsCount;
        if (originalWidth > 1280 || originalHeight > 1280) {
            compressionsCount = 5;
        } else if (originalWidth > 848 || originalHeight > 848) {
            compressionsCount = 4;
        } else if (originalWidth > 640 || originalHeight > 640) {
            compressionsCount = 3;
        } else if (originalWidth > 480 || originalHeight > 480) {
            compressionsCount = 2;
        } else {
            compressionsCount = 1;
        }

        if (selectedCompression >= compressionsCount) {
            selectedCompression = compressionsCount - 1;
        }
        if (selectedCompression != compressionsCount - 1) {
            float maxSize;
            int targetBitrate;
            switch (selectedCompression) {
                case 0:
                    maxSize = 432.0f;
                    targetBitrate = 400000;
                    break;
                case 1:
                    maxSize = 640.0f;
                    targetBitrate = 900000;
                    break;
                case 2:
                    maxSize = 848.0f;
                    targetBitrate = 1100000;
                    break;
                case 3:
                default:
                    targetBitrate = 2500000;
                    maxSize = 1280.0f;
                    break;
            }
            float scale = originalWidth > originalHeight ? maxSize / originalWidth : maxSize / originalHeight;
            resultWidth = Math.round(originalWidth * scale / 2) * 2;
            resultHeight = Math.round(originalHeight * scale / 2) * 2;
            if (bitrate != 0) {
                bitrate = Math.min(targetBitrate, (int) (originalBitrate / scale));
                long videoFramesSize = (bitrate / 8 * videoDuration / 1000);
                Timber.d("Frame siwze " + videoFramesSize);
            }
        }

        if (selectedCompression == compressionsCount - 1) {
            resultWidth = originalWidth;
            resultHeight = originalHeight;
            bitrate = originalBitrate;
        }
        return VideoTrimUtils.convertVideo(src, outputFile, resultWidth, resultHeight, frameRate, bitrate);
//        return result;
    }

    /**
     * https://github.com/deepandroid/video-trimmer/blob/master/videotrimmer/src/main/java/com/deep/videotrimmer/utils/TrimVideoUtils.java
     * https://stackoverflow.com/a/36166688/10770681
     * https://github.com/sannies/mp4parser/blob/master/examples/src/main/java/com/googlecode/mp4parser/ShortenExample.java
     */
    public static File trim(File src, File dst, double startTime, double endTime) throws IOException {
        FileDataSourceImpl file = new FileDataSourceImpl(src);
        Movie movie = MovieCreator.build(file);
        //Movie movie = MovieCreator.build(new FileDataSourceViaHeapImpl(src.getAbsolutePath()));

        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<Track>());
        // remove all tracks we will create new tracks from the old

        double startTime1 = startTime / 1000;
        double endTime1 = endTime / 1000;

        boolean timeCorrected = false;

        // Here we try to find a track that has sync samples. Since we can only start decoding
        // at such a sample we SHOULD make sure that the start of the new fragment is exactly
        // such a frame
        /*for (Track track : tracks) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                if (timeCorrected) {
                    // This exception here could be a false positive in case we have multiple tracks
                    // with sync samples at exactly the same positions. E.g. a single movie containing
                    // multiple qualities of the same video (Microsoft Smooth Streaming file)
                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                }
                startTime1 = correctTimeToSyncSample(track, startTime1, false);
                endTime1 = correctTimeToSyncSample(track, endTime1, true);
                timeCorrected = true;
            }
        }*/

        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            double lastTime = -1;
            long startSample1 = -1;
            long endSample1 = -1;

            for (int i = 0; i < track.getSampleDurations().length; i++) {
                long delta = track.getSampleDurations()[i];

                if (currentTime > lastTime && currentTime <= startTime1) {
                    // current sample is still before the new starttime
                    startSample1 = currentSample;
                }
                if (currentTime > lastTime && currentTime <= endTime1) {
                    // current sample is after the new start time and still before the new endtime
                    endSample1 = currentSample;
                }
                lastTime = currentTime;
                currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
            movie.addTrack(new AppendTrack(new CroppedTrack(track, startSample1, endSample1)));
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "TRIMMED_" + timeStamp + ".mp4";
        //final String filePath = dst + fileName;
        File outputFile = new File(dst, fileName);
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }

        long start1 = System.currentTimeMillis();
        Container out = new DefaultMp4Builder().build(movie);
        long start2 = System.currentTimeMillis();
        FileOutputStream fos = new FileOutputStream(outputFile);
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);

        fc.close();
        fos.close();
        long start3 = System.currentTimeMillis();
        System.err.println("Building IsoFile took : " + (start2 - start1) + "ms");
        System.err.println("Writing IsoFile took  : " + (start3 - start2) + "ms");
        System.err.println("Writing IsoFile speed : " + (new File(String.format("output-%f-%f.mp4", startTime1, endTime1)).length() / (start3 - start2) / 1000) + "MB/s");

        return outputFile;
    }


    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                // samples always start with 1 but we start with zero therefore +1
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;

        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }
}
