package me.skitttyy.kami.api.utils.discord.callbacks;

import com.sun.jna.Callback;
import me.skitttyy.kami.api.utils.discord.DiscordUser;

public interface JoinRequestCallback extends Callback {
    void apply(final DiscordUser p0);
}
