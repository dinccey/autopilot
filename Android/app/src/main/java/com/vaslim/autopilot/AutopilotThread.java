package com.vaslim.autopilot;

import com.vaslim.autopilot.fragments.AutopilotFragment;

import io.github.giuseppebrb.ardutooth.Ardutooth;

public class AutopilotThread extends Thread{

    public static final double CONTROLLER_ROTATION_LENGTH_TIME_SECONDS = 0.5;
    public volatile boolean running = true;
    public static final char CHAR_TURN_LEFT = 'L';
    public static final char CHAR_TURN_RIGHT = 'R';
    Ardutooth ardutooth;


    public AutopilotThread(Ardutooth ardutooth) {
        this.ardutooth = ardutooth;
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

                //number of rotations to send to arduino
                int rotationCommandsCount = (int) ((turn.offsetDegrees/100*sensitivity) / CONTROLLER_ROTATION_LENGTH_TIME_SECONDS);
                //send to arduino
                sendToController(turn, rotationCommandsCount);

            }
        }
    }

    private void sendToController(Turn turn, int rotationCommandsCount) {
        char turnTo = ' ';
        //make the turn;
        for (int i = 0; i < rotationCommandsCount; i++) {

            if(turn.direction == Turn.Direction.RIGHT){
                turnTo = CHAR_TURN_RIGHT;
            }else{
                turnTo = CHAR_TURN_LEFT;
            }

            ardutooth.sendChar(turnTo);
            //sleep the amount of time it takes to complete one rotation cycle
            try {
            sleep((long) (CONTROLLER_ROTATION_LENGTH_TIME_SECONDS*1000));
             } catch (InterruptedException e) {
            e.printStackTrace();
            }
        }
        //return rudder to (almost) previous position
        for (int i = 0; i < rotationCommandsCount -1; i++) {
            //reverse direction
            if(turnTo == CHAR_TURN_LEFT) turnTo = CHAR_TURN_RIGHT;
            if(turnTo == CHAR_TURN_RIGHT) turnTo = CHAR_TURN_LEFT;

            ardutooth.sendChar(turnTo);
            //sleep the amount of time it takes to complete one rotation cycle
            try {
                sleep((long) (CONTROLLER_ROTATION_LENGTH_TIME_SECONDS*1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
