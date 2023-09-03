package thunder.hack.injection;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thunder.hack.Thunderhack;
import thunder.hack.events.impl.EventCollision;

@Mixin(BlockCollisionSpliterator.class)
public class MixinBlockCollisionSpliterator {
    @Redirect(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"))
    private VoxelShape computeNextHook(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        EventCollision event = new EventCollision(state.getCollisionShape(world, pos, context),state,pos);
        Thunderhack.EVENT_BUS.post(event);
        return event.getShape();
    }
}