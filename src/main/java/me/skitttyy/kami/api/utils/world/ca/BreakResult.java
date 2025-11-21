package me.skitttyy.kami.api.utils.world.ca;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;


public class BreakResult {
    PlayerEntity target;
    double bestDMG;
    public Entity calcCrystal;
    float[] rots;
    public BreakResult(PlayerEntity target, double bestDMG, Entity calcCrystal, float rots[]){
        this.target = target;
        this.bestDMG = bestDMG;
        this.calcCrystal = calcCrystal;
        this.rots = rots;
    }
}
