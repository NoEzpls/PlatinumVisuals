package dev.platinum.visuals;

/** Hysteretic, bounded visual quality controller; it never alters world simulation. */
public final class Budget {
  private double smoothedMs = 16.7, quality = 1;
  private long lastFrame;
  private int frameCount, newParticles, admitted;
  private final float[] history=new float[64];private int historyIndex;private long lastSample;

  public void frame(long now, boolean active) {
    if (!active || lastFrame == 0) {
      lastFrame = now;
      return;
    }
    double ms = (now - lastFrame) / 1_000_000.0;
    lastFrame = now;
    if (ms <= 0 || ms > 250) return;
    smoothedMs += (ms - smoothedMs) * .04;
    if(now-lastSample>75_000_000L){history[historyIndex]=(float)smoothedMs;historyIndex=(historyIndex+1)%history.length;lastSample=now;}
    if (++frameCount % 30 != 0) return;
    double target = 1000 / Feature.OPTIMIZATION.value("fps");
    if (smoothedMs > target * 1.18) quality = Math.max(.25, quality - .08);
    else if (smoothedMs < target * .88) quality = Math.min(1, quality + .025);
  }

  public double quality() {
    return Feature.OPTIMIZATION.enabled && Feature.OPTIMIZATION.value("adaptive") > 0 ? quality : 1;
  }

  public int capacity() {
    return Math.max(
        8,
        (int)
            ((Feature.OPTIMIZATION.enabled ? Feature.OPTIMIZATION.value("budget") : 512)
                * quality()));
  }

  public int count(int n) {
    return Math.max(1, (int) (n * quality()));
  }

  public double distanceSquared() {
    double d = Feature.OPTIMIZATION.enabled ? Feature.OPTIMIZATION.value("distance") : 128;
    return d * d;
  }

  public void tick() {
    admitted = newParticles;
    newParticles = 0;
  }

  public boolean acceptParticle() {
    if (!Feature.OPTIMIZATION.enabled) return true;
    int max = Math.max(16, (int) (Feature.OPTIMIZATION.value("vanilla") * quality()));
    if (newParticles >= max) return false;
    newParticles++;
    return true;
  }

  public double frameMs() {
    return smoothedMs;
  }
  public float sample(int index){return history[Math.floorMod(historyIndex+index,history.length)];}

  public void reset() {
    lastFrame = 0;
    quality = 1;
    smoothedMs = 16.7;
    frameCount = newParticles = admitted = 0;
    java.util.Arrays.fill(history,0);historyIndex=0;lastSample=0;
  }
}
