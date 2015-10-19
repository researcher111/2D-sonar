package com.example.SonarMapping3D;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import DSP.DSP;

/*
 * Thread to manage live recording/playback of voice input from the device's microphone.
 */
public class Sonar {
    public Result result;
    private BlockingQueue<Runnable> dispatchQueue
            = new LinkedBlockingQueue<Runnable>();
    private final int sampleRate = 44100;
    private final int phase =0;
    private final int f0 = 6803;
    private final int bufferSize = 8000;//32768;
    private final double threshold =90;
    private final int freq = 20;
    private boolean stopped =false;
    private static boolean started = false;
    private static short[] pulse;
    private AudioRecord recorder;
    private short[] buffer;

    /**
     * Give the thread high priority so that it's not canceled unexpectedly, and
     * start it
     *
     *
     */
    public Sonar() {

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        AudioTrack track = null;
        pulse = DSP.ConvertToShort(
                DSP.sineWave(phase, f0, bufferSize, sampleRate));
        track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize, AudioTrack.MODE_STATIC);
        track.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
        track.write(pulse, 0, pulse.length);
        track.setLoopPoints(0, pulse.length/2, -1);
        track.play();

        buffer = new short[bufferSize];

        int ix = 0;

        int N = AudioRecord.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        recorder = new AudioRecord(AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2);
        recorder.startRecording();


        /**
         new Thread(
         new Runnable()
         {
         @Override
         public void run()
         {
         while (true)
         {
         try
         {
         dispatchQueue.take().run();
         } catch (InterruptedException e)
         {   // okay, just terminate the dispatcher
         }
         }
         }
         }
         ).start();
         **/
    }
    public void scheduleSensing() throws InterruptedException
    {
        new Thread(
                new Runnable()
                {
                    @Override
                    public void run() {
                        while(true){
                            result = getResult();
                        }
                    }
                }
        ).start();
    }



    public Result getResult() {
        Log.i("Audio", "Running Audio Thread");


        try {
            recorder.read(buffer, 0, buffer.length);
        }catch(Exception e){

        }
        finally {
            //if( started) {
            //    track.stop();
            //   track.release();
            //}
            recorder.stop();
            recorder.release();
        }

        return FilterAndClean.Distance(buffer, pulse, sampleRate, threshold,freq);


    }

    /**
     * Called from outside of the thread in order to stop the recording/playback
     * loop
     *
     */
    private void close() {
        stopped = true;
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                albumName);
        if (isExternalStorageWritable()) {
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.e("Write Error", "Directory not created");
                }
            }
        } else {
            Log.e("Write Error", "Storage Not writable");
        }
        return file;
    }

    public void writeStringToTextFile(String text, String fileName) {

        try {
            File dir = getAlbumStorageDir("Sonar");
            File file = new File(dir, fileName);
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(text);
            output.close();
            //MediaScannerConnection.scanFile(this, new String[] { file.getAbsolutePath() }, null, null);
        } catch (IOException e) {
            Log.e("Write Error", "Failed to write content");
        }
    }

}