package me.skitttyy.kami.api.utils.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.skitttyy.kami.impl.features.modules.client.AntiCheat;

@Getter
@Setter
@AllArgsConstructor
public class AntiFeetPlaceResult
{
    boolean placeAvailable;
    boolean isAntiFeetPlace;
}
