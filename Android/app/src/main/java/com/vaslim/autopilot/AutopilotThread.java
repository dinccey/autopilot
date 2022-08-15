package com.vaslim.autopilot;

import com.vaslim.autopilot.fragments.AutopilotFragment;

public class AutopilotThread extends Thread{

    public static final double NORMALIZER = 30;
    private static final double PAUSE_BEFORE_SPIN = 100;
    private static final double CYCLE_SLEEP = 3000;
    public volatile boolean running = true;
    public static final char CHAR_TURN_LEFT = 'L';
    public static final char CHAR_TURN_RIGHT = 'R';
    public static final char CHAR_TURN_STOP = 'N';

    public AutopilotThread() {


    }

    public void run(){
        autopilot();
    }

    private void autopilot() {
        double targetBearing = 0;
        double currentBearing = 0;
        int sensitivity = 1;

        while(running){
            if(AutopilotFragment.targetBearing >= 0 && AutopilotFragment.sensitivity >= 1 && AutopilotFragment.sensitivity <=10){
                targetBearing = AutopilotFragment.targetBearing;
                currentBearing = AutopilotFragment.currentBearing;
                sensitivity = AutopilotFragment.sensitivity;
                Turn turn = calculateTurn(targetBearing,currentBearing);
                System.out.println("TURN: "+turn.direction+", "+turn.offsetDegrees);

                double lengthOfTurn = ((turn.offsetDegrees * sensitivity) / NORMALIZER)*1000;
                sendToController(turn,lengthOfTurn);

                sleepMilliseconds(CYCLE_SLEEP);

            }
        }
    }

    private void sendToController(Turn turn, double lengthOfTurn) {
        char turnTo;
        //make the turn;
        if(turn.direction == Turn.Direction.RIGHT){
            turnTo = CHAR_TURN_RIGHT;
        }else{
            turnTo = CHAR_TURN_LEFT;
        }
        System.out.println("SLEEP TIME: "+lengthOfTurn/1000 + "s");

        MainActivity.ardutooth.sendChar(turnTo);
        sleepMilliseconds(lengthOfTurn);
        MainActivity.ardutooth.sendChar(CHAR_TURN_STOP);
        //return rudder to (almost) previous position

        if(turnTo == CHAR_TURN_LEFT) turnTo = CHAR_TURN_RIGHT; //TODO put this in Turn class
        else if(turnTo == CHAR_TURN_RIGHT) turnTo = CHAR_TURN_LEFT;
        sleepMilliseconds(lengthOfTurn);
        MainActivity.ardutooth.sendChar(turnTo);
        sleepMilliseconds(lengthOfTurn-(lengthOfTurn*0.1));

        MainActivity.ardutooth.sendChar(CHAR_TURN_STOP);
        sleepMilliseconds(PAUSE_BEFORE_SPIN);
    }

    private void sleepMilliseconds(double value) {
        System.out.println("SLEEP: "+value+" ms");
        try {
            sleep((long) value);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Turn calculateTurn(double destination, double origin){
        double LH = origin - destination;
        if(LH<0) LH+=360;
        double RH = destination - origin;
        if(RH<0) RH+=360;

        Turn.Direction direction;
        double offsetDegrees;
        if(LH<RH){
            direction = Turn.Direction.LEFT;
            offsetDegrees = LH;
        }else{
            direction = Turn.Direction.RIGHT;
            offsetDegrees = RH;
        }

        Turn turn = new Turn(direction,offsetDegrees);
        return turn;
    }
}
