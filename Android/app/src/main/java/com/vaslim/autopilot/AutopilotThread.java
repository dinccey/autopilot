package com.vaslim.autopilot;

import com.vaslim.autopilot.fragments.AutopilotFragment;

public class AutopilotThread extends Thread{

    public static final double CONTROLLER_ROTATION_LENGTH_TIME_SECONDS = 0.5;
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

                //number of turns to send to arduino
                double rotationCommandsCount =  ((turn.offsetDegrees/100*sensitivity) / CONTROLLER_ROTATION_LENGTH_TIME_SECONDS);
                int rotateAbstractTime= (int) rotationCommandsCount;
                //cannot turn less than once
                System.out.println("ROTATION COUNT "+rotationCommandsCount);
                if(rotationCommandsCount<1 && rotationCommandsCount>0.1) rotateAbstractTime = 1;
                else if(turn.offsetDegrees < 0.7) rotateAbstractTime = 0;
                //send to arduino
                System.out.println("ROTATE "+rotateAbstractTime);
                sendToController(turn,rotateAbstractTime);
                sleepMilliseconds(CYCLE_SLEEP);
                //System.out.println("isConnected: "+MainActivity.ardutooth.isConnected());

            }
        }
    }

    private void sendToController(Turn turn, int rotationCommandsCount) {
        if (rotationCommandsCount == 0) return;
        char turnTo = 'N';
        //make the turn;
        if(turn.direction == Turn.Direction.RIGHT){
            turnTo = CHAR_TURN_RIGHT;
        }else{
            turnTo = CHAR_TURN_LEFT;
        }
        MainActivity.ardutooth.sendChar(turnTo);
        double sleepTime = CONTROLLER_ROTATION_LENGTH_TIME_SECONDS * 1000 * rotationCommandsCount;
        System.out.println("SLEEP TIME: "+sleepTime/1000 + "s");
        sleepMilliseconds(sleepTime);
        if(rotationCommandsCount == 1) {
            MainActivity.ardutooth.sendChar(CHAR_TURN_STOP);
            sleepMilliseconds(PAUSE_BEFORE_SPIN);
            return;
        };
        //return rudder to (almost) previous position
        sleepMilliseconds(PAUSE_BEFORE_SPIN);
        if(turnTo == CHAR_TURN_LEFT) turnTo = CHAR_TURN_RIGHT;
        else if(turnTo == CHAR_TURN_RIGHT) turnTo = CHAR_TURN_LEFT;

        MainActivity.ardutooth.sendChar(turnTo);
        sleepTime = CONTROLLER_ROTATION_LENGTH_TIME_SECONDS*1000*(rotationCommandsCount-1);
        sleepMilliseconds(sleepTime);

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
