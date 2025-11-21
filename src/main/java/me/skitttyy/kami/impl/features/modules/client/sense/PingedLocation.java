package me.skitttyy.kami.impl.features.modules.client.sense;

public class PingedLocation {
    public String playerName;
    public double x, y, z;
    int dimensionID;
    public PingedLocation(String playerName, double x, double y, double z, int dimensionID) {
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimensionID = dimensionID;
    }
}
