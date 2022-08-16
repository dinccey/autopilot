package com.vaslim.autopilot;

import com.vaslim.autopilot.fragments.AutopilotFragment;

public class AutopilotThread extends Thread{

    public static final double NORMALIZER = 1300;
    private static final double CYCLE_SLEEP = 100;
    private static final double DEVIATION_THRESHOLD = 0.7;
    private static final long MAX_SMALL_TURN_TOTAL = 1000;
    public static final int SMALL_CORRECTION_MILISECONDS = 300;

    private static Turn turn;
    private Turn committedTurn = null;
    private long turnStartTime;
    private long timeDifference = -1;
    private boolean returningRudder = false;
    private long smallTurnMiliseconds = 0;
    public volatile boolean running = true;


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
            if(AutopilotFragment.targetBearing >= 0 && AutopilotFragment.sensitivity >= 1 && AutopilotFragment.sensitivity <=10){
                targetBearing = AutopilotFragment.targetBearing;
                currentBearing = AutopilotFragment.currentBearing;
                sensitivity = AutopilotFragment.sensitivity;
                long maxLengthOfTurn = (long) (sensitivity *NORMALIZER);
                calculateTurn(targetBearing,currentBearing);
                System.out.println("BEARING: "+currentBearing);

                rudderControl(sensitivity, maxLengthOfTurn);

                sleepMilliseconds(CYCLE_SLEEP); //bearing updates are not super fast

            }
        }
    }

    private void rudderControl(int sensitivity, long maxLengthOfTurn) {
        //SMALL CORRECTION UNLESS MAX SMALL TURNS EXCEEDED (negative for LEFT, positive for RIGHT)
        if(committedTurn ==  null  && turn.offsetDegrees < sensitivity &&
                ((turn.direction == Turn.Direction.RIGHT && smallTurnMiliseconds < MAX_SMALL_TURN_TOTAL)||
                        (turn.direction == Turn.Direction.LEFT && smallTurnMiliseconds > (MAX_SMALL_TURN_TOTAL*-1)))){
            doSmallCorrection();
        }
        //IF NOT CORRECTING AND NEEDS CORrECTiNG
        else if(committedTurn ==  null  && turn.offsetDegrees >= sensitivity){
            doBiggerCorrection();
        }
        //IF CORRECTING AND SHOULD START RETURNING RUDDER TO NEUTRAL
        else if(committedTurn != null && !returningRudder && turn.offsetDegrees <= sensitivity){
        //else if(committedTurn != null && !returningRudder && turn.offsetDegrees < sensitivity && committedTurn.direction == turn.direction){
            doReturnRudderFromBiggerCorrection(sensitivity);
        }
        //IF RETURNING RUDDER TO NEUTRAL IS COMPLETE
        if((returningRudder && turnStartTime + timeDifference >= System.currentTimeMillis())){
            doEndReturnRudder();
        }
        //IF CORRECTING AND EXCEEDED MAX RUDDER TURN
        if(committedTurn != null && (System.currentTimeMillis() - turnStartTime) >= maxLengthOfTurn){
            doExceededMaxTurningTime(sensitivity, maxLengthOfTurn);
        }
    }

    private void doExceededMaxTurningTime(int sensitivity, long maxLengthOfTurn) {
        timeDifference = maxLengthOfTurn;
        timeDifference = reverseTimeCalculate(timeDifference, sensitivity);
        sendToController(turn.getStopChar());
        System.out.println("CORRECTING------MAX TURN------");
    }

    private void doEndReturnRudder() {
        sendToController(turn.getStopChar());
        System.out.println("END--------------");
        committedTurn = null; //the current commited turn has completed
        returningRudder = false;
        smallTurnMiliseconds = 0; //RESET small turns cummulative value
    }

    private void doReturnRudderFromBiggerCorrection(int sensitivity) {
        long currentTime = System.currentTimeMillis();
        timeDifference = currentTime - turnStartTime;
        timeDifference = reverseTimeCalculate(timeDifference, sensitivity);
        sendToController(committedTurn.getReverseChar());
        System.out.println("RETURN------"+committedTurn.getReverseChar()+"------");
        turnStartTime = System.currentTimeMillis();
        returningRudder = true; //started returning rudder
    }

    private void doBiggerCorrection() {
        committedTurn = new Turn(turn.direction,turn.offsetDegrees);
        sendToController(committedTurn.getTurnChar());
        turnStartTime = System.currentTimeMillis();
        timeDifference = 0;
        System.out.println("------"+committedTurn.getTurnChar()+"------");
    }

    private void doSmallCorrection() {
        sendToController(turn.getTurnChar());
        sleepMilliseconds(SMALL_CORRECTION_MILISECONDS);
        sendToController(turn.getStopChar());
        if(turn.getTurnChar() == Turn.CHAR_TURN_LEFT){
            smallTurnMiliseconds-=SMALL_CORRECTION_MILISECONDS;
        }
        else if(turn.getTurnChar() == Turn.CHAR_TURN_RIGHT){
            smallTurnMiliseconds+=SMALL_CORRECTION_MILISECONDS;
        }
    }

    private void sendToController(char command) {
       MainActivity.ardutooth.sendChar(command);
    }

    private long reverseTimeCalculate(long time, int sensitivity){
        return time -(time*(sensitivity/10));
    }

    private void sleepMilliseconds(double value) {
        System.out.println("SLEEP: "+value+" ms");
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
