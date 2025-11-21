package me.skitttyy.kami.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gl.JsonEffectShaderProgram;
import net.minecraft.client.gl.ShaderStage;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(JsonEffectShaderProgram.class)
public class MixinJsonEffectGlShader
{
    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Identifier;ofVanilla(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"
            )
    )
    private Identifier ofVanilla0(String path,
                                  Operation<Identifier> original,
                                  ResourceFactory unused,
                                  String name)
    {
        if (!name.contains(":"))
        {
            return original.call(path);
        }

        Identifier split = Identifier.of(name);
        return Identifier.of(split.getNamespace(), "shaders/program/" + split.getPath() + ".json");
    }

    @WrapOperation(
            method = "loadEffect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Identifier;ofVanilla(Ljava/lang/String;)Lnet/minecraft/util/Identifier;")
    )
    private static Identifier ofVanilla1(String path,
                                         Operation<Identifier> original,
                                         ResourceFactory unused,
                                         ShaderStage.Type shaderType,
                                         String name)
    {
        if (!name.contains(":"))
        {
            return original.call(path);
        }

        Identifier split = Identifier.of(name);
        return Identifier.of(split.getNamespace(), "shaders/program/" + split.getPath() + shaderType.getFileExtension());
    }
}
