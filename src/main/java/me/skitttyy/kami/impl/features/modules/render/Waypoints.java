package me.skitttyy.kami.impl.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.WaypointManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.color.TextSection;
import me.skitttyy.kami.api.utils.render.Interpolator;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers;
import me.skitttyy.kami.impl.features.modules.client.HudColors;
import net.minecraft.client.render.Camera;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Waypoints extends Module {

    public static Waypoints INSTANCE;

    public Waypoints()
    {
        super("Waypoints", Category.Render);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {

        if(NullUtils.nullCheck()) return;
        if (!WaypointManager.INSTANCE.wayPoints.isEmpty())
        {
            RenderBuffers.scheduleRender(() ->
            {
                for (WaypointManager.WayPoint loc : WaypointManager.INSTANCE.wayPoints)
                {
                    if (loc.getName() == null) continue;

                    if ((mc.isInSingleplayer() && !loc.getServer().equals("SinglePlayer"))
                            || (mc.getNetworkHandler().getServerInfo() != null && !mc.getNetworkHandler().getServerInfo().address.contains(loc.getServer())))
                        continue;
                    if (!mc.world.getRegistryKey().getValue().getPath().equals(loc.getDimension())) continue;


                    renderWaypoint(loc, event);


                }
                RenderSystem.enableBlend();

            });
        }
    }



    private void renderWaypoint(WaypointManager.WayPoint loc, RenderWorldEvent event)
    {

        Vec3d interpolate = Interpolator.getInterpolatedEyePos(mc.getCameraEntity(), event.getTickDelta());
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d pos = camera.getPos();


        double dx = (pos.getX() - interpolate.getX()) - loc.getX();
        double dy = (pos.getY() - interpolate.getY()) - loc.getY();
        double dz = (pos.getZ() - interpolate.getZ()) - loc.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        TextSection[] text = new TextSection[1];

        String distance = Formatting.GRAY + " (" + Formatting.WHITE + MathHelper.floor(dist) + "m" + Formatting.GRAY + ")";

        text[0] = new TextSection(Formatting.WHITE + loc.getName() + distance, new Color(255, 255, 255));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderUtil.drawWaypoint(text, loc.getX(), loc.getY() + 1.4, loc.getZ(), mc.gameRenderer.getCamera(), HudColors.getTextColor(0));
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();

    }

    @Override
    public String getDescription()
    {
        return "Waypoints: renders waypoints (-waypoint create mcswag 0 64 0)";
    }
}
