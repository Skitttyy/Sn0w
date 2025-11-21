package me.skitttyy.kami.impl.features.commands;

import me.skitttyy.kami.api.command.Command;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.math.MathUtil;
import net.minecraft.util.math.MathHelper;

public class  MathCommand extends Command {
    public MathCommand()
    {
        super("Math", "does skittys ap physics", new String[]{"math"});
    }

    @Override
    public void run(String[] args)
    {
        try
        {
            double x1 = Double.parseDouble(args[1]);
            double o1 = MathUtil.rad((float) Double.parseDouble(args[2]));

            double x2 = Double.parseDouble(args[3]);
            double o2 = MathUtil.rad((float) Double.parseDouble(args[4]));


            double time = Double.parseDouble(args[5]);


            double x1x = x1 * MathHelper.cos((float) o1);
            double x1y = x1 * MathHelper.sin((float) o1);


            double x2x = x2 * MathHelper.cos((float) o2);
            double x2y = x2 * MathHelper.sin((float) o2);

            double changeX = (x2x - x1x);
            double changeY = (x2y - x1y);

            ChatUtils.sendMessage("Change X: " + changeX);
            ChatUtils.sendMessage("Change Y: " + changeY);
            ChatUtils.sendMessage("magoA: " + MathUtil.magnitudeOfAcceleration(changeX, changeY, time));
            ChatUtils.sendMessage("Angle: " + MathUtil.fix(Math.toDegrees(Math.atan(changeY / changeX))));



        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public String[] getFill(String[] args)
    {
        return new String[]{"x1", "o1", "x2", "o2", "time"};
    }
}
