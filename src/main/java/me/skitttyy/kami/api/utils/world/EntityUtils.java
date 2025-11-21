package me.skitttyy.kami.api.utils.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;


public class EntityUtils {
    /**
     * @param e
     * @return
     */
    public static boolean isMonster(Entity e) {
        return e instanceof Monster;
    }

    /**
     * @param e
     * @return
     */
    public static boolean isNeutral(Entity e) {
        return e instanceof Angerable && !((Angerable) e).hasAngerTime();
    }

    /**
     * @param e
     * @return
     */
    public static boolean isPassive(Entity e) {
        return e instanceof PassiveEntity || e instanceof AmbientEntity
                || e instanceof SquidEntity;
    }

    public static boolean isVehicle(Entity e) {
        return e instanceof BoatEntity || e instanceof MinecartEntity
                || e instanceof FurnaceMinecartEntity
                || e instanceof ChestMinecartEntity;
    }

}
