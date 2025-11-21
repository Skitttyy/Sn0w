package me.skitttyy.kami.api.utils.players;

import me.skitttyy.kami.api.event.events.move.MoveEvent;
import me.skitttyy.kami.api.management.BoostManager;
import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.management.PacketManager;
import me.skitttyy.kami.api.management.RotationManager;
import me.skitttyy.kami.api.utils.chat.ChatMessage;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.ducks.IClientPlayerEntity;
import me.skitttyy.kami.api.utils.ducks.IVec3d;
import me.skitttyy.kami.api.utils.players.rotation.RotationUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;
import me.skitttyy.kami.impl.features.modules.combat.AntiHolecamp;
import me.skitttyy.kami.impl.features.modules.combat.FastProjectile;
import me.skitttyy.kami.impl.features.modules.player.PhaseWalk;
import me.skitttyy.kami.mixin.accessor.IClientWorld;
import me.skitttyy.kami.mixin.accessor.IFireworkRocketEntity;
import me.skitttyy.kami.mixin.accessor.ILivingEntity;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.scoreboard.*;
import net.minecraft.stat.Stat;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

public class PlayerUtils implements IMinecraft
{

    public static String getColoredHealth(PlayerEntity entityPlayer, boolean friends)
    {
        final double ceil = Math.ceil(entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount());
        return getHealthColor(entityPlayer, friends, false) + "" + (int) ceil;
    }


    public static Formatting getHealthColor(PlayerEntity entityPlayer, boolean friends, boolean fromTab)
    {
        float health = fromTab ? getTabHealth(entityPlayer.getName().getString()) : entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount();

        if (friends && FriendManager.INSTANCE.isFriend(entityPlayer))
            return Formatting.BLUE;

        return (health > 18.0f ? Formatting.GREEN : (health > 16.0f ? Formatting.DARK_GREEN : (health > 12.0f ? Formatting.YELLOW : (health > 8.0f ? Formatting.GOLD : (health > 5.0f ? Formatting.RED : Formatting.DARK_RED)))));
    }

    public static BlockPos getPlayerPos()
    {
        return BlockUtils.getRoundedBlockPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
    }

    public static BlockPos getPos(Entity entity)
    {
        return BlockUtils.getRoundedBlockPos(entity.getX(), entity.getY(), entity.getZ());
    }

    public static float getStrictBaseSpeed(float baseSpeed)
    {
        float maxModifier = baseSpeed;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED))
            maxModifier *= (float) 1 + (0.1D * mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier());
        return maxModifier;
    }


    public static boolean isBoostedByFirework()
    {
        for (Entity entity : mc.world.getEntities())
        {
            if (!(entity instanceof FireworkRocketEntity)) continue;

            FireworkRocketEntity firework = (FireworkRocketEntity) entity;

            if (((IFireworkRocketEntity) firework).getShooter().equals(mc.player))
                return true;
        }
        return false;
    }

    public static String getColoredDistance(PlayerEntity entityPlayer)
    {
        float distance = mc.player.distanceTo(entityPlayer);
        if (distance <= 5)
        {
            return Formatting.RED.toString();
        } else if (distance > 5 && distance <= 10)
        {
            return Formatting.GOLD.toString();
        } else if (distance > 10 && distance <= 15)
        {
            return Formatting.YELLOW.toString();
        } else if (distance > 15 && distance <= 20)
        {
            return Formatting.DARK_GREEN.toString();
        }

        return Formatting.GREEN.toString();
    }

    public static float getBaseSpeed(float baseSpeed)
    {
        float maxModifier = baseSpeed;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED))
            maxModifier *= (float) 1 + (0.2D * mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier());
        return maxModifier;
    }


    public static double getDefaultBaseSpeed(boolean boost)
    {
        double baseSpeed = 0.2873;
        if (mc.player != null && mc.player.hasStatusEffect(StatusEffects.SPEED))
        {
            final int amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        if (boost)
        {
            baseSpeed += BoostManager.INSTANCE.getBoostSpeed(false);
            baseSpeed = Math.min(baseSpeed, 0.465);
        }

        return baseSpeed;
    }

    public static void doStrafe(MoveEvent event, float speed)
    {
        if (PlayerUtils.isMoving())
        {
            Vec2f strafe = getStrafeVec(speed);
            event.setX(strafe.x);
            event.setZ(strafe.y);
        } else
        {
            event.setX(0.0);
            event.setZ(0.0);
        }
    }

    public static double distanceTo(AntiHolecamp.HoleCampBlock blockPos)
    {
        BlockPos pos = blockPos.getPos().offset(blockPos.getDirection());
        return pos.toCenterPos().squaredDistanceTo(mc.player.getEyePos());
    }


    public static double[] getMoveSpeed(double speed)
    {
        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();

        if (!isMoving())
        {
            return new double[]{0, 0};
        } else if (forward != 0)
        {
            if (strafe >= 1)
            {
                yaw += (float) (forward > 0 ? -45 : 45);
                strafe = 0;
            } else if (strafe <= -1)
            {
                yaw += (float) (forward > 0 ? 45 : -45);
                strafe = 0;
            }

            if (forward > 0)
                forward = 1;

            else if (forward < 0)
                forward = -1;
        }

        double sin = Math.sin(Math.toRadians(yaw + 90));
        double cos = Math.cos(Math.toRadians(yaw + 90));

        double motionX = (double) forward * speed * cos + (double) strafe * speed * sin;
        double motionZ = (double) forward * speed * sin - (double) strafe * speed * cos;

        return new double[]{motionX, motionZ};
    }

    public static double fovFromEntity(Entity en)
    {
        return ((double) (mc.player.getYaw() - fovToEntity(en)) % 360.0D + 540.0D) % 360.0D - 180.0D;
    }

    public static float fovToEntity(Entity ent)
    {
        double x = ent.getX() - mc.player.getX();
        double z = ent.getZ() - mc.player.getZ();
        double yaw = Math.atan2(x, z) * 57.2957795D;
        return (float) (yaw * -1.0D);
    }

    public static Vec2f getStrafeVec(final float speed)
    {
        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;

        float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw) * mc.getRenderTickCounter().getTickDelta(false);

        if (AntiCheat.INSTANCE.strafeFix.getValue())
        {
            yaw = RotationUtils.getActualYaw();
        }
        if (forward == 0.0f && strafe == 0.0f)
        {
            return Vec2f.ZERO;
        } else if (forward != 0.0f)
        {
            if (strafe >= 1.0f)
            {
                yaw += forward > 0.0f ? -45 : 45;
                strafe = 0.0f;
            } else if (strafe <= -1.0f)
            {
                yaw += forward > 0.0f ? 45 : -45;
                strafe = 0.0f;
            }
            if (forward > 0.0f)
            {
                forward = 1.0f;
            } else if (forward < 0.0f)
            {
                forward = -1.0f;
            }
        }
        float rx = (float) Math.cos(Math.toRadians(yaw));
        float rz = (float) -Math.sin(Math.toRadians(yaw));
        return new Vec2f((forward * speed * rz) + (strafe * speed * rx),
                (forward * speed * rx) - (strafe * speed * rz));
    }

    public static boolean canSneak(Vec2f vec)
    {
        Vec3d velocity = mc.player.getVelocity();
        double shiftX = velocity.getX() - vec.x;
        double shiftZ = velocity.getZ() - vec.y;

        return Math.abs(shiftX) > 9.0E-4 || Math.abs(shiftZ) > 9.0E-4;
    }

    public static void setSpeed(final double speed)
    {
        double[] dir = forward(speed);
        setMotionX(dir[0]);
        setMotionZ(dir[1]);
    }

    public static void setSpeed(final double speed, MoveEvent event)
    {
        double[] dir = forward(speed);
        event.motionX(dir[0]);
        event.motionZ(dir[1]);
    }

    public static float getYawOffset(Input input, float rotationYaw)
    {
        if (input.movementForward < 0.0f) rotationYaw += 180.0f;

        float forward = 1.0f;
        if (input.movementForward < 0.0f)
        {
            forward = -0.5f;
        } else if (input.movementForward > 0.0f)
        {
            forward = 0.5f;
        }

        float strafe = input.movementSideways;
        if (strafe > 0.0f) rotationYaw -= 90.0f * forward;
        if (strafe < 0.0f) rotationYaw += 90.0f * forward;
        return rotationYaw;
    }

    public static double getGroundLevel()
    {
        for (int i = (int) Math.round(mc.player.getY()); i > mc.world.getBottomY(); --i)
        {
            Box mybb = mc.player.getBoundingBox();
            Box box = new Box(mybb.minX, i - 1, mybb.minZ, mybb.maxX, i, mybb.maxZ);
            if (!mc.world.canCollide(mc.player, box) || !(box.minY <= mc.player.getY()))
            {
                continue;
            }
            return i;
        }
        return mc.world.getBottomY();
    }

    public static double getGroundLevel(Entity entity)
    {
        for (int i = (int) Math.round(entity.getY()); i > mc.world.getBottomY(); --i)
        {
            Box mybb = entity.getBoundingBox();
            Box box = new Box(mybb.minX, i - 1, mybb.minZ, mybb.maxX, i, mybb.maxZ);
            if (!mc.world.canCollide(entity, box) || !(box.minY <= entity.getY()))
            {
                continue;
            }
            return i;
        }
        return mc.world.getBottomY();
    }


    public static float getMoveYaw(float yaw)
    {
        if (mc.options.forwardKey.isPressed() && !mc.options.backKey.isPressed())
        {
            if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
            {
                yaw -= 45f;
            } else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
            {
                yaw += 45f;
            }
            // Forward movement - no change to yaw
        } else if (mc.options.backKey.isPressed() && !mc.options.forwardKey.isPressed())
        {
            yaw += 180f;
            if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
            {
                yaw += 45f;
            } else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
            {
                yaw -= 45f;
            }
        } else if (mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed())
        {
            yaw -= 90f;
        } else if (mc.options.rightKey.isPressed() && !mc.options.leftKey.isPressed())
        {
            yaw += 90f;
        }
        return RotationUtils.fixYaw(MathHelper.wrapDegrees(yaw));
    }

    public static boolean isMoving()
    {
        ClientPlayerEntity player = mc.player;
        if (player != null)
        {
            return player.input.getMovementInput().lengthSquared() != 0F;
        }
        return false;
    }


    public static boolean isMovingOrSneaking()
    {
        ClientPlayerEntity player = mc.player;

        if (mc.player.input.jumping && !mc.player.input.sneaking) return true;

        if (mc.player.input.sneaking && mc.player.input.jumping) return true;


        if (player != null)
        {
            return player.input.getMovementInput().lengthSquared() != 0F;
        }
        return false;
    }


    public static boolean isActuallyMoving()
    {
        return mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0 || mc.player.getVelocity().y != 0;
    }


    public static String getDimensionNameFromID(int id)
    {
        switch (id)
        {
            case 0:
                return "Overworld";
            case 1:
                return "End";
            case -1:
                return "Nether";

        }
        return "subhumaninterdimensional";
    }

    public static void attackTarget(Entity entity)
    {
        if (mc.player.isSprinting() && AntiCheat.INSTANCE.acMode.getValue().equals("Strong"))
            mc.player.setSprinting(false);
        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking());
        PacketManager.INSTANCE.sendPacket(packet);
        mc.player.attack(entity);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.resetLastAttackedTicks();
    }

    public static Vec2f safeWalk(final double motionX, final double motionZ)
    {
        final double offset = 0.05;

        double moveX = motionX;
        double moveZ = motionZ;

        float fallDist = -mc.player.getStepHeight();
        if (!mc.player.isOnGround())
        {
            fallDist = -1.5f;
        }

        while (moveX != 0.0 && mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(moveX, fallDist, 0.0)))
        {
            if (moveX < offset && moveX >= -offset)
            {
                moveX = 0.0;
            } else if (moveX > 0.0)
            {
                moveX -= offset;
            } else
            {
                moveX += offset;
            }
        }

        while (moveZ != 0.0 && mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(0.0, fallDist, moveZ)))
        {
            if (moveZ < offset && moveZ >= -offset)
            {
                moveZ = 0.0;
            } else if (moveZ > 0.0)
            {
                moveZ -= offset;
            } else
            {
                moveZ += offset;
            }
        }

        while (moveX != 0.0 && moveZ != 0.0 && mc.world.isSpaceEmpty(mc.player, mc.player.getBoundingBox().offset(moveX, fallDist, moveZ)))
        {
            if (moveX < offset && moveX >= -offset)
            {
                moveX = 0.0;
            } else if (moveX > 0.0)
            {
                moveX -= offset;
            } else
            {
                moveX += offset;
            }

            if (moveZ < offset && moveZ >= -offset)
            {
                moveZ = 0.0;
            } else if (moveZ > 0.0)
            {
                moveZ -= offset;
            } else
            {
                moveZ += offset;
            }
        }

        return new Vec2f((float) moveX, (float) moveZ);

    }

    public static boolean canEntityBeSeen(Entity entity)
    {
        Vec3d entityPos = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());

        if (entity instanceof EndCrystalEntity)
        {
            if (canVecBeSeen(entity.getPos().add(0, 1.700000047683716, 0)))
            {
                return true;
            }
        }
        return canVecBeSeen(entityPos);
    }

    public static boolean canVecBeSeen(Vec3d vec)
    {

        BlockHitResult result = mc.world.raycast(new RaycastContext(mc.player.getEyePos(), vec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        return result.getType() == HitResult.Type.MISS;
    }

    public static void moveTowards(MoveEvent event, Vec3d vec3d, double speed, boolean vertical)
    {
        float[] rots = RotationUtils.getRotationsTo(mc.player.getEyePos(), vec3d);
        double angle = Math.toRadians(rots[0]);
        double x = -Math.sin(angle) * speed;

        double y = -MathHelper.sin((float) Math.toRadians(rots[1])) * speed;
        double z = Math.cos(angle) * speed;
        double[] difference = new double[]{vec3d.x - mc.player.getX(), vec3d.y - mc.player.getY(), vec3d.z - mc.player.getZ()};

        event.setMovement(new Vec3d(Math.abs(x) < Math.abs(difference[0]) ? x : difference[0], event.getMovement().getY(), event.getMovement().getZ()));


        if (vertical)
            event.setMovement(new Vec3d(event.getMovement().getX(), Math.abs(y) < Math.abs(difference[1]) ? y : difference[1], event.getMovement().getZ()));

        event.setMovement(new Vec3d(event.getMovement().getX(), event.getMovement().getY(), Math.abs(z) < Math.abs(difference[2]) ? z : difference[2]));

        ((IVec3d) mc.player.getVelocity()).set(0, mc.player.getVelocity().y, 0);
        event.setCancelled(true);
    }

    public static void setMotionXZ(double x, double z)
    {
        Vec3d motion = mc.player.getVelocity();
        mc.player.setVelocity(x, motion.y, z);
    }


    public static Vec2f computeMovementInput()
    {
        float forward = 0F;
        if (mc.options.forwardKey.isPressed())
            forward += 1F;
        if (mc.options.backKey.isPressed())
            forward -= 1F;
        float sideways = 0F;
        if (mc.options.leftKey.isPressed())
            sideways += 1F;
        if (mc.options.rightKey.isPressed())
            sideways -= 1F;
        return new Vec2f(forward, sideways);
    }


    public void doYBoost(float maxSpeed)
    {
        if (mc.player.getVelocity().y < 0)
        {
            Vec3d current = mc.player.getVelocity();

            Vec3d glide = glide(0.60f);
            double currentSpeed = Math.hypot(current.x, current.z);

            double glideSpeed = Math.hypot(glide.x, glide.z);

            if (currentSpeed < glideSpeed)
            {
                PlayerUtils.setMotionXZ(glide.x, glide.z);
                double postGlideSpeed = Math.hypot(mc.player.getVelocity().x, mc.player.getVelocity().z);
                if (postGlideSpeed > maxSpeed)
                {
                    PlayerUtils.setMotionXZ(mc.player.getVelocity().x * maxSpeed / postGlideSpeed, mc.player.getVelocity().z * maxSpeed / postGlideSpeed);
                    if (currentSpeed < Math.hypot(mc.player.getVelocity().x, mc.player.getVelocity().z))
                    {
                        mc.player.setVelocity(current);
                    }
                }
            }
        }
    }

    public Vec3d glide(float speed)
    {
        Vec3d glide = getGlideVec(speed / 50.0f);
        Vec3d motion = mc.player.getVelocity();
        return new Vec3d(motion.x + glide.x, 0, motion.z + glide.z);
    }

    public Vec3d getGlideVec(float speed)
    {

        float yaw = mc.player.getYaw();
        final double x = speed * Math.cos(Math.toRadians(yaw + 90.0f));
        final double z = speed * Math.sin(Math.toRadians(yaw + 90.0f));
        return new Vec3d(x, 0.0d, z);
    }


    private static PendingUpdateManager getUpdateManager(ClientWorld world)
    {
        return ((IClientWorld) world).accquirePendingUpdateManager();
    }

    public static void clientJump()
    {
        float f = mc.player.getJumpVelocity();
        if (!(f <= 1.0E-5F))
        {
            Vec3d vec3d = mc.player.getVelocity();
            mc.player.setVelocity(vec3d.x, (double) f, vec3d.z);
            if (mc.player.isSprinting())
            {
                float g = RotationUtils.getActualYaw() * 0.017453292F;
                mc.player.addVelocityInternal(new Vec3d((double) (-MathHelper.sin(g)) * 0.2, 0.0, (double) MathHelper.cos(g) * 0.2));
            }

            mc.player.velocityDirty = true;
        }
    }

    public static double[] forward(final double d)
    {
        return forward(d, mc.player.getYaw(), false);
    }

    public static double[] forward(final double distance, float iYaw, boolean override)
    {
        float forward = override ? 1.0f : mc.player.input.movementForward;
        float side = override ? 0.0f : mc.player.input.movementSideways;
        float yaw = iYaw;
        if (forward != 0.0f)
        {
            if (side > 0.0f)
            {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f)
            {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f)
            {
                forward = 1.0f;
            } else if (forward < 0.0f)
            {
                forward = -1.0f;
            }
        }
        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));

        final double posX = forward * distance * cos + side * distance * sin;
        final double posZ = forward * distance * sin - side * distance * cos;
        return new double[]{posX, posZ};
    }


    public static boolean isAboveWater(final Entity entity)
    {
        return isAboveWater(entity, false);
    }

    public static boolean isAboveWater(final Entity entity, final boolean packet)
    {
        if (entity == null)
        {
            return false;
        }
        final double y = entity.getY() - (packet ? 0.03 : ((entity instanceof PlayerEntity) ? 0.2 : 0.5));
        for (int x = MathHelper.floor(entity.getX()); x < MathHelper.ceil(entity.getX()); ++x)
        {
            for (int z = MathHelper.floor(entity.getZ()); z < MathHelper.ceil(entity.getZ()); ++z)
            {
                final BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);
                if (mc.world.getBlockState(pos).getBlock() instanceof FluidBlock)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCloseToAboveWater(final Entity entity, final boolean packet)
    {
        if (entity == null)
        {
            return false;
        }
        final double y = entity.getY() - (packet ? 0.1 : ((entity instanceof PlayerEntity) ? 0.2 : 0.5));
        for (int x = MathHelper.floor(entity.getX()); x < MathHelper.ceil(entity.getX()); ++x)
        {
            for (int z = MathHelper.floor(entity.getZ()); z < MathHelper.ceil(entity.getZ()); ++z)
            {
                final BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);
                if (mc.world.getBlockState(pos).getBlock() instanceof FluidBlock)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isInWater(final Entity entity)
    {
        if (entity == null)
        {
            return false;
        }
        final double y = entity.getY() + 0.01;
        for (int x = MathHelper.floor(entity.getX()); x < MathHelper.ceil(entity.getX()); ++x)
        {
            for (int z = MathHelper.floor(entity.getZ()); z < MathHelper.ceil(entity.getZ()); ++z)
            {
                final BlockPos pos = new BlockPos(x, (int) y, z);
                if (mc.world.getBlockState(pos).getBlock() instanceof FluidBlock)
                {
                    return true;
                }
            }
        }
        return false;
    }


    public static void setMotionX(double x)
    {
        mc.player.setVelocity(x, mc.player.getVelocity().y, mc.player.getVelocity().z);
    }

    public static void setMotionY(double y)
    {
        mc.player.setVelocity(mc.player.getVelocity().x, y, mc.player.getVelocity().z);
    }

    public static void setMotionZ(double z)
    {
        mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y, z);
    }


    public static boolean isEatingGap()
    {
        return (mc.player.getMainHandStack().getItem().equals(Items.GOLDEN_APPLE) || mc.player.getMainHandStack().getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)) && mc.player.isUsingItem();
    }

    public static boolean isElytraEquipped()
    {
        ItemStack stack = mc.player.getInventory().getArmorStack(2);
        if (stack == null) return false;


        Item chestplate = stack.getItem();

        return chestplate.equals(Items.ELYTRA);
    }


    public static void phaseSpeed(MoveEvent event, float speed, float factor)
    {
        if (PlayerUtils.isMoving())
        {
            float boost = 0f;
            if (!mc.player.horizontalCollision && PhaseWalk.INSTANCE.isPhasing() && mc.player.isOnGround())
            {
                boost = factor * 0.2F + 0.1F;
            }

            Vec2f strafe = getStrafeVec(speed + boost);
            event.motionX(strafe.x);
            event.motionZ(strafe.y);
        } else
        {
            event.setX(0.0);
            event.setZ(0.0);
        }
    }

    public static boolean runningPhysics = false;

    public static void runPhysicsTick()
    {
        if (runningPhysics) return;

        runningPhysics = true;

        int lastSwing = ((ILivingEntity) mc.player)
                .getLastAttackedTicks();

        int hurtTime = mc.player.hurtTime;
        float prevSwingProgress = mc.player.lastHandSwingProgress;
        float swingProgress = mc.player.handSwingProgress;
        int swingProgressInt = mc.player.handSwingTicks;
        boolean isSwingInProgress = mc.player.handSwinging;
        float rotationYaw = mc.player.getYaw();
        float prevRotationYaw = mc.player.prevYaw;
        float renderYawOffset = mc.player.renderYaw;
        float prevRenderYawOffset = mc.player.lastRenderYaw;
        float rotationYawHead = mc.player.headYaw;
        float prevRotationYawHead = mc.player.prevHeadYaw;
        float cameraYaw = mc.player.renderYaw;
        float prevCameraYaw = mc.player.lastRenderYaw;
        // float renderArmYaw        = mc.player.renderArmYaw;
        // float prevRenderArmYaw    = mc.player.prevRenderArmYaw;
        // float renderArmPitch      = mc.player.renderArmPitch;
        // float prevRenderArmPitch  = mc.player.prevRenderArmPitch;
        float walk = mc.player.distanceTraveled;
        // float prevWalk            = mc.player.prevDistanceWalkedModified;
        // double chasingPosX        = mc.player.chasingPosX;
        // double prevChasingPosX    = mc.player.prevChasingPosX;
        // double chasingPosY        = mc.player.chasingPosY;
        // double prevChasingPosY    = mc.player.prevChasingPosY;
        // double chasingPosZ        = mc.player.chasingPosZ;
        // double prevChasingPosZ    = mc.player.prevChasingPosZ;
        // float limbSwingAmount     = mc.player.limbAnimator.swin;
        // float prevLimbSwingAmount = mc.player.prevLimbSwingAmount;
        float limbSwing = mc.player.limbAnimator.getSpeed();

        ((IClientPlayerEntity) mc.player).doTick();

        ((ILivingEntity) mc.player)
                .setLastAttackedTicks(lastSwing);

        mc.player.hurtTime = hurtTime;
        mc.player.lastHandSwingProgress = prevSwingProgress;
        mc.player.handSwingProgress = swingProgress;
        mc.player.handSwingTicks = swingProgressInt;
        mc.player.handSwinging = isSwingInProgress;
        mc.player.setYaw(rotationYaw);
        mc.player.prevYaw = prevRotationYaw;
        // mc.player.renderYaw                  = renderYawOffset;
        // mc.player.lastRenderYaw              = prevRenderYawOffset;
        mc.player.headYaw = rotationYawHead;
        mc.player.prevHeadYaw = prevRotationYawHead;
        mc.player.renderYaw = cameraYaw;
        mc.player.lastRenderYaw = prevCameraYaw;
        // mc.player.renderArmYaw               = renderArmYaw;
        // mc.player.prevRenderArmYaw           = prevRenderArmYaw;
        // mc.player.renderArmPitch             = renderArmPitch;
        // mc.player.prevRenderArmPitch         = prevRenderArmPitch;
        mc.player.distanceTraveled = walk;
        // mc.player.prevDistanceWalkedModified = prevWalk;
        // mc.player.chasingPosX                = chasingPosX;
        // mc.player.prevChasingPosX            = prevChasingPosX;
        // mc.player.chasingPosY                = chasingPosY;
        // mc.player.prevChasingPosY            = prevChasingPosY;
        // mc.player.chasingPosZ                = chasingPosZ;
        // mc.player.prevChasingPosZ            = prevChasingPosZ;
        // mc.player.limbSwingAmount            = limbSwingAmount;
        // mc.player.prevLimbSwingAmount        = prevLimbSwingAmount;
        // mc.player.limbSwing                  = limbSwing;

        ((IClientPlayerEntity) mc.player).doSendMovementPackets();
        runningPhysics = false;
    }

    public static void use()
    {
        PacketManager.INSTANCE.sendPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
    }

    public static void switchAndUse(int slot)
    {
        int oldSlot = mc.player.getInventory().selectedSlot;
        InventoryUtils.switchToSlot(slot);
        use();
        InventoryUtils.switchToSlot(oldSlot);
    }

    public static void doFirework()
    {

        int fireworkHotbar = InventoryUtils.getHotbarItemSlot(Items.FIREWORK_ROCKET);
        if (fireworkHotbar != -1)
        {
            switchAndUse(fireworkHotbar);
        } else
        {
            int fireworkInv = InventoryUtils.getInventoryItemSlot(Items.FIREWORK_ROCKET);
            if (fireworkInv == -1)
            {
                ChatUtils.sendMessage(new ChatMessage(
                        "No fireworks in inv",
                        false,
                        0
                ));
                return;
            }
            InventoryUtils.swap(fireworkInv, mc.player.getInventory().selectedSlot);
            use();
            InventoryUtils.swap(fireworkInv, mc.player.getInventory().selectedSlot);
        }
    }

    public static BlockPos getAnvilPos(PlayerEntity player)
    {

        double decimalPoint = mc.player.getY() - Math.floor(mc.player.getY());
        return new BlockPos(player.getBlockX(), (int) (decimalPoint > 0.8 ? Math.floor(mc.player.getY()) + 1 : Math.floor(mc.player.getY())), player.getBlockZ());
    }


    public static void equipElytra()
    {

        if (!PlayerUtils.isElytraEquipped())
        {
            int slot = InventoryUtils.getInventoryItemSlot(Items.ELYTRA);
            if (slot != -1)
            {
                InventoryUtils.swapArmor(2, slot);
                PacketManager.INSTANCE.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                mc.player.startFallFlying();
            }
        }

    }

    public static void disEquipElytra()
    {

        if (PlayerUtils.isElytraEquipped())
        {
            int slot = InventoryUtils.findChestplate();
            if (slot != -1)
            {
                InventoryUtils.swapArmor(2, slot);
            } else
            {

            }
        }
    }

    public static BlockPos getHighestPlaceableAnvilPos(PlayerEntity player)
    {
        BlockPos pos = null;
        for (int i = (int) player.getY(); i < 255; i++)
        {
            BlockPos currentPos = new BlockPos(player.getBlockX(), i, player.getBlockZ());
            if (currentPos == null) continue;

            if (BlockUtils.canPlaceBlock(currentPos, false) && mc.player.getEyePos().distanceTo(currentPos.toCenterPos()) < 5)
            {
                pos = currentPos;
            }
        }
        return pos;

    }

    public static int getTabHealth(String player) {
        int score = 0;
        Scoreboard scoreboard = mc.world.getScoreboard();
        if (scoreboard == null) {return 0;}
        ScoreboardObjective obj = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.LIST);

        ReadableScoreboardScore playerScore = scoreboard.getScore(ScoreHolder.fromName(player), obj);
        if (playerScore != null) {
            score = playerScore.getScore();
        }
        return score;
    }

}
