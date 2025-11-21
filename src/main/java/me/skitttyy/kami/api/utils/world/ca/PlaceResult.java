package me.skitttyy.kami.api.utils.world.ca;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;


public class PlaceResult {
    PlayerEntity target;
    double bestDMG;
    BlockPos calcPos;
    float[] rots;
    public PlaceResult(PlayerEntity target, double bestDMG, BlockPos calcPos, float rots[]){
        this.target = target;
        this.bestDMG = bestDMG;
        this.calcPos = calcPos;
        this.rots = rots;
    }
}
