package dev.platinum.visuals;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

public final class Effects {
  private final ArrayDeque<Particle> particles = new ArrayDeque<>();
  private final ArrayList<Ring> rings = new ArrayList<>();
  private final Random random = new Random();
  private boolean wasGround, initialized;
  private int lastDead = -1;
  private final int[] cachedColors=new int[Feature.values().length];
  private final float[] cachedSizes=new float[Feature.values().length];
  private final DustParticleOptions[] dust=new DustParticleOptions[Feature.values().length];

  private static final class Ring {
    final Vec3 pos;
    final Feature feature;
    int age;

    Ring(Vec3 pos,Feature feature) {
      this.pos = pos;this.feature=feature;
    }
  }

  public void clear() {
    particles.clear();
    rings.clear();
    wasGround = false;
    initialized = false;
    lastDead = -1;
  }

  public int size() {
    return particles.size();
  }

  public void tick(Minecraft mc) {
    particles.removeIf(p -> !p.isAlive());
    int capacity = Client.BUDGET.capacity();
    while (particles.size() > capacity) particles.removeFirst().remove();
    var player = mc.player;
    for (var it = particles.iterator(); it.hasNext(); ) {
      Particle p = it.next();
      if (p.getPos().distanceToSqr(player.position()) > Client.BUDGET.distanceSquared()) {
        p.remove();
        it.remove();
      }
    }
    if (Feature.JUMP_CIRCLES.enabled
        && initialized
        && wasGround
        && !player.onGround()
        && player.getDeltaMovement().y > .12) addRing(player.position().add(0, .03, 0),Feature.JUMP_CIRCLES);
    wasGround = player.onGround();
    initialized = true;
    if (Feature.TRAILS.enabled && player.getDeltaMovement().horizontalDistanceSqr() > .002) {
      int count = Client.BUDGET.count((int) Feature.TRAILS.value("density"));
      for (int i = 0; i < count; i++)
        spawn(
            player
                .position()
                .add(
                    random.nextGaussian() * .08,
                    .15 + random.nextDouble() * .3,
                    random.nextGaussian() * .08),
            Vec3.ZERO,
            life(Feature.TRAILS),Feature.TRAILS);
    }
    if (Feature.ATMOSPHERE.enabled && Client.ticks % 3 == 0) {
      int count = Client.BUDGET.count((int) Feature.ATMOSPHERE.value("count"));
      for (int i = 0; i < count; i++)
        spawn(
            player
                .position()
                .add(
                    (random.nextDouble()*2-1)*Feature.ATMOSPHERE.value("radius"),
                    random.nextDouble() * 5,
                    (random.nextDouble()*2-1)*Feature.ATMOSPHERE.value("radius")),
            new Vec3(0, .015, 0),
            life(Feature.ATMOSPHERE),Feature.ATMOSPHERE);
    }
    if (Feature.ARROW_TRAILS.enabled && Client.ticks % 2 == 0) {
      double range = Feature.ARROW_TRAILS.value("distance");
      int density = Client.BUDGET.count((int) Feature.ARROW_TRAILS.value("density"));
      for (AbstractArrow arrow : mc.level.getEntitiesOfClass(AbstractArrow.class, player.getBoundingBox().inflate(range))) {
        for (int i = 0; i < density; i++) {
          Vec3 pos = arrow.position().add(random.nextGaussian() * .025, random.nextGaussian() * .025, random.nextGaussian() * .025);
          spawn(pos, Vec3.ZERO, life(Feature.ARROW_TRAILS), Feature.ARROW_TRAILS);
        }
      }
    }
    if (Feature.KILL_EFFECT.enabled
        && Client.target != null
        && Client.target.isDeadOrDying()
        && lastDead != Client.target.getId()) {
      lastDead = Client.target.getId();
      burst(Client.target.position().add(0, 1, 0), 30, .16, Feature.KILL_EFFECT);
    }
    for (var it = rings.iterator(); it.hasNext(); ) {
      Ring r = it.next();
      int lifetime=life(r.feature);
      if (++r.age > lifetime||!r.feature.enabled) {
        it.remove();
        continue;
      }
      if (r.age % 2 != 0) continue;
      double radius = .15 + r.age/(double)lifetime*r.feature.value("radius");
      int count = Client.BUDGET.count(24);
      for (int i = 0; i < count; i++) {
        double a = Math.PI * 2 * i / count;
        spawn(r.pos.add(Math.cos(a) * radius, 0, Math.sin(a) * radius), Vec3.ZERO, 5,r.feature);
      }
    }
  }

  public void attack(LivingEntity entity) {
    Vec3 center = entity.position().add(0, entity.getBbHeight() * .55, 0);
    if (Feature.HIT_PARTICLES.enabled)
      burst(center, (int) Feature.HIT_PARTICLES.value("count"), Feature.HIT_PARTICLES.value("speed"),Feature.HIT_PARTICLES);
    if (Feature.HIT_BUBBLE.enabled) addRing(center,Feature.HIT_BUBBLE);
  }

  private void addRing(Vec3 pos,Feature feature) {
    if (rings.size() >= 8) rings.removeFirst();
    rings.add(new Ring(pos,feature));
  }

  private static int life(Feature f){return Math.max(1,(int)(f.value("lifetime")*20));}
  private void burst(Vec3 pos, int count, double speed,Feature feature) {
    for (int i = 0; i < Client.BUDGET.count(count); i++)
      spawn(
          pos,
          new Vec3(
              random.nextGaussian() * speed,
              random.nextDouble() * speed + .015,
              random.nextGaussian() * speed),
          life(feature),feature);
  }

  private void spawn(Vec3 pos, Vec3 motion, int lifetime,Feature feature) {
    var mc = Minecraft.getInstance();
    if (mc.player == null
        || particles.size() >= Client.BUDGET.capacity()
        || pos.distanceToSqr(mc.player.position()) > Client.BUDGET.distanceSquared()) return;
    int color = (Feature.PARTICLE_COLOR.enabled ? Feature.PARTICLE_COLOR.effectColor() : feature.effectColor()) & 0xffffff, index=feature.ordinal();
    float size=(float)feature.value("size");
    if (dust[index] == null || cachedColors[index] != color||cachedSizes[index]!=size) {
      cachedColors[index] = color;cachedSizes[index]=size;
      dust[index] = new DustParticleOptions(color, size);
    }
    int style=(int)feature.value("style");ParticleOptions options=style==1?ParticleTypes.END_ROD:style==2?ParticleTypes.GLOW:dust[index];
    Particle p =
        mc.particleEngine.createParticle(options, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
    if (p != null && p.isAlive()) {
      if(style>0){p.scale(size);if(p instanceof SingleQuadParticle quad)quad.setColor(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f);if(p instanceof SimpleAnimatedParticle animated)animated.setFadeColor(color);}
      p.setParticleSpeed(motion.x, motion.y, motion.z);
      p.setLifetime(lifetime);
      particles.addLast(p);
    }
  }

  public static void blockOutline(ExtractBlockOutlineRenderStateEvent e) {
    if (!Feature.BLOCK_OVERLAY.enabled && !Feature.BLOCK_BREAK.enabled) return;
    int color = Feature.BLOCK_BREAK.enabled ? Feature.BLOCK_BREAK.effectColor() : Config.accent();
    e.addCustomRenderer(
        (state, buffer, pose, translucent, level) -> {
          Vec3 camera = level.cameraRenderState.pos;
          ShapeRenderer.renderShape(
              pose,
              buffer.getBuffer(RenderTypes.lines()),
              state.shape(),
              state.pos().getX() - camera.x,
              state.pos().getY() - camera.y,
              state.pos().getZ() - camera.z,
              color,
              Feature.BLOCK_BREAK.enabled
                  ? 1 + (float) Feature.BLOCK_BREAK.value("strength") * 3
                  : (float) Feature.BLOCK_OVERLAY.value("width"));
          return true;
        });
  }
}
