package me.skitttyy.kami.impl.features.modules.movement;


import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.world.CollisionBoxEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.mixin.accessor.IPlayerMoveC2SPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShapes;

public class Jesus extends Module {

    public static Jesus INSTANCE;
    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("Solid")
            .withModes("Solid", "Bounce", "Dolphin", "Dynamic")
            .register(this);

    public Jesus()
    {
        super("Jesus", Category.Movement);
        INSTANCE = this;
    }

    Timer timer = new Timer();

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event)
    {
        if (NullUtils.nullCheck()) return;


        if (getMode().equals("Solid"))
        {
            if (PlayerUtils.isInWater(mc.player) && !mc.player.isSneaking())
            {
                PlayerUtils.setMotionY(0.1);
                if (mc.player.getVehicle() != null && !(mc.player.getVehicle() instanceof BoatEntity))
                {
                    mc.player.getVehicle().setVelocity(mc.player.getVehicle().getVelocity().x, 0.3, mc.player.getVehicle().getVelocity().z);
                }
            }
        }


    }

    @SubscribeEvent
    public void onPlayerPre(TickEvent.PlayerTickEvent.Pre event)
    {

        if (NullUtils.nullCheck()) return;


        if (mode.getValue().equals("Dynamic") && !mc.options.jumpKey.isPressed())
        {
            grounded = false;
            jumping = false;
        }

        if (getMode().equals("Bounce"))
            doTrampoline();

        if (getMode().equals("Dolphin"))
        {
            if (PlayerUtils.isAboveWater(mc.player) && !mc.player.isSneaking() && !mc.options.jumpKey.isPressed())
            {
                PlayerUtils.setMotionY(0.1);
            }
        }
    }

    @SubscribeEvent
    public void onCollision(CollisionBoxEvent event)
    {
        if (NullUtils.nullCheck()) return;

        BlockState state = event.getState();

        if (mode.getValue().equals("Solid") && ((state.getBlock() == Blocks.WATER | state.getFluidState().getFluid() == Fluids.WATER) || state.getBlock() == Blocks.LAVA))
        {
            event.setCancelled(true);
            event.setVoxelShape(VoxelShapes.cuboid(new Box(0.0, 0.0, 0.0, 1.0, 0.99, 1.0)));
        }
    }

    private static boolean isAboveLand(final Entity entity)
    {
        if (entity == null)
        {
            return false;
        }
        final double y = entity.getY() - 0.01;
        for (int x = MathHelper.floor(entity.getX()); x < MathHelper.ceil(entity.getX()); ++x)
        {
            for (int z = MathHelper.floor(entity.getZ()); z < MathHelper.ceil(entity.getZ()); ++z)
            {
                final BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);
                if (mc.world.getBlockState(pos).isFullCube(mc.world, pos))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;


        if (getMode().equals("Solid"))
        {
            if (event.getPacket() instanceof PlayerMoveC2SPacket && PlayerUtils.isAboveWater(mc.player, true) && !PlayerUtils.isInWater(mc.player) && !isAboveLand(mc.player))
            {
                final int ticks = mc.player.age % 2;
                if (ticks == 0)
                {
                    PlayerMoveC2SPacket packet = ((PlayerMoveC2SPacket) event.getPacket());
                    IPlayerMoveC2SPacket inter = ((IPlayerMoveC2SPacket) packet);

                    inter.setY(inter.getY() + 0.02);

                }
            }
        }
    }

    boolean grounded;
    boolean jumping;

    //thx future

    private void doTrampoline()
    {
        if (mc.player.isSneaking())
        {
            return;
        }

        int minY = MathHelper.floor(mc.player.getBoundingBox().minY - 0.2D);
        boolean inLiquid = checkIfBlockInBB(FluidBlock.class, minY) != null;

        if (inLiquid && !mc.player.isSneaking())
        {
            mc.player.setOnGround(false);
        }

        Block block = mc.world.getBlockState(new BlockPos((int) Math.floor(mc.player.getX()), (int) Math.floor(mc.player.getY()), (int) Math.floor(mc.player.getZ()))).getBlock();

        if (jumping && !mc.player.getAbilities().flying && !mc.player.isSubmergedInWater())
        {
            if (mc.player.getVelocity().y < -0.3D || mc.player.isOnGround() || mc.player.isHoldingOntoLadder())
            {
                jumping = false;
                return;
            }

            PlayerUtils.setMotionY(mc.player.getVelocity().y / 0.9800000190734863D + 0.08D);
            PlayerUtils.setMotionY(mc.player.getVelocity().y - 0.03120000000005D);
        }

        if (mc.player.isSubmergedInWater() || mc.player.isInLava())
        {
            PlayerUtils.setMotionY(0.1);
        }

        if (!mc.player.isInLava() && (!mc.player.isSubmergedInWater()) && block instanceof FluidBlock && mc.player.getVelocity().y < 0.2D)
        {
            PlayerUtils.setMotionY(0.5);
            jumping = true;
        }
    }

    public static BlockState checkIfBlockInBB(Class<? extends Block> blockClass, int minY)
    {
        for (int iX = MathHelper.floor(mc.player.getBoundingBox().minX); iX < MathHelper.ceil(mc.player.getBoundingBox().maxX); iX++)
        {
            for (int iZ = MathHelper.floor(mc.player.getBoundingBox().minZ); iZ < MathHelper.ceil(mc.player.getBoundingBox().maxZ); iZ++)
            {
                BlockState state = mc.world.getBlockState(new BlockPos(iX, minY, iZ));
                if (blockClass.isInstance(state.getBlock()))
                {
                    return state;
                }
            }
        }
        return null;
    }

    public String getMode()
    {
        if (!NullUtils.nullCheck())
        {
            if (mode.getValue().equals("Dynamic"))
            {
                if (mc.options.jumpKey.isPressed())
                {
                    return "Bounce";
                } else
                {
                    return "Solid";
                }
            }
        }
        return mode.getValue();
    }

    @Override
    public String getHudInfo()
    {
        return getMode();
    }

    @Override
    public String getDescription()
    {
        return "Jesus: Walk on water like jesus";
    }
}
