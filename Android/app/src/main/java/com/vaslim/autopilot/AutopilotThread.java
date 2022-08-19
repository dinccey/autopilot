package com.vaslim.autopilot;

import com.vaslim.autopilot.fragments.AutopilotFragment;

public class AutopilotThread extends Thread{

    public static final double NORMALIZER = 500;
    private static final double CYCLE_SLEEP = 100;
    private static final double DEVIATION_THRESHOLD = 0.7;
    private static final long MAX_SMALL_TURN_TOTAL = 900;
    public static final int SMALL_CORRECTION_MILISECONDS = 300;
    public static final long TIMEOUT_MILLISECONDS = 7000;

    private static Turn turn;
    private Turn committedTurn = null;
    private long turnStartTime;
    private long timeDifference = -1;
    private boolean returningRudder = false;
    private long smallTurnMiliseconds = 0;
    public volatile boolean running = true;
    private boolean maxTurn = false;
    private int allowedMaxDeviation = 0;
    private char currentTurn;
    private double offsetDegressDifference; //difference from previous turn


    public AutopilotThread() {
        turn = new Turn();

    }

    public void run(){
        autopilot();
    }

    private void autopilot() {
        double targetBearing = 0;
        double currentBearing = 0;
        int sensitivity = 1;

        while(running){
            if(SharedData.targetBearing >= 0 && SharedData.mode!=SharedData.Mode.MANUAL){
                targetBearing = SharedData.targetBearing;
                currentBearing = SharedData.currentBearing;
                sensitivity = SharedData.sensitivity;
                long maxLengthOfTurn = (long) (4*SMALL_CORRECTION_MILISECONDS);
                allowedMaxDeviation = sensitivity + 5;
                double previousTurnOffset = turn.offsetDegrees;
                calculateTurn(targetBearing,currentBearing);
                offsetDegressDifference = turn.offsetDegrees - previousTurnOffset;
                System.out.println("BEARING: "+currentBearing + " TARGET: "+targetBearing + "TURN: "+currentTurn+" "+timeDifference);

                rudderControl(sensitivity, maxLengthOfTurn);

                sleepMilliseconds(CYCLE_SLEEP); //bearing updates are not super fast

            }
        }
    }

    private void rudderControl(int sensitivity, long maxLengthOfTurn) {
        //IF MAX TURN WAS TOO LITTLE
        if(maxTurn && offsetDegressDifference>=0 && turn.offsetDegrees > allowedMaxDeviation){
            //timeDifference+=SMALL_CORRECTION_MILISECONDS;
            sendToController(turn.getTurnChar());
            sleepMilliseconds(SMALL_CORRECTION_MILISECONDS);
            sendToController(turn.getStopChar());

        }

        //SMALL CORRECTION UNLESS OFFSET TOO LARGE AND MAX SMALL TURNS EXCEEDED (negative for LEFT, positive for RIGHT)
        if(committedTurn ==  null  && turn.offsetDegrees < allowedMaxDeviation &&
                ((turn.direction == Turn.Direction.RIGHT && smallTurnMiliseconds < MAX_SMALL_TURN_TOTAL)||
                        (turn.direction == Turn.Direction.LEFT && smallTurnMiliseconds > (MAX_SMALL_TURN_TOTAL*-1)))){
            doSmallCorrection();
        }
        //IF NOT CORRECTING AND NEEDS CORrECTiNG
        else if(committedTurn ==  null  && turn.offsetDegrees > allowedMaxDeviation){
            doBiggerCorrection();
        }
        //IF CORRECTING AND SHOULD START RETURNING RUDDER TO NEUTRAL
        else if(committedTurn != null && !returningRudder && turn.offsetDegrees < allowedMaxDeviation){
        //else if(committedTurn != null && !returningRudder && turn.offsetDegrees < sensitivity && committedTurn.direction == turn.direction){
            doReturnRudderFromBiggerCorrection(sensitivity);
        }
        //IF RETURNING RUDDER TO NEUTRAL IS COMPLETE
        if((returningRudder && turnStartTime + timeDifference <= System.currentTimeMillis())){
            doEndReturnRudder();
        }
        //IF CORRECTING AND EXCEEDED MAX RUDDER TURN
        if(committedTurn != null && !returningRudder && (System.currentTimeMillis() - turnStartTime) >= maxLengthOfTurn && !maxTurn){
            doExceededMaxTurningTime(sensitivity, maxLengthOfTurn);
        }
    }

    private void doExceededMaxTurningTime(int sensitivity, long maxLengthOfTurn) {
        timeDifference = maxLengthOfTurn;
        timeDifference = reverseTimeCalculate(timeDifference, sensitivity);
        sendToController(turn.getStopChar());
        currentTurn = turn.getStopChar();
        maxTurn = true;
        System.out.println("CORRECTING------MAX TURN------");
    }

    private void doEndReturnRudder() {
        sendToController(turn.getStopChar());
        currentTurn = turn.getStopChar();
        System.out.println("END--------------");
        committedTurn = null; //the current commited turn has completed
        returningRudder = false;
        maxTurn = false;
        smallTurnMiliseconds = 0; //RESET small turns cummulative value
        timeDifference = 0;
    }

    private void doReturnRudderFromBiggerCorrection(int sensitivity) {
        long currentTime = System.currentTimeMillis();
        if(!maxTurn){ //if not exceeded max turning time
            timeDifference = currentTime - turnStartTime; //TODO too long
            timeDifference = reverseTimeCalculate(timeDifference, sensitivity);
            System.out.println("!maxTurn RETURN FROM BIGGER CORRECTION - timeDifference: "+timeDifference);
        }
        sendToController(committedTurn.getReverseChar()); //reverse turning direction
        currentTurn = committedTurn.getReverseChar();
        turnStartTime = System.currentTimeMillis(); //get time when started turning
        returningRudder = true; //started returning rudder
        System.out.println("RETURN------"+committedTurn.getReverseChar()+"------");
    }

    private void doBiggerCorrection() {
        committedTurn = new Turn(turn.direction,turn.offsetDegrees);
        sendToController(committedTurn.getTurnChar());
        currentTurn = turn.getTurnChar();
        turnStartTime = System.currentTimeMillis();
        timeDifference = 0;
        System.out.println("------"+committedTurn.getTurnChar()+"------");
    }

    private void doSmallCorrection() {
        if(offsetDegressDifference>=0){ //if there is no improvement in direction
           sendToController(turn.getTurnChar());
           currentTurn = turn.getTurnChar();
           sleepMilliseconds(SMALL_CORRECTION_MILISECONDS);
           sendToController(turn.getStopChar());
           currentTurn = turn.getStopChar();
        }
        timeDifference = 0;
        if(turn.getTurnChar() == Turn.CHAR_TURN_LEFT && turn.offsetDegrees>allowedMaxDeviation-5){
            smallTurnMiliseconds-=SMALL_CORRECTION_MILISECONDS;
        }
        else if(turn.getTurnChar() == Turn.CHAR_TURN_RIGHT && turn.offsetDegrees>allowedMaxDeviation-5){
            smallTurnMiliseconds+=SMALL_CORRECTION_MILISECONDS;
        }
        sleepMilliseconds(NORMALIZER*2); //sleep a little bit before reviewing changes
    }

    private void sendToController(char command) {
       MainActivity.ardutooth.sendChar(command);
    }

    private long reverseTimeCalculate(long time, int sensitivity){
        return time -(time*(sensitivity/6));
    }

    private void sleepMilliseconds(double value) {
        //System.out.println("SLEEP: "+value+" ms");
        try {
            sleep((long) value);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void calculateTurn(double destination, double origin){
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
    }
}
