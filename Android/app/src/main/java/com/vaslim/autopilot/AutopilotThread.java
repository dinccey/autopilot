package com.vaslim.autopilot;

public class AutopilotThread extends Thread{

    private static final double CYCLE_SLEEP = 100;

    public static final int SMALL_CORRECTION_MILLISECONDS = 300;
    public static final int BIG_CORRECTION_MULTIPLIER = 3;

    private static Turn turn;
    public volatile boolean running = true;


    public AutopilotThread() {
        turn = new Turn();

    }

    public void run(){
        autopilot();
    }

    private void autopilot() {
        double targetBearing;
        double currentBearing;
        int sensitivity;

        while(running){
            if(SharedData.targetBearing >= 0 && SharedData.mode!=SharedData.Mode.MANUAL){
                targetBearing = SharedData.targetBearing;
                currentBearing = SharedData.currentBearing;
                sensitivity = SharedData.sensitivity;
                int smallCorrectionDeviation = sensitivity + 5;
                calculateTurn(targetBearing,currentBearing);
                if(turn.offsetDegrees <= smallCorrectionDeviation){
                    smallCorrection(smallCorrectionDeviation);
                }else{
                    bigCorrection(smallCorrectionDeviation);
                }

            }
        }
    }

    private void smallCorrection(int smallCorrectionDeviation){
        while(turn.offsetDegrees <= smallCorrectionDeviation){
            boolean improvement = isImprovement();
            if(!improvement){
                sendToController(turn.getTurnChar());
                sleepMilliseconds(SMALL_CORRECTION_MILLISECONDS);
                sendToController((turn.getStopChar()));
            }
            sleepMilliseconds(CYCLE_SLEEP);
            isImprovement();
        }
    }

    private void bigCorrection(int smallCorrectionDeviation){
        long turnTimeLimit = BIG_CORRECTION_MULTIPLIER * SMALL_CORRECTION_MILLISECONDS;
        long startTime = 0;
        long turningTimeTotal = 0;
        boolean isTurning = false;
        boolean maxTurn = false;
        Turn committedTurn = calculateTurn(SharedData.targetBearing,SharedData.currentBearing);
        turningTimeTotal = getTurningTimeTotal(smallCorrectionDeviation, turnTimeLimit, startTime, turningTimeTotal, isTurning, maxTurn, committedTurn);
        //RETURN RUDDER
        returnRudder(turningTimeTotal, committedTurn);
    }

    private void returnRudder(long turningTimeTotal, Turn committedTurn) {
        long startTime;
        sendToController(committedTurn.getReverseChar());
        startTime = System.currentTimeMillis();
        long currentTime = 0;
        while (currentTime-startTime<=reverseTimeCalculate(turningTimeTotal,SharedData.sensitivity)){
            currentTime = System.currentTimeMillis();
            if(!isImprovement() && committedTurn.direction == turn.direction) break;
        }
        sendToController(turn.getStopChar());
    }

    private long getTurningTimeTotal(int smallCorrectionDeviation, long turnTimeLimit, long startTime, long turningTimeTotal, boolean isTurning, boolean maxTurn, Turn committedTurn) {
        while (turn.offsetDegrees>= smallCorrectionDeviation){
            boolean improvement = isImprovement();
            long currentTime = System.currentTimeMillis();
            if(isTurning){
                turningTimeTotal = currentTime-startTime;
            }
            if(!improvement && !maxTurn){
                sendToController(committedTurn.getTurnChar());
                startTime = System.currentTimeMillis();
                isTurning = true;
            }
            if(!improvement && maxTurn){
                sendToController(committedTurn.getTurnChar());
                sleepMilliseconds(SMALL_CORRECTION_MILLISECONDS);
                sendToController(turn.getStopChar());
                turningTimeTotal += SMALL_CORRECTION_MILLISECONDS;

            }
            if(isTurning && currentTime- startTime >= turnTimeLimit){
                sendToController(turn.getStopChar());
                maxTurn = true;
                isTurning = false;
                turningTimeTotal = currentTime- startTime;
            }

            sleepMilliseconds(CYCLE_SLEEP);
            isImprovement();
        }
        sendToController(turn.getStopChar());
        return turningTimeTotal;
    }

    private boolean isImprovement() {
        double previousOffset = turn.offsetDegrees;
        Turn.Direction previousDirection = turn.direction;
        calculateTurn(SharedData.targetBearing, SharedData.currentBearing);
        return turn.offsetDegrees - previousOffset < 0 && previousDirection == turn.direction;
    }

    private void sendToController(char command) {
       MainActivity.ardutooth.sendChar(command);
    }

    private long reverseTimeCalculate(long time, int sensitivity){
        return time -(time*(sensitivity/50));
    }

    private void sleepMilliseconds(double value) {
        //System.out.println("SLEEP: "+value+" ms");
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
        turn.direction = direction;
        turn.offsetDegrees = offsetDegrees;
        return new Turn(turn.direction,turn.offsetDegrees);
    }
}
