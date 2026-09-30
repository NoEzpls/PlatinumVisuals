package dev.platinum.visuals;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
public final class PerformanceScreen extends PanelScreen {
  private int chunks=8;private String status="Ничего не меняется до нажатия кнопки.";
  public PerformanceScreen(Screen previous){super(previous,"Производительность",440,286);}
  protected void draw(GuiGraphics g,double x,double y){
    Ui.text(g,"Профиль для плавной игры",18,50,Ui.TEXT);
    Ui.small(g,"Цель — "+(int)Feature.OPTIMIZATION.value("fps")+" FPS. Это не гарантия на любом ПК.",18,69,Ui.MUTED);
    Ui.small(g,"Облака, тени сущностей и AO: выкл. Частицы: меньше.",18,88,Ui.MUTED);
    Ui.small(g,"Листва: простая. Смешивание биомов: 0. VSync: выкл.",18,104,Ui.MUTED);
    Ui.small(g,"Твои визуалы остаются; их бюджет регулируется отдельно.",18,120,Ui.MUTED);
    Ui.round(g,18,144,404,39,Ui.CARD);Ui.text(g,"Прорисовка мира",29,158,Ui.TEXT,.95f);
    Ui.button(g,"−",304,152,28,false,false);Ui.center(g,Integer.toString(chunks),351,159,Config.accent(),1);Ui.button(g,"+",371,152,28,false,false);
    Ui.button(g,"Применить профиль",18,200,195,true,false);Ui.button(g,"Вернуть мою графику",226,200,196,false,false);
    Ui.small(g,Ui.trim(status,495),18,237,Ui.MUTED);Ui.small(g,"Первое применение сохраняет исходные настройки на диск.",18,256,Ui.MUTED);
  }
  protected void click(double x,double y,int button){if(Ui.inside(x,y,304,152,28,22))chunks=Math.max(4,chunks-1);else if(Ui.inside(x,y,371,152,28,22))chunks=Math.min(16,chunks+1);else if(Ui.inside(x,y,18,200,195,22))status=GraphicsProfile.apply(chunks);else if(Ui.inside(x,y,226,200,196,22))status=GraphicsProfile.restore();}
}
