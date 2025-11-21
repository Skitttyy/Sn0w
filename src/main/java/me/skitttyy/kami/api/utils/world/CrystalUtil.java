package me.skitttyy.kami.api.utils.world;

import me.skitttyy.kami.api.management.breaks.BreakManager;
import me.skitttyy.kami.api.management.breaks.data.BreakData;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.wrapper.IMinecraft;
import me.skitttyy.kami.impl.features.modules.combat.CatAura;
import me.skitttyy.kami.impl.features.modules.misc.autobreak.AutoBreak;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.function.BiFunction;

public class CrystalUtil implements IMinecraft
{

    /**
     * Determines whether you can place a crystal without checking the entities inside it
     */
    public static boolean canPlaceCrystal(BlockPos pos, boolean protocol)
    {
        BlockState state = mc.world.getBlockState(pos);

        //can only place a block if obby or obsidian
        if (!state.getBlock().equals(Blocks.OBSIDIAN) && !state.getBlock().equals(Blocks.BEDROCK)) return false;

        BlockState crystal = mc.world.getBlockState(pos.up());
        BlockState above = mc.world.getBlockState(pos.up(2));
//         && !AutoBreak.INSTANCE.isInstantMining(pos.up())
        if (crystal.getBlock() != Blocks.AIR)
        {
            return false;
        }


        return !protocol || above.getBlock() == Blocks.AIR;
    }

    public static boolean canPlaceCrystalServer(BlockPos block)
    {
        BlockState blockState = mc.world.getBlockState(block);
        if (!blockState.isOf(Blocks.OBSIDIAN) && !blockState.isOf(Blocks.BEDROCK))
            return false;
        BlockPos blockPos2 = block.up();
        if (!mc.world.isAir(blockPos2))
            return false;
        double d = blockPos2.getX();
        double e = blockPos2.getY();
        double f = blockPos2.getZ();
        List<Entity> list = mc.world.getOtherEntities((Entity)null, new Box(d, e, f, d + 1.0D, e + 2.0D, f + 1.0D));

        return list.isEmpty();
    }

    public static boolean canPlaceCrystalAir(BlockPos block)
    {
        BlockPos blockPos2 = block.up();
        if (!mc.world.isAir(blockPos2))
            return false;
        double d = blockPos2.getX();
        double e = blockPos2.getY();
        double f = blockPos2.getZ();
        List<Entity> list = mc.world.getOtherEntities((Entity)null, new Box(d, e, f, d + 1.0D, e + 2.0D, f + 1.0D));

        return list.isEmpty();
    }

    public static boolean canPlaceCrystalIgnore(BlockPos pos, boolean protocol)
    {
        BlockState state = mc.world.getBlockState(pos);

        //can only place a block if obby or obsidian
        if (!state.getBlock().equals(Blocks.OBSIDIAN) && !state.getBlock().equals(Blocks.BEDROCK)) return false;

        BlockState above = mc.world.getBlockState(pos.up(2));
//         && !AutoBreak.INSTANCE.isInstantMining(pos.up())


        return !protocol || above.getBlock() == Blocks.AIR;
    }


    /**
     * gets the crstal damage
     */
    public static float calculateDamage(final LivingEntity entity, final Vec3d crystal, final boolean ignoreTerrain, boolean miningIgnore)
    {
        double ab = getExposure(crystal, entity, ignoreTerrain, miningIgnore, null);
        double w = Math.sqrt(entity.squaredDistanceTo(crystal)) / 12.0;
        double ac = (1.0 - w) * ab;
        float dmg = ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = calculateReductions(dmg, entity, mc.world.getDamageSources().explosion(null));
        return (float) Math.max(0.0, dmg);
    }

    public static float calculateDamage(final LivingEntity entity, final Vec3d crystal, final boolean ignoreTerrain, boolean miningIgnore, BlockPos ignore)
    {
        double ab = getExposure(crystal, entity, ignoreTerrain, miningIgnore, ignore);
        double w = Math.sqrt(entity.squaredDistanceTo(crystal)) / 12.0;
        double ac = (1.0 - w) * ab;
        float dmg = ((int) ((ac * ac + ac) / 2.0 * 7.0 * 12.0 + 1.0));
        dmg = calculateReductions(dmg, entity, mc.world.getDamageSources().explosion(null));
        return (float) Math.max(0.0, dmg);
    }


    public static float calculateReductions(float damage, LivingEntity entity, DamageSource damageSource)
    {
        if (damageSource.isScaledWithDifficulty())
        {
            switch (mc.world.getDifficulty())
            {
                case EASY -> damage = Math.min(damage / 2 + 1, damage);
                case HARD -> damage *= 1.5f;
            }
        }

        // Armor reduction
        damage = DamageUtil.getDamageLeft(entity, damage, damageSource, entity.getArmor(), (float) entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));

        // Resistance reduction
        damage = resistanceReduction(entity, damage);

        float protAmount = getProtectionAmount(entity.getArmorItems());

        if (protAmount > 0)
            damage = DamageUtil.getInflictedDamage(damage, protAmount);

        return Math.max(damage, 0);
    }


    public static int getProtectionAmount(Iterable<ItemStack> equipment)
    {
        MutableInt mutableInt = new MutableInt();
        equipment.forEach(i -> mutableInt.add(getProtectionAmount(i)));
        return mutableInt.intValue();
    }

    public static int getProtectionAmount(ItemStack stack)
    {
        if (CatAura.INSTANCE.armorAssume.getValue())
        {
            if (stack.hasEnchantments() && stack.getItem() instanceof ArmorItem armorItem)
            {
                return switch (armorItem.getType())
                {
                    case LEGGINGS -> 8;
                    case BODY -> 0;
                    default -> 4;
                };
            } else
            {
                return 0;
            }
        }
        int modifierBlast = EnchantmentHelper.getLevel(mc.world.getRegistryManager().get(Enchantments.BLAST_PROTECTION.getRegistryRef()).getEntry(Enchantments.BLAST_PROTECTION).get(), stack);
        int modifier = EnchantmentHelper.getLevel(mc.world.getRegistryManager().get(Enchantments.PROTECTION.getRegistryRef()).getEntry(Enchantments.PROTECTION).get(), stack);
        return modifierBlast * 2 + modifier;
    }

    private static float resistanceReduction(LivingEntity player, float damage)
    {
        StatusEffectInstance resistance = player.getStatusEffect(StatusEffects.RESISTANCE);
        if (resistance != null)
        {
            int lvl = resistance.getAmplifier() + 1;
            damage *= (1 - (lvl * 0.2f));
        }

        return Math.max(damage, 0);
    }


    /**
     * @param source
     * @param entity
     * @param ignoreTerrain
     * @return
     */
    private static float getExposure(final Vec3d source, final Entity entity, final boolean ignoreTerrain, boolean miningIgnore, BlockPos ignorePos)
    {
        final Box box = entity.getBoundingBox();
        return getExposure(source, box, ignoreTerrain, miningIgnore, ignorePos);
    }

    /**
     * @param source
     * @param box
     * @param ignoreTerrain
     * @return
     */
    private static float getExposure(final Vec3d source, final Box box, final boolean ignoreTerrain, boolean miningIgnore, BlockPos ignorePos)
    {
        RaycastFactory raycastFactory = getRaycastFactory(ignoreTerrain, miningIgnore, ignorePos);

        double xDiff = box.maxX - box.minX;
        double yDiff = box.maxY - box.minY;
        double zDiff = box.maxZ - box.minZ;

        double xStep = 1 / (xDiff * 2 + 1);
        double yStep = 1 / (yDiff * 2 + 1);
        double zStep = 1 / (zDiff * 2 + 1);

        if (xStep > 0 && yStep > 0 && zStep > 0)
        {
            int misses = 0;
            int hits = 0;

            double xOffset = (1 - Math.floor(1 / xStep) * xStep) * 0.5;
            double zOffset = (1 - Math.floor(1 / zStep) * zStep) * 0.5;

            xStep = xStep * xDiff;
            yStep = yStep * yDiff;
            zStep = zStep * zDiff;

            double startX = box.minX + xOffset;
            double startY = box.minY;
            double startZ = box.minZ + zOffset;
            double endX = box.maxX + xOffset;
            double endY = box.maxY;
            double endZ = box.maxZ + zOffset;

            for (double x = startX; x <= endX; x += xStep)
            {
                for (double y = startY; y <= endY; y += yStep)
                {
                    for (double z = startZ; z <= endZ; z += zStep)
                    {
                        Vec3d position = new Vec3d(x, y, z);

                        if (raycast(new ExposureRaycastContext(position, source), raycastFactory) == null) misses++;

                        hits++;
                    }
                }
            }

            return (float) misses / hits;
        }

        return 0f;
    }

    private static RaycastFactory getRaycastFactory(boolean ignoreTerrain, boolean miningIgnore, BlockPos ignorePos)
    {

        return (context, blockPos) ->
        {

            BlockState blockState = mc.world.getBlockState(blockPos);


            if(ignorePos != null && blockPos.equals(ignorePos)) return null;

            if (ignoreTerrain)
                if (blockState.getBlock().getBlastResistance() < 600) return null;


            if (miningIgnore)
            {

                if (BreakManager.INSTANCE.isPassed(blockPos, 0.7f, true))
                    return null;


                if (AutoBreak.INSTANCE.isAboutToBreak(blockPos))
                    return null;

                if (AutoBreak.INSTANCE.isInstantMining(blockPos))
                    return null;

            }
            return blockState.getCollisionShape(mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
        };
    }

    /* Raycasts */




    private static BlockHitResult raycast(ExposureRaycastContext context, RaycastFactory raycastFactory)
    {
        return BlockView.raycast(context.start, context.end, context, raycastFactory, ctx -> null);
    }

    public record ExposureRaycastContext(Vec3d start, Vec3d end)
    {
    }

    public static boolean isEndCrystal(Entity entity)
    {
        return (entity instanceof EndCrystalEntity);
    }


    @FunctionalInterface
    public interface RaycastFactory extends BiFunction<ExposureRaycastContext, BlockPos, BlockHitResult>
    {
    }

}
