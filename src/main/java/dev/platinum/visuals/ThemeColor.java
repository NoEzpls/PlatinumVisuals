package dev.platinum.visuals;
import java.util.OptionalInt;
public final class ThemeColor {
 public static int rgb(float hue,float saturation,float value){
  float h=((hue%1)+1)%1*6,s=Math.clamp(saturation,0,1),v=Math.clamp(value,0,1),f=h-(int)h,p=v*(1-s),q=v*(1-f*s),t=v*(1-(1-f)*s),r,g,b;
  switch((int)h){case 0->{r=v;g=t;b=p;}case 1->{r=q;g=v;b=p;}case 2->{r=p;g=v;b=t;}case 3->{r=p;g=q;b=v;}case 4->{r=t;g=p;b=v;}default->{r=v;g=p;b=q;}}
  return 0xff000000|Math.round(r*255)<<16|Math.round(g*255)<<8|Math.round(b*255);
 }
 public static float[] hsv(int rgb){float r=((rgb>>16)&255)/255f,g=((rgb>>8)&255)/255f,b=(rgb&255)/255f,max=Math.max(r,Math.max(g,b)),min=Math.min(r,Math.min(g,b)),d=max-min;float h=d==0?0:max==r?(g-b)/d:max==g?2+(b-r)/d:4+(r-g)/d;return new float[]{((h/6)+1)%1,max==0?0:d/max,max};}
 public static OptionalInt parse(String text){String clean=text.strip().replaceFirst("^#","");return clean.matches("[0-9a-fA-F]{6}")?OptionalInt.of(0xff000000|Integer.parseInt(clean,16)):OptionalInt.empty();}
}
