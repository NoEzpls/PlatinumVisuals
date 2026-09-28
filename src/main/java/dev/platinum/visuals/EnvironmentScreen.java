package dev.platinum.visuals;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** One discoverable place for sky, fog, clouds, water and visual time. */
public final class EnvironmentScreen extends PanelScreen {
  private static final Feature[] TABS={Feature.SKY,Feature.FOG,Feature.CLOUDS,Feature.WATER_COLOR,Feature.WORLD_TIME};
  private static final String[] NAMES={"Небо","Туман","Облака","Вода","Время"};
  private Feature selected;private Feature.Setting dragging;
  public EnvironmentScreen(Screen previous,Feature selected){super(previous,"Небо и мир",490,345);this.selected=selected.isEnvironment()?selected:Feature.SKY;}
  protected void draw(GuiGraphics g,double mx,double my){
    for(int i=0;i<TABS.length;i++){boolean active=TABS[i]==selected;Ui.button(g,NAMES[i],16+i*94,47,88,active,false);}
    Ui.text(g,selected.title,18,91,Ui.TEXT,1.08f);Ui.small(g,selected.enabled?"Включено":"Выключено — включи переключателем →",18,109,selected.enabled?Config.accent():Ui.MUTED);Ui.toggle(g,443,91,selected.enabled);
    if(selected.hasColorPicker()){Ui.round(g,303,89,21,21,selected.effectColor());Ui.button(g,"Выбрать цвет",333,89,100,false,false);}
    for(int i=0;i<selected.settings.size();i++){var s=selected.settings.get(i);int x=18+(i%2)*237,y=146+(i/2)*36;Ui.small(g,s.label,x,y,Ui.TEXT);if(s.min==0&&s.max==1&&s.step==1){Ui.toggle(g,x+187,y+12,s.value>0);continue;}Ui.small(g,s.text(),x+212-Ui.width(s.text())*.8f,y+16,Config.accent());float f=(float)((s.value-s.min)/(s.max-s.min));Ui.round(g,x,y+21,142,3,0xff393140);Ui.round(g,x,y+21,142*f,3,Config.accent());Ui.round(g,x-3+142*f,y+18,7,7,Ui.TEXT);}
    String note=selected==Feature.SKY?"Цвет и звёзды настраиваются здесь. Для ночи открой «Время».":selected==Feature.FOG?"Туман окрашивает горизонт; небо имеет отдельный цвет.":selected==Feature.WATER_COLOR?"Поверхность воды обновится после пересборки чанков.":selected==Feature.CLOUDS?"Параметры действуют, пока Clouds включён.":"Это время для отображения. Серверные часы не меняются.";
    Ui.small(g,Ui.trim(note,554),18,309,Ui.MUTED);Ui.small(g,"Внешний шейдер-пак может рисовать своё небо, облака и воду.",18,326,Ui.MUTED);
  }
  protected void click(double x,double y,int button){
    dragging=null;for(int i=0;i<TABS.length;i++)if(Ui.inside(x,y,16+i*94,47,88,22)){selected=TABS[i];return;}
    if(Ui.inside(x,y,439,85,35,30)){selected.toggle();return;}
    if(selected.hasColorPicker()&&Ui.inside(x,y,299,84,137,32)){minecraft.setScreen(new EffectColorScreen(this,selected));return;}
    for(int i=0;i<selected.settings.size();i++){var s=selected.settings.get(i);int sx=18+(i%2)*237,sy=146+(i/2)*36;if(Ui.inside(x,y,sx-3,sy-2,221,34)){if(s.min==0&&s.max==1&&s.step==1){s.set(s.value>0?0:1);Config.dirty();}else{dragging=s;slide(x,sx);}return;}}
  }
  private void slide(double x,int left){double f=Math.clamp((x-left)/142,0,1);dragging.set(dragging.min+f*(dragging.max-dragging.min));if(dragging.id.equals("hue"))Config.effectColors.remove(selected.name());Config.dirty();}
  protected void drag(double x,double y){if(dragging!=null){int index=selected.settings.indexOf(dragging);slide(x,18+(index%2)*237);}}
  protected void released(){dragging=null;}
}
