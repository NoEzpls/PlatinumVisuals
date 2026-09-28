package dev.platinum.visuals;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class HudEditorScreen extends Screen {
  private final Screen previous;
  private Hud.Bounds dragged;
  private double offsetX, offsetY;

  public HudEditorScreen(Screen previous) {
    super(Component.literal("HUD editor"));
    this.previous = previous;
  }

  @Override
  public void render(GuiGraphics g, int mx, int my, float dt) {
    g.fill(0, 0, width, height, 0x55080a12);
    Client.HUD.render(g, true);
    String hint = "Перетаскивай · Колесо над панелью: размер · S: привязка · R: сброс · Esc";
    Ui.round(g, Math.max(0, (width - 360) / 2), height - 24, Math.min(width, 360), 20, 0xf01a1b28);
    Ui.small(
        g,
        Ui.trim(hint, (int) ((width - 12) / .8)),
        Math.max(6, (width - 348) / 2),
        height - 17,
        Ui.TEXT);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent e, boolean twice) {
    for (int i = Client.HUD.bounds.size() - 1; i >= 0; i--) {
      var b = Client.HUD.bounds.get(i);
      if (Ui.inside(e.x(), e.y(), b.x(), b.y(), b.w(), b.h())) {
        dragged = b;
        offsetX = e.x() - b.x();
        offsetY = e.y() - b.y();
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
    if (dragged == null) return false;
    double left=e.x()-offsetX,top=e.y()-offsetY;
    if(Config.hudSnap){left=snap(left,width-dragged.w());top=snap(top,height-dragged.h());}
    float x = (float) Math.clamp(left / Math.max(1, width - dragged.w()), 0, 1),
        y = (float) Math.clamp(top / Math.max(1, height - dragged.h()), 0, 1);
    Config.positions.put(dragged.id(), new Config.Position(x, y));
    Config.dirty();
    return true;
  }

  @Override
  public boolean mouseReleased(MouseButtonEvent e) {
    dragged = null;
    return true;
  }

  @Override
  public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
    for(int i=Client.HUD.bounds.size()-1;i>=0;i--){var b=Client.HUD.bounds.get(i);if(Ui.inside(x,y,b.x(),b.y(),b.w(),b.h())){Config.hudScales.put(b.id(),(float)Math.clamp(Config.widgetScale(b.id())+vertical*.05,.5,2));Config.dirty();return true;}}
    return false;
  }

  @Override
  public boolean keyPressed(KeyEvent e) {
    if (e.key() == GLFW.GLFW_KEY_R) {
      Config.positions.clear();
      Config.hudScales.clear();
      Config.hudScale = 1;
      Config.dirty();
      return true;
    }
    if(e.key()==GLFW.GLFW_KEY_S){Config.hudSnap=!Config.hudSnap;Config.dirty();return true;}
    return super.keyPressed(e);
  }
  private static double snap(double value,double extent){for(double guide:new double[]{0,extent/2,extent})if(Math.abs(value-guide)<6)return guide;return value;}

  @Override
  public void onClose() {
    Config.save();
    minecraft.setScreen(previous);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }
}
