package dev.platinum.visuals;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import net.neoforged.fml.loading.FMLPaths;

public final class Config {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static boolean dirty;
  private static int delay;
  public static int theme;
  public static boolean customAccent,reduceMotion,hudSnap=true;
  public static int customColor=0xffb9a0ff;
  public static float menuScale=1,hudOpacity=.88f;
  public static final Map<String,Float> hudScales=new HashMap<>();
  public static final List<TimerRule> timerRules=new ArrayList<>();
  public static final Map<String,Integer> effectColors=new ConcurrentHashMap<>();
  public static float animationSeconds = .22f, menuOpacity = .97f;
  public static boolean softShadows = true;
  private static volatile boolean retryWrite;
  private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(r -> {
    Thread t = new Thread(r, "Platinum settings writer"); t.setDaemon(true); return t;
  });
  static {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      WRITER.shutdown();
      try { WRITER.awaitTermination(3, TimeUnit.SECONDS); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }, "Platinum settings flush"));
  }
  public static float hudScale = 1;

  public record Position(float x, float y) {}

  public static final Map<String, Position> positions = new HashMap<>();
  public static final String[] THEMES = {"Lavender", "Mint", "Rose", "Ocean"};
  private static final int[] COLORS = {0xffb9a0ff, 0xff71dfc2, 0xffff94bd, 0xff70c8ff};

  public static int color(int theme) { return COLORS[Math.floorMod(theme, 4)]; }

  public static int accent() {
    return customAccent?customColor|0xff000000:COLORS[Math.floorMod(theme,4)];
  }
  public static void accent(int color){customColor=color|0xff000000;customAccent=true;dirty();}
  public static float widgetScale(String id){return hudScales.getOrDefault(id,1f);}

  private static Path root() {
    return FMLPaths.CONFIGDIR.get().resolve("platinum-visuals");
  }

  public static void dirty() {
    dirty = true;
    delay = 30;
  }

  public static void tick() {
    if (retryWrite) { retryWrite = false; dirty = true; delay = 200; }
    if (dirty && --delay <= 0) save();
  }

  public static void load() {
    Path current = root().resolve("settings.json");
    if (Files.isRegularFile(current)) read(current);
    else if (read(FMLPaths.CONFIGDIR.get().resolve("aria-visuals/settings.json"))) dirty();
    else StylePreset.EVENING.apply();
  }

  public static void save() {
    if (!dirty) return;
    String snapshot = snapshot(); Path path = root().resolve("settings.json"); dirty = false;
    WRITER.execute(() -> { if (!write(path, snapshot)) retryWrite = true; });
  }

  public static boolean saveProfile(int slot) {
    return write(root().resolve("profile-" + Math.floorMod(slot, 3) + ".json"), snapshot());
  }

  public static boolean loadProfile(int slot) {
    boolean r = read(root().resolve("profile-" + Math.floorMod(slot, 3) + ".json"));
    if (r) dirty();
    return r;
  }

  private static String snapshot() {
    JsonObject obj = new JsonObject(), mods = new JsonObject();
    obj.addProperty("version", 5);
    obj.add("effectColors",GSON.toJsonTree(effectColors));
    obj.addProperty("customAccent",customAccent);obj.addProperty("customColor",customColor&0xffffff);
    obj.addProperty("menuScale",menuScale);obj.addProperty("hudOpacity",hudOpacity);
    obj.addProperty("reduceMotion",reduceMotion);obj.addProperty("hudSnap",hudSnap);
    obj.add("hudScales",GSON.toJsonTree(hudScales));obj.add("timerRules",GSON.toJsonTree(timerRules));
    obj.addProperty("theme", theme);
    obj.addProperty("hudScale", hudScale);
    obj.addProperty("animationSeconds", animationSeconds);
    obj.addProperty("menuOpacity", menuOpacity);
    obj.addProperty("softShadows", softShadows);
    for (Feature f : Feature.values()) {
      JsonObject m = new JsonObject(), s = new JsonObject();
      m.addProperty("enabled", f.enabled);
      m.addProperty("key", f.key);
      for (Feature.Setting v : f.settings) s.addProperty(v.id, v.value);
      m.add("settings", s);
      mods.add(f.name(), m);
    }
    obj.add("modules", mods);
    obj.add("positions", GSON.toJsonTree(positions));
    return GSON.toJson(obj);
  }

  private static boolean write(Path path, String snapshot) {
    try {
      Files.createDirectories(path.getParent());
      Path temp = Files.createTempFile(path.getParent(), "settings-", ".tmp");
      try {
        Files.writeString(temp, snapshot, StandardCharsets.UTF_8);
        try {
          Files.move(
              temp, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
          Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        }
      } finally {
        Files.deleteIfExists(temp);
      }
      return true;
    } catch (Exception e) {
      LogUtils.getLogger().warn("Could not save Platinum Visuals settings", e);
      return false;
    }
  }

  private static boolean read(Path path) {
    if (!Files.isRegularFile(path)) return false;
    try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
      effectColors.clear();
      JsonObject colors=child(obj,"effectColors");
      for(Feature feature:Feature.values())if(colors.has(feature.name()))effectColors.put(feature.name(),(int)number(colors,feature.name(),0,0,0xffffff));
      theme = (int) number(obj, "theme", theme, 0, 3);
      customAccent=bool(obj,"customAccent",false);customColor=0xff000000|(int)number(obj,"customColor",0xb9a0ff,0,0xffffff);
      menuScale=(float)number(obj,"menuScale",1,.7,1.3);hudOpacity=(float)number(obj,"hudOpacity",.88,.1,1);
      reduceMotion=bool(obj,"reduceMotion",false);hudSnap=bool(obj,"hudSnap",true);
      hudScales.clear();for(String id:child(obj,"hudScales").keySet())hudScales.put(id,(float)number(child(obj,"hudScales"),id,1,.5,2));
      timerRules.clear();
      if(obj.has("timerRules")&&obj.get("timerRules").isJsonArray())for(JsonElement item:obj.getAsJsonArray("timerRules")){
        if(!item.isJsonObject()||timerRules.size()>=32)continue;
        try{JsonObject r=item.getAsJsonObject();timerRules.add(new TimerRule(r.get("item").getAsString(),r.get("name").getAsString(),(int)number(r,"seconds",15,1,3600),bool(r,"matchName",true),r.has("server")?r.get("server").getAsString():""));}catch(RuntimeException ignored){}
      }
      hudScale = (float) number(obj, "hudScale", hudScale, .6, 1.6);
      animationSeconds = (float) number(obj, "animationSeconds", animationSeconds, .12, .4);
      menuOpacity = (float) number(obj, "menuOpacity", menuOpacity, .8, 1);
      if (obj.has("softShadows") && obj.get("softShadows").isJsonPrimitive() && obj.getAsJsonPrimitive("softShadows").isBoolean()) softShadows = obj.get("softShadows").getAsBoolean();
      for (Feature f : Feature.values()) {
        JsonObject m = child(child(obj, "modules"), f.name());
        if (m.has("enabled")
            && m.get("enabled").isJsonPrimitive()
            && m.getAsJsonPrimitive("enabled").isBoolean())
          f.enabled = m.get("enabled").getAsBoolean();
        f.key = (int) number(m, "key", f.key, -1, 348);
        for (Feature.Setting s : f.settings)
          s.set(number(child(m, "settings"), s.id, s.value, s.min, s.max));
      }
      positions.clear();
      for (var entry : child(obj, "positions").entrySet()) {
        if (!entry.getValue().isJsonObject()) continue;
        JsonObject p = entry.getValue().getAsJsonObject();
        positions.put(
            entry.getKey(),
            new Position((float) number(p, "x", 0, 0, 1), (float) number(p, "y", 0, 0, 1)));
      }
      return true;
    } catch (Exception e) {
      LogUtils.getLogger().warn("Could not load Platinum Visuals settings; keeping valid values", e);
      return false;
    }
  }

  private static JsonObject child(JsonObject o, String n) {
    return o.has(n) && o.get(n).isJsonObject() ? o.getAsJsonObject(n) : new JsonObject();
  }
  private static boolean bool(JsonObject o,String n,boolean f){JsonElement v=o.get(n);return v!=null&&v.isJsonPrimitive()&&v.getAsJsonPrimitive().isBoolean()?v.getAsBoolean():f;}

  private static double number(JsonObject o, String n, double fallback, double min, double max) {
    try {
      double v = o.get(n).getAsDouble();
      return Double.isFinite(v) ? Math.clamp(v, min, max) : fallback;
    } catch (RuntimeException e) {
      return fallback;
    }
  }
}
