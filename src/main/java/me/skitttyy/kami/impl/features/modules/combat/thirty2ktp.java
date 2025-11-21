package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import net.minecraft.util.Formatting;

public class thirty2ktp extends Module {

    public thirty2ktp()
    {
        super("32kTp", Category.Combat);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        if (NullUtils.nullCheck()) return;

        PlayerUtils.setMotionY(100);
    }


    @Override
    public String getDescription()
    {
        return "32kTp: Somewhat of a " + Formatting.RED + "exploit" + Formatting.WHITE + " that is intended to be used with blink and longjump";
    }
}
