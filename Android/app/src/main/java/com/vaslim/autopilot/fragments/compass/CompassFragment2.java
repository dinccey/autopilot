package com.vaslim.autopilot.fragments.compass;

import com.vaslim.autopilot.MainActivity;
import com.vaslim.autopilot.ruddercontrol.RudderControlRunnable;
import com.vaslim.autopilot.ruddercontrol.impl.RudderControlAlg1;

public class CompassFragment2 extends CompassFragmentAbstract{
    @Override
    protected RudderControlRunnable chooseThreadAlgorithm() {
        return new RudderControlAlg1();
    }
}
