package com.example.SonarMapping3D;
import android.widget.TextView;

import OrientationSensing.OrientationHelper;

/**
 * Created by dggra_000 on 2/8/2015.
 */
public class OrientationUpdateView  implements  OrientationHelper.Listener {
    private float mHeading;
    private float mPitch;
    private float mXDelta;
    private float mYDelta;
    private TextView mDirectionView;
    private TextView mPitchView;

    public OrientationUpdateView(TextView directionView, TextView pitchView){
        mDirectionView = directionView;
        mPitchView = pitchView;
    }


    @Override
    public void onOrientationChanged(float heading, float pitch, float xDelta, float yDelta) {
        mHeading = heading;
        mPitch = pitch;
        mXDelta = xDelta;
        mYDelta = yDelta;

        mDirectionView.setText(MainActivity.df.format(heading));
        mPitchView.setText(MainActivity.df.format(mPitch));


    }
}
