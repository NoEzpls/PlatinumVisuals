package dev.platinum.visuals;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.item.ItemStack;
public final class TimerScreen extends PanelScreen {
  private final TextInput seconds=new TextInput("15",4);
  private ItemStack chosen=ItemStack.EMPTY;
  private boolean matchName=true;private int page;
  private String status="Возьми нужный предмет в основную руку.";
  public TimerScreen(Screen previous){super(previous,"Свои таймеры",440,330);}
  private List<TimerRule> visible(){String server=Cooldowns.server();return Config.timerRules.stream().filter(r->r.server().isEmpty()||r.server().equals(server)).toList();}
  protected void draw(GuiGraphics g,double x,double y){
    Ui.small(g,"Серверные откаты без пакета нельзя измерить автоматически.",18,48,Ui.MUTED);
    Ui.small(g,"Здесь — твои оценки с отметкой ≈, отдельно для этого сервера.",18,64,Ui.MUTED);
    Ui.button(g,"Взять из руки",18,88,121,false,false);Ui.text(g,chosen.isEmpty()?"Предмет не выбран":Ui.trim(chosen.getHoverName().getString(),268),150,95,Ui.TEXT,.9f);
    seconds.draw(g,18,123,72,"Секунды");Ui.small(g,"секунд",99,132,Ui.MUTED);
    Ui.small(g,"Учитывать название",190,132,Ui.TEXT);Ui.toggle(g,390,126,matchName);
    Ui.button(g,"Добавить / обновить таймер",18,162,404,true,false);
    List<TimerRule> data=visible();page=Math.clamp(page,0,Math.max(0,(data.size()-1)/4));
    for(int i=0;i<4&&page*4+i<data.size();i++){TimerRule rule=data.get(page*4+i);int yy=196+i*21;Ui.round(g,18,yy,404,19,Ui.CARD);Ui.small(g,Ui.trim(rule.name()+" · "+rule.seconds()+" с"+(rule.matchName()?"":" · любой вариант"),443),25,yy+5,Ui.TEXT);Ui.icon(g,"close",402,yy+3,12,Ui.MUTED);}
    if(data.isEmpty())Ui.small(g,"Пока нет своих таймеров",25,210,Ui.MUTED);
    Ui.button(g,"‹",18,286,27,false,false);Ui.small(g,(page+1)+" / "+Math.max(1,(data.size()+3)/4),53,293,Ui.MUTED);Ui.button(g,"›",103,286,27,false,false);Ui.small(g,Ui.trim(status,350),142,293,Ui.MUTED);
  }
  protected void click(double x,double y,int button){
    seconds.click(x,y,18,123,72);
    if(Ui.inside(x,y,18,88,121,22)){if(minecraft.player!=null)chosen=minecraft.player.getMainHandItem().copyWithCount(1);status=chosen.isEmpty()?"В руке нет предмета":"Выбран предмет из руки";}
    else if(Ui.inside(x,y,186,121,236,28))matchName=!matchName;
    else if(Ui.inside(x,y,18,162,404,22))add();
    else if(Ui.inside(x,y,18,286,27,22))page=Math.max(0,page-1);
    else if(Ui.inside(x,y,103,286,27,22))page++;
    else {List<TimerRule> data=visible();for(int i=0;i<4&&page*4+i<data.size();i++)if(Ui.inside(x,y,395,196+i*21,27,19)){Config.timerRules.remove(data.get(page*4+i));Cooldowns.rulesChanged();status="Таймер удалён";break;}}
  }
  private void add(){
    if(chosen.isEmpty()){status="Сначала выбери предмет";return;}
    int duration;try{duration=Integer.parseInt(seconds.text);}catch(NumberFormatException e){status="Укажи целые секунды";return;}
    if(duration<1||duration>3600){status="Допустимо от 1 до 3600 с";return;}
    String id=Cooldowns.itemId(chosen),name=chosen.getHoverName().getString(),server=Cooldowns.server();
    if(name.length()>256||server.length()>512){status="Слишком длинное имя предмета / сервера";return;}
    TimerRule existing=Config.timerRules.stream().filter(r->r.item().equals(id)&&r.matchName()==matchName&&(!matchName||r.name().equals(name))&&r.server().equals(server)).findFirst().orElse(null);
    if(existing==null&&Config.timerRules.size()>=32){status="Лимит: 32 таймера";return;}
    Config.timerRules.remove(existing);Config.timerRules.add(new TimerRule(id,name,duration,matchName,server));Cooldowns.rulesChanged();status="Таймер сохранён";
  }
  public boolean charTyped(CharacterEvent e){return seconds.character(e)||super.charTyped(e);}
  public boolean keyPressed(KeyEvent e){return seconds.key(e)||super.keyPressed(e);}
}
