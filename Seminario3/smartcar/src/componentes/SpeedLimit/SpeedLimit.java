package componentes.SpeedLimit;

import componentes.RoadPlace;

public class SpeedLimit {
    private RoadPlace rp;
    private int speedLimit;

    public SpeedLimit(RoadPlace rp, int speedLimit) {
        this.rp = rp;
        this.speedLimit = speedLimit;
    }

    public RoadPlace getLocation() {
        return rp;
    }

    public void setLocation(RoadPlace rp) {
        this.rp = rp;
    }

    public int getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(int speedLimit) {
        this.speedLimit = speedLimit;
    }
}
