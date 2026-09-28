package dev.platinum.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.*;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.server.level.ParticleStatus;
import net.neoforged.fml.loading.FMLPaths;

/** Only the explicit Apply button changes vanilla settings; the original values survive restarts. */
public final class GraphicsProfile {
  private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
  private record Backup(boolean active,int render,int simulation,int fps,int biome,int weather,
      double entities,boolean shadows,boolean ao,boolean leaves,boolean transparency,boolean vsync,
      CloudStatus clouds,ParticleStatus particles) {
    Backup withActive(boolean value){return new Backup(value,render,simulation,fps,biome,weather,entities,shadows,ao,leaves,transparency,vsync,clouds,particles);}
    boolean valid(){return render>=2&&render<=64&&simulation>=2&&simulation<=64&&fps>=10&&fps<=260&&biome>=0&&biome<=7&&weather>=0&&weather<=16&&Double.isFinite(entities)&&entities>=.5&&entities<=5&&clouds!=null&&particles!=null;}
  }
  private static Path path(){return FMLPaths.CONFIGDIR.get().resolve("platinum-visuals/graphics-backup.json");}
  private static Backup read() throws Exception {
    if(!Files.isRegularFile(path()))return null;
    Backup backup=JSON.fromJson(Files.readString(path()),Backup.class);
    if(backup==null||!backup.valid())throw new IllegalStateException("Некорректная копия настроек графики");
    return backup;
  }
  private static void write(Backup backup) throws Exception {
    Path destination=path();Files.createDirectories(destination.getParent());
    Path temp=Files.createTempFile(destination.getParent(),"graphics-",".tmp");
    try{Files.writeString(temp,JSON.toJson(backup));try{Files.move(temp,destination,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temp,destination,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(temp);}
  }
  private static Backup capture(Options o){return new Backup(true,o.renderDistance().get(),o.simulationDistance().get(),o.framerateLimit().get(),o.biomeBlendRadius().get(),o.weatherRadius().get(),o.entityDistanceScaling().get(),o.entityShadows().get(),o.ambientOcclusion().get(),o.cutoutLeaves().get(),o.improvedTransparency().get(),o.enableVsync().get(),o.cloudStatus().get(),o.particles().get());}
  private static void set(Options o,Backup b){
    o.renderDistance().set(b.render);o.simulationDistance().set(b.simulation);o.framerateLimit().set(b.fps);
    o.biomeBlendRadius().set(b.biome);o.weatherRadius().set(b.weather);o.entityDistanceScaling().set(b.entities);
    o.entityShadows().set(b.shadows);o.ambientOcclusion().set(b.ao);o.cutoutLeaves().set(b.leaves);
    o.improvedTransparency().set(b.transparency);o.enableVsync().set(b.vsync);o.cloudStatus().set(b.clouds);o.particles().set(b.particles);o.save();
  }
  public static String apply(int chunks){
    try{Options o=Minecraft.getInstance().options;Backup previous=read();if(previous==null||!previous.active)write(capture(o));
      set(o,new Backup(true,Math.clamp(chunks,4,16),5,(int)Feature.OPTIMIZATION.value("fps"),0,3,.75,false,false,false,false,false,CloudStatus.OFF,ParticleStatus.DECREASED));
      return "Профиль применён. Исходная графика сохранена.";
    }catch(Exception e){com.mojang.logging.LogUtils.getLogger().warn("Graphics profile failed",e);return "Не удалось применить профиль — см. latest.log";}
  }
  public static String restore(){
    try{Backup backup=read();if(backup==null||!backup.active)return "Нет активной резервной копии";set(Minecraft.getInstance().options,backup);write(backup.withActive(false));return "Прежние настройки графики восстановлены";}
    catch(Exception e){com.mojang.logging.LogUtils.getLogger().warn("Graphics restore failed",e);return "Не удалось восстановить графику — см. latest.log";}
  }
  private GraphicsProfile(){}
}
