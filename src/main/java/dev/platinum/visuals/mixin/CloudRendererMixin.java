package dev.platinum.visuals.mixin;

import dev.platinum.visuals.EnvironmentVisuals;
import dev.platinum.visuals.Feature;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(CloudRenderer.class)
public abstract class CloudRendererMixin {
  @ModifyVariable(method="render(ILnet/minecraft/client/CloudStatus;FLnet/minecraft/world/phys/Vec3;JF)V",at=@At("HEAD"),argsOnly=true,ordinal=0)
  private int platinum$color(int original){return EnvironmentVisuals.clouds(original);}
  @ModifyVariable(method="render(ILnet/minecraft/client/CloudStatus;FLnet/minecraft/world/phys/Vec3;JF)V",at=@At("HEAD"),argsOnly=true,ordinal=0)
  private float platinum$height(float original){return Feature.CLOUDS.enabled?original+(float)Feature.CLOUDS.value("height"):original;}
}
