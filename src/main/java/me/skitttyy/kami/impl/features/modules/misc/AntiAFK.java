package me.skitttyy.kami.impl.features.modules.misc;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Hand;

import java.util.Random;

public class AntiAFK extends Module {

    public static AntiAFK INSTANCE;
    Value<Boolean> walk = new ValueBuilder<Boolean>()
            .withDescriptor("Walk")
            .withValue(true)
            .register(this);
    Value<Boolean> punch = new ValueBuilder<Boolean>()
            .withDescriptor("Punch")
            .withValue(true)
            .register(this);
    Value<Boolean> jump = new ValueBuilder<Boolean>()
            .withDescriptor("Jump")
            .withValue(false)
            .register(this);
    Value<Boolean> look = new ValueBuilder<Boolean>()
            .withDescriptor("Look")
            .withValue(false)
            .register(this);
    Random random;

    public AntiAFK() {
        super("AntiAFK", Category.Misc);
        INSTANCE = this;
        random = new Random();
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event) {
        if (NullUtils.nullCheck())
            return;

        randomAction();

    }

    public void randomAction() {
        if (mc.player.age % 30 == 0 && look.getValue()) {
            mc.player.setYaw((this.random.nextInt(360) - 180));
        }

        if (mc.player.age % 40 == 0 && jump.getValue()) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
            }
        }

        if (mc.player.age % 45 == 0 && punch.getValue()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        if (walk.getValue()) {
            KeyBinding.setKeyPressed(mc.options.forwardKey.getDefaultKey(), random.nextBoolean());
            KeyBinding.setKeyPressed(mc.options.backKey.getDefaultKey(), random.nextBoolean());
            KeyBinding.setKeyPressed(mc.options.leftKey.getDefaultKey(), random.nextBoolean());
            KeyBinding.setKeyPressed(mc.options.rightKey.getDefaultKey(), random.nextBoolean());
        }
    }
    @Override
    public String getDescription() {
        return "AntiAFK: Moves around so u cant get kicked for afk";
    }
}
