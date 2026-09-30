package dev.platinum.visuals.mixin;

import dev.platinum.visuals.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class PacketListenerMixin {
  @Inject(method = "handleEntityEvent", at = @At("TAIL"))
  private void aria$totem(ClientboundEntityEventPacket packet, CallbackInfo ci) {
    var level = Minecraft.getInstance().level;
    if (level != null && packet.getEventId() == 35) Client.HUD.popped(packet.getEntity(level));
  }

}
