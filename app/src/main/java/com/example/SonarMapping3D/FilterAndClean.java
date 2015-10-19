package com.example.SonarMapping3D;

import android.annotation.TargetApi;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.Arrays;
public class FilterAndClean {


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static Result Distance(short[] signal, short[] pulse,
                                  int sampleRate, double threshold, int freq) {

        int hopsToExpire =10;
        double distance =0;
        try {
            distance  = getToneWidth(signal, threshold, hopsToExpire, freq);
        }catch(Exception e){
            distance = 1.1;

        }
        return new Result(distance, signal);
    }

    public static double getToneWidth(short[] signal, double threshold, int hopExpire,int freq) {
        hopExpire =15;
        int count =0;
        int periodCount =2;
        int hops = 0;
        short threshold_max = 0;
        boolean peakSeen = false;
        boolean droppedBelow = true;
        int firstIndex = 0;
        int lastIndex = 0;

        ArrayList<Double> distances = new ArrayList();
        int signalLength = signal.length;
        short max =0;

        for (int i=0; i<signalLength; i++){
            if(signal[i]>max){
                max = signal[i];
            }
        }

        threshold_max = (short)(max*0.5);

        for (int i = 1; i < signalLength-1; i++) {
            if((signal[i] > signal[i+1] && signal[i] > signal[i-1]
                    && signal[i] > threshold_max && droppedBelow) || (signal[i] < signal[i+1] && signal[i] < signal[i-1]
                    && signal[i] < -threshold_max && droppedBelow)){
                hops =0;
                if(!peakSeen){
                    firstIndex = i;
                }
                peakSeen =true;
                count++;
                droppedBelow = false;
                lastIndex = i;

            }
            if(signal[i] <0 && droppedBelow == false){
                droppedBelow =true;
            }
            if(peakSeen){
                hops++;
            }
            if(hopExpire<hops){
                if(count>2) {
                   // distances.add((double) count );//* 0.15425671402);
                    distances.add((lastIndex - firstIndex)/6.4827);
                }
                lastIndex =0;
                firstIndex =0;
                hops =0;
                count =0;
                peakSeen =false;

            }

        }
        return Math.round(getMax(distances));
    }
    /*
    Count the number of peaks. to get the width of the pulse
     */
    public static double getToneWidthSecond(short[] signal, double threshold, int hopExpire,int freq) {
        hopExpire =300;
        int count =0;
        int periodCount =2;
        int hops = 0;
        short threshold_max = 0;
        boolean peakSeen = false;
        boolean droppedBelow = true;

        ArrayList<Double> distances = new ArrayList();
        int signalLength = signal.length;
        short max =0;

            for (int i=0; i<signalLength; i++){
            if(signal[i]>max){
                max = signal[i];
            }
        }

        threshold_max = (short)(max*0.5);

        for (int i = 1; i < signalLength-1; i++) {

            if(signal[i] > signal[i+1] && signal[i] > signal[i-1]
                    && signal[i] > threshold_max && droppedBelow ){
                    hops =0;
                    peakSeen =true;
                    count++;
                    droppedBelow = false;
            }

            if(signal[i] <0 && droppedBelow == false){
                droppedBelow =true;
            }

            if(peakSeen){
                hops++;

            }
            if(hopExpire<hops){
                //Ingore the roll off peaks.
                if(count>2) {
                    distances.add((double) count );//* 0.15425671402);
                }
                hops =0;
                count =0;
                peakSeen =false;

            }

        }
         return getMax(distances)*periodCount;
    }

    public static ArrayList<Double> removeMin(ArrayList<Double> values){
        values.remove(minIndex(values));
        return values;

    }

    public static int minIndex (ArrayList<Double> list) {
        return list.indexOf(Collections.min(list));
    }

    public static boolean containsValueOverThreshold(short[] signal, int start, int end, short threshold_max){
        for (int i = start; i < end-1; i++) {

            if ((signal[i] > threshold_max)) {
                return true;

            }
        }
            return false;
    }

    public static boolean detectSignal(short[] signal, int start, int end, int jump){
        boolean pass = false;
        if(detectRisingEdge(signal, start,  end) && dectectFallingEdge(signal, start,  end)
                && detectRisingEdge(signal, start+jump,  end)){
            pass = true;
        }
        return pass;
    }

    public static boolean detectRisingEdge(short[] signal, int start, int end){
        for (int i = start; i < end-1; i++) {

            if ((signal[i + 1] < (signal[i - 1]))) {
                return true;
            }
        }
      return false;
    }

    public static boolean dectectFallingEdge(short[] signal, int start, int end){
        for (int i = start; i < end-1; i++) {

            if ((signal[i + 1] > (signal[i - 1]))) {
                return true;
            }
        }
        return false;
    }

    public static double getMode(ArrayList<Double> distances){
        int maxCount =0;
        double maxValue = 0;
        HashMap<Double,Integer> hm = new HashMap();
        for (double distance : distances){

            if(hm.get(distance) !=null){
                hm.put(distance,hm.get(distance)+1);
            }
            else{
                hm.put(distance, 1);
            }

        }

        Set<Double> mapKeys = hm.keySet();
        for (double distance : mapKeys){

            if(hm.get(distance) >maxCount){
                maxCount = hm.get(distance);
                maxValue = distance;
            }
        }
    if(maxCount ==1){
        maxValue = 0.0;
    }

    return maxValue;
    }



    public static double getMax(ArrayList<Double> distacne) {
        double max =0;
        for (double distance : distacne){

            if(distance >max){
                max=  distance;
            }
        }
        return max;
    }

    public static double getAverage(ArrayList<Double> distances) {
        double sum =0;
        for (double distance : distances){

            sum+=distance;
        }
        return sum/distances.size();
    }

}

