package dev.platinum.visuals;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
public final class AtmosphereScreen extends PanelScreen {
  private String status="Выбери пресет, затем настрой отдельные эффекты в меню.";
  public AtmosphereScreen(Screen previous){super(previous,"Атмосфера мира",450,306);}
  protected void draw(GuiGraphics g,double x,double y){
    Ui.small(g,"Лёгкие образы без шейдеров. Цвет эффектов = цвет интерфейса.",18,49,Ui.MUTED);
    for(StylePreset p:StylePreset.values()){int yy=74+p.ordinal()*58;boolean hover=Ui.inside(x,y,18,yy,414,49);Ui.round(g,18,yy,414,49,hover?0xff302b39:Ui.CARD);Ui.round(g,28,yy+11,3,27,Config.accent());Ui.text(g,p.title,42,yy+11,Ui.TEXT);Ui.small(g,p.description,42,yy+29,Ui.MUTED);Ui.icon(g,"arrow",405,yy+17,13,Config.accent());}
    Ui.button(g,"Настроить небо и мир",18,251,200,true,false);Ui.button(g,"Цвет интерфейса",230,251,202,false,false);
    Ui.small(g,Ui.trim(status,509),18,286,Ui.MUTED);
  }
  protected void click(double x,double y,int button){for(StylePreset p:StylePreset.values())if(Ui.inside(x,y,18,74+p.ordinal()*58,414,49)){p.apply();status="Применён пресет: "+p.title;}if(Ui.inside(x,y,18,251,200,22))minecraft.setScreen(new EnvironmentScreen(this,Feature.SKY));else if(Ui.inside(x,y,230,251,202,22))minecraft.setScreen(new ThemeScreen(this));}
}
