package dev.platinum.visuals;

import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.world.level.dimension.DimensionType;

/** Exercises actual render-state edits without initializing Minecraft or an OpenGL context. */
public final class WorldVisualChecks {
  private static int assertions;
  private static void require(boolean condition,String message){assertions++;if(!condition)throw new AssertionError(message);}
  private static void set(Feature feature,String id,double value){for(var setting:feature.settings)if(setting.id.equals(id)){setting.set(value);return;}throw new AssertionError("Missing setting: "+id);}
  private static SkyRenderState sky(){SkyRenderState state=new SkyRenderState();state.skybox=DimensionType.Skybox.OVERWORLD;state.skyColor=0x204060;state.sunriseAndSunsetColor=0x80ff8000;state.starBrightness=.4f;return state;}
  public static void main(String[] args){
    require(VisualColor.tint(0x80102030,0xffa0b0c0,0)==0x80102030,"zero strength preserves the source and its alpha");
    require(VisualColor.tint(0x80102030,0xffa0b0c0,1)==0x80a0b0c0,"full strength changes RGB, not alpha");
    require(VisualColor.tint(0xff000000,0x00ffffff,.5)==0xff808080,"midpoint tint");
    require(VisualColor.tint(0x112233,0xffffff,Double.NaN)==0x112233,"invalid strength cannot poison colour");
    require(VisualColor.brightness(0x80ffffff,2)==0x80ffffff,"brightness saturation preserves alpha");
    require(VisualColor.brightness(0xff808080,.5)==0xff404040,"brightness scales channels");
    require((VisualColor.health(20,20)&0xffffff)==0x40ff40,"full health is green");
    require((VisualColor.health(0,20)&0xffffff)==0xff4040,"zero health is red");
    require(VisualColor.health(Float.NaN,0)==VisualColor.health(0,20),"invalid health is bounded");

    SkyRenderState disabled=sky();Feature.SKY.enabled=false;EnvironmentVisuals.sky(disabled);
    require(disabled.skyColor==0x204060&&disabled.starBrightness==.4f,"disabled module is a passthrough");
    Feature.SKY.enabled=true;Config.effectColors.put(Feature.SKY.name(),0xabcdef);set(Feature.SKY,"strength",1);set(Feature.SKY,"brightness",1);set(Feature.SKY,"stars",1);
    SkyRenderState tinted=sky();EnvironmentVisuals.sky(tinted);
    require((tinted.skyColor&0xffffff)==0xabcdef,"sky uses its own selected colour");
    require(tinted.sunriseAndSunsetColor>>>24==128,"sunset tint preserves visibility alpha");
    set(Feature.SKY,"sunset",0);SkyRenderState noSunset=sky();EnvironmentVisuals.sky(noSunset);require(noSunset.sunriseAndSunsetColor==0,"sunset toggle");
    set(Feature.SKY,"stars",3);set(Feature.SKY,"dayStars",1);SkyRenderState stars=sky();stars.starBrightness=0;EnvironmentVisuals.sky(stars);require(stars.starBrightness==1,"day stars and intensity stay bounded");
    set(Feature.SKY,"stars",0);SkyRenderState noStars=sky();EnvironmentVisuals.sky(noStars);require(noStars.starBrightness==0,"zero star intensity hides stars");
    for(var dimension:new DimensionType.Skybox[]{DimensionType.Skybox.NONE,DimensionType.Skybox.END}){SkyRenderState other=sky();other.skybox=dimension;EnvironmentVisuals.sky(other);require(other.skyColor==0x204060&&other.starBrightness==.4f,"no fabricated Overworld sky in "+dimension);}

    Feature.WATER_COLOR.enabled=false;require(EnvironmentVisuals.water(0x2070bb)==0x2070bb,"disabled water preserves biome colour");
    Feature.WATER_COLOR.enabled=true;Config.effectColors.put(Feature.WATER_COLOR.name(),0x45adfa);set(Feature.WATER_COLOR,"strength",1);require(EnvironmentVisuals.water(0x2070bb)==0x45adfa,"surface water tint");
    Feature.CLOUDS.enabled=false;require(EnvironmentVisuals.clouds(0x80ffffff)==0x80ffffff,"disabled clouds preserve alpha");
    Feature.CLOUDS.enabled=true;Config.effectColors.put(Feature.CLOUDS.name(),0x778899);set(Feature.CLOUDS,"strength",1);set(Feature.CLOUDS,"opacity",.25);require(EnvironmentVisuals.clouds(0x80ffffff)==0x20778899,"clouds combine custom colour and original alpha");
    Config.effectColors.remove(Feature.SKY.name());require(Feature.SKY.effectColor()==Config.accent(),"no custom colour follows interface accent");
    set(Feature.WATER_COLOR,"strength",.45);require(Feature.WATER_COLOR.settings.getFirst().text().equals("0.45"),"strength is numeric, not a boolean label");
    set(Feature.WORLD_TIME,"time",18000);require(Feature.WORLD_TIME.settings.getFirst().text().equals("00:00"),"visual time label at midnight");
    set(Feature.WORLD_TIME,"time",6000);require(Feature.WORLD_TIME.settings.getFirst().text().equals("12:00"),"visual time label at noon");
    System.out.println("WorldVisualChecks: "+assertions+" render-state and colour assertions passed");
  }
}
