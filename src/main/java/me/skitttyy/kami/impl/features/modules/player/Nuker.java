package me.skitttyy.kami.impl.features.modules.player;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import me.skitttyy.kami.api.event.events.world.EntityEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.HashSet;
import java.util.Set;



public class Nuker extends Module
{

    Value<String> mode = new ValueBuilder<String>()
            .withDescriptor("Mode")
            .withValue("All")
            .withModes("All", "Flatten", "Selective", "Farm", "Pearl")
            .register(this);
    Value<Boolean> predict = new ValueBuilder<Boolean>()
            .withDescriptor("Predict")
            .withValue(true)
            .withPage("Pearl")
            .withPageParent(mode)
            .register(this);
    Value<Number> breakRange = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(5d)
            .withRange(1d, 6d)
            .withPlaces(1)
            .register(this);
    Value<Boolean> rotate = new ValueBuilder<Boolean>()
            .withDescriptor("Rotate")
            .withValue(true)
            .register(this);
    Value<Boolean> strict = new ValueBuilder<Boolean>()
            .withDescriptor("Strict")
            .withValue(true)
            .register(this);
    private BlockPos current;

    private final Set<BlockPos> shulkerBlackList = new HashSet<BlockPos>();


    public Nuker()
    {
        super("Nuker", Category.Player);
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        BlockPos last = this.current;


        if (!mode.getValue().equals("Pearl"))
        {
            if (last == null || !canMine(last))
                current = getNukerBlock();


            if (current != null)
            {
                Direction side = BlockUtils.getMineableSide(current, strict.getValue());

                if (side == null) return;

                if (rotate.getValue())
                {
                    RotationUtils.doRotate(current, side);
                }

                mc.interactionManager.updateBlockBreakingProgress(current, side);
                mc.player.swingHand(Hand.MAIN_HAND);


            } else if (last != null)
            {
                if (mc.interactionManager.isBreakingBlock()) mc.interactionManager.cancelBlockBreaking();
            }
        } else
        {
            for (Entity entity : mc.world.getOtherEntities(null, mc.player.getBoundingBox()))
            {
                if (entity instanceof ItemFrameEntity)
                {

                    if (!((ItemFrameEntity) entity).getHeldItemStack().isEmpty())
                    {
                        PacketManager.INSTANCE.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                        PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    }

                    PacketManager.INSTANCE.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                    PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }

            }
        }
    }


    @SubscribeEvent
    public void onEntityAdd(EntityEvent.Add event)
    {
        if (NullUtils.nullCheck()) return;


        if (!predict.getValue()) return;

        if (event.getEntity() instanceof ItemFrameEntity entity)
        {
            if (new Box(mc.player.getBlockPos()).expand(0, 0.2, 0).intersects(entity.getBoundingBox()))
            {
                if (!entity.getHeldItemStack().isEmpty())
                {
                    PacketManager.INSTANCE.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                    PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }

                PacketManager.INSTANCE.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
                PacketManager.INSTANCE.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
    }

    public BlockPos getNukerBlock()
    {
        BlockPos nukerBlock = null;
        float range = breakRange.getValue().floatValue();

        double bestDistance = 9999;
        for (float x = range; x >= -range; x--)
        {
            for (float y = range; y >= (mode.getValue().equals("Flatten") ? 0 : -range); y--)
            {
                for (float z = range; z >= -range; z--)
                {
                    BlockPos pos = BlockPos.ofFloored(x, y, z);
                    pos = mc.player.getBlockPos().add(pos);
                    BlockState state = mc.world.getBlockState(pos);

                    double distance = mc.player.squaredDistanceTo(pos.toCenterPos());

                    if (distance >= bestDistance) continue;

                    if (canMine(pos))
                    {
                        bestDistance = distance;
                        nukerBlock = pos;
                    }
                }
            }
        }
        return nukerBlock;
    }

    public boolean canMine(BlockPos pos)
    {
        BlockState state = mc.world.getBlockState(pos);

        double distance = mc.player.squaredDistanceTo(pos.toCenterPos());


        if (mode.getValue().equals("Farm"))
        {
            if (state.getBlock() instanceof CropBlock crop)
            {
                int age = crop.getAge(state);
                if (age != crop.getMaxAge()) return false;
            } else
            {
                return false;
            }
        } else if (mode.getValue().equals("Selective"))
        {
            if (state.getBlock() instanceof ShulkerBoxBlock)
            {

            } else
            {
                return false;
            }
        }
        return !state.isAir() && !(state.getBlock() instanceof FluidBlock)
                && !(distance > MathUtil.square(breakRange.getValue().floatValue()))
                && BlockUtils.isMineable(state);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event)
    {
        if (NullUtils.nullCheck()) return;

        if (mode.getValue().equals("Selective"))
        {
            if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet)
            {
                if (BlockUtils.SHULKER_BLOCKS.contains(mc.player.getStackInHand(packet.getHand()).getItem()))
                {
                    shulkerBlackList.add(packet.getBlockHitResult().getBlockPos().offset(packet.getBlockHitResult().getSide()));
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event)
    {
        if (NullUtils.nullCheck()) return;

        if (mode.getValue().equals("Selective"))
        {
            if (event.getPacket() instanceof BlockUpdateS2CPacket packet)
            {
                if (shulkerBlackList.contains(packet.getPos()))
                {
                    if (!BlockUtils.SHULKER_BLOCKS.contains(packet.getState().getBlock()))
                    {
                        shulkerBlackList.remove(packet.getPos());
                    }
                }
            }
        }
    }


    @Override
    public String getHudInfo()
    {
        return mode.getValue();
    }

    @Override
    public String getDescription()
    {
        return "Nuker: Breaks blocks";
    }
}
