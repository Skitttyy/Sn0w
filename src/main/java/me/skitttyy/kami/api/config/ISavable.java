package me.skitttyy.kami.api.config;

import java.util.Map;

public interface ISavable {

    void load(Map<String, Object> objects);

    Map<String, Object> save();

    String getFileName();

    String getDirName();
}
