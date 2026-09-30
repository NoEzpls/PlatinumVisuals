package dev.platinum.visuals.mixin;

import dev.platinum.visuals.Feature;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A transient render override, without writing the user's options.txt. */
@Mixin(Options.class)
public abstract class CloudOptionsMixin {
  @Inject(method="getCloudsType",at=@At("RETURN"),cancellable=true)
  private void platinum$clouds(CallbackInfoReturnable<CloudStatus> cir){
    if(Feature.CLOUDS.enabled)cir.setReturnValue(switch((int)Feature.CLOUDS.value("cloudMode")){case 0->CloudStatus.OFF;case 1->CloudStatus.FAST;default->CloudStatus.FANCY;});
  }
}
