package com.hfad.batterycapacity.entities;

public class BatteryState {
    private double voltage;
    private double current;
    private int level;

    public BatteryState(double voltage, double current, int level) {
        this.voltage = voltage;
        this.current = current;
        this.level = level;
    }

    public double getCurrent() {
        return current;
    }

    public double getVoltage() {
        return voltage;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "BatteryState{" +
                "voltage=" + voltage +
                ", current=" + current +
                ", level=" + level +
                '}';
    }
}
