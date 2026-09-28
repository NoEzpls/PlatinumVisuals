package dev.platinum.visuals;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/** Shared atlas sprites and bounded text caches. Only this mod uses the custom font. */
public final class Ui {
  public static final int TEXT = 0xffeeedf5, MUTED = 0xff8b879d, PANEL = 0xff15141c, CARD = 0xff1e1c27;
  private static final Style FONT = Style.EMPTY.withFont(new FontDescription.Resource(id("ui")));
  private record Label(FormattedCharSequence sequence, int width) {}
  private record TrimKey(String text, int width) {}
  private static <K,V> Map<K,V> lru(int capacity) {
    return new LinkedHashMap<>(capacity, .75f, true) {
      @Override protected boolean removeEldestEntry(Map.Entry<K,V> entry) { return size() > capacity; }
    };
  }
  private static final Map<String,Label> LABELS = lru(2048);
  private static final Map<TrimKey,String> TRIMS = lru(1024);
  private static final Map<String,Identifier> SPRITES = new HashMap<>();
  public static long labelsBuilt;
  private static float opacity = 1;
  private static int spriteDepth;
  public static boolean linearSprites(){return spriteDepth>0;}
  public static Identifier id(String path) { return Identifier.fromNamespaceAndPath("platinumvisuals", path); }
  public static void opacity(float value) { opacity = Math.clamp(value,0,1); }
  public static int menuBackdrop(){return alpha(0xff090810,Feature.MONITOR_BLUR.enabled?Feature.MONITOR_BLUR.value("dim"):.52);}
  /** Bounded multi-pass veil used while a Platinum panel is open.  It keeps the
   * menu readable and gives the background a soft monitor-like diffusion without
   * allocating a second framebuffer every frame. */
  public static void monitorBlur(GuiGraphics g,int width,int height){
    if(!Feature.MONITOR_BLUR.enabled)return;
    // Use Minecraft's real two-pass framebuffer blur.  The old implementation
    // merely painted translucent stripes and could never blur the world.
    Minecraft.getInstance().gameRenderer.processBlurEffect();
    int passes=Math.clamp((int)Feature.MONITOR_BLUR.value("radius")/3,1,6);
    float strength=(float)Feature.MONITOR_BLUR.value("strength");
    float stretch=(float)Feature.MONITOR_BLUR.value("stretch");
    for(int i=0;i<passes;i++){
      float edge=i/(float)passes;
      int a=(int)(255*strength*.018f*(1-edge));
      g.fill(0,0,width,height,(a<<24)|(Config.accent()&0xffffff));
    }
    int band=Math.max(0,(int)(Math.min(width,height)*.055f*stretch));
    if(band>0){
      int c=alpha(0xff08070d,.34f*stretch);
      g.fill(0,0,band,height,c);g.fill(width-band,0,width,height,c);
      g.fill(0,0,width,band/2,c);g.fill(0,height-band/2,width,height,c);
    }
  }
  public static void invalidateFonts() { LABELS.clear(); TRIMS.clear(); CachedUiText.invalidate(); }
  private static Label label(String text) {
    Label existing = LABELS.get(text);
    if (existing != null) return existing;
    FormattedCharSequence seq = new CachedUiText(Component.literal(text).setStyle(FONT).getVisualOrderText());
    Label result = new Label(seq, Minecraft.getInstance().font.width(seq));
    LABELS.put(text,result); labelsBuilt++;
    return result;
  }
  public static int width(String text) { return label(text).width; }
  public static int alpha(int c, double a) { return ((int)(((c >>> 24) & 255)*Math.clamp(a,0,1)) << 24) | (c & 0xffffff); }
  public static int blend(int a, int b, float f) {
    f = Math.clamp(f,0,1); int out=0;
    for(int s=0;s<=24;s+=8) out |= (int)(((a>>>s)&255)*(1-f)+((b>>>s)&255)*f)<<s;
    return out;
  }
  public static void round(GuiGraphics g, float x, float y, float w, float h, int c) {
    if (w <= 0 || h <= 0) return;
    sprite(g,Math.min(w,h) < 18 ? "pill" : "round",x,y,w,h,c);
  }
  private static void sprite(GuiGraphics g,String name,float x,float y,float w,float h,int color) {
    if (((color>>>24)&255)*opacity < 1) return;
    Identifier sprite=SPRITES.computeIfAbsent(name,k->id("ui/"+k));
    g.pose().pushMatrix();g.pose().translate(x,y);
    // Fractional position and extent keep slow motion from snapping to GUI pixels.
    float resolution=name.equals("pill")?16/Math.min(w,h):1;
    int iw=Math.max(1,(int)Math.ceil(w*resolution)),ih=Math.max(1,(int)Math.ceil(h*resolution));
    g.pose().scale(w/iw,h/ih);
    spriteDepth++;
    try { g.blitSprite(RenderPipelines.GUI_TEXTURED,sprite,0,0,iw,ih,alpha(color,opacity)); }
    finally { spriteDepth--; }
    g.pose().popMatrix();
  }
  public static void shadow(GuiGraphics g,float x,float y,float w,float h) {
    if(Config.softShadows) sprite(g,"shadow",x-16,y-14,w+32,h+32,0x90000000);
  }
  public static void icon(GuiGraphics g,String name,float x,float y,float size,int color) { sprite(g,name,x,y,size,size,color); }
  public static void text(GuiGraphics g,String s,float x,float y,int color) { text(g,s,x,y,color,1); }
  public static void text(GuiGraphics g,String s,float x,float y,int color,float size) {
    if (s.isEmpty() || ((color>>>24)&255)*opacity < 1) return;
    g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(size,size);
    g.drawString(Minecraft.getInstance().font,label(s).sequence,0,0,alpha(color,opacity),false);
    g.pose().popMatrix();
  }
  public static void small(GuiGraphics g,String s,float x,float y,int color) { text(g,s,x,y,color,.85f); }
  public static void center(GuiGraphics g,String s,float x,float y,int color,float size) { text(g,s,x-width(s)*size/2,y,color,size); }
  public static void button(GuiGraphics g,String s,int x,int y,int w,boolean on,boolean hover) {
    round(g,x,y,w,22,on?alpha(Config.accent(),.16):hover?0xff2b2737:CARD);
    center(g,s,x+w/2f,y+6,on?Config.accent():TEXT,.9f);
  }
  public static void toggle(GuiGraphics g,float x,float y,boolean on) { toggle(g,x,y,on?1:0); }
  public static void toggle(GuiGraphics g,float x,float y,float progress) {
    round(g,x,y,24,13,blend(0xff393441,Config.accent(),progress));
    round(g,x+2+11*progress,y+2,9,9,blend(0xffa29bb1,0xfffaf7ff,progress));
  }
  public static boolean inside(double mx,double my,double x,double y,double w,double h) { return mx>=x && my>=y && mx<x+w && my<y+h; }
  public static String trim(String s,int w) {
    if (width(s)<=w) return s;
    TrimKey key=new TrimKey(s,w); String cached=TRIMS.get(key); if(cached!=null)return cached;
    int end=s.length();
    while(end>0 && width(s.substring(0,end)+"…")>w) end=s.offsetByCodePoints(end,-1);
    String result=s.substring(0,end)+"…";TRIMS.put(key,result);return result;
  }
  public static void itemDecorations(GuiGraphics g,ItemStack s,int x,int y) {
    if(s.isEmpty())return;
    if (Feature.ENCHANT_GLINT.enabled && s.isEnchanted()) {
      g.fill(x + 2, y + 1, x + 15, y + 2, Feature.ENCHANT_GLINT.effectColor());
    }
    if(s.getCount()>1){String count=Integer.toString(s.getCount());text(g,count,x+17-width(count)*.8f,y+9,TEXT,.8f);}
    if(s.isBarVisible()){
      g.fill(x+2,y+13,x+15,y+15,0xff09090b);
      g.fill(x+2,y+13,x+2+s.getBarWidth(),y+14,0xff000000|s.getBarColor());
    }
  }
}
