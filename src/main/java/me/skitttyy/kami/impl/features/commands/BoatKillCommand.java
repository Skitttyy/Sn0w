package me.skitttyy.kami.impl.features.commands;

import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.management.PacketManager;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class BoatKillCommand extends Command
{


    public BoatKillCommand()
    {
        super("BoatKill", "kills you with boats", new String[]{"boatkill"});
    }

    @Override
    public void run(String[] args)
    {
        if (!(mc.player.getVehicle() instanceof BoatEntity boat)) return;


        Vec3d originalPos = boat.getPos();
        boat.setPosition(originalPos.add(0, 0.05, 0));
        VehicleMoveC2SPacket groundPacket = new VehicleMoveC2SPacket(boat);
        boat.setPosition(originalPos.add(0, 20, 0));
        VehicleMoveC2SPacket skyPacket = new VehicleMoveC2SPacket(boat);
        boat.setPosition(originalPos);
        for (int i = 0; i < 20; i++)
        {
            PacketManager.INSTANCE.sendPacket(skyPacket);
            PacketManager.INSTANCE.sendPacket(groundPacket);
        }
        PacketManager.INSTANCE.sendPacket(new VehicleMoveC2SPacket(boat));
    }

}
