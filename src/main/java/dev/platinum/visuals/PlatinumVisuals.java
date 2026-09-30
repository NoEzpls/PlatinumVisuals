package dev.platinum.visuals;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = "platinumvisuals", dist = Dist.CLIENT)
public final class PlatinumVisuals {
  public PlatinumVisuals(
      net.neoforged.bus.api.IEventBus bus, net.neoforged.fml.ModContainer container) {
    Client.init(bus);
    container.registerExtensionPoint(
        net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
        (mod, parent) -> new VisualsScreen(parent));
  }
}
