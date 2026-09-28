package dev.platinum.visuals.mixin;

import dev.platinum.visuals.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client-side-only interpolation; XP awarding remains authoritative on server. */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
  @Inject(method="tick",at=@At("TAIL"))
  private void platinum$fastVisualPickup(CallbackInfo ci) {
    if (!Feature.FAST_XP.enabled) return;
    var player=Minecraft.getInstance().player;
    ExperienceOrb orb=(ExperienceOrb)(Object)this;
    if (player==null || !orb.level().isClientSide()) return;
    Vec3 target=player.position().add(0,player.getBbHeight()*.45,0);
    double range=Feature.FAST_XP.value("range");
    if (orb.position().distanceToSqr(target)>range*range) return;
    double speed=Feature.FAST_XP.value("speed");
    Vec3 next=orb.position().lerp(target,speed);
    orb.setPos(next.x,next.y,next.z);
  }
}
