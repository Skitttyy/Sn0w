package me.skitttyy.kami.api.friends;

import java.util.Objects;

public class Friend {

    String ign;

    public Friend(String ign){
        this.ign = ign;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friend friend = (Friend) o;
        return ign.equals(friend.ign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ign);
    }


    @Override
    public String toString() {
        return ign;
    }
}
