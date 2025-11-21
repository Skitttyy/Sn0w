/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package me.skitttyy.kami.mixin.chat;

import me.skitttyy.kami.api.utils.ducks.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.Visible.class)
public abstract class MixinChatHudLineVisible implements IChatHudLine {
    @Shadow
    @Final
    private OrderedText content;
    @Unique
    private int id;

    @Override
    public String getMessageText()
    {
        StringBuilder sb = new StringBuilder();

        content.accept((index, style, codePoint) ->
        {
            sb.appendCodePoint(codePoint);
            return true;
        });

        return sb.toString();
    }

    @Override
    public int getOverrideId()
    {
        return id;
    }

    @Override
    public void setOverrideId(int id)
    {
        this.id = id;
    }
}