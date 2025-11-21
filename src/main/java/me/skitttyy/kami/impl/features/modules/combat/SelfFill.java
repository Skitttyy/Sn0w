package me.skitttyy.kami.impl.features.modules.combat;

import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.event.events.move.MovementPacketsEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.PriorityManager;
import me.skitttyy.kami.api.utils.NullUtils;
import me.skitttyy.kami.api.utils.Timer;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.players.InventoryUtils;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.impl.features.modules.player.Blink;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;


public class SelfFill extends Module {

    Timer hitcrystalCooldown = new Timer();
    Timer burrowCooldown = new Timer();

    Value<String> rotate = new ValueBuilder<String>()
            .withDescriptor("Rotate", "RotateNewEnum")
            .withValue("None")
            .withModes("None", "Normal")
            .register(this);
    Value<Boolean> strictDirection = new ValueBuilder<Boolean>()
            .withDescriptor("Strict Direction")
            .withValue(false)
            .register(this);
    Value<Boolean> breakCrystal = new ValueBuilder<Boolean>()
            .withDescriptor("Break")
            .withValue(false)
            .register(this);
    Value<Number> breakDelay = new ValueBuilder<Number>()
            .withDescriptor("Break Delay")
            .withValue(0)
            .withRange(0, 1000)
            .withAction(set -> hitcrystalCooldown.setDelay(set.getValue().longValue()))
            .withParent(breakCrystal)
            .withParentEnabled(true)
            .register(this);
    Value<Number> ticksExisted = new ValueBuilder<Number>()
            .withDescriptor("Ticks Existed")
            .withValue(0.0f)
            .withRange(0.0f, 10.0f)
            .withPlaces(0)
            .withParent(breakCrystal)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> await = new ValueBuilder<Boolean>()
            .withDescriptor("Await")
            .withValue(false)
            .register(this);
    Value<Number> burrowDelay = new ValueBuilder<Number>()
            .withDescriptor("Burrow Delay")
            .withValue(100)
            .withRange(0, 1000)
            .withAction(set -> burrowCooldown.setDelay(set.getValue().longValue()))
            .withParent(await)
            .withParentEnabled(true)
            .register(this);
    Value<Boolean> breakSync = new ValueBuilder<Boolean>()
            .withDescriptor("Sync")
            .withValue(false)
            .withParent(await)
            .withParentEnabled(true)

            .register(this);
    //todo make this worth with any items (whitelist command?)
    Value<String> block = new ValueBuilder<String>()
            .withDescriptor("Block")
            .withValue("EnderChest")
            .withModes("EnderChest", "Obsidian", "Piston", "Dynamic")
            .register(this);
    Value<Boolean> dynamic = new ValueBuilder<Boolean>()
            .withDescriptor("Dynamic")
            .withValue(false)
            .register(this);
    Value<Boolean> sixB = new ValueBuilder<Boolean>()
            .withDescriptor("6b6t")
            .withValue(false)
            .register(this);
    public SelfFill()
    {
        super("SelfFill", Category.Combat);
    }

    BlockPos originalPos;
    List<BlockPos> placedPositions = new ArrayList<>();

    double startY = Double.NaN;
    boolean didBreak = false;
    @SubscribeEvent
    public void onUpdate(TickEvent.PlayerTickEvent.Pre event)
    {
        if (NullUtils.nullCheck()) return;


        if (Blink.INSTANCE.isEnabled()) return;

        if (!mc.player.isOnGround())
        {
            return;
        } else
        {
            if (Double.isNaN(startY))
                startY = mc.player.getY();
        }

        if (!Double.isNaN(startY) && mc.player.getY() > startY)
        {
            this.setEnabled(false);
            startY = Double.NaN;
            return;
        }
        if (InventoryUtils.getHotbarItemSlot(getSelectedItem()) == -1)
        {
            setEnabled(false);
            ChatUtils.sendMessage(new ChatMessage(
                    Formatting.RED + "Could not find block",
                    true,
                    64583
            ));
            return;
        }


        for (BlockPos position : placedPositions)
        {
            if (!BlockUtils.isReplaceable(position))
            {
                setEnabled(false);
                return;
            }
        }

        BlockPos pos = getPlayerPos();
        if (canPlaceBurrow(pos, false, false))
        {
            boolean intersectsWithCrystal = BlockUtils.isInterceptedByCrystal(pos);
            if (breakCrystal.getValue() && hitcrystalCooldown.isPassed())
            {
                EndCrystalEntity crystal = BlockUtils.attackInPos(pos, ticksExisted.getValue().intValue());
                didBreak = crystal != null;
                if (didBreak)
                {
                    hitcrystalCooldown.resetDelay();
                    if (!rotate.getValue().equals("None"))
                    {
                        float[] rots = RotationUtils.getRotationsTo(mc.player.getEyePos(), crystal.getPos().subtract(0, 1.1, 0));
                        RotationUtils.setRotation(rots);
                        PriorityManager.INSTANCE.lockUsageLock("SelfFill");

                    }
                }
            }

            if (breakSync.getValue() && await.getValue())
            {
                if (didBreak || !intersectsWithCrystal)
                {
                    PriorityManager.INSTANCE.lockUsageLock("SelfFill");
                    if (rotate.getValue().equals("Normal") && !didBreak)
                        RotationUtils.doRotate(pos, strictDirection.getValue());
                }
            } else
            {
                PriorityManager.INSTANCE.lockUsageLock("SelfFill");
                if (rotate.getValue().equals("Normal") && !didBreak)
                    RotationUtils.doRotate(pos, strictDirection.getValue());
            }


        }


    }

    Item getSelectedItem()
    {
        String b = block.getValue();
        Item selectedItem = Items.OBSIDIAN;
        switch (b)
        {
            case "Piston":
                selectedItem = Items.PISTON;
                break;
            case "EnderChest":
                selectedItem = Items.ENDER_CHEST;
                break;
            case "Obsidian":
                selectedItem = Items.OBSIDIAN;
                break;
            case "Dynamic":
                if (InventoryUtils.getHotbarItemSlot(Items.OBSIDIAN) == -1)
                {
                    return Items.ENDER_CHEST;
                }
                break;
        }
        return selectedItem;
    }


    public void doRotate(BlockPos pos, MovementPacketsEvent event)
    {
        float[] rots = RotationUtils.getBlockRotations(pos, BlockUtils.getPlaceableSide(pos, strictDirection.getValue()));

        if (rots != null)
        {
            event.setCancelled(true);
            event.setYaw(rots[0]);
            event.setPitch(rots[1]);
            RotationUtils.setRotation(rots);
        }
    }


    public void doRotate(BlockPos pos, boolean packet)
    {
        float[] rots = RotationUtils.getBlockRotations(pos, BlockUtils.getPlaceableSide(pos, strictDirection.getValue()));
        if (packet)
        {
            PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rots[0], rots[1], false));
        } else
        {
            RotationUtils.setRotation(rots);
        }
    }

    public void doRotate(float[] rots, MovementPacketsEvent event)
    {
        event.setCancelled(true);
        event.setYaw(rots[0]);
        event.setPitch(rots[1]);
        RotationUtils.setRotation(rots);
    }


    public void doRotate(float[] rots, boolean packet)
    {
        if (packet)
        {
            PacketManager.INSTANCE.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rots[0], rots[1], false));
        } else
        {
            RotationUtils.setRotation(rots);
        }
    }


    @SubscribeEvent
    public void onInput(TickEvent.MovementTickEvent.Post event)
    {
        if (NullUtils.nullCheck()) return;


        if (Blink.INSTANCE.isEnabled()) return;

        if (!mc.player.isOnGround()) return;

        BlockPos burrowPos = getPlayerPos();

        if (!canPlaceBurrow(burrowPos, true, await.getValue() && breakSync.getValue())) return;

        if (!BlockUtils.isInterceptedByCrystal(burrowPos))
        {
            didBreak = false;
        }
        int old = mc.player.getInventory().selectedSlot;
        if (Double.isNaN(startY) && mc.player.isOnGround())
            startY = mc.player.getY();
        InventoryUtils.switchToSlot(InventoryUtils.getHotbarItemSlot(getSelectedItem()));
        resetPos();

        if (!(rotate.getValue().equals("None")) && didBreak)
            doRotate(burrowPos, true);
        BlockUtils.placeBlock(burrowPos, BlockUtils.getPlaceableSide(burrowPos, strictDirection.getValue()), true);
        if (!placedPositions.contains(burrowPos))
            placedPositions.add(burrowPos);
        mc.player.setPosition(mc.player.getX(), mc.player.getY() - 1.170010501788138, mc.player.getZ());

        if(sixB.getValue())
        {
            movePacket(mc.player.getX(), mc.player.getY() + 1.01, mc.player.getZ(), false);
        }else{
            movePacket(mc.player.getX(), mc.player.getY() + 1.242610501394747 , mc.player.getZ(), false);
            movePacket(mc.player.getX(), mc.player.getY() + 2.340020003576277, mc.player.getZ(), false);


        }
        didBreak = false;
        InventoryUtils.switchToSlot(old);
        burrowCooldown.resetDelay();
        if (!await.getValue())
        {
            setEnabled(false);
        }

    }

    public void resetPos()
    {
        movePacket(mc.player.getX(), mc.player.getY() + 0.41999848688698, mc.player.getZ(), false);
        movePacket(mc.player.getX(), mc.player.getY() + 0.7500015, mc.player.getZ(), false);
        movePacket(mc.player.getX(), mc.player.getY() + 0.999997, mc.player.getZ(), false);
        movePacket(mc.player.getX(), mc.player.getY() + 1.17000300178814, mc.player.getZ(), false);
        movePacket(mc.player.getX(), mc.player.getY() + 1.170010501788138, mc.player.getZ(), false);
        mc.player.setPosition(mc.player.getX(), mc.player.getY() + 1.170010501788138, mc.player.getZ());

    }

    private void movePacket(double x, double y, double z, boolean ground)
    {
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, ground));
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        placedPositions.clear();
        if (PriorityManager.INSTANCE.isUsageLocked() && Objects.equals(PriorityManager.INSTANCE.usageLockCause, "SelfFill"))
        {
            PriorityManager.INSTANCE.unlockUsageLock();
        }

        if (NullUtils.nullCheck()) return;


        startY = Double.NaN;
    }


    @Override
    public void onEnable()
    {
        super.onEnable();

        placedPositions.clear();


        if (NullUtils.nullCheck()) return;


        startY = Double.NaN;
        originalPos = getPlayerPos();

        // If we can't place in our actual pos then toggle and return
        if (mc.world.getBlockState(originalPos).getBlock().equals(Blocks.OBSIDIAN) || intersectsWithEntity(getPlayerPos()))
        {
            setEnabled(false);
        }
        if (PriorityManager.INSTANCE.isUsageLocked() && !Objects.equals(PriorityManager.INSTANCE.usageLockCause, "SelfFill"))
        {
            PriorityManager.INSTANCE.usageLockCause = "SelfFill";
        }
    }


    public boolean canPlaceBurrow(BlockPos burrowPos, Boolean disable, boolean doSync)
    {
        if (mc.world.getBlockState(burrowPos.down()).getBlock() instanceof AirBlock)
        {
            if (!await.getValue() && disable)
            {
                setEnabled(false);
            }
            return false;
        }
        if (BlockUtils.isInterceptedByOtherPlayer(burrowPos))
        {
            setEnabled(false);
            return false;
        }

        if ((!mc.world.getBlockState(burrowPos).isReplaceable() || mc.world.getBlockState(burrowPos).getBlock() instanceof FluidBlock) && await.getValue())
        {
            setEnabled(false);
            return false;
        }

        if (!mc.world.getBlockState(burrowPos.up(2)).isReplaceable())
        {
            if (!await.getValue() && disable)
            {
                setEnabled(false);
            }
            return false;
        }

        if (await.getValue() && !burrowCooldown.isPassed()) return false;

        if (doSync)
        {
            boolean intercepted = BlockUtils.isInterceptedByCrystal(burrowPos);
            if (!didBreak && intercepted)
            {
                return false;
            }
        }
        return true;
    }

    boolean intersectsWithEntity(final BlockPos pos)
    {
        for (final Entity entity : mc.world.getEntities())
        {
            if (entity.equals(mc.player)) continue;
            if (entity instanceof ItemEntity) continue;

            if (entity instanceof EndCrystalEntity && (breakCrystal.getValue() || await.getValue()))
            {
                continue;
            }
            if (new Box(pos).intersects(entity.getBoundingBox())) return true;
        }
        return false;
    }


    BlockPos getPlayerPos()
    {
        double decimalPoint = mc.player.getY() - Math.floor(mc.player.getY());

        return new BlockPos(mc.player.getBlockPos().getX(), (int) (decimalPoint > 0.8 ?
                Math.floor(mc.player.getY()) + 1 :
                Math.floor(mc.player.getY())), mc.player.getBlockPos().getZ());
//        return BlockUtils.getRoundedBlockPos(
//                mc.player.getX(),
//                decimalPoint > 0.8 ?
//                        Math.floor(mc.player.getY()) + 1 :
//                        Math.floor(mc.player.getY()),
//                mc.player.getZ()
//        );
    }

    @Override
    public String getDescription()
    {
        return "SelfFill/Burrow: lags back to place a block inside of you";
    }
}