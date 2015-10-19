package com.example.SonarMapping3D;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.github.mikephil.charting.data.Entry;



import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import DSP.DSP;
import OrientationSensing.OrientationHelper;


public class MainActivity extends Activity {
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private LineGraphSeries<DataPoint> mSeries1;
    private Sonar sonsys ;
    Preview mPreview;
    private  TextView distanceView;
    private TextView countView;
    private TextView singleReading;
    private final int sampleRate = 44100;
    private final int phase =0;
    private final int f0 = 3402;//6803;
    private final int bufferSize = 8000;//32768;
    private final double threshold =90;
    private final int freq = 20;
    private boolean stopped =false;
    private static boolean started = false;
    private static short[] pulse;
    private AudioRecord recorder;
    private short[] buffer;
    public Result result;
    public ArrayList<Double> values = new ArrayList<Double>();
    public Context currentContext;
    public static DecimalFormat df = new DecimalFormat("#.##");
    public int countSamples =0;
    public ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
    public RadarChart chart;
    public ArrayList<String> xVals;
    public ArrayList<RadarDataSet> dataSets;
    public RadarDataSet setComp1;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        final TextView directionView = (TextView)  findViewById(R.id.direction);
        final TextView pitchView = (TextView)  findViewById(R.id.pitch);
         countView = (TextView)  findViewById(R.id.countSamples);

         distanceView = (TextView)  findViewById(R.id.DistanceDisplay);
        singleReading = (TextView)  findViewById(R.id.reading);
        OrientationHelper mOrientationHelper = new OrientationHelper(this);
        OrientationUpdateView myOrientationView = new OrientationUpdateView(directionView, pitchView);
        mOrientationHelper.registerListener(myOrientationView);


        chart  = (RadarChart)findViewById(R.id.chart);


        xVals = new ArrayList<String>();

        for(int i =0; i <360; i+=20){
            xVals.add(i+"");
            Entry vector = new Entry(0, i); // 0 == quarter 1
            valsComp1.add(vector);
        }




        // Create our Preview view and set it as the content of our activity.
        //SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        //mPreview = new Preview( mSurfaceView);
        //sonsys = new Sonar();

        //try {
        //    sonsys.scheduleSensing();
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}


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



        /*MySurfaceView glSurfaceView =
                (MySurfaceView)findViewById(R.id.surfaceviewclass);
        glSurfaceView = new MyGLSurfaceView(this);
        setContentView(glSurfaceView);
        */
        currentContext = this.getBaseContext();
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                try{
                    result = getResult();
                    //Calculate distance in meters to 2 dp

                     singleReading.setText("----");

                   if(result.distance <240  &&  result.distance> 20 ){
                        countSamples++;
                        countView.setText(countSamples + "");
                        values.add(result.distance);
                        double direction = Double.parseDouble(directionView.getText().toString());
                        int bin = (int)direction/18;
                        Entry vector = new Entry((float)result.distance, bin); // 0 == quarter 1
                        singleReading.setText(result.distance + "");
                        valsComp1.set(bin, vector);
                        setComp1 = new RadarDataSet(valsComp1, "Map");
                      setComp1.setColors(new int[] { R.color.red, R.color.red, R.color.red, R.color.red }, currentContext);

                       dataSets = new ArrayList<RadarDataSet>();
                        dataSets.add(setComp1);

                        RadarData data = new RadarData(xVals, dataSets);
                        chart.setData(data);
                        chart.invalidate(); // refresh


                    }
                    if(values.size()>9) {
                        distanceView.setText(df.format(getAverage(values))+" " + df.format(getSTDEV(values)));
                        values = new ArrayList<Double>();
                        //Toast toast = Toast.makeText(currentContext, "Value Ready", Toast.LENGTH_LONG);
                        //toast.show();
                        countSamples =0;

                    }
                    //(int)result.distance/12 +" "+ (int)result.distance%12 ); //""+Math.round(result.distance*0.0254*100)/100.0)
                    //mSeries1.resetData(generateData());
                }catch(Exception e){ }
                mHandler.postDelayed(this, 300);
            }
        };
        mHandler.postDelayed(mTimer1, 300);

        /**
        GraphView graph2 = (GraphView) findViewById(R.id.layout1);
        mSeries1 = new LineGraphSeries<DataPoint>();
        graph2.addSeries(mSeries1);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(360);

         **/
    }

    @Override
    public void onResume() {
        super.onResume();



        /**


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
    **/



    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        super.onPause();
    }

    private DataPoint[] generateData() {

        int count = 400;
        DataPoint[] values = new DataPoint[count];
        int index =0;
        for (int i=0; i<count; i++) {
            double x = i;
            double y = result.signal[index];
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
            index+=10                      ;
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
	
	
	public double getAverage (ArrayList<Double> distances){
		double average = 0;
		for(double x: distances){
			average+= x;
		}
		return average/distances.size();
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
	
	public double getSTDEV(ArrayList<Double> distances){
		double average = getAverage(distances);
		double stdev = 0;
		for(double x : distances){
			stdev += Math.pow(x-average, 2);
		}
		return Math.pow(stdev/distances.size(),0.5);
	}
	
	  public static double[] convertFromShortArrayToDoubleArray(short[] shortData) {
		    int size = shortData.length;
		    double[] doubleData = new double[size];
		    for (int i = 0; i < size; i++) {
		      doubleData[i] = shortData[i] / 32768.0;
		    }
		    return doubleData;
		  }



    public Result getResult() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2);
        try {
            recorder.startRecording();
            recorder.read(buffer, 0, buffer.length);
        }catch(Exception e){
            e.printStackTrace();
        }
        finally{
            recorder.stop();
            recorder.release();
        }


        return FilterAndClean.Distance(buffer, pulse, sampleRate, threshold, freq);


    }




}
