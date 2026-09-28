package dev.platinum.visuals.mixin;
import dev.platinum.visuals.Cooldowns;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(MultiPlayerGameMode.class)public abstract class ItemUseMixin{
 @Unique private ItemStack platinum$used=ItemStack.EMPTY;
 @Inject(method="useItem",at=@At("HEAD"))private void platinum$before(Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResult> cir){platinum$used=player.getItemInHand(hand).copyWithCount(1);}
 @Inject(method="useItem",at=@At("RETURN"))private void platinum$after(Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResult> cir){if(cir.getReturnValue().consumesAction())Cooldowns.used(platinum$used);platinum$used=ItemStack.EMPTY;}
}
