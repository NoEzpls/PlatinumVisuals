package dev.platinum.visuals;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

/** Two color variants per label, bounded by Ui's LRU; never caches vanilla text. */
public final class CachedUiText implements FormattedCharSequence {
  private static int generation;
  public static long prepared, hits;
  private final FormattedCharSequence source;
  private Font owner;
  private int epoch=-1,color0,color1;
  private Font.PreparedText first,second;
  public CachedUiText(FormattedCharSequence source){this.source=source;}
  @Override public boolean accept(FormattedCharSink sink){return source.accept(sink);}
  public static void invalidate(){generation++;}
  public Font.PreparedText cached(Font font,int color){
    if(epoch!=generation||owner!=font){epoch=generation;owner=font;first=second=null;}
    if(first!=null&&color==color0){hits++;return first;}
    if(second!=null&&color==color1){hits++;return second;}
    return null;
  }
  public void remember(int color,Font.PreparedText text){
    if(text==first||text==second)return;
    second=first;color1=color0;first=text;color0=color;prepared++;
  }
}
