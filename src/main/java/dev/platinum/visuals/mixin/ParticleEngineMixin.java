package dev.platinum.visuals.mixin;

import dev.platinum.visuals.Client;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
  @Inject(method = "add", at = @At("HEAD"), cancellable = true)
  private void aria$budget(Particle particle, CallbackInfo ci) {
    if (!Client.BUDGET.acceptParticle()) {
      particle.remove();
      ci.cancel();
    }
  }
}
