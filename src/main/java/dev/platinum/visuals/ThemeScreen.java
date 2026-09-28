package dev.platinum.visuals;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
public final class ThemeScreen extends PanelScreen{
 private final TextInput hex=new TextInput("",7);private float hue,sat,val;private int sliding;
 public ThemeScreen(Screen previous){super(previous,"Оформление",430,318);readColor();}
 private void readColor(){float[] h=ThemeColor.hsv(Config.accent());hue=h[0];sat=h[1];val=h[2];hex.set(String.format(Locale.ROOT,"#%06X",Config.accent()&0xffffff));}
 private void commit(){Config.accent(ThemeColor.rgb(hue,sat,val));hex.set(String.format(Locale.ROOT,"#%06X",Config.accent()&0xffffff));}
 protected void draw(GuiGraphics g,double mx,double my){Ui.small(g,"Любой цвет · HSV или HEX",18,47,Ui.MUTED);
  for(int i=0;i<64;i++)g.fillGradient(18+i*184/64,65,18+(i+1)*184/64,203,ThemeColor.rgb(hue,i/63f,1),0xff000000);
  Ui.round(g,16+sat*184,63+(1-val)*138,6,6,Ui.TEXT);Ui.round(g,18+sat*184,65+(1-val)*138,2,2,0xff151515);
  for(int i=0;i<64;i++)g.fill(18+i*184/64,212,18+(i+1)*184/64,223,ThemeColor.rgb(i/63f,1,1));Ui.round(g,16+hue*184,210,4,15,Ui.TEXT);
  hex.draw(g,18,237,113,"#B9A0FF");Ui.button(g,"Применить",138,239,64,false,false);for(int i=0;i<4;i++)Ui.round(g,18+i*48,278,40,18,Config.color(i));
  Ui.small(g,"Предпросмотр",224,47,Ui.MUTED);Ui.round(g,220,65,190,57,Ui.CARD);Ui.icon(g,"logo",233,76,17,Config.accent());Ui.text(g,"Platinum Visuals",258,79,Ui.TEXT,.95f);Ui.small(g,"Target HUD",233,103,Ui.MUTED);Ui.toggle(g,373,98,true);
  slider(g,"Прозрачность HUD",Config.hudOpacity,.1f,1,137);slider(g,"Масштаб меню",Config.menuScale,.7f,1.3f,174);slider(g,"Длительность анимаций",Config.animationSeconds,.12f,.4f,211);
  Ui.small(g,"Мягкие тени",224,253,Ui.TEXT);Ui.toggle(g,378,248,Config.softShadows);Ui.small(g,"Меньше движения",224,282,Ui.TEXT);Ui.toggle(g,378,277,Config.reduceMotion);
 }
 private void slider(GuiGraphics g,String label,float v,float min,float max,int y){Ui.small(g,label,224,y,Ui.TEXT);float f=(v-min)/(max-min);Ui.round(g,224,y+19,176,3,0xff3c3548);Ui.round(g,224,y+19,176*f,3,Config.accent());Ui.round(g,221+176*f,y+16,8,8,Ui.TEXT);}
 protected void click(double x,double y,int b){sliding=0;hex.click(x,y,18,237,113);if(Ui.inside(x,y,18,65,184,138))sliding=1;else if(Ui.inside(x,y,18,206,184,23))sliding=2;else if(Ui.inside(x,y,138,239,64,22))applyHex();else if(Ui.inside(x,y,220,149,188,17))sliding=3;else if(Ui.inside(x,y,220,186,188,17))sliding=4;else if(Ui.inside(x,y,220,223,188,17))sliding=5;else if(Ui.inside(x,y,220,244,188,24)){Config.softShadows=!Config.softShadows;Config.dirty();}else if(Ui.inside(x,y,220,274,188,24)){Config.reduceMotion=!Config.reduceMotion;Config.dirty();}for(int i=0;i<4;i++)if(Ui.inside(x,y,18+i*48,278,40,18)){Config.theme=i;Config.customAccent=false;Config.dirty();readColor();}drag(x,y);}
 protected void drag(double x,double y){if(sliding==1){sat=(float)Math.clamp((x-18)/184,0,1);val=1-(float)Math.clamp((y-65)/138,0,1);commit();}else if(sliding==2){hue=(float)Math.clamp((x-18)/184,0,1);commit();}else if(sliding>=3){float f=(float)Math.clamp((x-224)/176,0,1);if(sliding==3)Config.hudOpacity=.1f+.9f*f;else if(sliding==4)Config.menuScale=.7f+.6f*f;else Config.animationSeconds=.12f+.28f*f;Config.dirty();}}
 protected void released(){sliding=0;}private void applyHex(){var rgb=ThemeColor.parse(hex.text);if(rgb.isPresent()){Config.accent(rgb.getAsInt());readColor();}}
 public boolean charTyped(CharacterEvent e){return hex.character(e)||super.charTyped(e);}public boolean keyPressed(KeyEvent e){if(hex.focused&&e.key()==257)applyHex();return hex.key(e)||super.keyPressed(e);}
}
