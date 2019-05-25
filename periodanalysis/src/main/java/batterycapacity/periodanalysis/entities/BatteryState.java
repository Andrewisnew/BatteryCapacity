package batterycapacity.periodanalysis.entities;

public class BatteryState {
    private double voltage;
    private double current;
    private int level;
    private int period;


    public BatteryState(double voltage, double current, int level, int period) {
        this.voltage = voltage;
        this.current = current;
        this.level = level;
        this.period = period;
    }

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

    public int getPeriod() {
        return period;
    }

    @Override
    public String toString() {
        return "BatteryState{" +
                "voltage=" + voltage +
                ", current=" + current +
                ", level=" + level +
                ", period=" + period +
                '}';
    }


}
