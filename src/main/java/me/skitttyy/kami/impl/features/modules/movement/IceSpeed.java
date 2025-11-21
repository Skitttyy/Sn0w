package me.skitttyy.kami.impl.features.modules.movement;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.block.Blocks;

public class IceSpeed extends Module {


    public IceSpeed()
    {
        super("IceSpeed", Category.Movement);
    }

    Value<Number> slipperiness = new ValueBuilder<Number>()
            .withDescriptor("Slipperiness")
            .withValue(0.4)
            .withRange(0.2, 1.5)
            .register(this);

    @Override
    public void onDisable()
    {
        super.onDisable();
        if (NullUtils.nullCheck()) return;

        if (Blocks.ICE == null || Blocks.PACKED_ICE == null || Blocks.FROSTED_ICE == null)
        {
            return;
        }
        Blocks.ICE.slipperiness = 0.98f;
        Blocks.PACKED_ICE.slipperiness = 0.98f;
        Blocks.FROSTED_ICE.slipperiness = 0.98f;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck())
        {
            return;
        }

        if (Blocks.ICE == null || Blocks.PACKED_ICE == null || Blocks.FROSTED_ICE == null)
        {
            return;
        }
        Blocks.ICE.slipperiness = this.slipperiness.getValue().floatValue();
        Blocks.PACKED_ICE.slipperiness = this.slipperiness.getValue().floatValue();
        Blocks.FROSTED_ICE.slipperiness = this.slipperiness.getValue().floatValue();

    }

    @Override
    public String getHudInfo()
    {
        return MathUtil.round(slipperiness.getValue().doubleValue(), 1) + "";
    }

    @Override
    public String getDescription()
    {
        return "IceSpeed: Slip N' Slide at rapid speeds";
    }
}

