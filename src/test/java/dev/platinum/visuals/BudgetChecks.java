package dev.platinum.visuals;

/** Behavioral regression checks for adaptive budgets, pause handling and malformed settings. */
public final class BudgetChecks {
  public static void main(String[] args) {
    Budget b = new Budget();
    long now = 1;
    b.frame(now, true);
    for (int i = 0; i < 600; i++) b.frame(now += 40_000_000L, true);
    require(
        b.quality() >= .25 && b.quality() < .4, "slow frames must reduce quality within the floor");
    int cap = b.capacity();
    require(
        cap >= 8 && cap <= Feature.OPTIMIZATION.value("budget"),
        "particle capacity must stay bounded");
    double beforePause = b.quality();
    b.frame(now += 5_000_000_000L, true);
    require(b.quality() == beforePause, "a pause must not reduce quality");
    for (int i = 0; i < 1800; i++) b.frame(now += 2_000_000L, true);
    require(b.quality() > .95 && b.quality() <= 1, "quality must recover when frame time improves");
    b.tick();
    int admitted = 0;
    for (int i = 0; i < 10000; i++) if (b.acceptParticle()) admitted++;
    require(
        admitted <= Feature.OPTIMIZATION.value("vanilla"),
        "particle flood must be bounded per tick");
    b.tick();
    require(b.acceptParticle(), "new ticks must replenish the particle budget");
    Feature.OPTIMIZATION.enabled = false;
    require(
        b.capacity() == 512 && b.quality() == 1,
        "disabled adaptive optimization must retain a safe own-effect cap");
    Feature.OPTIMIZATION.enabled = true;
    var setting = Feature.ZOOM.settings.getFirst();
    double previous = setting.value;
    setting.set(Double.NaN);
    require(setting.value == previous, "NaN config values must not poison rendering");
    setting.set(1e30);
    require(setting.value == setting.max, "out-of-range settings must clamp");
    setting.set(previous);
    b.reset();
    require(b.quality() == 1, "dimension changes must reset adaptive state");
    System.out.println("BudgetChecks: all behavioral checks passed");
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
}
