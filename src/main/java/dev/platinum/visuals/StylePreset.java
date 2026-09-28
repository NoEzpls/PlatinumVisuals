package dev.platinum.visuals;
/** Original, lightweight looks. All effect hues follow the user's accent by default. */
public enum StylePreset {
  EVENING("Мягкий вечер","Закат, лёгкая дымка, короткие искры",12000,.16,144,1),
  NIGHT("Холодная ночь","Сумерки, светлячки и мягкое осветление",14000,.25,112,2),
  DAY("Чистый день","Ясный полдень и почти незаметные частицы",6000,.07,208,0);
  public final String title,description;private final int time,distance,style;private final double fog;
  StylePreset(String title,String description,int time,double fog,int distance,int style){this.title=title;this.description=description;this.time=time;this.fog=fog;this.distance=distance;this.style=style;}
  public void apply(){
    Feature.WORLD_TIME.enabled=true;set(Feature.WORLD_TIME,"time",time);
    Feature.SKY.enabled=true;Config.effectColors.remove(Feature.SKY.name());
    set(Feature.SKY,"strength",this==DAY?.1:this==NIGHT?.35:.25);set(Feature.SKY,"brightness",this==NIGHT?.55:1);
    set(Feature.SKY,"sunset",1);set(Feature.SKY,"sun",1);set(Feature.SKY,"moon",1);set(Feature.SKY,"stars",this==NIGHT?1.7:1);set(Feature.SKY,"dayStars",0);
    Config.effectColors.remove(Feature.FOG.name());
    Feature.FOG.enabled=true;set(Feature.FOG,"distance",distance);set(Feature.FOG,"strength",fog);set(Feature.FOG,"start",.4);set(Feature.FOG,"hue",-1);
    Feature.WEATHER.enabled=true;Feature.NO_HURT_SHAKE.enabled=true;
    Feature.ATMOSPHERE.enabled=this!=DAY;Feature.TRAILS.enabled=this!=DAY;
    Feature.HIT_PARTICLES.enabled=true;Feature.JUMP_CIRCLES.enabled=true;
    Feature.FULLBRIGHT.enabled=this==NIGHT;set(Feature.FULLBRIGHT,"strength",.16);
    set(Feature.ATMOSPHERE,"count",2);set(Feature.ATMOSPHERE,"radius",8);
    set(Feature.TRAILS,"density",2);set(Feature.HIT_PARTICLES,"count",12);
    for(Feature f:new Feature[]{Feature.HIT_PARTICLES,Feature.HIT_BUBBLE,Feature.KILL_EFFECT,Feature.TRAILS,Feature.JUMP_CIRCLES,Feature.ATMOSPHERE}){Config.effectColors.remove(f.name());set(f,"hue",-1);set(f,"style",style);set(f,"size",f==Feature.ATMOSPHERE?.4:.6);set(f,"lifetime",f==Feature.TRAILS?.6:f==Feature.ATMOSPHERE?2:1);}
    Config.dirty();
  }
  private static void set(Feature f,String id,double value){for(var s:f.settings)if(s.id.equals(id)){s.set(value);return;}throw new IllegalArgumentException(id);}
}
