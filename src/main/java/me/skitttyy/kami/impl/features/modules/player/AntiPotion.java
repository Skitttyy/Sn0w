package me.skitttyy.kami.impl.features.modules.player;


import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.entity.effect.StatusEffects;

public class AntiPotion extends Module {

    public AntiPotion()
    {
        super("AntiPotions", Category.Player);
    }


    Value<Boolean> levitation = new ValueBuilder<Boolean>()
            .withDescriptor("Levitiation")
            .withValue(false)
            .register(this);
    Value<Boolean> jumpBoost = new ValueBuilder<Boolean>()
            .withDescriptor("Jump Boost")
            .withValue(false)
            .register(this);
    Value<Boolean> slowness = new ValueBuilder<Boolean>()
            .withDescriptor("Slowness")
            .withValue(false)
            .register(this);


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;

        if (levitation.getValue() && mc.player.hasStatusEffect(StatusEffects.LEVITATION))
        {
            mc.player.removeStatusEffectInternal(StatusEffects.LEVITATION);
        }

        if (jumpBoost.getValue() && mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST))
        {
            mc.player.removeStatusEffectInternal(StatusEffects.JUMP_BOOST);
        }

        if (slowness.getValue() && mc.player.hasStatusEffect(StatusEffects.SLOWNESS))
        {
            mc.player.removeStatusEffectInternal(StatusEffects.SLOWNESS);
        }
    }

    @Override
    public String getDescription()
    {
        return "AntiPotion: Blocks malicious potion affects";
    }

}