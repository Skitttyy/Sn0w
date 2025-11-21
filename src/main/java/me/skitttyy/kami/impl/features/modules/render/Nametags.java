package me.skitttyy.kami.impl.features.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.render.RenderWorldEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.management.FriendManager;
import me.skitttyy.kami.api.management.PopManager;
import me.skitttyy.kami.api.utils.color.RainbowUtil;
import me.skitttyy.kami.api.utils.math.MathUtil;
import me.skitttyy.kami.api.utils.color.ColorUtil;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.players.PlayerUtils;
import me.skitttyy.kami.api.utils.render.Interpolator;
import me.skitttyy.kami.api.utils.render.RenderUtil;
import me.skitttyy.kami.api.utils.color.TextSection;
import me.skitttyy.kami.api.utils.render.world.buffers.RenderBuffers;
import me.skitttyy.kami.api.utils.world.HoleUtils;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.modules.client.FontModule;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Nametags extends Module
{

    /**
     * @see me.skitttyy.kami.mixin.MixinEntityRenderer for nametag cancel
     */
    public static Nametags INSTANCE;
    Value<String> page = new ValueBuilder<String>()
            .withDescriptor("Page")
            .withValue("Colors")
            .withModes("Colors", "Items")
            .withAction(s -> handlePage(s.getValue()))
            .register(this);

    public Value<Sn0wColor> borderColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Border Color")
            .withValue(new Sn0wColor(255, 0, 0, 255))
            .register(this);
    public Value<Boolean> safeBorder = new ValueBuilder<Boolean>()
            .withDescriptor("Safe Border")
            .withValue(false)
            .register(this);
    public Value<Sn0wColor> safeColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Safe Color")
            .withValue(new Sn0wColor(0, 255, 0, 255))
            .register(this);
    public Value<Sn0wColor> boxColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Back Color")
            .withValue(new Sn0wColor(25, 25, 25, 255))
            .register(this);
    public Value<Sn0wColor> normalColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Normal Color")
            .withValue(new Sn0wColor(0, 255, 255, 255))
            .register(this);
    public Value<Sn0wColor> friendsColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Friends Color")
            .withValue(new Sn0wColor(0, 255, 255, 255))
            .register(this);
    public Value<Boolean> friendBorder = new ValueBuilder<Boolean>()
            .withDescriptor("Friend Border")
            .withValue(true)
            .register(this);
    public Value<Sn0wColor> SneakingColor = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Sneaking Color")
            .withValue(new Sn0wColor(255, 153, 0))
            .register(this);
    public Value<Sn0wColor> popColorA = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Pop Color A")
            .withValue(new Sn0wColor(0, 255, 255, 255))
            .register(this);
    public Value<Sn0wColor> popColorB = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Pop Color B")
            .withValue(new Sn0wColor(0, 0, 255, 255))
            .register(this);
    public Value<Sn0wColor> popColorC = new ValueBuilder<Sn0wColor>()
            .withDescriptor("Pop Color C")
            .withValue(new Sn0wColor(87, 8, 97))
            .register(this);
    public Value<Boolean> ping = new ValueBuilder<Boolean>()
            .withDescriptor("Ping")
            .withValue(true)
            .register(this);
    public Value<Boolean> items = new ValueBuilder<Boolean>()
            .withDescriptor("Items")
            .withValue(true)
            .register(this);
    public Value<Boolean> durability = new ValueBuilder<Boolean>()
            .withDescriptor("Durability")
            .withValue(true)
            .register(this);
    public Value<Boolean> pops = new ValueBuilder<Boolean>()
            .withDescriptor("Pops")
            .withValue(true)
            .register(this);
    public Value<Boolean> dash = new ValueBuilder<Boolean>()
            .withDescriptor("Dash Pops")
            .withValue(true)
            .register(this);

    public Value<Boolean> itemName = new ValueBuilder<Boolean>()
            .withDescriptor("Item Name")
            .withValue(true)
            .register(this);

    public Value<Boolean> enchantNames = new ValueBuilder<Boolean>()
            .withDescriptor("Enchant Name")
            .withValue(true)
            .register(this);
    public Value<Boolean> shortEnchants = new ValueBuilder<Boolean>()
            .withDescriptor("Short Enchants")
            .withValue(false)
            .register(this);
    public Value<Boolean> rainbow32k = new ValueBuilder<Boolean>()
            .withDescriptor("Rainbow 32ks")
            .withValue(true)
            .register(this);
    public Value<Boolean> cursedRed = new ValueBuilder<Boolean>()
            .withDescriptor("Cursed")
            .withValue(true)
            .register(this);
    public Value<Boolean> health = new ValueBuilder<Boolean>()
            .withDescriptor("Health")
            .withValue(true)
            .register(this);
    public Value<Boolean> entityId = new ValueBuilder<Boolean>()
            .withDescriptor("Entity Id")
            .withValue(true)
            .register(this);
    public Value<Boolean> gamemode = new ValueBuilder<Boolean>()
            .withDescriptor("Gamemode")
            .withValue(true)
            .register(this);
    public Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(300)
            .withRange(0, 300)
            .register(this);
    public Value<Number> scalingSet = new ValueBuilder<Number>()
            .withDescriptor("Scaling")
            .withValue(0.1f)
            .withRange(0.1f, 0.5f)
            .withPlaces(2)
            .register(this);
    public Value<Number> closeScaling = new ValueBuilder<Number>()
            .withDescriptor("Close Scaling")
            .withValue(0.0245)
            .withRange(0.0200, 0.0300)
            .withPlaces(4)
            .register(this);
    Value<Boolean> ColoredPing = new ValueBuilder<Boolean>()
            .withDescriptor("Colored Ping")
            .withValue(true)
            .register(this);
    public Value<Boolean> tabHealth = new ValueBuilder<Boolean>()
            .withDescriptor("Tab Health")
            .withValue(true)
            .register(this);

    void handlePage(String page)
    {
        borderColor.setActive(page.equals("Colors"));
        safeBorder.setActive(page.equals("Colors"));
        safeColor.setActive(page.equals("Colors") && safeBorder.getValue());

        boxColor.setActive(page.equals("Colors"));
        normalColor.setActive(page.equals("Colors"));
        friendsColor.setActive(page.equals("Colors"));
        friendBorder.setActive(page.equals("Colors"));

        SneakingColor.setActive(page.equals("Colors"));

        popColorA.setActive(page.equals("Colors"));
        popColorB.setActive(page.equals("Colors"));
        popColorC.setActive(page.equals("Colors"));

        ping.setActive(page.equals("Items"));
        ColoredPing.setActive(page.equals("Items"));

        health.setActive(page.equals("Items"));
        items.setActive(page.equals("Items"));

        enchantNames.setActive(page.equals("Items"));
        shortEnchants.setActive(page.equals("Items") && enchantNames.getValue());
        rainbow32k.setActive(page.equals("Items") && enchantNames.getValue());
        cursedRed.setActive(page.equals("Items") && enchantNames.getValue());

        itemName.setActive(page.equals("Items"));
        durability.setActive(page.equals("Items"));

        pops.setActive(page.equals("Items"));

        dash.setActive(page.equals("Items") && pops.getValue());

        entityId.setActive(page.equals("Items"));
        gamemode.setActive(page.equals("Items"));
        range.setActive(page.equals("Items"));
        tabHealth.setActive(page.equals("Items") && health.getValue());

    }

    public Nametags()
    {
        super("Nametags", Category.Render);
        INSTANCE = this;
    }

    public static Set<RegistryKey<Enchantment>> IMPORTANT = Set.of(Enchantments.PROTECTION, Enchantments.SHARPNESS, Enchantments.MENDING, Enchantments.BLAST_PROTECTION, Enchantments.FEATHER_FALLING, Enchantments.UNBREAKING, Enchantments.KNOCKBACK, Enchantments.EFFICIENCY, Enchantments.CHANNELING, Enchantments.POWER, Enchantments.SILK_TOUCH, Enchantments.THORNS);

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent event)
    {

        if (mc.gameRenderer == null || mc.getCameraEntity() == null)
        {
            return;
        }
        RenderBuffers.scheduleRender(() ->
        {
            Vec3d interpolate = Interpolator.getInterpolatedEyePos(mc.getCameraEntity(), event.getTickDelta());
            Camera camera = mc.gameRenderer.getCamera();
            Vec3d pos = camera.getPos();
            for (Entity entity : mc.world.getEntities())
            {
                if (entity instanceof PlayerEntity player)
                {
                    if (!player.isAlive() || (player == mc.player && !Freecam.INSTANCE.isEnabled()))
                    {
                        continue;
                    }
                    TextSection[] sections = getInfo(player).toArray(new TextSection[0]);
                    Vec3d pinterpolate = Interpolator.getRenderPosition(player, event.getTickDelta());
                    double rx = player.getX() - pinterpolate.getX();
                    double ry = player.getY() - pinterpolate.getY();
                    double rz = player.getZ() - pinterpolate.getZ();
                    int width = 0;
                    StringBuilder actualText = new StringBuilder();
                    for (TextSection section : sections)
                    {
                        actualText.append(section.getText());
                    }
                    width = (int) Fonts.getTextWidth(actualText.toString());
                    float hwidth = width / 2.0f;
                    double dx = (pos.getX() - interpolate.getX()) - rx;
                    double dy = (pos.getY() - interpolate.getY()) - ry;
                    double dz = (pos.getZ() - interpolate.getZ()) - rz;
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > 4096.0)
                    {
                        continue;
                    }
                    float scaling = 0.0018f + (scalingSet.getValue().floatValue() * 0.01f) * (float) dist;
                    if (dist <= 8.0)
                    {
                        scaling = closeScaling.getValue().floatValue();
                    }

                    render(sections, hwidth, player, rx, ry, rz, camera, scaling);
                }
            }
            RenderSystem.enableBlend();
        });
    }


    private void render(TextSection[] sections, float width, PlayerEntity entity, double x, double y, double z, Camera camera, float scaling)
    {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        GL11.glDepthFunc(GL11.GL_ALWAYS);

        renderInfo(sections, width, entity, x, y, z, camera, scaling);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    public List<TextSection> getInfo(PlayerEntity player)
    {
        List<TextSection> sections = new ArrayList<>();
        String section = player.getName().getString();


//        if (BotManager.INSTANCE.isSn0wUser(section))
//            section = Formatting.AQUA + (FontModule.INSTANCE.isEnabled() ? "*" : KamiMod.NAME_UNICODE) + Formatting.RESET + section;

        if (entityId.getValue())
        {
            section += " ID: " + player.getId();
        }
        if (gamemode.getValue())
        {
            if (player.isCreative())
            {
                section += " [C]";
            } else if (player.isSpectator())
            {
                section += " [I]";
            } else
            {
                section += " [S]";
            }
        }

        if (ping.getValue() && mc.getNetworkHandler() != null)
        {
            PlayerListEntry playerEntry = mc.getNetworkHandler().getPlayerListEntry(player.getGameProfile().getId());
            if (playerEntry != null)
            {
                int latency = playerEntry.getLatency();
                section += " " + getPingColor(latency) + latency + "ms" + Formatting.RESET;
            }
        }
        if (health.getValue())
        {
            float health = tabHealth.getValue() ? PlayerUtils.getTabHealth(player.getName().getString()) : player.getHealth() + player.getAbsorptionAmount();
            String color = PlayerUtils.getHealthColor(player, false, tabHealth.getValue()).toString();

            DecimalFormat format = new DecimalFormat("#.#");

            section += " " + color + format.format(health);
        }
        sections.add(new TextSection(section, renderPing(player)));

        if (pops.getValue())
        {
            int popCount = PopManager.INSTANCE.getPops(player);
            if (popCount >= 1)
            {
                sections.add(new TextSection(" " + (dash.getValue() ? "-" : "") + popCount, getPopColor(popCount)));
            }
        }

        return sections;
    }

    private Color renderPing(final PlayerEntity player)
    {
        if (FriendManager.INSTANCE.isFriend(player))
        {
            return friendsColor.getValue().getColor();
        }
        if (player.isInvisible())
        {
            return new Color(128, 128, 128);
        }
        if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getPlayerListEntry(player.getGameProfile().getId()) == null)
        {
            return new Color(239, 1, 71);
        }
        if (player.isSneaking())
        {
            return SneakingColor.getValue().getColor();
        }
        return normalColor.getValue().getColor();
    }

    public Formatting getPingColor(double ping)
    {
        if (ColoredPing.getValue())
        {
            if (ping <= 40)
            {
                return Formatting.GREEN;
            }
            if (ping <= 70)
            {
                return Formatting.DARK_GREEN;
            }
            if (ping <= 99)
            {
                return Formatting.YELLOW;
            }

            return Formatting.RED;
        } else
        {
            return Formatting.RESET;
        }
    }

    public Color getPopColor(int pops)
    {
        if (pops == 0) return Color.WHITE;


        if (pops < 5)
        {
            return ColorUtil.interpolate((float) MathHelper.clamp(MathUtil.normalize(pops, 1, 5), 0, 1), popColorB.getValue().getColor(), popColorA.getValue().getColor());
        } else
        {
            return ColorUtil.interpolate((float) MathHelper.clamp(MathUtil.normalize(pops, 5, 10), 0, 1), popColorC.getValue().getColor(), popColorB.getValue().getColor());
        }
    }

    private void renderInfo(TextSection[] sections, float width, PlayerEntity entity, double x, double y, double z, Camera camera, float scaling)
    {
        final Vec3d pos = camera.getPos();
        MatrixStack matrices = new MatrixStack();
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        matrices.translate(x - pos.getX(), y + (double) entity.getHeight() + (entity.isSneaking() ? 0.4f : 0.43f) - pos.getY(), z - pos.getZ());
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.scale(-scaling, -scaling, -1.0f);
        RenderUtil.renderRect(matrices, -width - 1.0f, -1.0f, width * 2.0f + 2.0f, mc.textRenderer.fontHeight + 1.5f, 0.0, boxColor.getValue().getColor().getRGB());


        RenderUtil.renderOutline(matrices, -width - 1.0f, -1.0f, width * 2.0f + 2.0f, mc.textRenderer.fontHeight + 1.5f, (friendBorder.getValue() && FriendManager.INSTANCE.isFriend(entity)) ? friendsColor.getValue().getColor().getRGB() : (safeBorder.getValue() && HoleUtils.isHole(PlayerUtils.getPos(entity)) ? safeColor.getValue().getColor().getRGB() : borderColor.getValue().getColor().getRGB()), true);
        RenderUtil.drawSections(sections, matrices, -width, 0.0f);

        renderItems(matrices, entity);
        matrices.pop();


    }


    private void renderItems(MatrixStack matrixStack, PlayerEntity player)
    {
        List<ItemStack> displayItems = new CopyOnWriteArrayList<>();
        if (!player.getOffHandStack().isEmpty())
        {
            displayItems.add(player.getOffHandStack());
        }
        player.getInventory().armor.forEach(armorStack ->
        {
            if (!armorStack.isEmpty())
            {
                displayItems.add(armorStack);
            }
        });
        if (!player.getMainHandStack().isEmpty())
        {
            displayItems.add(player.getMainHandStack());
        }
        Collections.reverse(displayItems);
        float offset = 0;
        int baseOffset = 0;
        for (ItemStack stack : displayItems)
        {
            if (!stack.isDamageable() && !items.getValue()) continue;

            offset -= 8;

            ItemEnchantmentsComponent enchants = EnchantmentHelper.getEnchantments(stack);

            int count = 0;


            for (RegistryEntry<Enchantment> enchantment : enchants.getEnchantments())
            {
                boolean available = !shortEnchants.getValue();

                if (shortEnchants.getValue())
                    for (RegistryKey<Enchantment> key : IMPORTANT)
                    {
                        if (enchantment.matchesKey(key))
                        {
                            available = true;
                            break;
                        }
                    }


                if (!available) continue;

                count++;
            }

            if (count > baseOffset) baseOffset = count;
        }
        float enchantOffset = getOffset(baseOffset);


        for (ItemStack stack : displayItems)
        {

            if (!stack.isDamageable() && !items.getValue()) continue;

            if (items.getValue())
            {
                matrixStack.push();
                matrixStack.translate(offset, enchantOffset, 0.0f);
                matrixStack.translate(8.0f, 8.0f, 0.0f);
                matrixStack.scale(16.0f, 16.0f, 16.0F);
                matrixStack.multiplyPositionMatrix(new Matrix4f().scaling(1.0f, -1.0f, 0.0001f));
                Vector3f[] shaderLights = RenderSystem.shaderLightDirections.clone();
                DiffuseLighting.disableGuiDepthLighting();

                RenderUtil.renderItem(stack, ModelTransformationMode.GUI, matrixStack, mc.getBufferBuilders().getEntityVertexConsumers(), null, 0);
                mc.getBufferBuilders().getEntityVertexConsumers().draw();
                DiffuseLighting.enableGuiDepthLighting();
                RenderSystem.setShaderLights(shaderLights[0], shaderLights[1]);
                matrixStack.pop();

                if (stack.getCount() != 1)
                {
                    String string = String.valueOf(stack.getCount());
                    Fonts.renderText(matrixStack, string, offset + 17 - Fonts.getTextWidth(string), enchantOffset + 9.0f, Color.WHITE, true);
                }
                double k;
                double l;
                if (stack.isItemBarVisible())
                {
                    int i = stack.getItemBarStep();
                    int j = stack.getItemBarColor();
                    k = (offset + 2);

                    l = (enchantOffset + 13);
                    RenderUtil.renderRect(matrixStack, k, l, 13, 2, -16777216);
                    RenderUtil.renderRect(matrixStack, k, l, i, 1, j | -16777216);
                }
            }
            matrixStack.scale(0.5f, 0.5f, 0.5f);
            if (durability.getValue())
            {
                renderDurability(matrixStack, stack, offset + 2.0f, enchantOffset - 3);
            }
                renderEnchants(matrixStack, stack, offset + 2.0f, enchantOffset + 1);
            matrixStack.scale(2.0f, 2.0f, 2.0f);
            offset += 16;
        }
        ItemStack heldItem = player.getMainHandStack();
        if (heldItem.isEmpty())
        {
            return;
        }
        matrixStack.scale(0.5f, 0.5f, 0.5f);
        if (itemName.getValue())
        {
            renderItemName(matrixStack, heldItem, 0, durability.getValue() ? enchantOffset - 7.5f : enchantOffset - 4);
        }
        matrixStack.scale(2.0f, 2.0f, 2.0f);
    }

    private void renderDurability(MatrixStack matrixStack, ItemStack itemStack, float x, float y)
    {
        if (!itemStack.isDamageable())
        {
            return;
        }
        int n = itemStack.getMaxDamage();
        int n2 = itemStack.getDamage();
        int durability = (int) ((n - n2) / ((float) n) * 100.0f);


        Fonts.renderText(matrixStack, durability + "%", x * 2, y * 2, ColorUtil.hslToColor((float) (n - n2) / (float) n * 120.0f, 100.0f, 50.0f, 1.0f), true);
    }

    private void renderEnchants(MatrixStack matrixStack, ItemStack itemStack, float x, float y)
    {
        if (itemStack.getItem() == Items.ENCHANTED_GOLDEN_APPLE && items.getValue())
        {
            Fonts.renderText(matrixStack, "God", x * 2, y * 2, new Color(195, 77, 65), true);
            return;
        }
        if (!enchantNames.getValue()) return;

        if (!itemStack.hasEnchantments())
        {
            return;
        }
        ItemEnchantmentsComponent enchants = EnchantmentHelper.getEnchantments(itemStack);

        float n2 = 0;
        for (RegistryEntry<Enchantment> enchantment : enchants.getEnchantments())
        {
            boolean available = !shortEnchants.getValue();

            if (shortEnchants.getValue())
                for (RegistryKey<Enchantment> key : IMPORTANT)
                {
                    if (enchantment.matchesKey(key))
                    {
                        available = true;
                        break;
                    }
                }


            if (!available) continue;

            int lvl = enchants.getLevel(enchantment);

            StringBuilder enchantString = new StringBuilder();
            String translatedName = Enchantment.getName(enchantment, lvl).getString();
            if (translatedName.contains("Vanish"))
            {
                enchantString.append((cursedRed.getValue() ? Formatting.DARK_RED : "") + "Va1");
            } else if (translatedName.contains("Bind"))
            {
                enchantString.append((cursedRed.getValue() ? Formatting.DARK_RED : "") + "Bi1");
            } else
            {
                int maxLen = 2;
                if (translatedName.length() > maxLen)
                {
                    translatedName = translatedName.substring(0, maxLen);
                }
                enchantString.append(translatedName);
                enchantString.append(lvl);
            }

            if (rainbow32k.getValue() && lvl > 42)
                RainbowUtil.render32k(matrixStack, enchantString.toString(), x * 2, (y + n2) * 2);
            else
                Fonts.renderText(matrixStack, enchantString.toString(), x * 2, (y + n2) * 2, new Color(-1), true);
            n2 += 4.5f;
        }
    }


    private float getOffset(final int n)
    {

        if (!items.getValue() )
        {
            float n2 = -3.0f;
            if (enchantNames.getValue() && n != 0)
                n2 -= n * 5.5f;
            return n2;
        }
        if (!enchantNames.getValue() || n <= 3)
        {
            return -19.0f;
        }
        float n2 = -14.0f;
        n2 -= (n - 3) * 5.5f;
        return n2;
    }

    /**
     * @param matrixStack
     * @param itemStack
     * @param x
     * @param y
     */
    private void renderItemName(MatrixStack matrixStack, ItemStack itemStack, float x, float y)
    {
        String itemName = itemStack.getName().getString();
        float width = Fonts.getTextWidth(itemName) / 4.0f;
        Fonts.renderText(matrixStack, itemName, (x - width) * 2, y * 2, Color.WHITE, true);
    }

    @Override
    public String getDescription()
    {
        return "Nametags: renders descriptive nametags above opps";
    }
}