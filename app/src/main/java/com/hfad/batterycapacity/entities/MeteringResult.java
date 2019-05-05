package com.hfad.batterycapacity.entities;

public class MeteringResult {
    double sumOfPowers;
    double avgVoltage;
    int startLevel;
    int finishLevel;

    public MeteringResult(double sumOfPowers, double avgVoltage, int startLevel, int finishLevel) {
        this.sumOfPowers = sumOfPowers;
        this.avgVoltage = avgVoltage;
        this.startLevel = startLevel;
        this.finishLevel = finishLevel;
    }

    public double getSumOfPowers() {
        return sumOfPowers;
    }

    public double getAvgVoltage() {
        return avgVoltage;
    }

    public int getStartLevel() {
        return startLevel;
    }

    public int getFinishLevel() {
        return finishLevel;
    }

    @Override
    public String toString() {
        return "MeteringResult{" +
                "sumOfPowers=" + sumOfPowers +
                ", avgVoltage=" + avgVoltage +
                ", startLevel=" + startLevel +
                ", finishLevel=" + finishLevel +
                '}';
    }
}
