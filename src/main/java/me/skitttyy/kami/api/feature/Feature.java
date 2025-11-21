package me.skitttyy.kami.api.feature;

import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.management.SavableManager;
import me.skitttyy.kami.api.utils.chat.ChatUtils;
import me.skitttyy.kami.api.utils.color.Sn0wColor;
import me.skitttyy.kami.api.utils.render.animation.Easing;
import me.skitttyy.kami.api.utils.render.animation.type.ConsistentDynamicAnimation;
import me.skitttyy.kami.impl.KamiMod;
import me.skitttyy.kami.impl.features.hud.FeatureList;
import me.skitttyy.kami.api.config.ISavable;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Feature implements ISavable
{

    String name;
    boolean enabled;
    FeatureType type;
    List<Value<?>> values;
    Category category;
    public Value<Boolean> visible;
    public Value<String> displayName;
    public float offset;

    public String lastHudInfo;
    public final ConsistentDynamicAnimation animation = new ConsistentDynamicAnimation(0, Easing.LINEAR, 50);

    public Feature(String name, Category category, FeatureType type)
    {
        this.name = name;
        this.enabled = false;
        this.type = type;
        values = new ArrayList<>();
        this.category = category;
        SavableManager.INSTANCE.getSavables().add(this);
        visible = new ValueBuilder<Boolean>()
                .withDescriptor("Visible")
                .withValue(true)
                .register(this);
        displayName = new ValueBuilder<String>()
                .withDescriptor("Name")
                .withValue(getName())
                .register(this);

        offset = -30;
        lastHudInfo = "";
    }

    public double x;

    public long startTime;

    public double endpoint;
    private int prevTextWidth;

    private boolean wasDrawing;

    public void animation()
    {


//        if (this.isEnabled())
//        {
//            doneUntilEnabled = false;
//
//            if (offset != 0.0f)
//            {
//                sliding = true;
//            }
//        } else
//        {
//            System.out.println(this.getDisplayName() + (getHudInfo() != "" ? "[" + this.getHudInfo() + "]" : ""));

//
//        String hudInfo = getHudInfo();
//        animation.setUnitsPer100(FeatureList.INSTANCE.animationSpeed.getValue().floatValue() * 50);
//        animation.setEasing(FeatureList.INSTANCE.getEasing());
//        this.getOffset();
//        float goal = 0;
//        float curWidth = Fonts.getTextWidth(this.getDisplayName() + " " + (hudInfo != "" ? "[" + hudInfo + "]" : "")) + 1;
//
//        if(FeatureList.INSTANCE.animateHudInfo.getValue()){
//        if (!Objects.equals(hudInfo, lastHudInfo))
//        {
//            String lastInfo = (lastHudInfo != "" ? "[" + lastHudInfo + "]" : "");
//            String curInfo = (hudInfo != "" ? "[" + hudInfo + "]" : "");
//
//            float lastWidth = Fonts.getTextWidth(lastInfo) + 1;
//            float curInfoWidth = Fonts.getTextWidth(curInfo) + 1;
//
//            if (curInfoWidth != lastWidth)
//            {
//                float value = animation.getAnimationValue();
//                animation.updateLastValue(value - (curInfoWidth - lastWidth));
//            }
//        }
//        }
//
//        if (!this.isEnabled())
//        {
//            goal = -curWidth;
//        } else
//        {
//            goal = 0;
//        }
//
//        animation.goal(goal);
//        this.lastHudInfo = hudInfo;


        String text = this.getDisplayName() + (!Objects.equals(getHudInfo(), "") ? " [" + this.getHudInfo() + "]" : "");
        int textWidth = (int) Fonts.getTextWidth(text);
        boolean drawing = this.isEnabled() && this.visible.getValue();
        if (drawing != wasDrawing || prevTextWidth != textWidth)
        {
            startTime = System.currentTimeMillis();

            if (drawing)
            {
                endpoint = -textWidth;
            } else
            {
                endpoint = 0.0f;
            }
            wasDrawing = drawing;
            prevTextWidth = textWidth;
        }

        double animationProgress = Math.min((System.currentTimeMillis() - startTime) / (FeatureList.INSTANCE.animationSpeed.getValue().floatValue() * 1000F), 1.0);
        double factor = FeatureList.INSTANCE.getEasing().ease(animationProgress);
        this.x = this.x * (1.0 - factor) + (endpoint * factor);


    }

    public double getOffset()
    {
        return this.x;
    }

    public void animationIcky()
    {
//        if (mc.textRenderer == null) return;
//
//        if (this.isEnabled())
//        {
//            doneUntilEnabled = false;
//
//            sliding = offset != 0.0f;
//
//        } else
//        {
////            System.out.println(this.getDisplayName() + (getHudInfo() != "" ? "[" + this.getHudInfo() + "]" : ""));
//            if (offset >= Math.negateExact((int) (Fonts.getTextWidth(this.getDisplayName() + " " + (getHudInfo() != "" ? "[" + this.getHudInfo() + "]" : "")) + 1)) && !doneUntilEnabled)
//            {
//                sliding = true;
//            } else
//            {
//                doneUntilEnabled = true;
//                sliding = false;
//            }
//        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDisplayName()
    {
        try
        {


            return displayName.getValue();
        } catch (Exception e)
        {
            ChatUtils.sendMessage(this.name + " I AM THE PROBELM");
            return this.name;
        }
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        if (this.enabled)
        {
            onEnable();
        } else
        {
            onDisable();
        }
    }

    public void setEnabledA(boolean enabled)
    {
        if (this.enabled == enabled) return;

        this.enabled = enabled;
        if (this.enabled)
        {
            onEnable();
        } else
        {
            onDisable();
        }
    }

    public Category getCategory()
    {
        return category;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void onEnable()
    {
        animationIcky();
        KamiMod.EVENT_BUS.register(this);
    }

    public void onDisable()
    {
        animationIcky();
        KamiMod.EVENT_BUS.unregister(this);
    }

    public void toggle()
    {
        setEnabled(!isEnabled());
    }

    public FeatureType getType()
    {
        return type;
    }

    public void setType(FeatureType type)
    {
        this.type = type;
    }

    public List<Value<?>> getValues()
    {
        return values;
    }

    public void setValues(List<Value<?>> values)
    {
        this.values = values;
    }

    public String getHudInfo()
    {
        return "";
    }

    public String getDescription()
    {
        return "";
    }

    @Override
    public void load(Map<String, Object> objects)
    {
        if (objects == null) return;

        Object e = objects.get("enabled");
        if (e != null)
        {
            setEnabled(((boolean) e));
        }
        for (Value value : getValues())
        {
            Object o = objects.get(value.getTag());
            if (o != null)
            {
                try
                {
                    if (value.getValue() instanceof Sn0wColor)
                    {
                        Map<String, Object> map = ((Map<String, Object>) o);
                        Sn0wColor sn0wColor = new Sn0wColor(new Color((int) map.get("red"), (int) map.get("green"), (int) map.get("blue"), (int) map.get("alpha")), (boolean) map.get("sync"));
                        value.setValue(sn0wColor);
                    } else
                    {
                        value.setValue(o);
                    }
                } catch (Exception ignored)
                {
                }
            }
        }
    }

    public void loadModule(Map<String, Object> objects)
    {
        Object e = objects.get("enabled");
        if (e != null)
        {
            setEnabledA(((boolean) e));
        }
        setEnabledA(objects.get("enabled") != null ? (boolean) objects.get("enabled") : isEnabled());
        for (Value value : getValues())
        {
            Object o = objects.get(value.getTag());
            if (o != null)
            {
                try
                {
                    if (value.getValue() instanceof Sn0wColor)
                    {
                        Map<String, Object> map = ((Map<String, Object>) o);
                        Sn0wColor sn0wColor = new Sn0wColor(new Color((int) map.get("red"), (int) map.get("green"), (int) map.get("blue"), (int) map.get("alpha")), (boolean) map.get("sync"));
                        value.setValue(sn0wColor);
                    } else
                    {
                        value.setValue(o);
                    }
                } catch (Exception ignored)
                {
                }
            }
        }
    }

    public void softLoadModule(Map<String, Object> objects)
    {
        Object e = objects.get("enabled");
        if (e != null)
        {
            setEnabledA(((boolean) e));
        }
        setEnabledA(objects.get("enabled") != null ? (boolean) objects.get("enabled") : isEnabled());
        for (Value value : getValues())
        {
            Object o = objects.get(value.getTag());
            if (Objects.equals(value.getTag(), "bind")) continue;

            if (o != null)
            {
                try
                {
                    if (!(value.getValue() instanceof Sn0wColor))
                    {
                        value.setValue(o);
                    }
                } catch (Exception ignored)
                {
                }
            }
        }
    }

    @Override
    public Map<String, Object> save()
    {
        Map<String, Object> toSave = new HashMap<>();
        toSave.put("enabled", enabled);

        for (Value<?> value : getValues())
        {
            if (value.getValue() instanceof Sn0wColor)
            {
                Value<Sn0wColor> val = ((Value<Sn0wColor>) value);
                Map<String, Object> color = new HashMap<>();
                color.put("red", val.getValue().getColor().getRed());
                color.put("green", val.getValue().getColor().getGreen());
                color.put("blue", val.getValue().getColor().getBlue());
                color.put("alpha", val.getValue().getColor().getAlpha());
                color.put("sync", val.getValue().isSyncing());
                toSave.put(value.getTag(), color);
            } else
            {
                toSave.put(value.getTag(), value.getValue());
            }
        }
        return toSave;
    }


    @Override
    public String getFileName()
    {
        return getName() + ".yml";
    }

    @Override
    public String getDirName()
    {
        return "features";
    }

    public enum FeatureType
    {
        Module,
        Hud
    }

    public enum Category
    {
        Client,
        Combat,
        Movement,
        Player,
        Misc,
        Render,
        Ghost,
        Hud
    }
}
