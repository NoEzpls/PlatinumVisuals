package dev.platinum.visuals;
import com.mojang.logging.LogUtils;
import java.util.Arrays;
/** Opt-in CPU submission timing, not GPU time or a game FPS benchmark. */
public final class UiProfile {
  private static final boolean ENABLED=Boolean.getBoolean("platinum.visuals.profile");
  private static final long[] SAMPLES=new long[600];
  private static int warmup,index;private static long labels,glyphs,hits;
  public static void frame(long nanos){
    if(!ENABLED)return;
    if(warmup++<120){labels=Ui.labelsBuilt;glyphs=CachedUiText.prepared;hits=CachedUiText.hits;return;}
    SAMPLES[index++]=nanos;
    if(index==SAMPLES.length){long[] sorted=SAMPLES.clone();Arrays.sort(sorted);double mean=Arrays.stream(sorted).average().orElse(0)/1e6;
      LogUtils.getLogger().info("Platinum GUI CPU submit: mean={} ms, p95={} ms, p99={} ms, new labels={}, glyph layouts={}, reused layouts={}",mean,sorted[569]/1e6,sorted[593]/1e6,Ui.labelsBuilt-labels,CachedUiText.prepared-glyphs,CachedUiText.hits-hits);
      index=0;labels=Ui.labelsBuilt;glyphs=CachedUiText.prepared;hits=CachedUiText.hits;
    }
  }
}
