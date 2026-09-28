package dev.platinum.visuals.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.platinum.visuals.Feature;
import dev.platinum.visuals.ItemPhysicsState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemEntityRenderer.class)
public abstract class ItemPhysicsMixin {
  @Unique private final RandomSource platinum$random=RandomSource.create(0);
  @Inject(method="extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V",at=@At("TAIL"))
  private void platinum$state(ItemEntity entity,ItemEntityRenderState state,float partial,CallbackInfo ci){((ItemPhysicsState)state).platinum$setGround(entity.onGround());}
  @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",at=@At("HEAD"),cancellable=true)
  private void platinum$render(ItemEntityRenderState state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera,CallbackInfo ci){
    if(!Feature.ITEM_PHYSICS.enabled||state.item.isEmpty())return;
    boolean ground=((ItemPhysicsState)state).platinum$onGround();var box=state.item.getModelBoundingBox();boolean flat=box.getZsize()<.15;
    pose.pushPose();pose.translate(0,ground?(flat?box.maxZ+.03:-box.minY+.03):.15,0);pose.mulPose(Axis.YP.rotationDegrees(Math.floorMod(state.seed,360)));
    if(ground&&flat)pose.mulPose(Axis.XP.rotationDegrees(90));else if(!ground&&Feature.ITEM_PHYSICS.value("spin")>0)pose.mulPose(Axis.XP.rotation(state.ageInTicks*.15f));
    ItemEntityRenderer.submitMultipleFromCount(pose,collector,state.lightCoords,state,platinum$random);pose.popPose();ci.cancel();
  }
}
