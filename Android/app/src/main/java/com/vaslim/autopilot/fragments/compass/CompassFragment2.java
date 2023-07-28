package com.vaslim.autopilot.fragments.compass;

import com.vaslim.autopilot.ruddercontrol.RudderControlAlg2;
import com.vaslim.autopilot.ruddercontrol.RudderControlThread;

public class CompassFragment2 extends CompassFragmentAbstract{
    @Override
    protected RudderControlThread chooseThreadAlgorithm() {
        return new RudderControlAlg2();
    }
}
