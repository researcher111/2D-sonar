package com.example.SonarMapping;

import com.jjoe64.graphview.GraphView;

import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.Menu;
import android.view.SurfaceView;
import android.widget.TextView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

import OrientationSensing.OrientationHelper;


public class MainActivity extends Activity {
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private LineGraphSeries<DataPoint> mSeries1;
    private Sonar sonsys ;
    Preview mPreview;
    private  TextView distanceView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        TextView directionView = (TextView)  findViewById(R.id.direction);
        TextView pitchView = (TextView)  findViewById(R.id.pitch);
         distanceView = (TextView)  findViewById(R.id.DistanceDisplay);

        OrientationHelper mOrientationHelper = new OrientationHelper(this);
        OrientationUpdateView myOrientationView = new OrientationUpdateView(directionView, pitchView);
        mOrientationHelper.registerListener(myOrientationView);

        // Create our Preview view and set it as the content of our activity.
        SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mPreview = new Preview( mSurfaceView);
        sonsys = new Sonar();

        GraphView graph2 = (GraphView) findViewById(R.id.layout1);
        mSeries1 = new LineGraphSeries<DataPoint>();
        graph2.addSeries(mSeries1);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(360);
    }

    @Override
    public void onResume() {
        super.onResume();

        mTimer1 = new Runnable() {
            @Override
            public void run() {
                try {
                    sonsys.scheduleSensing();
                }catch (Exception e){  }
                try {
                    distanceView.setText(Double.toString(sonsys.result.distance));
                    mSeries1.resetData(generateData());
                }catch(Exception e){ }
                mHandler.postDelayed(this, 500);
            }
        };
        mHandler.postDelayed(mTimer1, 500);




    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        super.onPause();
    }

    private DataPoint[] generateData() {

        int count = 360;
        int base = 120;
        DataPoint[] values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = i;
            double y = sonsys.result.signal[i+1000];
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }


    Random mRand = new Random();
    private double getRandom() {
        return mRand.nextDouble()*50 - 25;
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
