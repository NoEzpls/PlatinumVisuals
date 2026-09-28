package dev.platinum.visuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.platinum.visuals.Feature;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
  @ModifyExpressionValue(
      method = "updateLightTexture",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/level/dimension/DimensionType;ambientLight()F"))
  private float aria$bright(float original) {
    return Feature.FULLBRIGHT.enabled ? Math.max(original,(float)Feature.FULLBRIGHT.value("strength")) : original;
  }
}
