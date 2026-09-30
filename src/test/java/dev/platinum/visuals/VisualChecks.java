package dev.platinum.visuals;

/** Headless logic checks, not a substitute for a graphical Minecraft smoke test. */
public final class VisualChecks {
  private static int assertions;
  private static void require(boolean condition,String detail){assertions++;if(!condition)throw new AssertionError(detail);}
  public static void main(String[] args){
    TimerLedger<String> timers=new TimerLedger<>(3);
    timers.start("pearl",100,300,false);
    require(timers.active("pearl",100),"received cooldown must be active");
    require(timers.entries().get("pearl").remaining(200)==200,"remaining duration is in ticks, independent of inventory");
    require(Math.abs(timers.entries().get("pearl").fraction(250)-.5)<1e-9,"fraction must be half at half time");
    require(timers.entries().get("pearl").remaining(1000)==0,"time cannot be negative");
    require(!timers.active("pearl",400),"exact expiry");timers.expire(400);require(timers.entries().isEmpty(),"expire removes labels' clock keys");
    timers.start("a",0,20,false);timers.start("b",0,20,false);timers.start("c",0,20,false);timers.start("d",0,20,false);
    require(timers.entries().size()==3&&!timers.active("a",0),"bounded memory");
    timers.start("b",5,40,false);require(timers.entries().get("b").remaining(5)==40,"server correction replaces previous timer");
    timers.start("c",6,0,false);require(!timers.active("c",6),"zero duration removes cooldown");
    timers.start("estimate",10,30,true);require(timers.entries().get("estimate").estimated(),"estimates are distinguishable");
    try{timers.entries().clear();throw new AssertionError("ledger view must be immutable");}catch(UnsupportedOperationException expected){assertions++;}
    timers.clear();require(timers.entries().isEmpty(),"world reset");
    require(TimerLedger.seconds(1).equals("0.1 с"),"one tick remains visible");
    require(TimerLedger.seconds(299).equals("15.0 с"),"tenths round up");
    require(TimerLedger.seconds(1200).equals("1:00"),"minute notation");
    require(TimerLedger.seconds(0).equals("0.0 с"),"zero time");
    require(ThemeColor.parse("#1aB2c3").orElseThrow()==0xff1ab2c3,"HEX accepts mixed case");
    require(ThemeColor.parse("1AB2C3").isPresent(),"hash is optional");
    for(String invalid:new String[]{"","#12","#XX1234","11223344","not-a-colour"})require(ThemeColor.parse(invalid).isEmpty(),"reject invalid colour: "+invalid);
    for(int r=0;r<=255;r+=17)for(int g=0;g<=255;g+=17)for(int b=0;b<=255;b+=17){int rgb=0xff000000|r<<16|g<<8|b;float[] hsv=ThemeColor.hsv(rgb);int roundtrip=ThemeColor.rgb(hsv[0],hsv[1],hsv[2]);require(Math.abs(((roundtrip>>16)&255)-r)<=1&&Math.abs(((roundtrip>>8)&255)-g)<=1&&Math.abs((roundtrip&255)-b)<=1,"RGB HSV roundtrip");}
    require(new TimerRule("minecraft:ender_pearl","Жемчуг",9999,true,"server").seconds()==3600,"timer max duration");
    require(new TimerRule("minecraft:ender_pearl","Жемчуг",-5,true,null).seconds()==1,"timer min duration");
    require(new TimerRule("id","name",20,false,null).server().isEmpty(),"legacy/global scope");
    for(Feature f:Feature.values())for(Feature.Setting s:f.settings){require(s.min<=s.value&&s.value<=s.max&&s.step>0,"valid initial setting "+f+"/"+s.id);double old=s.value;s.set(Double.POSITIVE_INFINITY);require(s.value==old,"nonfinite values rejected");s.set(old);}
    System.out.println("VisualChecks: "+assertions+" assertions passed");
  }
}
