package com.vaslim.autopilot;

public class SharedData {

    public enum Mode {
        GPS,
        COMPASS,
        MANUAL
    }

    public static double currentBearing = -1;
    public static double targetBearing = -1;
    public static int sensitivity = 3;
    public static Mode mode;

}
