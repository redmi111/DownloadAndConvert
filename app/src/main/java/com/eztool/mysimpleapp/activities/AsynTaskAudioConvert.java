package com.eztool.mysimpleapp.activities;

import android.content.Context;
import android.os.AsyncTask;

import com.eztool.mysimpleapp.model.AudioModel;

import java.lang.ref.WeakReference;

public class AsynTaskAudioConvert extends AsyncTask<AudioModel, Integer, Long> {
        private WeakReference<Context> contextRef;
        public AsynTaskAudioConvert(Context context) {
                contextRef = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
                super.onPreExecute();
        }

        @Override
        protected Long doInBackground(AudioModel ... params) {
                AudioModel audioModel = params[0];

                String input = audioModel.getInput();
                String output = audioModel.getOutput();
                int id = audioModel.getId();


                return null;
        }



        @Override
        protected void onProgressUpdate(Integer... values) {
//                AudioConverter.updateProgress(values[0]);
                super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Long aLong) {
                super.onPostExecute(aLong);
        }
}