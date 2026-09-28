package dev.platinum.visuals;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
public abstract class PanelScreen extends Screen{
 protected final Screen previous;protected final int panelWidth,panelHeight;
 private UiViewport viewport=new UiViewport(1,0,0);private final float initialScale=Config.menuScale;
 protected PanelScreen(Screen previous,String title,int w,int h){super(Component.literal(title));this.previous=previous;panelWidth=w;panelHeight=h;}
 public final void render(GuiGraphics g,int mx,int my,float partial){
  viewport=UiViewport.centered(width,height,panelWidth,panelHeight,1.4f*initialScale);
  Ui.opacity(1);g.fill(0,0,width,height,Ui.menuBackdrop());Ui.monitorBlur(g,width,height);g.pose().pushMatrix();g.pose().translate(viewport.left(),viewport.top());g.pose().scale(viewport.scale(),viewport.scale());
  Ui.shadow(g,0,0,panelWidth,panelHeight);Ui.round(g,0,0,panelWidth,panelHeight,Ui.PANEL);Ui.icon(g,"logo",16,14,18,Config.accent());Ui.text(g,getTitle().getString(),43,17,Ui.TEXT,1.1f);Ui.icon(g,"close",panelWidth-30,16,14,Ui.MUTED);
  draw(g,viewport.x(mx),viewport.y(my));g.pose().popMatrix();
 }
 protected abstract void draw(GuiGraphics g,double x,double y);protected abstract void click(double x,double y,int button);protected void drag(double x,double y){}protected void released(){}
 public final boolean mouseClicked(MouseButtonEvent e,boolean twice){double x=viewport.x(e.x()),y=viewport.y(e.y());if(Ui.inside(x,y,panelWidth-36,10,26,26))onClose();else click(x,y,e.button());return true;}
 public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){drag(viewport.x(e.x()),viewport.y(e.y()));return true;}
 public boolean mouseReleased(MouseButtonEvent e){released();return true;}
 public void onClose(){Config.save();minecraft.setScreen(previous);}public boolean isPauseScreen(){return false;}
}
