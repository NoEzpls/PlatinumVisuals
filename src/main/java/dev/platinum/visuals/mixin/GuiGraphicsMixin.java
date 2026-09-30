package dev.platinum.visuals.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import dev.platinum.visuals.Ui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Linear sampling is scoped to our sprite submissions; vanilla retains its own sampler. */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
  @ModifyExpressionValue(method="innerBlit",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/texture/AbstractTexture;getSampler()Lcom/mojang/blaze3d/textures/GpuSampler;"))
  private GpuSampler platinum$smoothSprites(GpuSampler original){
    return Ui.linearSprites()?RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR):original;
  }
}
