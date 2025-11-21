package me.skitttyy.kami.impl.features.modules.client;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalXZ;
import me.skitttyy.kami.api.event.eventbus.SubscribeEvent;
import me.skitttyy.kami.api.event.events.TickEvent;
import me.skitttyy.kami.api.feature.module.Module;
import me.skitttyy.kami.api.gui.font.Fonts;
import me.skitttyy.kami.api.value.Value;
import me.skitttyy.kami.api.value.builder.ValueBuilder;

public class Baritone extends Module
{

    public Value<Number> range = new ValueBuilder<Number>()
            .withDescriptor("Range")
            .withValue(4.0f)
            .withRange(1.0f, 6.0f)
            .withPlaces(1)
            .register(this);

    public Value<Boolean> place = new ValueBuilder<Boolean>()
            .withDescriptor("Place")
            .withValue(true)
            .register(this);
    public Value<Boolean> breakSetting = new ValueBuilder<Boolean>()
            .withDescriptor("Break")
            .withValue(true)
            .register(this);
    public Value<Boolean> sprint = new ValueBuilder<Boolean>()
            .withDescriptor("Sprint")
            .withValue(true)
            .register(this);
    public Value<Boolean> inventory = new ValueBuilder<Boolean>()
            .withDescriptor("UseInventory")
            .withValue(false)
            .register(this);
    public Value<Boolean> vines = new ValueBuilder<Boolean>()
            .withDescriptor("Vines")
            .withValue(true)
            .register(this);
    public Value<Boolean> jump256 = new ValueBuilder<Boolean>()
            .withDescriptor("JumpAt256")
            .withValue(false)
            .register(this);
    public Value<Boolean> waterBucketFall = new ValueBuilder<Boolean>()
            .withDescriptor("WaterBucketFall")
            .withValue(false)
            .register(this);
    public Value<Boolean> parkour = new ValueBuilder<Boolean>()
            .withDescriptor("Parkour")
            .withValue(true)
            .register(this);
    public Value<Boolean> parkourPlace = new ValueBuilder<Boolean>()
            .withDescriptor("ParkourPlace")
            .withValue(false)
            .register(this);
    public Value<Boolean> parkourAscend = new ValueBuilder<Boolean>()
            .withDescriptor("ParkourAscend")
            .withValue(true)
            .register(this);
    public Value<Boolean> diagonalAscend = new ValueBuilder<Boolean>()
            .withDescriptor("DiagonalAscend")
            .withValue(false)
            .register(this);
    public Value<Boolean> diagonalDescend = new ValueBuilder<Boolean>()
            .withDescriptor("DiagonalDescend")
            .withValue(false)
            .register(this);
    public Value<Boolean> mineDown = new ValueBuilder<Boolean>()
            .withDescriptor("MineDownward")
            .withValue(true)
            .register(this);
    public Value<Boolean> legitMine = new ValueBuilder<Boolean>()
            .withDescriptor("LegitMine")
            .withValue(false)
            .register(this);
    public Value<Boolean> logOnArrival = new ValueBuilder<Boolean>()
            .withDescriptor("LogOnArrival")
            .withValue(false)
            .register(this);
    public Value<Boolean> freeLook = new ValueBuilder<Boolean>()
            .withDescriptor("FreeLook")
            .withValue(true)
            .register(this);
    public Value<Boolean> antiCheat = new ValueBuilder<Boolean>()
            .withDescriptor("AntiCheat")
            .withValue(false)
            .register(this);
    public Value<Boolean> strictLiquid = new ValueBuilder<Boolean>()
            .withDescriptor("Strict-Liquid")
            .withValue(false)
            .register(this);
    public Value<Boolean> censorCoords = new ValueBuilder<Boolean>()
            .withDescriptor("CensorCoords")
            .withValue(false)
            .register(this);
    public Value<Boolean> censorCommands = new ValueBuilder<Boolean>()
            .withDescriptor("CensorCommands")
            .withValue(false)
            .register(this);
    public Value<Boolean> debug = new ValueBuilder<Boolean>()
            .withDescriptor("Debug")
            .withValue(false)
            .register(this);
    public static Baritone INSTANCE;

    public Baritone()
    {
        super("Baritone", Category.Client);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent.Pre event)
    {
        BaritoneAPI.getSettings().blockReachDistance.value = range.getValue().floatValue();
        BaritoneAPI.getSettings().allowPlace.value = place.getValue();
        BaritoneAPI.getSettings().allowBreak.value = breakSetting.getValue();
        BaritoneAPI.getSettings().allowSprint.value = sprint.getValue();
        BaritoneAPI.getSettings().allowInventory.value = inventory.getValue();
        BaritoneAPI.getSettings().allowVines.value = vines.getValue();
        BaritoneAPI.getSettings().allowJumpAt256.value = jump256.getValue();
        BaritoneAPI.getSettings().allowWaterBucketFall.value = waterBucketFall.getValue();
        BaritoneAPI.getSettings().allowParkour.value = parkour.getValue();
        BaritoneAPI.getSettings().allowParkourAscend.value = parkourAscend.getValue();
        BaritoneAPI.getSettings().allowParkourPlace.value = parkourPlace.getValue();
        BaritoneAPI.getSettings().allowDiagonalAscend.value = diagonalAscend.getValue();
        BaritoneAPI.getSettings().allowDiagonalDescend.value = diagonalDescend.getValue();
        BaritoneAPI.getSettings().allowDownward.value = mineDown.getValue();
        BaritoneAPI.getSettings().legitMine.value = legitMine.getValue();
        BaritoneAPI.getSettings().disconnectOnArrival.value = logOnArrival.getValue();
        BaritoneAPI.getSettings().freeLook.value = freeLook.getValue();
        BaritoneAPI.getSettings().antiCheatCompatibility.value = antiCheat.getValue();
        BaritoneAPI.getSettings().strictLiquidCheck.value = strictLiquid.getValue();
        BaritoneAPI.getSettings().censorCoordinates.value = censorCoords.getValue();
        BaritoneAPI.getSettings().censorRanCommands.value = censorCommands.getValue();
        BaritoneAPI.getSettings().chatDebug.value = debug.getValue();
    }

    public void doObstaclePass(){
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(GoalXZ.fromDirection(mc.player.getPos(), mc.player.getYaw(), 10));
    }
    @Override
    public String getDescription()
    {
        return "Baritone: Manages baritone api settings";
    }


}
