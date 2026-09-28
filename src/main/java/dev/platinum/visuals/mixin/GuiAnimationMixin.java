package dev.platinum.visuals.mixin;

import dev.platinum.visuals.Feature;
import dev.platinum.visuals.InterfaceAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiAnimationMixin {
  @Inject(method="renderTabList",at=@At("HEAD"))
  private void platinum$tabIn(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
    if (!Feature.ANIMATION.enabled) return;
    float p=InterfaceAnimation.tabOpen(), cx=graphics.guiWidth()/2f;
    graphics.pose().pushMatrix();graphics.pose().translate(cx,0);graphics.pose().scale(.92f+.08f*p,.92f+.08f*p);graphics.pose().translate(-cx,-(1-p)*18);
  }
  @Inject(method="renderTabList",at=@At("RETURN"))
  private void platinum$tabOut(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
    if (Feature.ANIMATION.enabled) graphics.pose().popMatrix();
  }
  @Inject(method="renderHotbar",at=@At("HEAD"))
  private void platinum$hotbarIn(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
    if (!Feature.ANIMATION.enabled) return;
    var player=Minecraft.getInstance().player;
    float offset=player==null?0:InterfaceAnimation.hotbar(player.getInventory().getSelectedSlot());
    graphics.pose().pushMatrix();graphics.pose().translate(offset,Math.abs(offset)*.16f);
  }
  @Inject(method="renderHotbar",at=@At("RETURN"))
  private void platinum$hotbarOut(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
    if (Feature.ANIMATION.enabled) graphics.pose().popMatrix();
  }
}
