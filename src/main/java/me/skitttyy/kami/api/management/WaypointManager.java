package me.skitttyy.kami.api.management;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.config.ISavable;
import me.skitttyy.kami.api.friends.Friend;
import me.skitttyy.kami.api.utils.StringUtils;
import me.skitttyy.kami.api.utils.world.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class WaypointManager implements ISavable {
    public static WaypointManager INSTANCE;
    public CopyOnWriteArrayList<WayPoint> wayPoints = new CopyOnWriteArrayList<>();

    public WaypointManager()
    {
        INSTANCE = this;
        SavableManager.INSTANCE.getSavables().add(this);
    }

    public void removeWayPoint(WayPoint macro)
    {
        wayPoints.remove(macro);
    }

    public CopyOnWriteArrayList<WayPoint> getWayPoints()
    {
        return wayPoints;
    }

    public WayPoint getWayPointByName(String name)
    {
        for (WayPoint wayPoint : getWayPoints())
            if (wayPoint.name.equalsIgnoreCase(name))
                return wayPoint;
        return null;
    }


    @Override
    public void load(Map<String, Object> objects)
    {
        if (objects.get("waypoints") != null)
        {
            List<String> list = ((List<String>) objects.get("waypoints"));
            for (String s : list)
            {
                try
                {
                    String[] line = s.split(":");
                    String x = line[0];
                    String y = line[1];
                    String z = line[2];
                    String name = line[3];
                    String server = line[4];
                    String dimension = line.length == 6 ? line[5] : "overworld";

                    addWayPoint(new WayPoint(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z), name, server, dimension));
                } catch (Exception e)
                {
                    continue;
                }
            }
        }
    }

    public void addWayPoint(WayPoint wp)
    {
        if (!wayPoints.contains(wp))
            wayPoints.add(wp);
    }

    @Override
    public Map<String, Object> save()
    {
        Map<String, Object> toSave = new HashMap<>();
        List<String> waypointList = new ArrayList<>();
        for (WayPoint waypoint : wayPoints)
        {
            waypointList.add(waypoint.x + ":" + waypoint.y + ":" + waypoint.z + ":" + waypoint.name + ":" + waypoint.server + ":" + waypoint.dimension);
        }
        toSave.put("waypoints", waypointList);
        return toSave;
    }

    public WayPoint getClosestMatchingWaypoint(String text)
    {
        WayPoint bestWaypoint = null;
        double lowestLength = Double.MAX_VALUE;
        for (WayPoint loc : getWayPoints())
        {

            if (Objects.equals(text, "")) return loc;


            if (text.equals(loc.toString())) return loc;

            if (text.length() > loc.toString().length()) continue;

            if (loc.toString().startsWith(text) && lowestLength > loc.toString().length())
            {
                bestWaypoint = loc;
                lowestLength = loc.toString().length();
            }
        }
        return bestWaypoint;
    }

    @Override
    public String getFileName()
    {
        return "waypoints.yml";
    }

    @Override
    public String getDirName()
    {
        return "misc";
    }

    @Getter
    @Setter
    public static class WayPoint {

        private int x, y, z;
        private String name, server, dimension;

        public WayPoint(int x, int y, int z, String name, String server, String dimension)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.name = name;
            this.server = server;
            this.dimension = dimension;
        }
    }

}
