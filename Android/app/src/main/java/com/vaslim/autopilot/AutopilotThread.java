package com.vaslim.autopilot;

public class AutopilotThread extends Thread{

    private static final double CYCLE_SLEEP = 100;

    public static final int SMALL_CORRECTION_MILLISECONDS = 300;
    public static final int BIG_CORRECTION_MULTIPLIER = 3;
    private static final long MAX_SMALL_TURN_CUMULATIVE = 1500;
    private static final long MAX_SMALL_TURN_CUMULATIVE_TIMEOUT = 5000;
    private static final double IS_IMPROVEMENT_THRESHOLD = 0.5;

    private static Turn turn;
    public volatile boolean running = true;
    private boolean reachedMaxSmallTurnLimit = false;
    private long reachedMaxSmallTurnLimitTimestamp;


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
        long maxTurnTime = 0;//negative for LEFT, positive for RIGHT
        while(turn.offsetDegrees <= smallCorrectionDeviation){
            boolean improvement = isImprovement();
            if(!improvement){
                System.out.println("SMALL offset:"+turn.offsetDegrees+" improvement: "+improvement);
                if((maxTurnTime*-1 < MAX_SMALL_TURN_CUMULATIVE && turn.getTurnChar() == Turn.CHAR_TURN_LEFT) ||
                        (maxTurnTime < MAX_SMALL_TURN_CUMULATIVE && turn.getTurnChar() == Turn.CHAR_TURN_RIGHT)){
                    System.out.println("SMALL TURN "+ turn.getTurnChar());
                    maxTurnTime = updateMaxTurnTime(maxTurnTime, turn.getTurnChar());
                    sendToController(turn.getTurnChar());
                    sleepMilliseconds(SMALL_CORRECTION_MILLISECONDS);
                    sendToController((turn.getStopChar()));
                }
                if(reachedMaxSmallTurnLimit && smallTurnLimitTimeout()){
                    System.out.println("SMALL TURN LIMIT TIMEOUT "+maxTurnTime);
                    if(maxTurnTime>0) maxTurnTime = maxTurnTime - SMALL_CORRECTION_MILLISECONDS;
                    else if(maxTurnTime<0) maxTurnTime = maxTurnTime + SMALL_CORRECTION_MILLISECONDS;
                }
            }
            sleepMilliseconds(CYCLE_SLEEP);
            //isImprovement();
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
        long currentTime = System.currentTimeMillis();
        while (currentTime-startTime<=reverseTimeCalculate(turningTimeTotal,SharedData.sensitivity)){
            currentTime = System.currentTimeMillis();
            //if(!isImprovement() && committedTurn.direction == turn.direction) break;//TODO remove
        }
        System.out.println("RETURN RUDDER TIME: "+(System.currentTimeMillis()-startTime));
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
            System.out.println("BIG TURN offset: "+turn.offsetDegrees+" improvement: "+improvement+ " turnTimeLimit: "+ turnTimeLimit);
            sleepMilliseconds(CYCLE_SLEEP);
            //isImprovement();
        }
        sendToController(turn.getStopChar());
        return turningTimeTotal;
    }

    private boolean isImprovement() {
        double previousOffset = turn.offsetDegrees;
        Turn.Direction previousDirection = turn.direction;
        sleepMilliseconds(SMALL_CORRECTION_MILLISECONDS*2);
        calculateTurn(SharedData.targetBearing, SharedData.currentBearing);
        System.out.println("TARGET: "+SharedData.targetBearing+ " CURRENT: "+SharedData.currentBearing+ " IMPROVEMENT: "+(turn.offsetDegrees - previousOffset < IS_IMPROVEMENT_THRESHOLD));
        return turn.offsetDegrees - previousOffset < IS_IMPROVEMENT_THRESHOLD ;//&& previousDirection == turn.direction;
    }

    private boolean smallTurnLimitTimeout() {
        long currentTime = System.currentTimeMillis();
        System.out.println("SMALL TURN TIMEOUT RECHED");
        if(currentTime-reachedMaxSmallTurnLimitTimestamp > MAX_SMALL_TURN_CUMULATIVE_TIMEOUT){
            return true;
        }
        return false;
    }

    private long updateMaxTurnTime(long maxTurnTime, char turnChar) {
        if(turnChar == Turn.CHAR_TURN_LEFT){
            maxTurnTime = maxTurnTime - SMALL_CORRECTION_MILLISECONDS;
        }
        else if(turnChar == Turn.CHAR_TURN_RIGHT){
            maxTurnTime = maxTurnTime + SMALL_CORRECTION_MILLISECONDS;
        }
        if(Math.abs(maxTurnTime) > MAX_SMALL_TURN_CUMULATIVE){
            reachedMaxSmallTurnLimit = true;
            reachedMaxSmallTurnLimitTimestamp = System.currentTimeMillis();
        }else{
            reachedMaxSmallTurnLimit = false;
        }
        System.out.println("maxTurnTime "+maxTurnTime);
        return maxTurnTime;
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
