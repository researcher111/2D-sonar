package com.example.SonarMapping;

import com.jjoe64.graphview.GraphView.GraphViewData;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.SurfaceView;
import android.widget.TextView;

import OrientationSensing.OrientationHelper;


public class MainActivity extends Activity {

    private Preview mPreview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        TextView directionView = (TextView)  findViewById(R.id.direction);
        TextView pitchView = (TextView)  findViewById(R.id.pitch);
        OrientationHelper mOrientationHelper = new OrientationHelper(this);
        OrientationUpdateView myOrientationView = new OrientationUpdateView(directionView, pitchView);
        mOrientationHelper.registerListener(myOrientationView);

        // Create our Preview view and set it as the content of our activity.
        SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mPreview = new Preview( mSurfaceView);


	}
	
	public GraphViewData[] generateConstantLine(double value, int length){
		
		GraphViewData[] data = new GraphViewData[length];  
		double v=1000000000.0*value;  
		for (int i=0; i<length; i++) {    
		   data[i] = new GraphViewData(i, v);  
		}  
		return data;
	}
	
	
	public GraphViewData[] generateSeries(double[] signal){
		int length = signal.length;
		GraphViewData[] data = new GraphViewData[length];  
		double v=0;  
		for (int i=0; i<length; i++) {    
		   data[i] = new GraphViewData(i, (double)(signal[i]));  
		}  
		return data;
	}
	
	public GraphViewData[] generateSeries(short[] signal){
		int length = signal.length;
		GraphViewData[] data = new GraphViewData[length];  
		double v=0;  
		for (int i=0; i<length; i++) {    
		   data[i] = new GraphViewData(i, (double)(signal[i]));  
		}  
		return data;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	public double getAverage (double[] distances){
		double average = 0;
		for(double x: distances){
			average+= x;
		}
		return average/distances.length;
	}
	
	public double[] getSumArray(double[] measurement, double[] sumArray){
		int i =0;
		if(sumArray == null){
			sumArray = new double[measurement.length];
		}
		for(double x: measurement){
			sumArray[i]+=x;
			i++;
		}
		return sumArray;
	}
	
	public double[] divideArray(double[] sumArray, double value){
		int i=0;
		for(double x: sumArray){
			sumArray[i]=x/value;
			i++;
		}
		return sumArray;
	}
	
	public double getSTDEV(double[] distances){
		double average = getAverage(distances);
		double stdev = 0;
		for(double x : distances){
			stdev += Math.pow(x-average, 2);
		}
		return Math.pow(stdev/distances.length,0.5);
	}
	
	  public static double[] convertFromShortArrayToDoubleArray(short[] shortData) {
		    int size = shortData.length;
		    double[] doubleData = new double[size];
		    for (int i = 0; i < size; i++) {
		      doubleData[i] = shortData[i] / 32768.0;
		    }
		    return doubleData;
		  }

}
