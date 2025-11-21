package me.skitttyy.kami.api.management;

import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.api.feature.Feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
public class FeatureManager {

    public static FeatureManager INSTANCE;

    List<Feature> features;

    public FeatureManager()
    {
        features = new ArrayList<>();
    }

    public HashMap<String, Integer> MODULE_COUNTS = new HashMap<String, Integer>();



    public Feature getClosestMatchingFeature(String text)
    {
        Feature bestFeature = null;
        double lowestLength = Double.MAX_VALUE;

        for (Feature feature : features)
        {


            if (Objects.equals(text, "")) return feature;


            if (text.equals(feature.getName())) return feature;

            if (text.length() > feature.getName().length()) continue;

            if (feature.getName().startsWith(text) && lowestLength > feature.getName().length())
            {
                lowestLength = feature.getName().length();
                bestFeature = feature;
            }
        }
        return bestFeature;
    }


    public void initModuleCounts()
    {
        for (Feature feature : FeatureManager.INSTANCE.getFeatures())
        {
            MODULE_COUNTS.put(feature.getCategory().toString().toLowerCase(), MODULE_COUNTS.getOrDefault(feature.getCategory().toString().toLowerCase(), 0) + 1);
        }
    }
}
