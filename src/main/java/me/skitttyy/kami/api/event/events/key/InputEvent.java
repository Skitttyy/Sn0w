package me.skitttyy.kami.api.event.events.key;

import me.skitttyy.kami.api.event.Event;
import me.skitttyy.kami.api.event.events.network.PacketEvent;
import net.minecraft.client.input.Input;
import net.minecraft.network.packet.Packet;

public class InputEvent extends Event {

    public Input input;

    public InputEvent(Input input)
    {
        this.input = input;
    }

}