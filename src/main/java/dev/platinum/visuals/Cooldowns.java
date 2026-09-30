package dev.platinum.visuals;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
public final class Cooldowns{
 public record Row(String label,String time,ItemStack icon,double fraction,boolean estimated){}
 private record Label(String name,ItemStack stack,String group){}
 private static final TimerLedger<String> CLOCKS=new TimerLedger<>(128);
 private static final LinkedHashMap<String,Label> LABELS=new LinkedHashMap<>(),SEEN=new LinkedHashMap<>();
 private static List<Row> rows=List.of();public static List<Row> rows(){return rows;}
 public static void reset(){CLOCKS.clear();LABELS.clear();SEEN.clear();rows=List.of();}
 public static String server(){var s=Minecraft.getInstance().getCurrentServer();return s==null?"singleplayer":s.ip.toLowerCase(Locale.ROOT);}
 public static String itemId(ItemStack s){return BuiltInRegistries.ITEM.getKey(s.getItem()).toString();}
 private static void remember(ItemStack s){var p=Minecraft.getInstance().player;if(s.isEmpty()||p==null)return;String group=p.getCooldowns().getCooldownGroup(s).toString();SEEN.put(group,new Label(s.getHoverName().getString(),s.copyWithCount(1),group));while(SEEN.size()>256)SEEN.remove(SEEN.keySet().iterator().next());}
 public static void started(Identifier group,int ticks){String id=group.toString();CLOCKS.start(id,Client.ticks,ticks,false);if(ticks<=0){LABELS.remove(id);return;}var p=Minecraft.getInstance().player;if(p!=null){remember(p.getMainHandItem());remember(p.getOffhandItem());for(int i=0;i<p.getInventory().getContainerSize();i++)remember(p.getInventory().getItem(i));}Label label=SEEN.get(id);if(label==null){var item=BuiltInRegistries.ITEM.getValue(group);ItemStack s=item==null?ItemStack.EMPTY:new ItemStack(item);label=new Label(s.isEmpty()?group.getPath():s.getHoverName().getString(),s,id);}LABELS.put(id,label);for(var e:new ArrayList<>(LABELS.entrySet()))if(e.getKey().startsWith("~")&&e.getValue().group.equals(id))CLOCKS.start(e.getKey(),Client.ticks,0,true);}
 public static void used(ItemStack s){if(s.isEmpty()||Feature.COOLDOWNS.value("estimates")==0)return;remember(s);var p=Minecraft.getInstance().player;if(p==null)return;String group=p.getCooldowns().getCooldownGroup(s).toString();if(CLOCKS.active(group,Client.ticks))return;String item=itemId(s),name=s.getHoverName().getString(),server=server();for(int i=0;i<Config.timerRules.size();i++){TimerRule r=Config.timerRules.get(i);if(!r.item().equals(item)||(r.matchName()&&!r.name().equals(name))||(!r.server().isEmpty()&&!r.server().equals(server)))continue;String key="~"+i;if(!CLOCKS.active(key,Client.ticks)){CLOCKS.start(key,Client.ticks,r.seconds()*20,true);LABELS.put(key,new Label(name,s.copyWithCount(1),group));}break;}}
 public static void tick(){CLOCKS.expire(Client.ticks);LABELS.keySet().retainAll(CLOCKS.entries().keySet());if(Client.ticks%2!=0)return;List<Row> out=new ArrayList<>();for(var e:CLOCKS.entries().entrySet()){Label l=LABELS.get(e.getKey());var w=e.getValue();if(l==null||(w.estimated()&&Feature.COOLDOWNS.value("estimates")==0))continue;out.add(new Row(l.name,(w.estimated()?"≈ ":"")+TimerLedger.seconds(w.remaining(Client.ticks)),l.stack,w.fraction(Client.ticks),w.estimated()));}rows=List.copyOf(out);}
 public static void rulesChanged(){for(String k:new ArrayList<>(CLOCKS.entries().keySet()))if(k.startsWith("~"))CLOCKS.start(k,Client.ticks,0,true);Config.dirty();}
}
