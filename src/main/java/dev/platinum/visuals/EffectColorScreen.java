package dev.platinum.visuals;

import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

/** Full colour picker for one effect; never changes the interface's accent. */
public final class EffectColorScreen extends PanelScreen {
  private final Feature feature;
  private final TextInput hex=new TextInput("",7);
  private float hue,saturation,value;private int dragging;
  private String status="Выбери оттенок или введи HEX.";
  public EffectColorScreen(Screen previous,Feature feature){super(previous,"Цвет · "+feature.title,400,296);this.feature=feature;readColor();}
  private void readColor(){float[] hsv=ThemeColor.hsv(feature.effectColor());hue=hsv[0];saturation=hsv[1];value=hsv[2];hex.set(String.format(Locale.ROOT,"#%06X",feature.effectColor()&0xffffff));}
  private void commit(){Config.effectColors.put(feature.name(),ThemeColor.rgb(hue,saturation,value)&0xffffff);Config.dirty();hex.set(String.format(Locale.ROOT,"#%06X",feature.effectColor()&0xffffff));status="Свой цвет сохранён для этого эффекта.";}
  protected void draw(GuiGraphics g,double mx,double my){
    Ui.small(g,Config.effectColors.containsKey(feature.name())?"Свой цвет эффекта":"Цвет темы / оттенок модуля",18,48,Ui.MUTED);
    for(int i=0;i<64;i++)g.fillGradient(18+i*220/64,68,18+(i+1)*220/64,214,ThemeColor.rgb(hue,i/63f,1),0xff000000);
    Ui.round(g,15+saturation*220,65+(1-value)*146,7,7,Ui.TEXT);Ui.round(g,17+saturation*220,67+(1-value)*146,3,3,0xff17131d);
    for(int i=0;i<64;i++)g.fill(18+i*220/64,225,18+(i+1)*220/64,237,ThemeColor.rgb(i/63f,1,1));Ui.round(g,16+hue*220,222,4,18,Ui.TEXT);
    Ui.round(g,258,68,124,53,feature.effectColor());hex.draw(g,258,136,124,"#B9A0FF");
    Ui.button(g,"Применить HEX",258,177,124,true,false);Ui.button(g,"Цвет темы",258,211,124,false,false);
    Ui.small(g,Ui.trim(status,449),18,264,Ui.MUTED);
  }
  protected void click(double x,double y,int button){dragging=0;hex.click(x,y,258,136,124);if(Ui.inside(x,y,18,68,220,146))dragging=1;else if(Ui.inside(x,y,18,220,220,24))dragging=2;else if(Ui.inside(x,y,258,177,124,22))applyHex();else if(Ui.inside(x,y,258,211,124,22)){Config.effectColors.remove(feature.name());for(var setting:feature.settings)if(setting.id.equals("hue"))setting.set(-1);Config.dirty();readColor();status="Эффект следует цвету интерфейса.";}drag(x,y);}
  protected void drag(double x,double y){if(dragging==1){saturation=(float)Math.clamp((x-18)/220,0,1);value=1-(float)Math.clamp((y-68)/146,0,1);commit();}else if(dragging==2){hue=(float)Math.clamp((x-18)/220,0,1);commit();}}
  protected void released(){dragging=0;}
  private void applyHex(){var color=ThemeColor.parse(hex.text);if(color.isEmpty()){status="Нужен HEX вида #B9A0FF.";return;}float[] hsv=ThemeColor.hsv(color.getAsInt());hue=hsv[0];saturation=hsv[1];value=hsv[2];commit();}
  public boolean charTyped(CharacterEvent e){return hex.character(e)||super.charTyped(e);}
  public boolean keyPressed(KeyEvent e){if(hex.focused&&e.key()==257)applyHex();return hex.key(e)||super.keyPressed(e);}
}
