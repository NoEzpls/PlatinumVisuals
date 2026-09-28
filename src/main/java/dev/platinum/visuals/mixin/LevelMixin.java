package dev.platinum.visuals.mixin;

import dev.platinum.visuals.Feature;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
  @Inject(method={"getRainLevel","getThunderLevel"},at=@At("HEAD"),cancellable=true)
  private void platinum$weather(float partial,CallbackInfoReturnable<Float> cir){if((Object)this instanceof ClientLevel&&Feature.WEATHER.enabled)cir.setReturnValue(0f);}
  @Inject(method = "getDayTime", at = @At("HEAD"), cancellable = true)
  private void aria$time(CallbackInfoReturnable<Long> cir) {
    if ((Object) this instanceof ClientLevel && Feature.WORLD_TIME.enabled)
      cir.setReturnValue((long) Feature.WORLD_TIME.value("time"));
  }
}
