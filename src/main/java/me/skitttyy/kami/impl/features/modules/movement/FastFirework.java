package me.skitttyy.kami.impl.features.modules.movement;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.util.math.MathHelper;

import static me.skitttyy.kami.impl.features.hud.Info.coordsDiff;

public class FastFirework extends Module {
    public static FastFirework INSTANCE;

    public Value<Number> speed = new ValueBuilder<Number>()
            .withDescriptor("Speed")
            .withValue(0.4f)
            .withRange(0.1, 4)
            .withPlaces(2)
            .register(this);
    Value<Boolean> boost = new ValueBuilder<Boolean>()
            .withDescriptor("Boost")
            .withValue(false)
            .register(this);
    public Value<Number> factor = new ValueBuilder<Number>()
            .withDescriptor("Factor")
            .withValue(0.25f)
            .withRange(0.1, 0.5f)
            .withPlaces(2)
            .withParent(boost)
            .withParentEnabled(true)
            .register(this);
    public FastFirework()
    {
        super("FastFirework", Category.Movement);
        INSTANCE = this;
    }

    public float getSpeed()
    {
        float amount = (float) (MathHelper.sqrt((float) (Math.pow(coordsDiff('x'), 2.0) + Math.pow(coordsDiff('z'), 2.0))) / 0.05 * 3.6);
        if (boost.getValue() && amount > 90)
        {

            return speed.getValue().floatValue() + (amount / 100f * factor.getValue().floatValue());
        }
        return speed.getValue().floatValue();
    }


    @Override
    public String getHudInfo()
    {
        return MathUtil.round(getSpeed(), 1) + "";
    }

    @Override
    public String getDescription()
    {
        return "FastFirework: Speeds up the boost from fireworks";
    }

}
