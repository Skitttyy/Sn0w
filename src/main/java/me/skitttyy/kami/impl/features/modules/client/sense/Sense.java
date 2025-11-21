package me.skitttyy.kami.impl.features.modules.client.sense;

import com.mojang.blaze3d.systems.RenderSystem;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.color.TextSection;
import me.skitttyy.kami.api.utils.render.Interpolator;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers;
import me.skitttyy.kami.api.utils.world.WorldUtils;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Sense extends Module {

    public PingedLocation currLoc;
    public static Sense INSTANCE;
    Timer timeSystem = new Timer();

    public Sense()
    {
        super("Sense", Category.Client);
        INSTANCE = this;
        currLoc = null;
        this.setEnabled(true);
        KamiMod.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {

    }

    @Override
    public void onDisable()
    {
        setEnabled(true);

        if (mc.player != null && mc.world != null)
        {
            ChatUtils.sendMessage(Formatting.RED + "You shall not disable this. You are making mcswag ANGRY!!!");
        }
    }

    private void renderWaypoint(PingedLocation loc, RenderWorldEvent event)
    {
        int dimensionID = WorldUtils.getDimensionId(mc.world.getRegistryKey());
        double waypointX = loc.x;
        double waypointZ = loc.z;
        boolean inHell = dimensionID == -1;
        if (dimensionID != loc.dimensionID)
        {
            if (inHell && !(loc.dimensionID == -1))
            {
                waypointX = waypointX / 8;
                waypointZ = waypointZ / 8;
            } else if (!inHell && (loc.dimensionID == -1))
            {
                waypointX = waypointX * 8;
                waypointZ = waypointZ * 8;
            }
        }

        Vec3d interpolate = Interpolator.getInterpolatedEyePos(mc.getCameraEntity(), event.getTickDelta());
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d pos = camera.getPos();


        double dx = (pos.getX() - interpolate.getX()) - waypointX;
        double dy = (pos.getY() - interpolate.getY()) - loc.y;
        double dz = (pos.getZ() - interpolate.getZ()) - waypointZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        TextSection[] text = new TextSection[1];

        String distance = Formatting.GRAY + " (" + Formatting.WHITE + MathHelper.floor(dist) + "m" + Formatting.GRAY + ")";

        text[0] = new TextSection(Formatting.AQUA + loc.playerName + Formatting.WHITE + " Ping" + distance, new Color(255, 255, 255));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderUtil.drawWaypoint(text, waypointX, loc.y + 1.4, waypointZ, mc.gameRenderer.getCamera(), HudColors.getTextColor(0));
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();

    }

    @Override
    public String getDescription()
    {
        return "Sense: Communicate with other sn0w users!";
    }
}
