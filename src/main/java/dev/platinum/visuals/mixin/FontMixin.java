package dev.platinum.visuals.mixin;

import dev.platinum.visuals.CachedUiText;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public abstract class FontMixin {
  @Inject(method="prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;",at=@At("HEAD"),cancellable=true)
  private void platinum$cached(FormattedCharSequence text,float x,float y,int color,boolean shadow,boolean empty,int background,CallbackInfoReturnable<Font.PreparedText> cir){
    if(text instanceof CachedUiText cache && x==0 && y==0 && !shadow && !empty && background==0){
      Font.PreparedText hit=cache.cached((Font)(Object)this,color);if(hit!=null)cir.setReturnValue(hit);
    }
  }
  @Inject(method="prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;",at=@At("RETURN"))
  private void platinum$remember(FormattedCharSequence text,float x,float y,int color,boolean shadow,boolean empty,int background,CallbackInfoReturnable<Font.PreparedText> cir){
    if(text instanceof CachedUiText cache && x==0 && y==0 && !shadow && !empty && background==0)cache.remember(color,cir.getReturnValue());
  }
}
