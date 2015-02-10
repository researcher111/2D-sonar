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
			int sampleRate, double threshold, double maxDistanceMeters,
			int deadZoneLenght, int pthreshold, float tempature) {

		double[] val ={0.0,0.0};
		return new Result(0, 0,
				signal,val );
	}

	public static int getMaxIndex(double[] x) {
		int lenx = x.length;
		double max = 0;
		int maxIndex = 0;
		for (int i = 0; i < lenx; i++) {
			if (x[i] > max) {
				max = x[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	// Suppressed negative values.
	public static Complex[] SuppressNegative(Complex[] z) {
		int zlen = z.length;
		int zlenHalf = zlen / 2;
		Complex[] zHilbert = new Complex[zlen];
		for (int i = zlenHalf; i < zlen; i++) {
			zHilbert[i] = new Complex(0, 0);
		}
		for (int i = 0; i < zlenHalf; i++) {
			zHilbert[i] = z[i].times(2);
		}

		return zHilbert;
	}

	// Public Void Kill dead zone
	public static double[] zeroDeadZone(int index, int pulseLenght,
			double[] signal) {
		index = index + pulseLenght;
		for (int i = 0; i <= index; i++) {
			signal[i] = 0;
		}
		return signal;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static int[] peakIndex(double[] timeSeries, double thres,
			 int sharpness) {
		int[] peaks = new int[2];
		int length = timeSeries.length;
		int count = 0;
		int start = getMaxIndex(timeSeries);
		for (int i = start; i < length - sharpness; i++) {
			if (timeSeries[i] > thres) {
				if ((timeSeries[i] > timeSeries[i + sharpness])
						&& (timeSeries[i] > timeSeries[i - sharpness])) {
					peaks[count] = i;
					if(count < 1){
						count++;
					}
					
					i+=10; // For bumbs
				
				}
			}
		}
		//double[] realivant = Arrays.copyOfRange(timeSeries, start-30, start+500);
		return peaks;

	}
	/*
	 * //This method calculate public static double[] CrossCorr(short[] signal,
	 * short[] pulse){ double[] xcorrR = new double[signal.length+pulse.length];
	 * for(int i=0; i<xcorrR.length; i++ ){ int sum =0; for(int j=0;
	 * j<pulse.length; j++){ if(i+j<signal.length){ sum+= signal[i+j]*pulse[j];
	 * } } xcorrR[i] = Math.abs(sum); }
	 * 
	 * return xcorrR; }
	 */

}

// 0 1 2 3 0
// 0 0 1 2 3 0
// 0 0 0 1 2 3
