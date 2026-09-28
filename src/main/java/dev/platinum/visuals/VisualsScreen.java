package dev.platinum.visuals;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Compact two-column menu. Animation state changes independently of the tick rate. */
public final class VisualsScreen extends Screen {
  private static final int W=452,H=294,COL=213,ROW=33,PW=184;
  private static final Feature[] FEATURES=Feature.values();
  private final Screen previous;
  private final Motion.Clock clock=new Motion.Clock();
  private final float[] hover=new float[FEATURES.length], toggle=new float[FEATURES.length];
  private final ArrayList<Feature> filtered=new ArrayList<>();
  private int category, inspector, lastInspector, caret, anchor, profile;
  private float open, inspection, shift, tab, scroll, wantedScroll, settingsScroll, wantedSettingsScroll;
  private float baseScale=1,scale=1,vw,vh,x,y,px,py,ph,searchOffset;
  private boolean closing,searching,binding,animationSliding;
  private String query="",status="ЛКМ — переключить · ПКМ — настройки";
  private Feature selected,hovered;
  private UiViewport viewport=new UiViewport(1,0,0);
  private Feature.Setting sliding;
  private long hoverSince;

  public VisualsScreen(){this(null);}
  public VisualsScreen(Screen previous){
    super(Component.literal("Platinum Visuals"));this.previous=previous;
    for(Feature f:FEATURES)toggle[f.ordinal()]=f.enabled?1:0;
  }
  @Override protected void init(){closing=false;filter();}
  private void filter(){
    filtered.clear();String q=query.strip().toLowerCase(Locale.ROOT);
    String[] words=q.split("\\s+");
    for(Feature f:FEATURES){String aliases=f==Feature.PLAYER_GLOW?" glowplayer glow player":f==Feature.SKY?" неба skycolor":f==Feature.WATER_COLOR?" воды watercolor":"";String searchable=(f.title+" "+f.description+aliases).toLowerCase(Locale.ROOT);if((!q.isEmpty()||inCategory(f))&&java.util.Arrays.stream(words).allMatch(searchable::contains))filtered.add(f);}
    wantedScroll=Math.clamp(wantedScroll,0,maxScroll());
  }
  private float maxScroll(){return Math.max(0,((filtered.size()+1)/2)*ROW-(H-80));}
  private boolean inCategory(Feature f){return category==0?f.category==0&&!f.isHud():category==1?f.isHud():f.category==1;}
  private boolean extraButton(){return selected!=null&&(selected==Feature.COOLDOWNS||selected==Feature.OPTIMIZATION||selected.isEnvironment()||selected.hasColorPicker());}
  private String extraLabel(){return selected==Feature.COOLDOWNS?"Свои таймеры →":selected==Feature.OPTIMIZATION?"Профиль FPS →":selected.isEnvironment()?"Небо и мир →":"Выбрать цвет →";}
  private void openExtra(){minecraft.setScreen(selected==Feature.COOLDOWNS?new TimerScreen(this):selected==Feature.OPTIMIZATION?new PerformanceScreen(this):selected.isEnvironment()?new EnvironmentScreen(this,selected):new EffectColorScreen(this,selected));}
  private int settingsTop(){return extraButton()?127:96;}
  private float maxSettingsScroll(){return selected==null?0:Math.max(0,selected.settings.size()*39-(ph-settingsTop()-7));}
  private double mx(double value){return viewport.x(value);}
  private double my(double value){return viewport.y(value);}
  private void inspector(int value){
    inspector=value;if(value!=0)lastInspector=value;binding=false;sliding=null;animationSliding=false;
    settingsScroll=wantedSettingsScroll=0;
  }
  @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partialTick){
    long start=System.nanoTime();double dt=clock.step();
    open=Motion.approach(open,closing?0:1,5/Config.animationSeconds,dt);
    inspection=Motion.approach(inspection,inspector==0?0:1,23,dt);
    if(Config.reduceMotion){open=closing?0:1;inspection=inspector==0?0:1;}
    if(closing && open<.015f){minecraft.setScreen(previous);return;}
    // Larger high-DPI ceiling keeps controls legible on 4K displays.
    baseScale=Math.min(1.65f*Config.menuScale,Math.min(width/740f,height/420f));
    vw=width/baseScale;vh=height/baseScale;
    float center=(vw-W)/2;
    float desired=inspector==0?-16:(PW+12)/2f;
    shift=Config.reduceMotion?desired:Motion.approach(shift,desired,21,dt);tab=Motion.approach(tab,category*74,23,dt);
    for(Feature f:FEATURES)toggle[f.ordinal()]=Motion.approach(toggle[f.ordinal()],f.enabled?1:0,24,dt);
    scroll=Motion.approach(scroll,wantedScroll,20,dt);settingsScroll=Motion.approach(settingsScroll,wantedSettingsScroll,20,dt);
    x=center-shift;y=(vh-H)/2-8;px=x+W+12+7*(1-inspection);py=y+10;
    ph=lastInspector==1&&selected!=null?Math.min(H-10,Math.max(132,settingsTop()+7+selected.settings.size()*39)):184;
    scale=baseScale;
    viewport=new UiViewport(scale,width/2f-vw/2f*scale,height/2f-vh/2f*scale);
    double mx=mx(mouseX),my=my(mouseY);
    g.fill(0,0,width,height,Ui.alpha(Ui.menuBackdrop(),open));
    if(open>.01f)Ui.monitorBlur(g,width,height);
    g.pose().pushMatrix();g.pose().translate(viewport.left(),viewport.top());g.pose().scale(viewport.scale(),viewport.scale());
    Ui.opacity(open);
    Ui.icon(g,"logo",x+W/2-76,y-39,23,Config.accent());
    Ui.text(g,"Platinum",x+W/2-46,y-38,Ui.TEXT,1.35f);
    Ui.small(g,"V I S U A L S",x+W/2+25,y-33,Ui.MUTED);
    Ui.shadow(g,x,y,W,H);Ui.round(g,x,y,W,H,Ui.alpha(Ui.PANEL,Config.menuOpacity));
    Ui.round(g,x+11+tab,y+13,70,27,Ui.alpha(Config.accent(),.15));
    for(int i=0;i<3;i++)Ui.center(g,i==0?"Визуалы":i==1?"HUD":"Функции",x+46+i*74,y+22,i==category?Config.accent():Ui.MUTED,.87f);
    Ui.round(g,x+W-194,y+18,17,17,Config.accent());
    drawSearch(g);
    g.enableScissor((int)(x+10),(int)(y+52),(int)(x+W-10),(int)(y+H-28));
    Feature under=null;
    int first=Math.max(0,((int)scroll/ROW)*2),last=Math.min(filtered.size(),first+18);
    for(int i=first;i<last;i++){
      Feature f=filtered.get(i);int id=f.ordinal();float rx=x+10+(i%2)*(COL+6),ry=y+52+(i/2)*ROW-scroll;
      boolean hit=Ui.inside(mx,my,rx,ry,COL,28)&&my>=y+52&&my<y+H-28;
      if(hit)under=f;
      hover[id]=Motion.approach(hover[id],hit?1:0,20,dt);
      Ui.round(g,rx,ry,COL,28,Ui.blend(Ui.CARD,0xff302938,hover[id]));
      if(selected==f&&inspector==1)Ui.round(g,rx,ry,COL,28,Ui.alpha(Config.accent(),.09));
      Ui.text(g,Ui.trim(f.title,COL-56),rx+10,ry+8,f.enabled?Ui.TEXT:0xffa8a1b4,.93f);
      Ui.toggle(g,rx+COL-34,ry+7.5f,toggle[id]);
    }
    if(filtered.isEmpty())Ui.center(g,"Ничего не найдено",x+W/2,y+109,Ui.MUTED,1);
    g.disableScissor();
    if(maxScroll()>0){float track=H-82,knob=Math.max(24,track*track/(track+maxScroll()));Ui.round(g,x+W-5,y+53+scroll/maxScroll()*(track-knob),2,knob,Ui.alpha(Config.accent(),.7));}
    if(under!=hovered){hovered=under;hoverSince=System.nanoTime();}
    String hint=hovered!=null && System.nanoTime()-hoverSince>300_000_000L?hovered.description:status;
    Ui.small(g,Ui.trim(hint,475),x+13,y+H-17,Ui.MUTED);
    dock(g,mx,my);
    if(inspection>.01f){Ui.opacity(open*inspection);drawInspector(g,mx,my);}
    Ui.opacity(1);g.pose().popMatrix();UiProfile.frame(System.nanoTime()-start);
  }
  private void dock(GuiGraphics g,double mx,double my){
    float dx=x-47,dy=y+48;
    Ui.shadow(g,dx,dy,36,186);Ui.round(g,dx,dy,36,186,0xf518161f);
    String[] icons={"hud","profile","tune","functions","visuals"};
    String[] hints={"Редактор HUD","Сохранённые профили","Цвет и оформление","Настройки производительности","Пресеты атмосферы"};
    for(int i=0;i<5;i++){
      float iy=dy+5+i*35;boolean hit=Ui.inside(mx,my,dx+4,iy,28,30);
      if(hit)Ui.round(g,dx+4,iy,28,30,0xff302b3b);
      Ui.icon(g,icons[i],dx+10,iy+7,16,hit||(i==1&&inspector==2)?Config.accent():Ui.MUTED);
      if(hit)Ui.small(g,Ui.trim(hints[i],234),x+254,y+H+14,Ui.TEXT);
    }
    Ui.button(g,"Небо и мир",(int)x+10,(int)y+H+6,116,false,false);
    Ui.button(g,"Атмосфера",(int)x+134,(int)y+H+6,110,false,false);
    Ui.small(g,"Platinum Visuals 1.5  ·  Right Ctrl  ·  H: редактор HUD",x+10,y+H+38,Ui.MUTED);
  }
  private void drawSearch(GuiGraphics g){
    float sx=x+W-169,sy=y+13;
    Ui.round(g,sx,sy,155,27,searching?0xff2b2536:0xff211e2a);
    Ui.icon(g,"search",sx+8,sy+7,13,searching?Config.accent():Ui.MUTED);
    g.enableScissor((int)(sx+28),(int)(sy+2),(int)(sx+146),(int)(sy+26));
    float pos=Ui.width(query.substring(0,caret))*.9f;
    if(pos-searchOffset>110)searchOffset=pos-110;if(pos-searchOffset<0)searchOffset=pos;
    if(query.isEmpty())Ui.text(g,"Поиск",sx+28,sy+8,Ui.MUTED,.9f);
    else{
      if(searching&&caret!=anchor){float a=Ui.width(query.substring(0,Math.min(caret,anchor)))*.9f,b=Ui.width(query.substring(0,Math.max(caret,anchor)))*.9f;Ui.round(g,sx+28+a-searchOffset,sy+5,b-a,17,Ui.alpha(Config.accent(),.23));}
      Ui.text(g,query,sx+28-searchOffset,sy+8,Ui.TEXT,.9f);
    }
    if(searching&&(System.nanoTime()/500_000_000L)%2==0)Ui.round(g,sx+28+pos-searchOffset,sy+7,1,13,Config.accent());
    g.disableScissor();
  }
  private void drawInspector(GuiGraphics g,double mx,double my){
    Ui.shadow(g,px,py,PW,ph);Ui.round(g,px,py,PW,ph,0xfa19161f);
    String title=lastInspector==1&&selected!=null?selected.title:lastInspector==2?"Профили":"Оформление";
    Ui.text(g,Ui.trim(title,141),px+12,py+13,Ui.TEXT,.98f);Ui.icon(g,"close",px+PW-24,py+11,13,Ui.MUTED);
    if(lastInspector==1&&selected!=null){
      Ui.small(g,"Состояние",px+12,py+39,Ui.MUTED);Ui.toggle(g,px+PW-36,py+35,toggle[selected.ordinal()]);
      Ui.round(g,px+11,py+57,PW-22,25,binding?Ui.alpha(Config.accent(),.2):Ui.CARD);
      Ui.center(g,binding?"Нажми клавишу…":"Клавиша  ·  "+keyName(selected.key),px+PW/2,py+65,binding?Config.accent():Ui.MUTED,.83f);
      if(extraButton()){Ui.round(g,px+11,py+90,PW-22,25,Ui.alpha(Config.accent(),.14));Ui.center(g,extraLabel(),px+PW/2,py+98,Config.accent(),.85f);}
      g.enableScissor((int)(px+8),(int)(py+settingsTop()-7),(int)(px+PW-8),(int)(py+ph-7));
      for(int i=0;i<selected.settings.size();i++){
        Feature.Setting s=selected.settings.get(i);float sy=py+settingsTop()+i*39-settingsScroll;
        if(sy+31<py+settingsTop()-7||sy>py+ph-7)continue;
        if(s.min==0&&s.max==1&&s.step==1){Ui.small(g,s.label,px+12,sy,Ui.TEXT);Ui.toggle(g,px+PW-38,sy+15,s.value>0);continue;}
        Ui.small(g,s.label,px+12,sy,Ui.TEXT);String value=s.text();Ui.text(g,value,px+PW-12-Ui.width(value)*.8f,sy+15,Config.accent(),.8f);
        float track=PW-62,progress=(float)((s.value-s.min)/(s.max-s.min));
        Ui.round(g,px+12,sy+19,track,3,0xff3d3648);Ui.round(g,px+12,sy+19,track*progress,3,Config.accent());Ui.round(g,px+9+track*progress,sy+16,8,8,Ui.TEXT);
      }
      if(selected.settings.isEmpty())Ui.small(g,"Дополнительных настроек нет",px+12,py+95,Ui.MUTED);
      g.disableScissor();
    }else if(lastInspector==2){
      Ui.small(g,"Локальные настройки",px+12,py+34,Ui.MUTED);
      for(int i=0;i<3;i++){
        float sy=py+54+i*37;Ui.round(g,px+10,sy,PW-20,30,Ui.CARD);Ui.text(g,"0"+(i+1),px+19,sy+10,Config.accent(),.9f);
        Ui.text(g,"Сохранить",px+46,sy+10,Ui.TEXT,.73f);Ui.text(g,"Загрузить",px+105,sy+10,Ui.MUTED,.73f);
      }
    }else if(lastInspector==3){
      Ui.small(g,"Акцент",px+12,py+38,Ui.MUTED);
      for(int i=0;i<4;i++){Ui.round(g,px+12+i*37,py+54,29,23,Config.color(i));if(Config.theme==i)Ui.icon(g,"check",px+19+i*37,py+58,15,0xff21172b);}
      Ui.small(g,"Мягкие тени",px+12,py+94,Ui.TEXT);Ui.toggle(g,px+PW-36,py+89,Config.softShadows);
      Ui.small(g,"Скорость анимаций",px+12,py+120,Ui.TEXT);
      float fraction=(Config.animationSeconds-.12f)/.28f;
      Ui.round(g,px+12,py+142,PW-24,3,0xff3d3648);Ui.round(g,px+12,py+142,(PW-24)*(1-fraction),3,Config.accent());Ui.round(g,px+9+(PW-24)*(1-fraction),py+139,8,8,Ui.TEXT);
      Ui.small(g,"Плавно",px+12,py+162,Ui.MUTED);Ui.small(g,"Быстро",px+PW-48,py+162,Ui.MUTED);
    }
  }
  @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){
    if(closing)return true;double mx=mx(e.x()),my=my(e.y());
    if(inspector!=0&&Ui.inside(mx,my,px,py,PW,ph)){
      searching=false;
      if(Ui.inside(mx,my,px+PW-29,py+7,22,22)){inspector(0);return true;}
      if(inspector==1&&selected!=null){
        if(Ui.inside(mx,my,px+PW-42,py+32,34,21)){selected.toggle();return true;}
        if(Ui.inside(mx,my,px+11,py+57,PW-22,25)){binding=true;return true;}
        if(extraButton()&&Ui.inside(mx,my,px+11,py+90,PW-22,25)){openExtra();return true;}
        if(my>=py+settingsTop()-7&&my<py+ph-7)for(int i=0;i<selected.settings.size();i++)if(Ui.inside(mx,my,px+8,py+settingsTop()+i*39-settingsScroll,PW-16,35)){Feature.Setting s=selected.settings.get(i);if(s.min==0&&s.max==1&&s.step==1){s.set(s.value>0?0:1);Config.dirty();}else{sliding=s;slide(mx);}return true;}
      }else if(inspector==2){
        for(int i=0;i<3;i++)if(Ui.inside(mx,my,px+10,py+54+i*37,PW-20,30)){
          profile=i;boolean saving=mx<px+101;boolean ok=saving?Config.saveProfile(i):Config.loadProfile(i);
          status=ok?(saving?"Профиль сохранён":"Профиль загружен"):"Профиль недоступен";return true;
        }
      }else if(inspector==3){
        for(int i=0;i<4;i++)if(Ui.inside(mx,my,px+12+i*37,py+54,29,23)){Config.theme=i;Config.dirty();return true;}
        if(Ui.inside(mx,my,px+10,py+84,PW-20,24)){Config.softShadows=!Config.softShadows;Config.dirty();return true;}
        if(Ui.inside(mx,my,px+8,py+130,PW-16,26)){animationSliding=true;slide(mx);return true;}
      }
      return true;
    }
    binding=false;
    if(Ui.inside(mx,my,x+W-169,y+13,155,27)){
      searching=true;double qx=mx-(x+W-141)+searchOffset;caret=0;
      while(caret<query.length()){
        int next=query.offsetByCodePoints(caret,1);if((Ui.width(query.substring(0,caret))+Ui.width(query.substring(0,next)))*.45>qx)break;caret=next;
      }
      anchor=caret;return true;
    }
    searching=false;
    if(Ui.inside(mx,my,x+10,y+H+6,116,22)){minecraft.setScreen(new EnvironmentScreen(this,Feature.SKY));return true;}
    if(Ui.inside(mx,my,x+134,y+H+6,110,22)){minecraft.setScreen(new AtmosphereScreen(this));return true;}
    for(int i=0;i<3;i++)if(Ui.inside(mx,my,x+11+i*74,y+13,70,27)){category=i;scroll=wantedScroll=0;filter();return true;}
    if(Ui.inside(mx,my,x+W-197,y+13,23,27)){minecraft.setScreen(new ThemeScreen(this));return true;}
    for(int i=0;i<5;i++)if(Ui.inside(mx,my,x-43,y+53+i*35,28,30)){switch(i){case 0->minecraft.setScreen(new HudEditorScreen(this));case 1->inspector(inspector==2?0:2);case 2->minecraft.setScreen(new ThemeScreen(this));case 3->minecraft.setScreen(new PerformanceScreen(this));case 4->minecraft.setScreen(new AtmosphereScreen(this));}return true;}
    if(Ui.inside(mx,my,x+10,y+52,W-20,H-80))for(int i=0;i<filtered.size();i++){
      float rx=x+10+(i%2)*(COL+6),ry=y+52+(i/2)*ROW-scroll;
      if(Ui.inside(mx,my,rx,ry,COL,28)){
        Feature f=filtered.get(i);
        // Both primary and secondary clicks open the same spacious settings page;
        // this keeps the hit target predictable and avoids a tiny legacy inspector.
        if(e.button()==0 || e.button()==1){minecraft.setScreen(new FeatureScreen(this,f));}
        else if(e.button()==2){f.toggle();}
        return true;
      }
    }
    if(inspector!=0)inspector(0);return true;
  }
  private void slide(double mx){
    if(sliding!=null){double f=Math.clamp((mx-px-12)/(PW-62),0,1);sliding.set(sliding.min+f*(sliding.max-sliding.min));if(sliding.id.equals("hue")&&selected!=null)Config.effectColors.remove(selected.name());Config.dirty();}
    if(animationSliding){Config.animationSeconds=(float)(.12+.28*(1-Math.clamp((mx-px-12)/(PW-24),0,1)));Config.dirty();}
  }
  @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){slide(mx(e.x()));return sliding!=null||animationSliding;}
  @Override public boolean mouseReleased(MouseButtonEvent e){sliding=null;animationSliding=false;return true;}
  @Override public boolean mouseScrolled(double xx,double yy,double horizontal,double vertical){
    double mx=mx(xx),my=my(yy);
    if(inspector==1&&Ui.inside(mx,my,px,py,PW,ph))wantedSettingsScroll=(float)Math.clamp(wantedSettingsScroll-vertical*28,0,maxSettingsScroll());
    else if(Ui.inside(mx,my,x,y,W,H))wantedScroll=(float)Math.clamp(wantedScroll-vertical*33,0,maxScroll());
    return true;
  }
  private boolean selection(){return caret!=anchor;}
  private void insert(String text){
    String clean=text.codePoints().filter(c->!Character.isISOControl(c)).collect(StringBuilder::new,StringBuilder::appendCodePoint,StringBuilder::append).toString();
    int a=Math.min(caret,anchor),b=Math.max(caret,anchor),room=64-(query.length()-(b-a));
    if(clean.length()>room){int end=room;if(end>0&&Character.isHighSurrogate(clean.charAt(end-1)))end--;clean=clean.substring(0,end);}
    query=query.substring(0,a)+clean+query.substring(b);caret=anchor=a+clean.length();wantedScroll=scroll=0;filter();
  }
  @Override public boolean charTyped(CharacterEvent e){
    if(!searching||binding)return false;insert(new String(Character.toChars(e.codepoint())));return true;
  }
  @Override public boolean keyPressed(KeyEvent e){
    int key=e.key();boolean ctrl=(e.modifiers()&GLFW.GLFW_MOD_CONTROL)!=0,shiftKey=(e.modifiers()&GLFW.GLFW_MOD_SHIFT)!=0;
    if(binding&&selected!=null){
      if(key!=GLFW.GLFW_KEY_ESCAPE){selected.key=key==GLFW.GLFW_KEY_DELETE||key==GLFW.GLFW_KEY_BACKSPACE?-1:key;Config.dirty();}binding=false;return true;
    }
    if(searching){
      if(key==GLFW.GLFW_KEY_ESCAPE||key==GLFW.GLFW_KEY_ENTER){searching=false;return true;}
      if(ctrl&&key==GLFW.GLFW_KEY_A){anchor=0;caret=query.length();return true;}
      if(ctrl&&(key==GLFW.GLFW_KEY_C||key==GLFW.GLFW_KEY_X)){if(selection())minecraft.keyboardHandler.setClipboard(query.substring(Math.min(caret,anchor),Math.max(caret,anchor)));if(key==GLFW.GLFW_KEY_X&&selection())insert("");return true;}
      if(ctrl&&key==GLFW.GLFW_KEY_V){insert(minecraft.keyboardHandler.getClipboard());return true;}
      if(key==GLFW.GLFW_KEY_BACKSPACE||key==GLFW.GLFW_KEY_DELETE){if(!selection()){if(key==GLFW.GLFW_KEY_BACKSPACE&&caret>0)anchor=query.offsetByCodePoints(caret,-1);else if(key==GLFW.GLFW_KEY_DELETE&&caret<query.length())anchor=query.offsetByCodePoints(caret,1);}insert("");return true;}
      if(key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_RIGHT||key==GLFW.GLFW_KEY_HOME||key==GLFW.GLFW_KEY_END){
        if(!shiftKey&&selection()&&(key==GLFW.GLFW_KEY_LEFT||key==GLFW.GLFW_KEY_RIGHT))caret=key==GLFW.GLFW_KEY_LEFT?Math.min(caret,anchor):Math.max(caret,anchor);
        else if(key==GLFW.GLFW_KEY_HOME)caret=0;else if(key==GLFW.GLFW_KEY_END)caret=query.length();else if(key==GLFW.GLFW_KEY_LEFT&&caret>0)caret=query.offsetByCodePoints(caret,-1);else if(key==GLFW.GLFW_KEY_RIGHT&&caret<query.length())caret=query.offsetByCodePoints(caret,1);
        if(!shiftKey)anchor=caret;return true;
      }
      return true;
    }
    if(key==GLFW.GLFW_KEY_ESCAPE){if(inspector!=0)inspector(0);else onClose();return true;}
    if(key==GLFW.GLFW_KEY_RIGHT_CONTROL){onClose();return true;}
    if(key==GLFW.GLFW_KEY_H){minecraft.setScreen(new HudEditorScreen(this));return true;}
    if(ctrl&&key==GLFW.GLFW_KEY_F){searching=true;return true;}
    return super.keyPressed(e);
  }
  public static String keyName(int key){if(key<0)return "—";return InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName().getString();}
  @Override public void onClose(){closing=true;}
  @Override public void removed(){Config.save();Ui.opacity(1);}
  @Override public boolean isPauseScreen(){return false;}
}
