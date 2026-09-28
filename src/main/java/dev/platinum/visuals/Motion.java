package dev.platinum.visuals;

/** Exponential response in seconds: same duration at 30, 60 and 240 FPS. */
public final class Motion {
  private Motion() {}
  public static float approach(float current, float target, double rate, double seconds) {
    if (!Double.isFinite(seconds) || seconds <= 0) return current;
    float result = (float) (target + (current - target) * Math.exp(-rate * Math.min(seconds, .25)));
    return Math.abs(result - target) < .0001 ? target : result;
  }
  public static final class Clock {
    private long previous;
    public double step() {
      long now = System.nanoTime(), old = previous;
      previous = now;
      return old == 0 ? 0 : Math.clamp((now - old) / 1e9, 0, .1);
    }
  }
}
