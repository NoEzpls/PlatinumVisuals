package dev.platinum.visuals.mixin;
import dev.platinum.visuals.ItemPhysicsState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(ItemEntityRenderState.class)
public abstract class ItemStateMixin implements ItemPhysicsState {
  @Unique private boolean platinum$ground;
  public boolean platinum$onGround(){return platinum$ground;}
  public void platinum$setGround(boolean value){platinum$ground=value;}
}
