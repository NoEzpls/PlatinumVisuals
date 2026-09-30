package dev.platinum.visuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;

public final class EnvironmentVisuals {
  private static boolean initialized,lastWaterEnabled;
  private static int lastWaterColor,pendingRefresh;
  private static double lastWaterStrength;

  /** The existing sky buffers are reused; no new meshes, shaders or per-frame textures. */
  public static void sky(SkyRenderState state){
    if(!Feature.SKY.enabled||state.skybox!=DimensionType.Skybox.OVERWORLD)return;
    int color=Feature.SKY.effectColor();double strength=Feature.SKY.value("strength");
    state.skyColor=VisualColor.brightness(VisualColor.tint(state.skyColor,color,strength),Feature.SKY.value("brightness"));
    if(Feature.SKY.value("sunset")==0)state.sunriseAndSunsetColor=0;
    else state.sunriseAndSunsetColor=VisualColor.tint(state.sunriseAndSunsetColor,color,strength*.5);
    float stars=Feature.SKY.value("dayStars")>0?Math.max(.5f,state.starBrightness):state.starBrightness;
    state.starBrightness=Math.clamp(stars*(float)Feature.SKY.value("stars"),0,1);
  }

  /** Water tint is baked into chunk geometry; delay a single rebuild until editing settles. */
  public static void tick(Minecraft mc){
    boolean enabled=Feature.WATER_COLOR.enabled;
    int color=enabled?Feature.WATER_COLOR.effectColor():0;
    double strength=enabled?Feature.WATER_COLOR.value("strength"):0;
    if(!initialized){initialized=true;lastWaterEnabled=enabled;lastWaterColor=color;lastWaterStrength=strength;return;}
    if(enabled!=lastWaterEnabled||color!=lastWaterColor||strength!=lastWaterStrength){
      lastWaterEnabled=enabled;lastWaterColor=color;lastWaterStrength=strength;pendingRefresh=10;
    }
    if(pendingRefresh>0&&--pendingRefresh==0&&mc.level!=null)mc.levelRenderer.allChanged();
  }
  public static void reset(){initialized=false;pendingRefresh=0;}
  public static int water(int original){return Feature.WATER_COLOR.enabled?VisualColor.tint(original,Feature.WATER_COLOR.effectColor(),Feature.WATER_COLOR.value("strength")):original;}
  public static int clouds(int original){
    if(!Feature.CLOUDS.enabled)return original;
    int rgb=VisualColor.tint(original,Feature.CLOUDS.effectColor(),Feature.CLOUDS.value("strength"))&0xffffff;
    int alpha=(int)Math.round((original>>>24)*Feature.CLOUDS.value("opacity"));
    return rgb|Math.clamp(alpha,0,255)<<24;
  }
  private EnvironmentVisuals(){}
}
