package com.example.SonarMapping;

import android.annotation.TargetApi;
import android.os.Build;

import DSP.Complex;

public class FilterAndClean {
    public static double soundSpeed = 346.65; // Speed of sound in air
    public static double peakThreshold = 22000000000.0;
    public static double multiple = 1000000000.0;
    public static Complex[] cachedPulse = null;
    public static int sharpness = 1;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static Result Distance(short[] signal, short[] pulse,
                                  int sampleRate, double threshold) {

        int hopsToExpire =10;
        double distance =0;
        try {
            distance  = getToneWidth(signal, threshold, hopsToExpire);
        }catch(Exception e){
            distance = 1.1;

        }
        return new Result(distance, signal);
    }

    /*
    Count the number of peaks. to get the width of the pulse
     */
    public static double getToneWidth(short[] signal, double threshold, int hopExpire) {
        int signalLength = signal.length;
        int count =0;
        int periodCount =0;
        int hops = 0;
        boolean peakSeen = false;
        for (int i = 0; i < signalLength; i++) {
            if(signal[i] > threshold) {
                if ((signal[i - 1] < signal[i]) && (signal[i] > signal[i + 1])) {
                        count++;
                        hops =0;
                        peakSeen =true;
                }
                if(peakSeen){
                    hops++;
                }
            }
            if(hopExpire<hops){
                break;
            }

        }
        return count*periodCount;
    }



}

