package dev.platinum.visuals;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.lwjgl.glfw.GLFW;
import net.minecraft.util.TriState;

public final class Client {
  public static final Budget BUDGET = new Budget();
  public static final Effects EFFECTS = new Effects();
  public static final Hud HUD = new Hud();
  public static final KeyMapping.Category CATEGORY =
      new KeyMapping.Category(Identifier.fromNamespaceAndPath("platinumvisuals", "main"));
  public static final KeyMapping MENU =
      new KeyMapping(
          "key.platinumvisuals.menu",
          InputConstants.Type.KEYSYM,
          GLFW.GLFW_KEY_RIGHT_CONTROL,
          CATEGORY);
  public static final KeyMapping ZOOM =
      new KeyMapping("key.platinumvisuals.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY);
  public static final KeyMapping LOOK =
      new KeyMapping(
          "key.platinumvisuals.freelook", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
  public static LivingEntity target;
  public static int ticks, targetTick;
  public static boolean looking;
  public static float lookYaw, lookPitch;
  public static volatile float lastDamage;
  public static volatile long damageUntil, hitMarkerUntil, totemEffectUntil;
  public static volatile String lastDamageText = "";
  private static float previousTargetHealth = -1;
  private static final java.util.ArrayDeque<Long> clickTimes = new java.util.ArrayDeque<>();
  private static ClientLevel previousLevel;
  private static CameraType oldCamera;
  private static float zoom = 1;
  private static String lastChat = "";
  private static final java.util.LinkedHashMap<String,Integer> recentChat=new java.util.LinkedHashMap<>();
  private static final java.util.EnumSet<EquipmentSlot> weakArmor=java.util.EnumSet.noneOf(EquipmentSlot.class);

  public static void init(IEventBus modBus) {
    Config.load();
    modBus.addListener(Client::registerKeys);
    modBus.addListener(Client::reloadListeners);
    modBus.addListener(Tooltips::register);
    var b = NeoForge.EVENT_BUS;
    b.addListener(Client::tick);
    b.addListener(Client::key);
    b.addListener(Client::fov);
    b.addListener(Client::camera);
    b.addListener(Client::hand);
    b.addListener(Client::overlay);
    b.addListener(Client::fog);
    b.addListener(Client::fogColor);
    b.addListener(Client::attack);
    b.addListener(Client::gui);
    b.addListener(Client::layer);
    b.addListener(Client::chat);
    b.addListener(Client::nameTag);
    b.addListener(Tooltips::gather);
    b.addListener(Effects::blockOutline);
  }

  private static void reloadListeners(AddClientReloadListenersEvent e) {
    e.addListener(Ui.id("ui_font_cache"), (net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager -> Ui.invalidateFonts());
    e.addDependency(net.neoforged.neoforge.client.resources.VanillaClientListeners.FONTS, Ui.id("ui_font_cache"));
  }

  private static void registerKeys(RegisterKeyMappingsEvent e) {
    e.registerCategory(CATEGORY);
    e.register(MENU);
    e.register(ZOOM);
    e.register(LOOK);
  }

  private static void tick(ClientTickEvent.Post e) {
    var mc = Minecraft.getInstance();
    Config.tick();
    while (MENU.consumeClick()) if (mc.screen == null) mc.setScreen(new VisualsScreen());
    if (mc.level != previousLevel) {
      endLook();
      previousLevel = mc.level;
      target = null;
      ticks = targetTick = 0;
      zoom = 1;
      lastChat = "";
      lastDamage = 0;
      lastDamageText = "";
      damageUntil = hitMarkerUntil = totemEffectUntil = 0;
      previousTargetHealth = -1;
      clickTimes.clear();
      BUDGET.reset();
      EFFECTS.clear();
      HUD.reset();
      EnvironmentVisuals.reset();
      Cooldowns.reset();recentChat.clear();weakArmor.clear();
    }
    BUDGET.tick();
    if (mc.level == null || mc.player == null || mc.isPaused()) return;
    ticks++;
    EnvironmentVisuals.tick(mc);
    Cooldowns.tick();
    if (target != null && (target.isRemoved() || ticks - targetTick > Feature.TARGET_HUD.value("timeout")*20)) target = null;
    if (target != null && target.isAlive()) {
      float health = target.getHealth();
      if (previousTargetHealth >= 0 && previousTargetHealth - health > .01f) {
        lastDamage = previousTargetHealth - health;
        lastDamageText = String.format(java.util.Locale.ROOT, "−%.1f", lastDamage);
        damageUntil = System.nanoTime() + (long) (Feature.DAMAGE_NUMBERS.value("lifetime") * 1_000_000_000L);
        HUD.notice("Попадание  " + lastDamageText);
      }
      previousTargetHealth = health;
    } else previousTargetHealth = -1;
    if(ticks%20==0){for(EquipmentSlot slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}){var item=mc.player.getItemBySlot(slot);boolean weak=Feature.ARMOR_ALERT.enabled&&item.isDamageableItem()&&100.0*(item.getMaxDamage()-item.getDamageValue())/item.getMaxDamage()<=Feature.ARMOR_ALERT.value("threshold");if(weak&&weakArmor.add(slot))notice("Низкая прочность: "+item.getHoverName().getString());else if(!weak)weakArmor.remove(slot);}}
    boolean active = Feature.FREELOOK.enabled && LOOK.isDown() && mc.screen == null;
    if (active && !looking) {
      lookYaw = mc.player.getYRot();
      lookPitch = mc.player.getXRot();
      oldCamera = mc.options.getCameraType();
      mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
      looking = true;
    } else if (!active && looking) endLook();
    EFFECTS.tick(mc);
    if (!(mc.screen instanceof VisualsScreen)) HUD.tick(mc);
  }

  private static void endLook() {
    if (looking && oldCamera != null) Minecraft.getInstance().options.setCameraType(oldCamera);
    looking = false;
    oldCamera = null;
  }

  private static void key(InputEvent.Key e) {
    var mc = Minecraft.getInstance();
    if (e.getAction() != GLFW.GLFW_PRESS || mc.screen != null || mc.player == null) return;
    if (Feature.CHAT_COPY.enabled
        && e.getKey() == GLFW.GLFW_KEY_C
        && (e.getModifiers() & GLFW.GLFW_MOD_CONTROL) != 0) {
      mc.keyboardHandler.setClipboard(lastChat);
      notice("Последнее сообщение скопировано");
      return;
    }
    for (Feature f : Feature.values())
      if (f.key >= 0 && f.key == e.getKey()) {
        f.toggle();
        notice(f.title + (f.enabled ? " включён" : " выключен"));
      }
  }

  public static boolean reveal(Entity e) {
    var p = Minecraft.getInstance().player;
    return Feature.ANTI_INVIS.enabled
        && p != null
        && e instanceof AbstractClientPlayer
        && e != p
        && e.isInvisible();
  }

  public static boolean glow(Entity e) {
    var p = Minecraft.getInstance().player;
    if (p == null) return false;
    if (glowPlayer(e)) return true;
    if (reveal(e)&&Feature.ANTI_INVIS.value("outline")>0) return true;
    if (Feature.TARGET_GLOW.enabled && e == target && e.isAlive()) return true;
    if (!Feature.ITEM_GLOW.enabled || !(e instanceof ItemEntity)) return false;
    double r = Feature.ITEM_GLOW.value("distance");
    return p.distanceToSqr(e) <= r * r;
  }

  public static boolean glowPlayer(Entity e){
    var p=Minecraft.getInstance().player;
    if(!Feature.PLAYER_GLOW.enabled||p==null||!(e instanceof AbstractClientPlayer)||!e.isAlive()||e.isRemoved())return false;
    if(e==p&&Feature.PLAYER_GLOW.value("self")==0)return false;
    if(e.isInvisible()&&Feature.PLAYER_GLOW.value("invisible")==0)return false;
    if(e!=p&&Feature.PLAYER_GLOW.value("team")==0&&p.getTeam()!=null&&p.getTeam()==e.getTeam())return false;
    double distance=Feature.PLAYER_GLOW.value("distance");return p.distanceToSqr(e)<=distance*distance;
  }

  public static int glowColor(Entity e){
    if(!glowPlayer(e))return Config.accent()&0xffffff;
    int mode=(int)Feature.PLAYER_GLOW.value("glowMode");
    if(mode==1&&e instanceof LivingEntity living)return VisualColor.health(living.getHealth(),living.getMaxHealth())&0xffffff;
    if(mode==2&&e.getTeam()!=null){Integer color=e.getTeam().getColor().getColor();if(color!=null)return color&0xffffff;}
    return Feature.PLAYER_GLOW.effectColor()&0xffffff;
  }

  private static void fov(ViewportEvent.ComputeFov e) {
    if (!e.usedConfiguredFov()) return;
    var mc = Minecraft.getInstance();
    float wanted =
        Feature.ZOOM.enabled && ZOOM.isDown() && mc.screen == null
            ? (float) Feature.ZOOM.value("factor")
            : 1;
    zoom += (wanted - zoom) * (float) (1 - Math.exp(-Math.min(.05, BUDGET.frameMs() / 1000) * 15));
    e.setFOV(e.getFOV() / zoom);
  }

  private static void camera(ViewportEvent.ComputeCameraAngles e) {
    if (looking) {
      e.setYaw(lookYaw);
      e.setPitch(lookPitch);
    }
  }

  private static void hand(RenderHandEvent e) {
    if (!Feature.CUSTOM_HAND.enabled && !Feature.HAND_ANIMATION.enabled) return;
    Feature base = Feature.CUSTOM_HAND.enabled ? Feature.CUSTOM_HAND : Feature.HAND_ANIMATION;
    boolean main = e.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND;
    float sign = main ? 1 : -1;
    if (Feature.CUSTOM_HAND.enabled && Feature.CUSTOM_HAND.value("mirror") > 0) sign *= -1;
    boolean separate = Feature.CUSTOM_HAND.enabled && Feature.CUSTOM_HAND.value("offSeparate") > 0 && !main;
    double x = separate ? Feature.CUSTOM_HAND.value("offX") : (Feature.CUSTOM_HAND.enabled ? Feature.CUSTOM_HAND.value("x") : 0);
    double y = separate ? Feature.CUSTOM_HAND.value("offY") : (Feature.CUSTOM_HAND.enabled ? Feature.CUSTOM_HAND.value("y") : 0);
    double z = separate ? Feature.CUSTOM_HAND.value("offZ") : (Feature.CUSTOM_HAND.enabled ? Feature.CUSTOM_HAND.value("z") : 0);
    e.getPoseStack().translate(x * sign, y, z);
    float s = (float)(separate ? Feature.CUSTOM_HAND.value("offScale") : (Feature.CUSTOM_HAND.enabled ? Feature.CUSTOM_HAND.value("scale") : 1));
    e.getPoseStack().scale(s, s, s);
    if (Feature.CUSTOM_HAND.enabled) {
      float pitch = (float)(separate ? Feature.CUSTOM_HAND.value("offPitch") : Feature.CUSTOM_HAND.value("pitch"));
      float yaw = (float)(separate ? Feature.CUSTOM_HAND.value("offYaw") : Feature.CUSTOM_HAND.value("yaw"));
      float roll = (float)(separate ? Feature.CUSTOM_HAND.value("offRoll") : Feature.CUSTOM_HAND.value("rotation"));
      e.getPoseStack().mulPose(Axis.XP.rotationDegrees(pitch));
      e.getPoseStack().mulPose(Axis.YP.rotationDegrees(yaw * sign));
      e.getPoseStack().mulPose(Axis.ZP.rotationDegrees(roll * sign));
    }
    if (Feature.NO_LIQUID_BOB.enabled && Minecraft.getInstance().player != null && Minecraft.getInstance().player.isUnderWater())
      e.getPoseStack().translate(0, -.018f * (float) Feature.NO_LIQUID_BOB.value("strength"), 0);
    if (Feature.HAND_ANIMATION.enabled || Feature.CUSTOM_HAND.enabled) {
      Feature f = Feature.HAND_ANIMATION.enabled ? Feature.HAND_ANIMATION : Feature.CUSTOM_HAND;
      float speed = (float) f.value("speed");
      float rawSwing = Math.clamp(e.getSwingProgress(), 0, 1);
      // Speed changes the curve rather than the game timer, so animation stays
      // synchronized with hits while still feeling faster or more deliberate.
      float swing = (float)Math.pow(rawSwing, 1f / Math.max(.2f, speed));
      float eased = Mth.sin(swing * swing * Mth.PI);
      float pulse = Mth.sin(Mth.sqrt(swing) * Mth.PI);
      float wave = Mth.sin((Client.ticks + e.getPartialTick()) * .18f * speed);
      float power = (float) f.value("swing");
      float sway = (float) f.value("sway");
      int style = (int) f.value("handStyle");
      float handed = sign > 0 ? 1 : -1;
      float sx = (float)f.value("swingX") * power;
      float sy = (float)f.value("swingY") * power;
      float sz = (float)f.value("swingZ") * power;
      boolean using = Minecraft.getInstance().player != null && Minecraft.getInstance().player.isUsingItem();
      if (!(using && f.value("whileUsing") == 0) && style != 3) {
        switch (style) {
          case 1 -> { // плавная дуга
            e.getPoseStack().mulPose(Axis.XP.rotationDegrees(-eased * sx * .52f));
            e.getPoseStack().mulPose(Axis.YP.rotationDegrees(handed * eased * sy * .38f));
            e.getPoseStack().mulPose(Axis.ZP.rotationDegrees(handed * eased * sz * .32f));
          }
          case 2 -> { // классика
            e.getPoseStack().mulPose(Axis.XP.rotationDegrees(-eased * sx));
            e.getPoseStack().mulPose(Axis.ZP.rotationDegrees(handed * eased * sz * .42f));
          }
          case 4 -> { // вращение
            e.getPoseStack().mulPose(Axis.YP.rotationDegrees(handed * eased * sy));
            e.getPoseStack().mulPose(Axis.ZP.rotationDegrees(handed * (eased * sz + wave * 8 * sway)));
          }
          case 5 -> { // короткий укол
            e.getPoseStack().translate(0, 0, -eased * (float)f.value("travel") * .18f);
            e.getPoseStack().mulPose(Axis.XP.rotationDegrees(-eased * sx * .68f));
            e.getPoseStack().mulPose(Axis.YP.rotationDegrees(handed * eased * sy * .2f));
          }
          case 6 -> { // своя траектория: независимые оси и лёгкая волна
            e.getPoseStack().translate(handed * wave * .012f * sway, -eased * .018f, -eased * (float)f.value("travel") * .1f);
            e.getPoseStack().mulPose(Axis.XP.rotationDegrees(-eased * sx));
            e.getPoseStack().mulPose(Axis.YP.rotationDegrees(handed * eased * sy));
            e.getPoseStack().mulPose(Axis.ZP.rotationDegrees(handed * eased * sz + wave * 4 * sway));
          }
          default -> {
            // Pulse-style diagonal slash: a short forward reach followed by a
            // soft return.  Applying translation before rotation avoids the
            // abrupt wrist snap of the previous implementation.
            e.getPoseStack().translate(handed*pulse*.025f*power,-pulse*.018f,-pulse*(float)f.value("travel")*.12f);
            e.getPoseStack().mulPose(Axis.XP.rotationDegrees(-eased * sx * .58f));
            e.getPoseStack().mulPose(Axis.YP.rotationDegrees(handed * eased * sy * .54f));
            e.getPoseStack().mulPose(Axis.ZP.rotationDegrees(handed * (eased*sz*.34f+wave*3*sway)));
          }
        }
        e.getPoseStack().translate(0, wave * .012f * sway, Math.abs(wave) * .008f * sway);
      }
    }
  }

  private static void overlay(RenderBlockScreenEffectEvent e) {
    if (Feature.LOW_FIRE.enabled && e.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE) {
      e.getPoseStack().translate(0, Feature.LOW_FIRE.value("height") * .55f, 0);
    }
    if (Feature.CLEAR_OVERLAYS.enabled&&Feature.CLEAR_OVERLAYS.value(e.getOverlayType().name().toLowerCase(java.util.Locale.ROOT))>0) e.setCanceled(true);
  }

  private static void fog(ViewportEvent.RenderFog e) {
    if (!Feature.FOG.enabled || e.getType() != net.minecraft.world.level.material.FogType.NONE)
      return;
    float d = (float) Feature.FOG.value("distance");
    e.setNearPlaneDistance(d * (float)Feature.FOG.value("start"));
    e.setFarPlaneDistance(d);
  }

  private static void fogColor(ViewportEvent.ComputeFogColor e) {
    if(Feature.WATER_COLOR.enabled&&Feature.WATER_COLOR.value("underwater")>0&&e.getCamera().getFluidInCamera()==net.minecraft.world.level.material.FogType.WATER){
      int c=Feature.WATER_COLOR.effectColor();float s=(float)Feature.WATER_COLOR.value("strength");
      e.setRed(e.getRed()*(1-s)+((c>>16)&255)/255f*s);e.setGreen(e.getGreen()*(1-s)+((c>>8)&255)/255f*s);e.setBlue(e.getBlue()*(1-s)+(c&255)/255f*s);return;
    }
    if (!Feature.FOG.enabled
        || e.getCamera().getFluidInCamera() != net.minecraft.world.level.material.FogType.NONE)
      return;
    int c = Feature.FOG.effectColor();float strength=(float)Feature.FOG.value("strength");
    e.setRed(e.getRed()*(1-strength)+((c >> 16) & 255)/255f*strength);
    e.setGreen(e.getGreen()*(1-strength)+((c >> 8) & 255)/255f*strength);
    e.setBlue(e.getBlue()*(1-strength)+(c & 255)/255f*strength);
  }

  private static void attack(AttackEntityEvent e) {
    if (e.getEntity() != Minecraft.getInstance().player
        || !(e.getTarget() instanceof LivingEntity living)) return;
    target = living;
    targetTick = ticks;
    previousTargetHealth = living.getHealth();
    hitMarkerUntil = System.nanoTime() + 350_000_000L;
    clickTimes.addLast(System.nanoTime());
    trimClicks();
    EFFECTS.attack(living);
  }

  private static void trimClicks() {
    long cutoff = System.nanoTime() - 5_000_000_000L;
    while (!clickTimes.isEmpty() && clickTimes.peekFirst() < cutoff) clickTimes.removeFirst();
  }

  public static int cps() {
    trimClicks();
    long cutoff = System.nanoTime() - (long) (Feature.CPS.value("window") * 1_000_000_000L);
    int count = 0;
    for (Long time : clickTimes) if (time >= cutoff) count++;
    return Math.min((int) Feature.CPS.value("limit"), count);
  }

  private static void nameTag(RenderNameTagEvent.CanRender e) {
    var entity = e.getEntity();
    var player = Minecraft.getInstance().player;
    if (player == null || entity == player) return;
    double distance = player.distanceToSqr(entity);
    if (entity instanceof net.minecraft.client.player.AbstractClientPlayer other
        && (Feature.NAME_TAGS.enabled || Feature.NAME_HEALTH.enabled || Feature.NAME_DISTANCE.enabled)
        && distance <= Math.pow(Math.max(Feature.NAME_TAGS.value("distance"),
            Math.max(Feature.NAME_HEALTH.value("distance"), Feature.NAME_DISTANCE.value("distance"))), 2)) {
      StringBuilder text = new StringBuilder(other.getName().getString());
      if (Feature.NAME_HEALTH.enabled) text.append("  ").append(String.format(java.util.Locale.ROOT, "%.1f HP", other.getHealth()));
      if (Feature.NAME_DISTANCE.enabled) text.append("  ").append(String.format(java.util.Locale.ROOT, "%.1f м", player.distanceTo(other)));
      e.setContent(Component.literal(text.toString()));
      e.setCanRender(TriState.TRUE);
    } else if (entity instanceof ItemEntity item
        && (Feature.ITEM_NAMES.enabled || Feature.ITEM_STACKS.enabled)
        && player.distanceToSqr(item) <= Math.pow(Math.max(Feature.ITEM_NAMES.value("distance"), Feature.ITEM_STACKS.value("distance")), 2)) {
      StringBuilder text = new StringBuilder();
      if (Feature.ITEM_NAMES.enabled) text.append(item.getItem().getHoverName().getString());
      if (Feature.ITEM_STACKS.enabled) {
        if (text.length() > 0) text.append("  ");
        text.append("×").append(item.getItem().getCount());
      }
      e.setContent(Component.literal(text.toString()));
      e.setCanRender(TriState.TRUE);
    }
  }

  private static void gui(RenderGuiEvent.Post e) {
    var mc = Minecraft.getInstance();
    BUDGET.frame(System.nanoTime(), mc.level != null && !mc.isPaused());
    if (!mc.options.hideGui && !(mc.screen instanceof HudEditorScreen) && !(mc.screen instanceof VisualsScreen) && !(mc.screen instanceof PanelScreen))
      HUD.render(e.getGuiGraphics(), false);
  }

  private static void layer(RenderGuiLayerEvent.Pre e) {
    if (!Feature.STREAMER.enabled) return;
    String p = e.getName().getPath();
    if (p.equals("chat") || p.equals("tab_list") || p.equals("scoreboard_sidebar"))
      e.setCanceled(true);
  }

  private static void chat(ClientChatReceivedEvent e) {
    if (Feature.CHAT_COPY.enabled) lastChat = e.getMessage().getString();
    if(Feature.CHAT_HELPER.enabled){String text=e.getMessage().getString();Integer before=recentChat.put(text,ticks);if(before!=null&&ticks-before<Feature.CHAT_HELPER.value("seconds")*20)e.setCanceled(true);while(recentChat.size()>128)recentChat.remove(recentChat.keySet().iterator().next());}
  }

  public static void notice(String text) {
    HUD.notice(text);
  }
}
