package dev.platinum.visuals;

import java.util.ArrayList;
import java.util.List;

public enum Feature {
  WATERMARK(0, "Watermark", "FPS, задержка, скорость и координаты", true),
  ARMOR(0, "Armor HUD", "Броня и оставшаяся прочность", true),
  POTIONS(0, "Potions", "Эффекты и время действия", true),
  HOTKEYS(0, "Hotkeys", "Активные модули с горячими клавишами", true),
  INVENTORY(0, "Inventory HUD", "Содержимое основного инвентаря", false),
  TOTEMS(0, "Totem Bar", "Количество тотемов в инвентаре", true),
  SATURATION(0, "Saturation", "Здоровье, голод и известное насыщение", false),
  COOLDOWNS(0, "Cooldowns", "Откаты предметов, известные клиенту", true),
  TARGET_HUD(0, "Target HUD", "Последняя цель: здоровье и дистанция", true),
  LOW_HP(0, "Low HP", "Мягкое предупреждение о низком здоровье", true),
  TNT_TIMER(0, "TNT Timer", "Список таймеров загруженного TNT", false),
  HIT_PARTICLES(0, "Hit Particles", "Цветные частицы у последней цели", true),
  HIT_BUBBLE(0, "Hit Bubble", "Расширяющееся кольцо при ударе", false),
  KILL_EFFECT(0, "Kill Effect", "Частицы смерти последней цели", false),
  TRAILS(0, "Trails", "Короткий след за своим персонажем", false),
  JUMP_CIRCLES(0, "Jump Circles", "Кольцо в точке прыжка", true),
  ATMOSPHERE(0, "World Particles", "Редкие частицы вокруг камеры", false),
  TARGET_GLOW(0, "Target Glow", "Обводка последней цели", false),
  PLAYER_GLOW(0, "Glow Players", "Игроки: цветная обводка всех загруженных игроков", false),
  ITEM_GLOW(0, "Item Highlighter", "Обводка выброшенных предметов", false),
  BLOCK_OVERLAY(0, "Block Overlay", "Цветная рамка выбранного блока", false),
  CUSTOM_HAND(0, "Custom Hand", "Положение, размер и поворот рук", false),
  ZOOM(0, "Zoom", "Плавное приближение: удерживать C", true),
  FREELOOK(0, "Freelook", "Свободный обзор: удерживать Alt", false),
  WORLD_TIME(0, "Time Changer", "Свое время суток для отображения", false),
  SKY(0, "Sky Color", "Небо: цвет, яркость, закат, звёзды, солнце и луна", false),
  CLOUDS(0, "Clouds", "Облака: режим, высота, цвет и прозрачность", false),
  WATER_COLOR(0, "Water Color", "Вода: оттенок поверхности и подводного тумана", false),
  FOG(0, "Fog Color", "Оттенок и дальность обычного тумана", false),
  FULLBRIGHT(0, "Fullbright", "Осветление мира на клиенте", false),
  CLEAR_OVERLAYS(0, "Clear Overlays", "Скрытие огня, воды и удушья", false),
  NO_HURT_SHAKE(0, "No Hurt Shake", "Отключает тряску при уроне", false),
  NO_BOBBING(0,"No View Bobbing","Убирает покачивание камеры при ходьбе",false),
  WEATHER(0,"Weather","Локально скрывает дождь и грозу",false),
  ITEM_PHYSICS(0,"Item Physics","Выброшенные предметы лежат на поверхности",false),
  HAND_ANIMATION(0,"Hand Animation","Плавная анимация удара и взмаха рукой",false),
  MOTION_BLUR(0,"Motion Blur","Мягкий визуальный шлейф движения камеры",false),
  MONITOR_BLUR(0,"Monitor Blur","Размытие мира под меню Platinum",false),
  ANIMATION(0,"Animation","Плавное открытие TAB и переключение хотбара",false),
  FAST_XP(0,"Fast XP","Ускоряет визуальный полёт сфер опыта к игроку",false),
  SCREEN_TINT(0,"Screen Tint","Ненавязчивый цветной оттенок экрана",false),
  LOW_FIRE(0,"Low Fire","Опускает огонь ниже поля зрения",false),
  NAME_TAGS(0,"Name Tags","Чистые именные таблички игроков",false),
  NAME_HEALTH(0,"Name Health","Здоровье игрока в именной табличке",false),
  NAME_DISTANCE(0,"Name Distance","Дистанция до игрока в метрах",false),
  ITEM_NAMES(0,"Item Names","Названия выброшенных предметов",false),
  ITEM_STACKS(0,"Item Stacks","Количество выброшенных предметов",false),
  ARROW_TRAILS(0,"Arrow Trails","След за стрелами и трезубцами",false),
  HIT_MARKER(0,"Hit Marker","Анимированный маркер попадания",false),
  DAMAGE_NUMBERS(0,"Damage Numbers","Последний нанесённый урон",false),
  DAMAGE_TILT(0,"Damage Tilt","Лёгкий наклон интерфейса при попадании",false),
  TOTEM_POP_EFFECT(0,"Totem Pop Effect","Анимация срабатывания тотема",false),
  PARTICLE_COLOR(0,"Particle Color","Единый цвет пользовательских эффектов",false),
  ENCHANT_GLINT(0,"Glint Color","Цветной блеск предметов",false),
  BLOCK_BREAK(0,"Block Break","Мягкая индикация разрушения блока",false),
  ITEM_SHADOW(0,"Item Shadow","Тени под выброшенными предметами",false),
  NO_LIQUID_BOB(0,"No Liquid Bob","Убирает покачивание камеры в воде",false),
  HUD_BLUR(0,"HUD Blur","Мягкая глубина у HUD-панелей",false),
  CPS(1,"CPS","Счётчик кликов в секунду",false),
  FPS_GRAPH(1,"FPS Graph","График плавности кадров",false),
  PING(1,"Ping","Текущая задержка до сервера",false),
  DAMAGE_LOG(1,"Damage Log","Лента последних попаданий",false),
  PERFORMANCE_HUD(0,"Frame Monitor","Время кадра и бюджет эффектов",false),
  ARMOR_ALERT(1,"Armor Notifier","Предупреждение о низкой прочности брони",true),
  CHAT_HELPER(1,"Chat Helper","Скрывает повторные сообщения; без отправки команд",false),
  ANTI_INVIS(1, "Anti Invis", "Показывает загруженных невидимых игроков", true),
  OPTIMIZATION(1, "Optimization", "Адаптивный бюджет и дальность эффектов", true),
  SHULKER_PREVIEW(1, "Shulker Preview", "Содержимое контейнера в подсказке", true),
  STREAMER(1, "Streamer Mode", "Скрывает чат, TAB и scoreboard", false),
  CHAT_COPY(1, "Chat Copy", "Последнее сообщение: Ctrl+C вне чата", false),
  PICKUP_LOG(1, "Pickup Logger", "Лента прироста количества предметов", false),
  TOTEM_TRACKER(1, "Totem Tracker", "Счетчик видимых использований тотема", false)
  ;

  public final int category;
  public final String title, description;
  public final boolean defaultEnabled;
  public volatile boolean enabled;
  public int key = -1;
  public final List<Setting> settings = new ArrayList<>();

  Feature(int category, String title, String description, boolean on) {
    this.category = category;
    this.title = title;
    this.description = description;
    this.enabled = this.defaultEnabled = on;
  }

  static {
    LOW_HP.add("health", "Порог здоровья", 6, 2, 16, 1);
    HIT_PARTICLES.add("count", "Частиц за удар", 12, 2, 40, 1);
    TRAILS.add("density", "Плотность", 2, 1, 5, 1);
    ATMOSPHERE.add("count", "Частиц за такт", 2, 1, 8, 1);
    CUSTOM_HAND.add("x", "Основная рука · X", 0, -2, 2, .01);
    CUSTOM_HAND.add("y", "Основная рука · Y", -.1, -2, 2, .01);
    CUSTOM_HAND.add("z", "Основная рука · Z", 0, -3, 2, .01);
    CUSTOM_HAND.add("scale", "Размер предмета", .9, .1, 2.5, .01);
    CUSTOM_HAND.add("rotation", "Поворот Z", 0, -180, 180, 1);
    CUSTOM_HAND.add("pitch", "Поворот X", 0, -180, 180, 1);
    CUSTOM_HAND.add("yaw", "Поворот Y", 0, -180, 180, 1);
    CUSTOM_HAND.add("offSeparate", "Левую руку отдельно", 0, 0, 1, 1);
    CUSTOM_HAND.add("offX", "Вторая рука · X", 0, -2, 2, .01);
    CUSTOM_HAND.add("offY", "Вторая рука · Y", -.1, -2, 2, .01);
    CUSTOM_HAND.add("offZ", "Вторая рука · Z", 0, -3, 2, .01);
    CUSTOM_HAND.add("offScale", "Вторая рука · размер", .9, .1, 2.5, .01);
    CUSTOM_HAND.add("offPitch", "Вторая рука · поворот X", 0, -180, 180, 1);
    CUSTOM_HAND.add("offYaw", "Вторая рука · поворот Y", 0, -180, 180, 1);
    CUSTOM_HAND.add("offRoll", "Вторая рука · поворот Z", 0, -180, 180, 1);
    CUSTOM_HAND.add("mirror", "Зеркальная рука", 0, 0, 1, 1);
    CUSTOM_HAND.add("swing", "Сила взмаха", 1, 0, 1.5, .05);
    CUSTOM_HAND.add("sway", "Плавное покачивание", .35, 0, 1, .05);
    CUSTOM_HAND.add("speed", "Скорость анимации", 1, .2, 2.5, .05);
    CUSTOM_HAND.add("handStyle", "Стиль анимации", 0, 0, 6, 1);
    HAND_ANIMATION.add("swing", "Сила взмаха", 1, 0, 1.5, .05);
    HAND_ANIMATION.add("speed", "Скорость", 1, .2, 2.5, .05);
    HAND_ANIMATION.add("sway", "Покачивание", .35, 0, 1, .05);
    HAND_ANIMATION.add("handStyle", "Стиль", 0, 0, 6, 1);
    for(Feature f:new Feature[]{CUSTOM_HAND,HAND_ANIMATION}){
      f.add("swingX","Дуга удара · X, °",65,-180,180,1);
      f.add("swingY","Дуга удара · Y, °",35,-180,180,1);
      f.add("swingZ","Дуга удара · Z, °",70,-180,180,1);
      f.add("travel","Вынос руки вперёд",.24,0,1,.01);
      f.add("equip","Анимация смены предмета",1,0,1,1);
      f.add("whileUsing","При еде / натяжении лука",0,0,1,1);
    }
    MOTION_BLUR.add("strength", "Интенсивность", .18, 0, .6, .02);
    MOTION_BLUR.add("decay", "Затухание", .82, .5, .98, .01);
    MOTION_BLUR.add("threshold","Порог движения, °",.1,0,3,.05);
    MOTION_BLUR.add("maxAngle","Сброс при резком повороте, °",75,15,180,1);
    MONITOR_BLUR.add("radius","Радиус размытия, px",6,0,16,1);
    MONITOR_BLUR.add("strength","Сила размытия",.85,0,1,.01);
    MONITOR_BLUR.add("dim","Затемнение фона",.35,0,.8,.01);
    MONITOR_BLUR.add("stretch","Растяжение краёв",.18,0,1,.01);
    ANIMATION.add("tabSpeed","Скорость TAB",14,4,30,1);
    ANIMATION.add("hotbarSpeed","Скорость хотбара",18,4,36,1);
    ANIMATION.add("bounce","Упругость",.22,0,.65,.01);
    FAST_XP.add("range","Дальность притяжения",8,2,24,1);
    FAST_XP.add("speed","Скорость сфер",.42,.1,.9,.01);
    SCREEN_TINT.add("strength", "Сила оттенка", .08, 0, .35, .01);
    SCREEN_TINT.add("hue", "Оттенок ° (−1: тема)",-1,-1,359,1);
    LOW_FIRE.add("height", "Высота огня", .35, 0, .8, .05);
    NAME_TAGS.add("distance", "Дальность", 64, 8, 128, 8);
    NAME_TAGS.add("background", "Фон таблички", 1, 0, 1, 1);
    NAME_HEALTH.add("distance", "Дальность", 64, 8, 128, 8);
    NAME_DISTANCE.add("distance", "Дальность", 64, 8, 128, 8);
    ITEM_NAMES.add("distance", "Дальность", 32, 8, 96, 8);
    ITEM_STACKS.add("distance", "Дальность", 32, 8, 96, 8);
    ARROW_TRAILS.add("density", "Плотность", 2, 1, 6, 1);
    ARROW_TRAILS.add("lifetime", "Время жизни, с", .5, .1, 2, .1);
    ARROW_TRAILS.add("distance", "Дальность", 48, 8, 128, 8);
    HIT_MARKER.add("lifetime", "Время на экране, с", .35, .1, 1.5, .05);
    HIT_MARKER.add("size", "Размер", 8, 4, 20, 1);
    DAMAGE_NUMBERS.add("lifetime", "Время на экране, с", 1.1, .2, 3, .1);
    DAMAGE_NUMBERS.add("scale", "Размер текста", 1, .6, 1.8, .1);
    DAMAGE_TILT.add("strength", "Сила наклона", .35, 0, 1, .05);
    TOTEM_POP_EFFECT.add("lifetime", "Время на экране, с", 1.2, .3, 3, .1);
    PARTICLE_COLOR.add("hue", "Оттенок ° (−1: тема)",-1,-1,359,1);
    ENCHANT_GLINT.add("hue", "Оттенок ° (−1: тема)",-1,-1,359,1);
    BLOCK_BREAK.add("strength", "Сила индикации", .5, 0, 1, .05);
    ITEM_SHADOW.add("strength", "Непрозрачность", .45, 0, 1, .05);
    NO_LIQUID_BOB.add("strength", "Сила подавления", 1, 0, 1, .05);
    HUD_BLUR.add("strength", "Мягкость", .35, 0, 1, .05);
    CPS.add("window", "Окно подсчёта, с", 1, .5, 5, .5);
    CPS.add("limit", "Максимум кликов", 20, 5, 60, 1);
    FPS_GRAPH.add("seconds", "История, с", 5, 2, 10, 1);
    PING.add("compact", "Компактный вид", 1, 0, 1, 1);
    DAMAGE_LOG.add("rows", "Строк", 5, 1, 10, 1);
    ZOOM.add("factor", "Приближение", 3, 1.5, 10, .5);
    WORLD_TIME.add("time", "Время суток", 6000, 0, 23900, 100);
    SKY.add("strength", "Сила цвета", .55, 0, 1, .05);
    SKY.add("brightness", "Яркость неба", 1, .1, 1.5, .05);
    SKY.add("sunset", "Закат и рассвет", 1, 0, 1, 1);
    SKY.add("sun", "Солнце", 1, 0, 1, 1);
    SKY.add("moon", "Луна", 1, 0, 1, 1);
    SKY.add("stars", "Яркость звёзд", 1, 0, 3, .1);
    SKY.add("dayStars", "Звёзды днём", 0, 0, 1, 1);
    CLOUDS.add("cloudMode", "Режим облаков", 1, 0, 2, 1);
    CLOUDS.add("height", "Смещение высоты", 0, -192, 192, 8);
    CLOUDS.add("strength", "Сила цвета", .2, 0, 1, .05);
    CLOUDS.add("opacity", "Прозрачность", 1, .1, 1, .05);
    WATER_COLOR.add("strength", "Сила оттенка", .45, 0, 1, .05);
    WATER_COLOR.add("underwater", "Подводный туман", 1, 0, 1, 1);
    PLAYER_GLOW.add("distance", "Дальность, блоки", 96, 16, 256, 8);
    PLAYER_GLOW.add("glowMode", "Цвет подсветки", 0, 0, 2, 1);
    PLAYER_GLOW.add("self", "Свой игрок в третьем лице", 0, 0, 1, 1);
    PLAYER_GLOW.add("invisible", "Невидимые игроки", 1, 0, 1, 1);
    PLAYER_GLOW.add("team", "Игроки своей команды", 1, 0, 1, 1);
    FOG.add("distance", "Дальность тумана", 96, 16, 256, 8);
    ANTI_INVIS.add("distance", "Дальность списка", 96, 16, 256, 16);
    ANTI_INVIS.key = 86;
    ITEM_GLOW.add("distance", "Дальность обводки", 32, 8, 96, 8);
    OPTIMIZATION.add("fps", "Целевой FPS", 120, 30, 240, 10);
    OPTIMIZATION.add("budget", "Лимит своих частиц", 160, 32, 512, 16);
    OPTIMIZATION.add("distance", "Дальность эффектов", 48, 16, 128, 8);
    OPTIMIZATION.add("adaptive", "Адаптивное качество", 1, 0, 1, 1);
    OPTIMIZATION.add("vanilla", "Новых частиц / тик", 256, 32, 1024, 32);
    OPTIMIZATION.add("hudRate","Обновлений HUD / с",5,2,10,1);
    WATERMARK.add("coords","Координаты",1,0,1,1);WATERMARK.add("speed","Скорость движения",1,0,1,1);
    ARMOR.add("durability","Прочность в процентах",1,0,1,1);ARMOR_ALERT.add("threshold","Порог прочности, %",15,1,50,1);
    POTIONS.add("rows","Максимум эффектов",8,1,20,1);COOLDOWNS.add("rows","Максимум строк",8,1,20,1);
    COOLDOWNS.add("icons","Значки предметов",1,0,1,1);COOLDOWNS.add("bars","Полосы времени",1,0,1,1);COOLDOWNS.add("estimates","Локальные таймеры",1,0,1,1);
    TARGET_HUD.add("timeout","Показывать после удара, с",5,1,20,1);TARGET_HUD.add("portrait","Портрет игрока",1,0,1,1);TARGET_HUD.add("width","Ширина панели",184,150,260,2);
    ANTI_INVIS.add("list","Список невидимых",1,0,1,1);ANTI_INVIS.add("outline","Обводка",1,0,1,1);
    CLEAR_OVERLAYS.add("fire","Огонь",1,0,1,1);CLEAR_OVERLAYS.add("water","Вода",1,0,1,1);CLEAR_OVERLAYS.add("block","Внутри блока",1,0,1,1);
    FOG.add("strength","Сила оттенка",.35,0,1,.05);FOG.add("start","Начало тумана",.25,0,.9,.05);FOG.add("hue","Оттенок ° (−1: тема)",-1,-1,359,1);
    FULLBRIGHT.add("strength","Сила освещения",1,0,1,.05);BLOCK_OVERLAY.add("width","Толщина рамки",2,1,5,.25);
    CHAT_HELPER.add("seconds","Окно повторов, с",10,1,60,1);ITEM_PHYSICS.add("spin","Вращение в воздухе",1,0,1,1);
    TNT_TIMER.add("distance","Дальность",48,8,96,8);TNT_TIMER.add("rows","Максимум TNT",6,1,12,1);
    for(Feature f:new Feature[]{HIT_PARTICLES,HIT_BUBBLE,KILL_EFFECT,TRAILS,JUMP_CIRCLES,ATMOSPHERE}){
      f.add("size","Размер частиц",.7,.2,2,.1);f.add("lifetime","Время жизни, с",f==TRAILS?.7:1,.2,3,.1);f.add("hue","Оттенок ° (−1: тема)",-1,-1,359,1);
      f.add("style","Форма частиц",0,0,2,1);
    }
    HIT_PARTICLES.add("speed","Разлёт",.12,.02,.4,.02);JUMP_CIRCLES.add("radius","Радиус",1.3,.3,3,.1);HIT_BUBBLE.add("radius","Радиус",1.3,.3,3,.1);ATMOSPHERE.add("radius","Область вокруг игрока",8,2,20,1);
    ARROW_TRAILS.add("size","Размер частиц",.55,.1,2,.05);ARROW_TRAILS.add("style","Форма частиц",0,0,2,1);ARROW_TRAILS.add("hue","Оттенок ° (−1: тема)",-1,-1,359,1);
    HIT_PARTICLES.add("vertical","Разлёт вверх",1,0,3,.05);
    KILL_EFFECT.add("count","Количество частиц",30,4,100,1);KILL_EFFECT.add("speed","Разлёт",.16,.02,.7,.01);
    TRAILS.add("height","Высота следа",.3,0,2.5,.05);TRAILS.add("spread","Ширина следа",.08,0,.8,.01);
    ATMOSPHERE.add("height","Высота области",5,1,16,.5);ATMOSPHERE.add("drift","Движение вверх",.015,-.1,.1,.005);ATMOSPHERE.add("interval","Интервал, тиков",3,1,10,1);
    for(Feature f:new Feature[]{JUMP_CIRCLES,HIT_BUBBLE}){f.add("segments","Плотность кольца",24,8,64,1);f.add("expansion","Скорость расширения",1,.25,3,.05);}
    TARGET_GLOW.add("distance","Дальность обводки",96,8,256,8);
    BLOCK_OVERLAY.add("opacity","Непрозрачность рамки",1,.1,1,.05);
    ZOOM.add("smooth","Скорость приближения",15,3,40,1);
    ITEM_PHYSICS.add("spinSpeed","Скорость вращения",1,0,4,.1);ITEM_PHYSICS.add("height","Высота над землёй",.03,0,.3,.01);
    LOW_HP.add("strength","Сила предупреждения",1,0,3,.05);LOW_HP.add("speed","Скорость пульсации",1,.1,3,.1);LOW_HP.add("width","Ширина края",5,1,30,1);
    WATERMARK.add("fps","Показывать FPS",1,0,1,1);WATERMARK.add("ping","Показывать пинг",1,0,1,1);
    TARGET_HUD.add("health","Показывать здоровье",1,0,1,1);TARGET_HUD.add("distance","Показывать дистанцию",1,0,1,1);TARGET_HUD.add("smooth","Плавность полосы HP",12,2,30,1);
    COOLDOWNS.add("width","Ширина панели",202,160,360,2);COOLDOWNS.add("rowHeight","Высота строки",25,22,40,1);
    for(Feature f:new Feature[]{POTIONS,HOTKEYS,TNT_TIMER,SATURATION,PICKUP_LOG,TOTEM_TRACKER,ANTI_INVIS}){f.add("width","Ширина панели",f==SATURATION||f==PICKUP_LOG||f==TOTEM_TRACKER?170:155,130,360,2);f.add("rowHeight","Высота строки",13,12,24,1);f.add("showEmpty","Показывать пустую панель",0,0,1,1);}
    for(Feature f:values())if((f.isHud()&&f!=LOW_HP)||f==PICKUP_LOG||f==TOTEM_TRACKER||f==ANTI_INVIS){f.add("backgroundOpacity","Непрозрачность панели",.88,0,1,.01);f.add("panelShadow","Тень панели",1,0,1,1);f.add("accentLine","Акцентная линия",0,0,1,1);}
  }
  public boolean isHud(){return switch(this){case WATERMARK,ARMOR,POTIONS,HOTKEYS,INVENTORY,TOTEMS,SATURATION,COOLDOWNS,TARGET_HUD,LOW_HP,TNT_TIMER,CPS,FPS_GRAPH,PING,DAMAGE_LOG,PERFORMANCE_HUD->true;default->false;};}
  public int effectColor(){Integer custom=Config.effectColors.get(name());if(custom!=null)return custom|0xff000000;for(Setting s:settings)if(s.id.equals("hue")&&s.value>=0)return ThemeColor.rgb((float)s.value/360,.52f,1);return Config.accent();}
  public boolean hasColorPicker(){return switch(this){case PLAYER_GLOW,TARGET_GLOW,ITEM_GLOW,BLOCK_OVERLAY,ARROW_TRAILS,SKY,CLOUDS,WATER_COLOR,FOG,HIT_PARTICLES,HIT_BUBBLE,KILL_EFFECT,TRAILS,JUMP_CIRCLES,ATMOSPHERE,SCREEN_TINT,PARTICLE_COLOR,ENCHANT_GLINT->true;default->false;};}
  public boolean isEnvironment(){return this==SKY||this==CLOUDS||this==WATER_COLOR||this==FOG||this==WORLD_TIME;}

  private void add(String id, String label, double value, double min, double max, double step) {
    settings.add(new Setting(id, label, value, min, max, step));
  }

  public double value(String id) {
    for (Setting s : settings) if (s.id.equals(id)) return s.value;
    throw new IllegalArgumentException(name() + ":" + id);
  }
  public static Feature byTitle(String title){for(Feature f:values())if(f.title.equals(title))return f;return null;}
  public boolean hasSetting(String id){for(var s:settings)if(s.id.equals(id))return true;return false;}

  public void toggle() {
    enabled = !enabled;
    Config.dirty();
  }

  public static final class Setting {
    public final String id, label;
    public final double initial, min, max, step;
    public volatile double value;
    private double textValue = Double.NaN;
    private String cachedText;

    Setting(String id, String label, double value, double min, double max, double step) {
      this.id = id;
      this.label = label;
      this.value = this.initial = value;
      this.min = min;
      this.max = max;
      this.step = step;
    }

    public void set(double n) {
      if (Double.isFinite(n)) value = Math.clamp(Math.round(n / step) * step, min, max);
    }
    public boolean isBoolean(){return min==0&&max==1&&step==1;}
    public boolean isChoice(){return id.equals("handStyle")||id.equals("style")||id.equals("glowMode")||id.equals("cloudMode");}

    public String text() {
      if (cachedText != null && textValue == value) return cachedText;
      textValue = value;
      return cachedText = id.equals("time")?String.format(java.util.Locale.ROOT,"%02d:%02d",((int)value/1000+6)%24,(int)((value%1000)*60/1000)):
          id.equals("glowMode")?new String[]{"Свой цвет","Здоровье","Команда"}[(int)value]:
          id.equals("cloudMode")?new String[]{"Выкл","Быстро","Объёмные"}[(int)value]:
          id.equals("hue")&&value<0?"Тема":id.equals("style")?new String[]{"Пыль","Искры","Светлячки"}[(int)value]:id.equals("handStyle")?new String[]{"Взмах","Плавная дуга","Классика","Без анимации","Вращение","Укол","Своя траектория"}[(int)value]:max == 1 && min == 0 && step == 1
          ? (value > 0 ? "Вкл" : "Выкл")
          : step >= 1
              ? Integer.toString((int) value)
              : String.format(java.util.Locale.ROOT, "%.2f", value);
    }
  }
}
