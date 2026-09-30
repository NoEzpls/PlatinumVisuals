package dev.platinum.visuals;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

public final class Tooltips {
  public record Preview(List<ItemStack> items) implements TooltipComponent {}

  public static void gather(RenderTooltipEvent.GatherComponents e) {
    if (!Feature.SHULKER_PREVIEW.enabled) return;
    var contents = e.getItemStack().get(DataComponents.CONTAINER);
    if (contents == null || contents.getSlots() == 0) return;
    List<ItemStack> items = new ArrayList<>();
    for (int i = 0; i < Math.min(54, contents.getSlots()); i++)
      items.add(contents.getStackInSlot(i).copy());
    e.getTooltipElements().add(Either.right(new Preview(List.copyOf(items))));
  }

  public static void register(RegisterClientTooltipComponentFactoriesEvent e) {
    e.register(Preview.class, Grid::new);
  }

  private record Grid(Preview data) implements ClientTooltipComponent {
    public int getHeight(Font f) {
      return ((data.items.size() + 8) / 9) * 18 + 6;
    }

    public int getWidth(Font f) {
      return 166;
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics g) {
      Ui.round(g, x, y, 166, getHeight(font), 0xff20212f);
      for (int i = 0; i < data.items.size(); i++) {
        int sx = x + 2 + i % 9 * 18, sy = y + 3 + i / 9 * 18;
        g.fill(sx, sy, sx + 17, sy + 17, 0xff323344);
        ItemStack stack = data.items.get(i);
        g.renderItem(stack, sx, sy);
        Ui.itemDecorations(g, stack, sx, sy);
      }
    }
  }
}
