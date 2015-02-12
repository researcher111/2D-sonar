package com.example.SonarMapping;

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
    private final int f0 = 4000;
	private final int bufferSize = 8000;//32768;
    private final double threshold =0;
    private boolean stopped =false;
	/**
	 * Give the thread high priority so that it's not canceled unexpectedly, and
	 * start it
	 * 
	 *
	 */
	public Sonar() {

		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
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
	}
    public void scheduleSensing() throws InterruptedException
    {
        dispatchQueue.put(
                new Runnable()
                {
                    public void run() {
                        result = getResult();
                    }
                }
        );
    }



	public Result getResult() {
            Log.i("Audio", "Running Audio Thread");
            AudioRecord recorder = null;
            AudioTrack track = null;
            short[] buffer = new short[bufferSize];
            short[] pulse = DSP.ConvertToShort(
                    DSP.sineWave(phase, f0, bufferSize, sampleRate));
            int ix = 0;

            int N = AudioRecord.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            recorder = new AudioRecord(AudioSource.MIC, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2);


            track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2, AudioTrack.MODE_STREAM);

            track.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());

            try {
                track.write(pulse, 0, pulse.length);
                track.play();
                recorder.startRecording();
                recorder.read(buffer, 0, buffer.length);

            } finally {
                track.stop();
                track.release();
                recorder.stop();
                recorder.release();
            }

            return FilterAndClean.Distance(buffer, pulse, sampleRate, threshold);


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