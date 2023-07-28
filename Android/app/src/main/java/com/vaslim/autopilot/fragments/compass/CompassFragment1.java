package com.vaslim.autopilot.fragments.compass;

import com.vaslim.autopilot.ruddercontrol.RudderControlAlg1;
import com.vaslim.autopilot.ruddercontrol.RudderControlThread;

public class CompassFragment1 extends CompassFragmentAbstract{
    @Override
    protected RudderControlThread chooseThreadAlgorithm() {
        return new RudderControlAlg1();
    }
}
