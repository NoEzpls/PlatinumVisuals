package dev.platinum.visuals;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Lightweight Pulse-like screen effects. All work is bounded and client-only. */
public final class VisualAddons {
  private VisualAddons() {}

  public static void render(GuiGraphics g, int width, int height, boolean edit) {
    long now = System.nanoTime();
    if(Feature.CROSSHAIR.enabled&&!edit)renderCrosshair(g,width,height);
    if (Feature.MOTION_BLUR.enabled && !edit) {
      float strength = (float) Feature.MOTION_BLUR.value("strength");
      int bands = 6;
      for (int i = 0; i < bands; i++) {
        int a = (int) (255 * strength * (1 - i / (float) bands) * .16f);
        int c = (a << 24) | (Config.accent() & 0xffffff);
        int h = Math.max(1, height / 18);
        g.fill(0, i * h, width, (i + 1) * h, c);
        g.fill(0, height - (i + 1) * h, width, height - i * h, c);
      }
    }
    if (Feature.SCREEN_TINT.enabled && !edit) {
      int color = Feature.SCREEN_TINT.effectColor();
      int alpha = (int) (255 * Feature.SCREEN_TINT.value("strength"));
      g.fill(0, 0, width, height, (alpha << 24) | (color & 0xffffff));
    }
    if (Feature.CPS.enabled) {
      panel(g, "CPS", 8, height - 72, 74, 27, edit);
      Ui.text(g, "CPS", 16, height - 65, Config.accent(), .82f);
      Ui.text(g, Integer.toString(Client.cps()), 47, height - 65, Ui.TEXT, .9f);
    }
    if (Feature.PING.enabled) {
      int ping = 0;
      var mc = Minecraft.getInstance();
      if (mc.player != null && mc.getConnection() != null && mc.getConnection().getPlayerInfo(mc.player.getUUID()) != null)
        ping = mc.getConnection().getPlayerInfo(mc.player.getUUID()).getLatency();
      panel(g, "Ping", 88, height - 72, 86, 27, edit);
      Ui.text(g, "PING", 96, height - 65, Config.accent(), .78f);
      Ui.text(g, ping + " ms", 132, height - 65, Ui.TEXT, .82f);
    }
    if (Feature.FPS_GRAPH.enabled) {
      int x = width - 145, y = height - 83;
      panel(g, "FPS Graph", x, y, 137, 58, edit);
      Ui.small(g, "FPS / frame time", x + 8, y + 7, Config.accent());
      for (int i = 0; i < 54; i++) {
        float ms = Client.BUDGET.sample(i);
        float bar = Math.clamp(ms / 33.3f, 0, 1) * 27;
        Ui.round(g, x + 8 + i * 2.2f, y + 48 - bar, 1.5f, Math.max(1, bar), ms > 16.7f ? 0xffffb26f : Config.accent());
      }
    }
    if (Feature.DAMAGE_LOG.enabled && Client.lastDamageText != null && !Client.lastDamageText.isEmpty()) {
      panel(g, "Damage Log", 8, height - 111, 154, 28, edit);
      String target = Client.target == null ? "цель" : Client.target.getName().getString();
      Ui.small(g, Ui.trim(target + "  " + Client.lastDamageText, 176), 16, height - 104, Ui.TEXT);
    }
    if (Feature.DAMAGE_NUMBERS.enabled && Client.damageUntil > now && !Client.lastDamageText.isEmpty() && !edit) {
      double remaining = (Client.damageUntil - now) / 1_000_000_000.0;
      float rise = (float) ((1.1 - remaining) * 22);
      float alpha = (float) Math.clamp(remaining / .35, 0, 1);
      Ui.opacity(alpha);
      Ui.center(g, Client.lastDamageText, width / 2f, height / 2f - 28 - rise, 0xffff91a9, (float) Feature.DAMAGE_NUMBERS.value("scale"));
      Ui.opacity(1);
    }
    if (Feature.HIT_MARKER.enabled && Client.hitMarkerUntil > now && !edit) {
      float alpha = (Client.hitMarkerUntil - now) / 350_000_000f;
      int size = (int) Feature.HIT_MARKER.value("size");
      int color = Ui.alpha(0xffffffff, alpha);
      int cx = width / 2, cy = height / 2;
      g.fill(cx - size - 1, cy - size - 1, cx - 2, cy - size + 1, color);
      g.fill(cx - size - 1, cy + size - 1, cx - 2, cy + size + 1, color);
      g.fill(cx + 2, cy - size - 1, cx + size + 1, cy - size + 1, color);
      g.fill(cx + 2, cy + size - 1, cx + size + 1, cy + size + 1, color);
      if (Feature.DAMAGE_TILT.enabled) {
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().rotate((float) (Math.sin(now / 90_000_000.0) * .0524 * Feature.DAMAGE_TILT.value("strength")));
        g.fill(-size - 3, -1, -size + 1, 1, Ui.alpha(0xff8de6ff, alpha));
        g.fill(size - 1, -1, size + 3, 1, Ui.alpha(0xff8de6ff, alpha));
        g.pose().popMatrix();
      }
    }
    if (Feature.TOTEM_POP_EFFECT.enabled && Client.totemEffectUntil > now && !edit) {
      float alpha = (Client.totemEffectUntil - now) / 1_200_000_000f;
      Ui.opacity(Math.min(1, alpha * 1.7f));
      Ui.center(g, "TOTEM POP", width / 2f, height / 2f + 38, Config.accent(), 1.05f);
      Ui.opacity(1);
    }
  }

  private static void renderCrosshair(GuiGraphics g,int width,int height){
    var mc=Minecraft.getInstance();int cx=width/2,cy=height/2;
    int gap=(int)Feature.CROSSHAIR.value("gap"),length=(int)Feature.CROSSHAIR.value("length"),thick=(int)Feature.CROSSHAIR.value("width");
    if(Feature.CROSSHAIR.value("dynamic")>0&&mc.player!=null)gap+=Math.min(7,(int)Math.round(mc.player.getDeltaMovement().horizontalDistance()*18));
    int color=Feature.CROSSHAIR.effectColor(),outline=0xb0000000;
    if(Feature.CROSSHAIR.value("outline")>0){
      g.fill(cx-thick/2-1,cy-gap-length-1,cx+(thick+1)/2+1,cy-gap+1,outline);
      g.fill(cx-thick/2-1,cy+gap-1,cx+(thick+1)/2+1,cy+gap+length+1,outline);
      g.fill(cx-gap-length-1,cy-thick/2-1,cx-gap+1,cy+(thick+1)/2+1,outline);
      g.fill(cx+gap-1,cy-thick/2-1,cx+gap+length+1,cy+(thick+1)/2+1,outline);
    }
    g.fill(cx-thick/2,cy-gap-length,cx+(thick+1)/2,cy-gap,color);g.fill(cx-thick/2,cy+gap,cx+(thick+1)/2,cy+gap+length,color);
    g.fill(cx-gap-length,cy-thick/2,cx-gap,cy+(thick+1)/2,color);g.fill(cx+gap,cy-thick/2,cx+gap+length,cy+(thick+1)/2,color);
    if(Feature.CROSSHAIR.value("dot")>0)g.fill(cx,cy,cx+1,cy+1,color);
  }

  private static void panel(GuiGraphics g, String id, int x, int y, int w, int h, boolean edit) {
    int color = edit ? 0xea211c2a : Ui.alpha(Ui.PANEL, Config.hudOpacity);
    if (Feature.HUD_BLUR.enabled) color = Ui.alpha(color, .92);
    Ui.shadow(g, x, y, w, h);
    Ui.round(g, x, y, w, h, color);
    if (edit) g.renderOutline(x, y, w, h, 0xff5b526e);
  }
}
