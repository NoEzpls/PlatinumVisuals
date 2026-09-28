package dev.platinum.visuals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

/** One geometry definition for each visible control, reused by drawing and input. */
public final class FeatureScreen extends PanelScreen {
  private static final int X=205, TOP=64, BOTTOM=329, ROW=47, WIDTH=288;
  private final Feature feature;
  private final TextInput search=new TextInput("",64), number=new TextInput("",16);
  private final List<Feature.Setting> visible=new ArrayList<>();
  private final Motion.Clock clock=new Motion.Clock();
  private float scroll,wanted;
  private Feature.Setting sliding,editing;
  private boolean binding;
  private String error="";
  public FeatureScreen(Screen previous,Feature feature){super(previous,feature.title,516,362);this.feature=feature;filter();}
  private void filter(){visible.clear();String q=search.text.toLowerCase(java.util.Locale.ROOT);for(var s:feature.settings)if(s.label.toLowerCase(java.util.Locale.ROOT).contains(q)||s.id.toLowerCase(java.util.Locale.ROOT).contains(q))visible.add(s);wanted=Math.clamp(wanted,0,maxScroll());}
  private float maxScroll(){return Math.max(0,visible.size()*ROW-(BOTTOM-TOP));}
  private UiViewport.Rect row(int i){return new UiViewport.Rect(X,TOP+i*ROW-scroll,WIDTH,41);}
  private UiViewport.Rect track(UiViewport.Rect r){return new UiViewport.Rect(r.x()+11,r.y()+28,r.w()-98,4);}
  private UiViewport.Rect value(UiViewport.Rect r){return new UiViewport.Rect(r.x()+r.w()-80,r.y()+19,69,19);}
  @Override protected void draw(GuiGraphics g,double mx,double my){
    scroll=Config.reduceMotion?wanted:Motion.approach(scroll,wanted,24,clock.step());
    Ui.text(g,"Настройки модуля",18,48,Ui.MUTED,.85f);
    for(int i=0;i<Math.min(4,wrap(feature.description,158).size());i++)Ui.small(g,wrap(feature.description,158).get(i),18,73+i*13,Ui.TEXT);
    Ui.button(g,feature.enabled?"Включено":"Выключено",18,137,162,feature.enabled,Ui.inside(mx,my,18,137,162,22));
    Ui.button(g,binding?"Нажми клавишу…":"Клавиша: "+VisualsScreen.keyName(feature.key),18,170,162,binding,false);
    Ui.button(g,"Сбросить настройки",18,203,162,false,Ui.inside(mx,my,18,203,162,22));
    if(feature.hasColorPicker())Ui.button(g,"Цвет эффекта",18,236,162,false,false);
    Ui.small(g,"Числа: нажми на значение",18,282,Ui.MUTED);
    Ui.small(g,"Enter — сохранить · Esc — назад",18,297,Ui.MUTED);
    search.draw(g,X,35,WIDTH,"Поиск параметра");
    g.enableScissor(X,TOP,X+WIDTH,BOTTOM);
    for(int i=0;i<visible.size();i++){
      var s=visible.get(i);var r=row(i);if(r.y()+r.h()<TOP||r.y()>BOTTOM)continue;
      Ui.round(g,r.x(),r.y(),r.w(),r.h(),Ui.CARD);
      Ui.small(g,Ui.trim(s.label,(int)((r.w()-22)/.85f)),r.x()+11,r.y()+7,Ui.TEXT);
      if(s.isBoolean()){Ui.toggle(g,r.x()+r.w()-38,r.y()+23,s.value>0);Ui.small(g,s.text(),r.x()+11,r.y()+25,Ui.MUTED);}
      else if(s.isChoice()){Ui.round(g,r.x()+11,r.y()+22,r.w()-22,17,0xff292532);Ui.center(g,"‹   "+s.text()+"   ›",r.x()+r.w()/2,r.y()+25,Config.accent(),.85f);}
      else{var t=track(r);float f=(float)((s.value-s.min)/(s.max-s.min));Ui.round(g,t.x(),t.y(),t.w(),t.h(),0xff403749);Ui.round(g,t.x(),t.y(),t.w()*f,t.h(),Config.accent());Ui.round(g,t.x()+t.w()*f-3,t.y()-2,8,8,Ui.TEXT);var v=value(r);Ui.round(g,v.x(),v.y(),v.w(),v.h(),0xff302939);if(editing==s)number.draw(g,(int)v.x(),(int)v.y(),(int)v.w(),s.text());else Ui.center(g,s.text(),v.x()+v.w()/2,v.y()+5,Config.accent(),.85f);}
    }
    g.disableScissor();
    if(maxScroll()>0){float h=(BOTTOM-TOP)*(BOTTOM-TOP)/(float)(visible.size()*ROW);Ui.round(g,X+WIDTH+5,TOP+scroll/maxScroll()*(BOTTOM-TOP-h),2,h,Config.accent());}
    Ui.small(g,error.isEmpty()?visible.size()+" параметров · колесо: прокрутка · ПКМ по значению: сброс":error,18,340,error.isEmpty()?Ui.MUTED:0xffff9ab0);
  }
  private List<String> wrap(String text,int width){List<String> out=new ArrayList<>();String line="";for(String word:text.split(" ")){String next=line.isEmpty()?word:line+" "+word;if(Ui.width(next)*.85f>width&&!line.isEmpty()){out.add(line);line=word;}else line=next;}if(!line.isEmpty())out.add(line);return out;}
  @Override protected void click(double x,double y,int button){
    if(editing!=null&&!number.focused)commit();
    search.click(x,y,X,35,WIDTH);
    if(Ui.inside(x,y,18,137,162,22)){feature.toggle();return;}
    if(Ui.inside(x,y,18,170,162,22)){binding=true;return;}
    if(Ui.inside(x,y,18,203,162,22)){for(var s:feature.settings)s.set(s.initial);Config.effectColors.remove(feature.name());Config.dirty();return;}
    if(feature.hasColorPicker()&&Ui.inside(x,y,18,236,162,22)){minecraft.setScreen(new EffectColorScreen(this,feature));return;}
    if(y<TOP||y>=BOTTOM)return;
    for(int i=0;i<visible.size();i++){var r=row(i);if(!r.contains(x,y))continue;var s=visible.get(i);editing=null;number.focused=false;
      if(button==1){s.set(s.initial);changed(s);return;}
      if(s.isBoolean()){s.set(s.value>0?0:1);changed(s);}
      else if(s.isChoice()){double next=s.value+(x<r.x()+r.w()/2?-1:1);s.set(next>s.max?s.min:next<s.min?s.max:next);changed(s);}
      else if(value(r).contains(x,y)){editing=s;number.set(Double.toString(s.value));number.focused=true;}
      else if(y>=r.y()+19){sliding=s;slide(x,r);}
      return;
    }
  }
  private void changed(Feature.Setting s){if(s.id.equals("hue"))Config.effectColors.remove(feature.name());Config.dirty();error="";}
  private void slide(double x,UiViewport.Rect r){var t=track(r);sliding.set(sliding.min+Math.clamp((x-t.x())/t.w(),0,1)*(sliding.max-sliding.min));changed(sliding);}
  @Override protected void drag(double x,double y){if(sliding!=null){int i=visible.indexOf(sliding);if(i>=0)slide(x,row(i));}}
  @Override protected void released(){sliding=null;}
  @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){wanted=(float)Math.clamp(wanted-vertical*30,0,maxScroll());return true;}
  private boolean commit(){if(editing==null)return true;try{double v=Double.parseDouble(number.text.replace(',','.'));if(!Double.isFinite(v)||v<editing.min||v>editing.max)throw new IllegalArgumentException();editing.set(v);changed(editing);editing=null;number.focused=false;return true;}catch(IllegalArgumentException ex){error="Допустимо от "+editing.min+" до "+editing.max;return false;}}
  @Override public boolean charTyped(CharacterEvent e){if(editing!=null)return number.character(e);if(search.character(e)){filter();return true;}return super.charTyped(e);}
  @Override public boolean keyPressed(KeyEvent e){if(binding){feature.key=e.key()==GLFW.GLFW_KEY_ESCAPE||e.key()==GLFW.GLFW_KEY_DELETE?-1:e.key();binding=false;Config.dirty();return true;}if(editing!=null){if(e.key()==GLFW.GLFW_KEY_ENTER||e.key()==GLFW.GLFW_KEY_KP_ENTER){commit();return true;}if(e.key()==GLFW.GLFW_KEY_ESCAPE){editing=null;number.focused=false;return true;}return number.key(e);}if(search.key(e)){filter();return true;}return super.keyPressed(e);}
}
