package com.vaslim.autopilot;

public class Turn {
    public enum Direction {
        LEFT,
        RIGHT
    }

    public static final char CHAR_TURN_LEFT = 'L';
    public static final char CHAR_TURN_RIGHT = 'R';
    public static final char CHAR_TURN_STOP = 'N';

    public Direction direction;
    public double offsetDegrees;

    public Turn(Direction direction, double offsetDegrees) {
        this.direction = direction;
        this.offsetDegrees = offsetDegrees;
    }

    public Turn(){

    }
    public char getTurnChar(){
        if(direction == Direction.RIGHT) return CHAR_TURN_RIGHT;
        else if(direction == Direction.LEFT) return  CHAR_TURN_LEFT;
        return CHAR_TURN_STOP;
    }
    public char getStopChar(){
        return CHAR_TURN_STOP;
    }
    public char getReverseChar(){
        if(direction == Direction.RIGHT) return CHAR_TURN_LEFT;
        else if(direction == Direction.LEFT) return  CHAR_TURN_RIGHT;
        return CHAR_TURN_STOP;
    }
}
