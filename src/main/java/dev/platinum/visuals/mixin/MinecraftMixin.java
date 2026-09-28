package dev.platinum.visuals.mixin;

import dev.platinum.visuals.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
  @Inject(method = "shouldEntityAppearGlowing", at = @At("RETURN"), cancellable = true)
  private void aria$glow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
    if (Client.glow(entity)) cir.setReturnValue(true);
  }
}
