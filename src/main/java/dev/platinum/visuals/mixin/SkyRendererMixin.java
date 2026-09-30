package dev.platinum.visuals.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.platinum.visuals.EnvironmentVisuals;
import dev.platinum.visuals.Feature;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
  @Inject(method="extractRenderState",at=@At("TAIL"))
  private void platinum$sky(ClientLevel level,float partial,Camera camera,SkyRenderState state,CallbackInfo ci){EnvironmentVisuals.sky(state);}
  @Inject(method="renderSun",at=@At("HEAD"),cancellable=true)
  private void platinum$sun(float brightness,PoseStack pose,CallbackInfo ci){if(Feature.SKY.enabled&&Feature.SKY.value("sun")==0)ci.cancel();}
  @Inject(method="renderMoon",at=@At("HEAD"),cancellable=true)
  private void platinum$moon(MoonPhase phase,float brightness,PoseStack pose,CallbackInfo ci){if(Feature.SKY.enabled&&Feature.SKY.value("moon")==0)ci.cancel();}
}
