package com.vaslim.autopilot.ruddercontrol;

import com.vaslim.autopilot.MainActivity;
import com.vaslim.autopilot.SharedData;
import com.vaslim.autopilot.Turn;

public class RudderControlAlg2 extends RudderControlThread {


    private static final long WAIT_TIME_MILIS = 100;
    private static final double MAX_RUDDER_RETURN_PERCENT = 0.9;
    private static final long MAX_CUMULATIVE_TURN_TIME = 5000;
    private static final double ACTION_OFFSET_DEGREES = 3;
    private static Turn turn;
    private boolean running = true;
    private static final int BASE_TURN_STEP = 100;

    private Long cumulativeTurnTime = 0L;

    public RudderControlAlg2() {
        turn = new Turn();

    }

    public void run(){
        autopilot();
    }

    @Override
    public void shutdown() {
        this.running = false;
    }

    private void autopilot() {
        while(running){
            if(SharedData.targetBearing >= 0 && SharedData.mode!=SharedData.Mode.MANUAL){
                doCorrection();
            }
        }
        MainActivity.rudderControlRunnable = null;
    }

    private void doCorrection() {
        Turn turn = calculateTurn(SharedData.targetBearing, SharedData.currentBearing);
        if(turn.offsetDegrees > ACTION_OFFSET_DEGREES){
            double targetBearing = SharedData.targetBearing;
            long turnStartTime = System.currentTimeMillis();
            sendToController(turn.getTurnChar());

            Turn turnInProgress = calculateTurn(SharedData.targetBearing, targetBearing);
            long turnTimeElapsed = 0;
            while(running
                    && (turnInProgress.offsetDegrees > turn.offsetDegrees / 2
                    || calculateMaxTurnTime() - turnTimeElapsed > 0)
                    && targetBearing == SharedData.targetBearing)
            {
                sleepMilliseconds(WAIT_TIME_MILIS);
                turnTimeElapsed = System.currentTimeMillis() - turnStartTime;
                turnInProgress = calculateTurn(SharedData.targetBearing, SharedData.currentBearing);
            }
            long turnEndTime = System.currentTimeMillis();
            long turnTimeTotal = turnEndTime -  turnStartTime;
            addCumulativeTime(turnTimeTotal, turn.getTurnChar());

            sendToController(turn.getReverseChar());
            turnStartTime = System.currentTimeMillis();
            long returnTimeElapsed = 0;
            while(running
                    && (turn.direction == turnInProgress.direction
                            && turnInProgress.offsetDegrees > ACTION_OFFSET_DEGREES
                            && returnTimeElapsed < turnTimeTotal * MAX_RUDDER_RETURN_PERCENT)
                    && targetBearing == SharedData.targetBearing)
            {
                returnTimeElapsed = System.currentTimeMillis() - turnStartTime;
                turnInProgress = calculateTurn(targetBearing, SharedData.currentBearing);
            }
            addCumulativeTime(System.currentTimeMillis() - turnStartTime, turn.getReverseChar());
            sendToController(turn.getStopChar());
        }
    }

    private long calculateMaxTurnTime() {
        if(Math.abs(cumulativeTurnTime) >= MAX_CUMULATIVE_TURN_TIME) return 0;
        return MAX_CUMULATIVE_TURN_TIME - Math.abs(cumulativeTurnTime);
    }

    private void addCumulativeTime(long turnTimeTotal,  char currentTurnChar) {
        if(currentTurnChar == 'L') cumulativeTurnTime += turnTimeTotal*(-1);
        if(currentTurnChar == 'R') cumulativeTurnTime += turnTimeTotal;
        System.out.println("CUMULATIVE TIME: "+cumulativeTurnTime + " turnTimeTotal:" + turnTimeTotal + " turnChar: "+ currentTurnChar);
    }

    private long calculateTurnTime(double offsetDegrees) {
        return (long) (offsetDegrees * 10 * SharedData.sensitivity);
    }


    private void sendToController(char command) {
       MainActivity.ardutooth.sendChar(command);
        System.out.println("COMMAND: " + command);
    }

    private void sleepMilliseconds(double value) {
        //System.out.println("SLEEP: "+value+" ms");
        try {
             Thread.sleep((long) value);
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
        turn.direction = direction;
        turn.offsetDegrees = offsetDegrees;
        return new Turn(turn.direction,turn.offsetDegrees);
    }
}
