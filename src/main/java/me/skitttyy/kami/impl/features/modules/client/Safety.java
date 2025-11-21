package me.skitttyy.kami.impl.features.modules.client;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.world.HoleUtils;
import net.minecraft.util.Formatting;

public class Safety extends Module {
    public Safety() {
        super("Safety", Category.Client);
        safety = SafetyMode.UNSAFE;

    }

    SafetyMode safety;

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event){
        if (NullUtils.nullCheck()) return;

        if (mc.world.getBlockState(PlayerUtils.getPlayerPos()).isSolid()){
            safety = SafetyMode.SAFE;
            return;
        }

        if (HoleUtils.isHole(PlayerUtils.getPlayerPos())){
            safety = SafetyMode.SAFE;
            return;
        }

        safety = SafetyMode.UNSAFE;
    }

    @Override
    public String getHudInfo() {
        if(NullUtils.nullCheck()){
            return "";
        }else {
            return safety != null ? safety.color + (safety.toString().substring(0, 1).toUpperCase() + safety.toString().toLowerCase().substring(1)) : "Null";
        }
    }

    enum SafetyMode {
        SAFE(Formatting.GREEN),
        UNSAFE(Formatting.RED);

        Formatting color;

        SafetyMode(Formatting color){
            this.color = color;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }


    @Override
    public String getDescription()
    {
        return "Safety: useless just looks cool in arraylist lol";
    }

}
