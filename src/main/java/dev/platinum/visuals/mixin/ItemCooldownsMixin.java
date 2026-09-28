package dev.platinum.visuals.mixin;
import dev.platinum.visuals.Cooldowns;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemCooldowns.class)public abstract class ItemCooldownsMixin{
 @Inject(method="addCooldown(Lnet/minecraft/resources/Identifier;I)V",at=@At("TAIL"))private void platinum$add(Identifier id,int ticks,CallbackInfo ci){var p=Minecraft.getInstance().player;if(p!=null&&(Object)this==p.getCooldowns())Cooldowns.started(id,ticks);}
 @Inject(method="removeCooldown",at=@At("TAIL"))private void platinum$remove(Identifier id,CallbackInfo ci){var p=Minecraft.getInstance().player;if(p!=null&&(Object)this==p.getCooldowns())Cooldowns.started(id,0);}
}
