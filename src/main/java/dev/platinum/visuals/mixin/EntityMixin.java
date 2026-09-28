package dev.platinum.visuals.mixin;

import dev.platinum.visuals.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Entity.class)
public abstract class EntityMixin {
  @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
  private void aria$visibility(Player viewer, CallbackInfoReturnable<Boolean> cir) {
    if (viewer == Minecraft.getInstance().player && Client.reveal((Entity) (Object) this))
      cir.setReturnValue(false);
  }

  @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
  private void aria$color(CallbackInfoReturnable<Integer> cir) {
    Entity e = (Entity) (Object) this;
    if (Client.glow(e)) cir.setReturnValue(Client.glowColor(e));
  }

  @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
  private void aria$look(double dx, double dy, CallbackInfo ci) {
    if (Client.looking && (Object) this == Minecraft.getInstance().player) {
      Client.lookYaw += (float) (dx * .15);
      Client.lookPitch = (float) Math.clamp(Client.lookPitch + dy * .15, -90, 90);
      ci.cancel();
    }
  }
}
