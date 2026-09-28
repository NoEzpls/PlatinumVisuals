package dev.platinum.visuals;
public record TimerRule(String item,String name,int seconds,boolean matchName,String server){
 public TimerRule{if(item==null||item.isBlank()||item.length()>256||name==null||name.length()>256)throw new IllegalArgumentException();seconds=Math.clamp(seconds,1,3600);if(server==null)server="";if(server.length()>512)throw new IllegalArgumentException();}
}
