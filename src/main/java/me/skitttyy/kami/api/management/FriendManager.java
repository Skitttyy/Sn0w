package me.skitttyy.kami.api.management;

import net.minecraft.entity.Entity;
import me.skitttyy.kami.api.config.ISavable;
import me.skitttyy.kami.api.friends.Friend;

import java.util.*;

public class FriendManager implements ISavable {

    public static FriendManager INSTANCE;

    List<Friend> friends = new ArrayList<>();

    public FriendManager()
    {
        SavableManager.INSTANCE.getSavables().add(this);
    }

    public List<Friend> getFriends()
    {
        return friends;
    }

    public boolean isFriend(Entity entity)
    {
        Friend testFriend = new Friend(entity.getName().getString());

        return friends.contains(testFriend);
    }

    public void addFriend(Entity entity)
    {
        Friend friend = new Friend(entity.getName().getString());
        friends.add(friend);
    }

    public void removeFriend(Entity entity)
    {
        Friend friend = new Friend(entity.getName().getString());
        friends.remove(friend);
    }

    @Override
    public void load(Map<String, Object> objects)
    {
        if (objects.get("friends") != null)
        {
            List<String> friendsList = ((List<String>) objects.get("friends"));
            for (String s : friendsList)
            {
                friends.add(new Friend(
                        s
                ));
            }
        }
    }

    public Friend getClosestMatchingFriend(String text)
    {
        Friend bestFriend = null;
        double lowestLength = Double.MAX_VALUE;
        for (Friend friend : getFriends())
        {

            if (Objects.equals(text, "")) return friend;


            if (text.equals(friend.toString())) return friend;

            if (text.length() > friend.toString().length()) continue;

            if (friend.toString().startsWith(text) && lowestLength > friend.toString().length())
            {
                bestFriend = friend;
                lowestLength = friend.toString().length();
            }
        }
        return bestFriend;
    }

    @Override
    public Map<String, Object> save()
    {
        Map<String, Object> toSave = new HashMap<>();
        List<String> friendList = new ArrayList<>();
        for (Friend friend : friends)
        {
            friendList.add(friend.toString());
        }
        toSave.put("friends", friendList);
        return toSave;
    }

    @Override
    public String getFileName()
    {
        return "friends.yml";
    }

    @Override
    public String getDirName()
    {
        return "misc";
    }
}
