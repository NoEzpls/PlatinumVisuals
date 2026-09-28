package dev.platinum.visuals;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Expensive scans and text formatting happen at 5 Hz, not on every rendered frame. */
public final class Hud {
  public record Bounds(String id, int x, int y, int w, int h) {}

  public final List<Bounds> bounds = new ArrayList<>();
  private final Map<String, List<String>> rows = new HashMap<>();
  private final Map<Item, Integer> previousItems = new HashMap<>();
  private final LinkedHashMap<UUID, Integer> pops = new LinkedHashMap<>();
  private final ArrayDeque<String> pickupLog = new ArrayDeque<>(), totemLog = new ArrayDeque<>();
  private int totems;
  private String totemText="× 0", targetName="Target HUD", targetInfo="16.0 HP  ·  3.0 м", noticeText="";
  private long noticeUntil;
  private float targetRatio=.8f;
  private final Motion.Clock animationClock=new Motion.Clock();
  private List<String> pickupRows=List.of(), totemRows=List.of();
  private static final ItemStack TOTEM_ICON=new ItemStack(Items.TOTEM_OF_UNDYING);
  private static final ItemStack ARMOR_PREVIEW=new ItemStack(Items.DIAMOND_HELMET);
  private static final String[] PERCENT=new String[101];
  static { for(int i=0;i<=100;i++)PERCENT[i]=i+"%"; }
  public void notice(String text){noticeText=text;noticeUntil=System.nanoTime()+2_400_000_000L;}

  private String watermark = "", position = "";
  private boolean inventoryInitialized;
  private static final EquipmentSlot[] ARMOR = {
    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
  };
  private int scaledW, scaledH;
  private float scale;
  private boolean widgetOpen;
  private String performance="";

  public void reset() {
    rows.clear();
    previousItems.clear();
    pops.clear();
    pickupLog.clear();
    totemLog.clear();
    inventoryInitialized = false;
    bounds.clear();
    pickupRows=totemRows=List.of();noticeUntil=0;noticeText="";
  }

  public void tick(Minecraft mc) {
    if (Client.ticks % Math.max(1,Math.round(20f/(float)Feature.OPTIMIZATION.value("hudRate"))) != 0) return;
    var p = mc.player;
    if (Feature.TARGET_HUD.enabled && Client.target != null) {
      targetName=Ui.trim(name(Client.target),140);
      targetInfo=format(Client.target.getHealth())+" HP  ·  "+format(p.distanceTo(Client.target))+" м";
    }
    if (Feature.WATERMARK.enabled) {
      int latency = 0;
      if (mc.getConnection() != null) {
        var info = mc.getConnection().getPlayerInfo(p.getUUID());
        if (info != null) latency = info.getLatency();
      }
      StringBuilder stamp=new StringBuilder();
      if(Feature.WATERMARK.value("fps")>0)stamp.append(mc.getFps()).append(" FPS");
      if(Feature.WATERMARK.value("ping")>0){if(!stamp.isEmpty())stamp.append("  ·  ");stamp.append(latency).append(" ms");}
      if(Feature.WATERMARK.value("speed")>0){if(!stamp.isEmpty())stamp.append("  ·  ");stamp.append(format(p.getDeltaMovement().horizontalDistance()*20)).append(" м/с");}
      watermark=stamp.toString();
      position = String.format(Locale.ROOT, "XYZ  %.0f  %.0f  %.0f", p.getX(), p.getY(), p.getZ());
    }
    if (Feature.POTIONS.enabled) {
      List<String> out = new ArrayList<>();
      for (var effect : p.getActiveEffects()) {
        int seconds = effect.getDuration() / 20;
        String time =
            effect.isInfiniteDuration()
                ? "∞"
                : seconds / 60 + ":" + String.format(Locale.ROOT, "%02d", seconds % 60);
        out.add(
            effect.getEffect().value().getDisplayName().getString()
                + " "
                + (effect.getAmplifier() + 1)
                + "  "
                + time);
        if (out.size() >= Feature.POTIONS.value("rows")) break;
      }
      rows.put("Potions", List.copyOf(out));
    }
    if (Feature.HOTKEYS.enabled) {
      List<String> out = new ArrayList<>();
      for (Feature f : Feature.values())
        if (f.enabled && f.key >= 0) out.add(f.title + "  [" + VisualsScreen.keyName(f.key) + "]");
      rows.put("Hotkeys", List.copyOf(out));
    }
    if (Feature.ANTI_INVIS.enabled&&Feature.ANTI_INVIS.value("list")>0) {
      double range = Feature.ANTI_INVIS.value("distance");
      List<String> out =
          mc.level.players().stream()
              .filter(e -> Client.reveal(e) && p.distanceToSqr(e) <= range * range)
              .sorted(Comparator.comparingDouble(p::distanceToSqr))
              .limit(6)
              .map(e -> name(e) + " · " + format(p.distanceTo(e)) + " м")
              .toList();
      rows.put("Anti Invis", out);
    }
    if (Feature.TNT_TIMER.enabled) {
      List<String> out =
          mc.level.getEntitiesOfClass(PrimedTnt.class, p.getBoundingBox().inflate(Feature.TNT_TIMER.value("distance"))).stream()
              .sorted(Comparator.comparingDouble(p::distanceToSqr))
              .limit((int)Feature.TNT_TIMER.value("rows"))
              .map(t -> format(t.getFuse() / 20.0) + " с  ·  " + format(p.distanceTo(t)) + " м")
              .toList();
      rows.put("TNT Timer", out);
    }
    if(Feature.PERFORMANCE_HUD.enabled)performance=format(Client.BUDGET.frameMs())+" ms  ·  "+(int)(Client.BUDGET.quality()*100)+"% эффектов";
    if (Feature.TOTEMS.enabled) {
      totems = p.getOffhandItem().is(Items.TOTEM_OF_UNDYING) ? p.getOffhandItem().getCount() : 0;
      for (int i = 0; i < 36; i++) {
        ItemStack s = p.getInventory().getItem(i);
        if (s.is(Items.TOTEM_OF_UNDYING)) totems += s.getCount();
      }
    }
    totemText="× "+totems;
    if (Feature.SATURATION.enabled)
      rows.put(
          "Saturation",
          List.of(
              "HP " + format(p.getHealth()) + "  ·  Голод " + p.getFoodData().getFoodLevel(),
              "Насыщение " + format(p.getFoodData().getSaturationLevel())));
    if (Feature.PICKUP_LOG.enabled) {
      Map<Item, Integer> counts = new HashMap<>();
      for (int i = 0; i < 36; i++) {
        ItemStack s = p.getInventory().getItem(i);
        if (!s.isEmpty()) counts.merge(s.getItem(), s.getCount(), Integer::sum);
      }
      if (!p.getOffhandItem().isEmpty())
        counts.merge(p.getOffhandItem().getItem(), p.getOffhandItem().getCount(), Integer::sum);
      if (inventoryInitialized)
        for (var entry : counts.entrySet()) {
          int delta = entry.getValue() - previousItems.getOrDefault(entry.getKey(), 0);
          if (delta > 0)
            addLine(
                pickupLog,
                "+" + delta + " " + new ItemStack(entry.getKey()).getHoverName().getString());
        }
      previousItems.clear();
      previousItems.putAll(counts);
      inventoryInitialized = true;
    } else {
      previousItems.clear();
      inventoryInitialized = false;
      pickupLog.clear();
    }
    pickupRows=List.copyOf(pickupLog);
  }

  public void popped(Entity e) {
    if ((!Feature.TOTEM_TRACKER.enabled && !Feature.TOTEM_POP_EFFECT.enabled) || e == null) return;
    if (Feature.TOTEM_POP_EFFECT.enabled)
      Client.totemEffectUntil = System.nanoTime() + (long) (Feature.TOTEM_POP_EFFECT.value("lifetime") * 1_000_000_000L);
    if (!pops.containsKey(e.getUUID()) && pops.size() >= 128)
      pops.remove(pops.keySet().iterator().next());
    int count = pops.merge(e.getUUID(), 1, Integer::sum);
    addLine(totemLog, name(e) + "  ×" + count);
    totemRows=List.copyOf(totemLog);
  }

  private static void addLine(ArrayDeque<String> list, String s) {
    if (list.size() >= 5) list.removeFirst();
    list.addLast(s);
  }

  private static String name(Entity e) {
    return Feature.STREAMER.enabled ? "Player" : e.getName().getString();
  }

  private static String format(double n) {
    return String.format(Locale.ROOT, "%.1f", n);
  }

  public void render(GuiGraphics g, boolean edit) {
    var mc = Minecraft.getInstance();
    if (mc.player == null && !edit) return;
    bounds.clear();
    double dt=animationClock.step();
    scale = Config.hudScale;
    scaledW = (int) (g.guiWidth() / scale);
    scaledH = (int) (g.guiHeight() / scale);
    g.pose().pushMatrix();
    g.pose().scale(scale, scale);
    widgetOpen=false;
    VisualAddons.render(g, scaledW, scaledH, edit);
    if (Feature.WATERMARK.enabled) {
      boolean coords=Feature.WATERMARK.value("coords")>0;
      Bounds b = panel(g, "Watermark", 8, 8, 266, coords?44:28, edit);
      Ui.icon(g,"logo",b.x+6,b.y+5,15,Config.accent());
      Ui.text(g, "Platinum", b.x + 25, b.y + 7, Config.accent(),.93f);
      Ui.small(g, watermark.isEmpty() ? "FPS · ms · м/с" : watermark, b.x + 80, b.y + 8, Ui.TEXT);
      if(coords)Ui.small(
          g, Feature.STREAMER.enabled ? "Streamer mode" : position, b.x + 8, b.y + 24, Ui.MUTED);
    }
    if (Feature.ARMOR.enabled) {
      Bounds b = panel(g, "Armor HUD", 8, 60, 128, 48, edit);
      title(g, "Броня", b);
      for (int i = 0; i < 4; i++) {
        ItemStack s =
            mc.player == null
                ? ARMOR_PREVIEW
                : mc.player.getItemBySlot(ARMOR[i]);
        int ix = b.x + 8 + i * 29;
        g.renderItem(s, ix, b.y + 20);
        if (!s.isEmpty() && s.isDamageableItem()&&Feature.ARMOR.value("durability")>0) {
          int pct = (int) (100.0 * (s.getMaxDamage() - s.getDamageValue()) / s.getMaxDamage());
          Ui.small(g, PERCENT[Math.clamp(pct,0,100)], ix, b.y + 37, pct <= 15 ? 0xffff788c : Ui.MUTED);
        }
      }
    }
    if (Feature.POTIONS.enabled)
      list(
          g, "Potions", 8, 116, 154, rows.getOrDefault("Potions", List.of()), edit, "Нет эффектов");
    if (Feature.HOTKEYS.enabled)
      list(
          g,
          "Hotkeys",
          scaledW - 163,
          8,
          155,
          rows.getOrDefault("Hotkeys", List.of()),
          edit,
          "Назначь клавиши в меню");
    if (Feature.ANTI_INVIS.enabled&&Feature.ANTI_INVIS.value("list")>0)
      list(
          g,
          "Anti Invis",
          scaledW - 163,
          82,
          155,
          rows.getOrDefault("Anti Invis", List.of()),
          edit,
          "Невидимых рядом нет");
    if (Feature.COOLDOWNS.enabled)cooldowns(g,edit);
    if (Feature.TNT_TIMER.enabled)
      list(
          g,
          "TNT Timer",
          scaledW - 163,
          265,
          155,
          rows.getOrDefault("TNT Timer", List.of()),
          edit,
          "Нет загруженного TNT");
    if (Feature.SATURATION.enabled)
      list(
          g,
          "Saturation",
          8,
          scaledH - 97,
          170,
          rows.getOrDefault("Saturation", List.of()),
          edit,
          "HP · Голод · Насыщение");
    if (Feature.PICKUP_LOG.enabled)
      list(
          g,
          "Pickup Logger",
          8,
          scaledH - 174,
          170,
          pickupRows,
          edit,
          "Пока нет изменений");
    if (Feature.TOTEM_TRACKER.enabled)
      list(
          g,
          "Totem Tracker",
          scaledW - 178,
          scaledH - 114,
          170,
          totemRows,
          edit,
          "Использований не замечено");
    if (Feature.TOTEMS.enabled) {
      Bounds b = panel(g, "Totem Bar", scaledW / 2 + 108, scaledH - 52, 71, 28, edit);
      g.renderItem(TOTEM_ICON, b.x + 6, b.y + 6);
      Ui.text(g, totemText, b.x + 28, b.y + 10, Ui.TEXT);
    }
    if (Feature.INVENTORY.enabled) {
      Bounds b = panel(g, "Inventory HUD", scaledW / 2 - 89, scaledH - 153, 178, 77, edit);
      title(g, "Инвентарь", b);
      for (int i = 0; i < 27; i++) {
        int ix = b.x + 8 + i % 9 * 18, iy = b.y + 20 + i / 9 * 18;
        g.fill(ix, iy, ix + 17, iy + 17, 0x553f4050);
        ItemStack s = mc.player == null ? ItemStack.EMPTY : mc.player.getInventory().getItem(i + 9);
        g.renderItem(s, ix, iy);
        Ui.itemDecorations(g, s, ix, iy);
      }
    }
    if (Feature.TARGET_HUD.enabled && (Client.target != null || edit)) {
      int tw=(int)Feature.TARGET_HUD.value("width");
      Bounds b = panel(g, "Target HUD", scaledW / 2 - tw/2, scaledH / 2 + 43, tw, 56, edit);
      var t = Client.target;
      int inset=8;
      if(Feature.TARGET_HUD.value("portrait")>0){var portrait=t instanceof AbstractClientPlayer player?player:edit?mc.player:null;if(portrait!=null){PlayerFaceRenderer.draw(g,portrait.getSkin(),b.x+8,b.y+8,32);inset=48;}}
      Ui.text(g,Ui.trim(t == null ? "Target HUD" : targetName,tw-inset-8), b.x + inset, b.y + 8, Ui.TEXT);
      float hp = t == null ? 16 : t.getHealth(), max = t == null ? 20 : t.getMaxHealth();
      String targetLine=t == null ? "16.0 HP  ·  3.0 м" : targetInfo;
      if(Feature.TARGET_HUD.value("health")<=0 && t!=null) targetLine=Feature.TARGET_HUD.value("distance")>0?format(mc.player.distanceTo(t))+" м":"";
      else if(Feature.TARGET_HUD.value("distance")<=0 && t!=null) targetLine=format(t.getHealth())+" HP";
      Ui.small(g,targetLine,b.x+inset,b.y+23,Ui.MUTED);
      targetRatio=Motion.approach(targetRatio,Math.clamp(hp/Math.max(1,max),0,1),12,dt);
      Ui.round(g, b.x + 8, b.y + 45, tw-16, 4, 0xff3b354a);
      Ui.round(
          g,
          b.x + 8,
          b.y + 45,
          Math.max(1,(tw-16) * targetRatio),
          4,
          Config.accent());
    }
    if(Feature.PERFORMANCE_HUD.enabled){Bounds b=panel(g,"Frame Monitor",8,scaledH-242,202,61,edit);title(g,"Frame Monitor",b);Ui.small(g,performance.isEmpty()?"Время кадра · бюджет эффектов":performance,b.x+8,b.y+21,Ui.TEXT);for(int i=0;i<64;i++){float ms=Client.BUDGET.sample(i);float bar=Math.clamp(ms/33.3f,0,1)*19;Ui.round(g,b.x+8+i*2.9f,b.y+55-bar,2,Math.max(1,bar),ms>1000/Feature.OPTIMIZATION.value("fps")?0xffffb58b:Config.accent());}}
    endWidget(g);
    g.pose().popMatrix();
    long remaining=noticeUntil-System.nanoTime();
    if(remaining>0&&!edit){
      float alpha=(float)Math.min(1,Math.min(remaining/250_000_000.0,(2_400_000_000L-remaining)/180_000_000.0));
      int tw=Ui.width(noticeText)+26;float tx=(g.guiWidth()-tw)/2f,ty=g.guiHeight()-80+(1-alpha)*8;
      Ui.opacity(alpha);Ui.shadow(g,tx,ty,tw,27);Ui.round(g,tx,ty,tw,27,Ui.PANEL);Ui.center(g,noticeText,g.guiWidth()/2f,ty+8,Ui.TEXT,.93f);Ui.opacity(1);
    }
    if (Feature.LOW_HP.enabled
        && mc.player != null
        && mc.player.getHealth() > 0
        && mc.player.getHealth() <= Feature.LOW_HP.value("health")
        && !edit) {
      int a = 30 + (int) (12 * Math.sin(System.nanoTime() / 250_000_000.0));
      int c = (a << 24) | 0xff304f;
      g.fill(0, 0, g.guiWidth(), 5, c);
      g.fill(0, g.guiHeight() - 5, g.guiWidth(), g.guiHeight(), c);
      g.fill(0, 5, 5, g.guiHeight() - 5, c);
      g.fill(g.guiWidth() - 5, 5, g.guiWidth(), g.guiHeight() - 5, c);
    }
  }

  private Bounds panel(GuiGraphics g, String id, int dx, int dy, int w, int h, boolean edit) {
    endWidget(g);Feature module=Feature.byTitle(id);float local=Config.widgetScale(id);
    Config.Position p = Config.positions.get(id);
    int x =
        p == null
            ? (int)Math.clamp(dx, 0, Math.max(0, scaledW - w*local))
            : (int) (p.x() * Math.max(0, scaledW - w*local));
    int y =
        p == null
            ? (int)Math.clamp(dy, 0, Math.max(0, scaledH - h*local))
            : (int) (p.y() * Math.max(0, scaledH - h*local));
    Bounds logical = new Bounds(id, x, y, w, h);
    if (edit) bounds.add(
        new Bounds(id, (int) (x * scale), (int) (y * scale), (int) (w * scale*local), (int) (h * scale*local)));
    g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(local,local);g.pose().translate(-x,-y);widgetOpen=true;
    boolean shadow=module==null||!module.hasSetting("panelShadow")||module.value("panelShadow")>0;
    if(shadow)Ui.shadow(g,x,y,w,h);
    float opacity=module!=null&&module.hasSetting("backgroundOpacity")?(float)module.value("backgroundOpacity"):Config.hudOpacity;
    Ui.round(g, x, y, w, h, edit ? 0xea211c2a : Ui.alpha(0xff15141c,opacity));
    if(module!=null&&module.hasSetting("accentLine")&&module.value("accentLine")>0)g.fill(x,y,x+2,y+h,Config.accent());
    if (edit) g.renderOutline(x, y, w, h, 0xff5b526e);
    return logical;
  }
  private void endWidget(GuiGraphics g){if(widgetOpen){g.pose().popMatrix();widgetOpen=false;}}
  private void cooldowns(GuiGraphics g,boolean edit){
    List<Cooldowns.Row> data=Cooldowns.rows();if(data.isEmpty()&&!edit)return;
    int count=Math.min(data.size(),(int)Feature.COOLDOWNS.value("rows"));boolean icons=Feature.COOLDOWNS.value("icons")>0,bars=Feature.COOLDOWNS.value("bars")>0;
    int width=(int)Feature.COOLDOWNS.value("width"), row=(int)Feature.COOLDOWNS.value("rowHeight");
    Bounds b=panel(g,"Cooldowns",scaledW-width-8,183,width,24+Math.max(1,count)*row,edit);title(g,"Cooldowns · секунды",b);
    if(count==0)Ui.small(g,"Предметы готовы · ≈ свой таймер",b.x+8,b.y+28,Ui.MUTED);
    for(int i=0;i<count;i++){Cooldowns.Row item=data.get(i);int yy=b.y+23+i*row,inset=icons?29:8;if(icons&&!item.icon().isEmpty())g.renderItem(item.icon(),b.x+8,yy);float timeWidth=Ui.width(item.time())*.8f;Ui.small(g,Ui.trim(item.label(),(int)((width-inset-18-timeWidth)/.8f)),b.x+inset,yy+2,Ui.TEXT);Ui.small(g,item.time(),b.x+width-8-timeWidth,yy+2,item.estimated()?0xffffc38a:Config.accent());if(bars){Ui.round(g,b.x+inset,yy+row-7,width-inset-16,2,0xff393240);Ui.round(g,b.x+inset,yy+row-7,(float)((width-inset-16)*item.fraction()),2,Config.accent());}}
  }

  private static void title(GuiGraphics g, String s, Bounds b) {
    Ui.small(g, s, b.x + 8, b.y + 7, Config.accent());
  }

  private void list(
      GuiGraphics g,
      String id,
      int x,
      int y,
      int width,
      List<String> data,
      boolean edit,
      String empty) {
    Feature module=Feature.byTitle(id);
    boolean showEmpty=edit||module==null||!module.hasSetting("showEmpty")||module.value("showEmpty")>0;
    if (data.isEmpty() && !showEmpty) return;
    if(module!=null&&module.hasSetting("width"))width=(int)module.value("width");
    int rowHeight=module!=null&&module.hasSetting("rowHeight")?(int)module.value("rowHeight"):11;
    Bounds b = panel(g, id, x, y, width, 22 + Math.max(1, data.size()) * rowHeight, edit);
    title(g, id, b);
    int lineY = b.y + 22;
    if (data.isEmpty())
      Ui.small(g, Ui.trim(empty, (int) ((width - 16) / .8)), b.x + 8, lineY, Ui.MUTED);
    else
      for (String s : data) {
        Ui.small(g, Ui.trim(s, (int) ((width - 16) / .8)), b.x + 8, lineY, Ui.TEXT);
        lineY += rowHeight;
      }
  }
}
