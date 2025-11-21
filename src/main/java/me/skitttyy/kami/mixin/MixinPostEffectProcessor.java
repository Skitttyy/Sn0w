package me.skitttyy.kami.mixin;

import me.skitttyy.kami.api.utils.ducks.IPostEffectProcessor;
import me.skitttyy.kami.mixin.accessor.IPostEffectPass;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(PostEffectProcessor.class)
public class MixinPostEffectProcessor implements IPostEffectProcessor
{
    @Shadow
    @Final
    private List<PostEffectPass> passes;

    @Shadow
    @Final
    private Map<String, Framebuffer> targetsByName;

    @Unique
    private final List<String> bufferTargets = new CopyOnWriteArrayList<>();

    @Override
    public void overwriteBuffer(String name, Framebuffer buffer)
    {
        Framebuffer target = targetsByName.get(name);
        if (target == buffer)
        {
            return;
        }
        if (target != null)
        {
            for (PostEffectPass pass : passes)
            {
                if (pass.input == target)
                {
                    ((IPostEffectPass) pass).hookSetInput(buffer);
                }
                if (pass.output == target)
                {
                    ((IPostEffectPass) pass).hookSetOutput(buffer);
                }
            }
            targetsByName.remove(name);
            bufferTargets.remove(name);
        }

        targetsByName.put(name, buffer);
        bufferTargets.add(name);
    }

    @Inject(method = "close", at = @At(value = "HEAD"))
    private void hookClose(CallbackInfo ci)
    {
        for (String fakedBufferName : bufferTargets)
        {
            targetsByName.remove(fakedBufferName);
        }
    }
}
