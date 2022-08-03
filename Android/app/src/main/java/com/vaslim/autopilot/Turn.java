package com.vaslim.autopilot;

public class Turn {
    public enum Direction {
        LEFT,
        RIGHT
    }

    public Direction direction;
    public double offsetDegrees;

    public Turn(Direction direction, double offsetDegrees) {
        this.direction = direction;
        this.offsetDegrees = offsetDegrees;
    }
}
