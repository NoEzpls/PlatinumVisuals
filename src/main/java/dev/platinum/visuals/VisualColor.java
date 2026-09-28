package dev.platinum.visuals;

/** Pure colour math shared by the renderer and its headless regression checks. */
public final class VisualColor {
  public static int tint(int original,int target,double strength){
    double f=Double.isFinite(strength)?Math.clamp(strength,0,1):0;
    int out=original&0xff000000;
    for(int shift=0;shift<24;shift+=8)out|=(int)Math.round(((original>>>shift)&255)*(1-f)+((target>>>shift)&255)*f)<<shift;
    return out;
  }
  public static int brightness(int color,double brightness){
    double factor=Double.isFinite(brightness)?Math.clamp(brightness,0,2):1;
    int out=color&0xff000000;
    for(int shift=0;shift<24;shift+=8)out|=Math.clamp((int)Math.round(((color>>>shift)&255)*factor),0,255)<<shift;
    return out;
  }
  public static int health(float health,float maxHealth){
    float ratio=Float.isFinite(health)&&Float.isFinite(maxHealth)&&maxHealth>0?Math.clamp(health/maxHealth,0,1):0;
    return ThemeColor.rgb(ratio/3,.75f,1);
  }
  private VisualColor(){}
}
