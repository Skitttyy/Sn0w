package me.skitttyy.kami.api.event.events;

import me.skitttyy.kami.api.event.Event;

public class TickEvent {
    /**
     * @see me.skitttyy.kami.mixin.MixinMinecraftClient
     */
    public static class ClientTickEvent extends Event {
    }
    /**
     * @see me.skitttyy.kami.mixin.MixinMinecraftClient
     */
    public static class AfterClientTickEvent extends Event {
    }

    /**
     * @see me.skitttyy.kami.mixin.MixinMinecraftClient
     */
    public static class InputTick extends Event {

    }



    /**
     * @see me.skitttyy.kami.mixin.MixinMinecraftClient
     */
    public static class VanillaTick extends Event {

    }


    /**
     * @see me.skitttyy.kami.mixin.MixinClientPlayerEntity
     */
    public static class MovementTickEvent extends Event {


        public static class Pre extends Event {

            public Pre()
            {
            }
        }


        public static class Post extends Event {

            public Post()
            {

            }
        }
    }

    /**
     * @see me.skitttyy.kami.mixin.MixinClientPlayerEntity
     */
    public static class PlayerTickEvent extends Event {
        public static class Pre extends Event {

            public Pre()
            {

            }
        }


        public static class Post extends Event {

            public Post()
            {

            }
        }
    }

}
