package dev.platinum.visuals.mixin;

import dev.platinum.visuals.EnvironmentVisuals;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public abstract class BiomeColorsMixin {
  @Inject(method="getAverageWaterColor",at=@At("RETURN"),cancellable=true)
  private static void platinum$water(BlockAndTintGetter level,BlockPos pos,CallbackInfoReturnable<Integer> cir){cir.setReturnValue(EnvironmentVisuals.water(cir.getReturnValue()));}
}
