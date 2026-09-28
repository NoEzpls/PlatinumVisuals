package dev.platinum.visuals.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.platinum.visuals.Feature;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
  @Inject(method="bobView",at=@At("HEAD"),cancellable=true)
  private void platinum$walk(PoseStack pose,float partial,CallbackInfo ci){if(Feature.NO_BOBBING.enabled)ci.cancel();}
  @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
  private void aria$hurt(PoseStack pose, float partial, CallbackInfo ci) {
    if (Feature.NO_HURT_SHAKE.enabled) ci.cancel();
  }
}
