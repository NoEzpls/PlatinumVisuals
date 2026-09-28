package dev.platinum.visuals;

/** Frame-rate-independent state shared by the vanilla HUD mixin. */
public final class InterfaceAnimation {
  private static final Motion.Clock TAB_CLOCK = new Motion.Clock(), HOTBAR_CLOCK = new Motion.Clock();
  private static float tab, hotbarOffset;
  private static int selected = -1;
  private static long lastTabFrame;

  private InterfaceAnimation() {}

  public static float tabOpen() {
    long now=System.nanoTime();
    if(now-lastTabFrame>150_000_000L)tab=0;
    lastTabFrame=now;
    tab = Motion.approach(tab, 1, Feature.ANIMATION.value("tabSpeed"), TAB_CLOCK.step());
    return Config.reduceMotion ? 1 : tab;
  }

  public static float hotbar(int slot) {
    if (selected < 0) selected = slot;
    if (slot != selected) {
      int delta = slot - selected;
      if (Math.abs(delta) > 4) delta -= Integer.signum(delta) * 9;
      hotbarOffset += delta * (3.5f + (float)Feature.ANIMATION.value("bounce") * 7);
      selected = slot;
    }
    hotbarOffset = Motion.approach(hotbarOffset, 0, Feature.ANIMATION.value("hotbarSpeed"), HOTBAR_CLOCK.step());
    return Config.reduceMotion ? 0 : hotbarOffset;
  }
}
