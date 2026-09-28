package dev.platinum.visuals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
public final class TextInput{
 public String text;public boolean focused;private boolean selected;private int caret;private final int limit;
 public TextInput(String text,int limit){this.text=text;caret=text.length();this.limit=limit;}public void set(String text){this.text=text;caret=text.length();selected=false;}
 public void draw(GuiGraphics g,int x,int y,int w,String hint){Ui.round(g,x,y,w,27,focused?0xff302937:Ui.CARD);g.enableScissor(x+6,y+2,x+w-6,y+25);float offset=Math.max(0,Ui.width(text.substring(0,caret))*.9f-(w-20));if(focused&&selected)Ui.round(g,x+7-offset,y+4,Ui.width(text)*.9f,18,Ui.alpha(Config.accent(),.2));Ui.text(g,text.isEmpty()?hint:text,x+8-offset,y+8,text.isEmpty()?Ui.MUTED:Ui.TEXT,.9f);if(focused&&(System.nanoTime()/500_000_000L)%2==0)Ui.round(g,x+8+Ui.width(text.substring(0,caret))*.9f-offset,y+6,1,14,Config.accent());g.disableScissor();}
 public boolean click(double mx,double my,int x,int y,int w){focused=Ui.inside(mx,my,x,y,w,27);selected=false;if(focused)caret=text.length();return focused;}
 public boolean character(CharacterEvent e){if(!focused||Character.isISOControl(e.codepoint()))return false;insert(new String(Character.toChars(e.codepoint())));return true;}
 private void insert(String v){v=v.codePoints().filter(c->!Character.isISOControl(c)).collect(StringBuilder::new,StringBuilder::appendCodePoint,StringBuilder::append).toString();if(selected){text="";caret=0;selected=false;}int room=limit-text.length();if(v.length()>room){int n=room;if(n>0&&Character.isHighSurrogate(v.charAt(n-1)))n--;v=v.substring(0,n);}text=text.substring(0,caret)+v+text.substring(caret);caret+=v.length();}
 public boolean key(KeyEvent e){if(!focused)return false;boolean ctrl=(e.modifiers()&GLFW.GLFW_MOD_CONTROL)!=0;int k=e.key();
  if(k==256||k==257){focused=false;return true;}if(ctrl&&k==65){selected=true;return true;}if(ctrl&&k==86){insert(Minecraft.getInstance().keyboardHandler.getClipboard());return true;}if(ctrl&&(k==67||k==88)){if(selected){Minecraft.getInstance().keyboardHandler.setClipboard(text);if(k==88)insert("");}return true;}
  if(k==259||k==261){if(selected)insert("");else if(k==259&&caret>0){int p=text.offsetByCodePoints(caret,-1);text=text.substring(0,p)+text.substring(caret);caret=p;}else if(k==261&&caret<text.length())text=text.substring(0,caret)+text.substring(text.offsetByCodePoints(caret,1));return true;}
  if(k==263&&caret>0)caret=text.offsetByCodePoints(caret,-1);else if(k==262&&caret<text.length())caret=text.offsetByCodePoints(caret,1);else if(k==268)caret=0;else if(k==269)caret=text.length();selected=false;return true;
 }
}
