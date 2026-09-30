package dev.platinum.visuals;
import java.util.*;
public final class TimerLedger<K>{
 public record Window(long start,long end,boolean estimated){public long remaining(long tick){return Math.max(0,end-tick);}public double fraction(long tick){return Math.clamp(remaining(tick)/(double)Math.max(1,end-start),0,1);}}
 private final LinkedHashMap<K,Window> entries=new LinkedHashMap<>();private final int limit;
 public TimerLedger(int limit){this.limit=Math.max(1,limit);}public void start(K key,long now,int ticks,boolean estimated){if(ticks<=0){entries.remove(key);return;}entries.remove(key);while(entries.size()>=limit)entries.remove(entries.keySet().iterator().next());entries.put(key,new Window(now,now+(long)ticks,estimated));}
 public boolean active(K key,long now){Window w=entries.get(key);return w!=null&&w.remaining(now)>0;}public void expire(long now){entries.values().removeIf(w->w.remaining(now)==0);}public Map<K,Window> entries(){return Collections.unmodifiableMap(entries);}public void clear(){entries.clear();}
 public static String seconds(long ticks){if(ticks<=0)return "0.0 с";long n=(ticks+1)/2;return n<600?n/10+"."+n%10+" с":(long)Math.ceil(ticks/20.0)/60+":"+String.format(Locale.ROOT,"%02d",(long)Math.ceil(ticks/20.0)%60);}
}
