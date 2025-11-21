package me.skitttyy.kami.api.utils.players;

public class Sn0wUser {
    private final String name;
    private final String uuid;
    private String selectedCape;

    public Sn0wUser(String name, String uuid, String selectedCape) {
        this.name = name;
        this.uuid = uuid;
        this.selectedCape = selectedCape;
    }

    public String getName() {
        return name;
    }

    public String getUUID() {
        return uuid;
    }

    public String getSelectedCape() {
        return selectedCape;
    }

    public void setSelectedCape(String selectedCape) {
        this.selectedCape = selectedCape;
    }


}
