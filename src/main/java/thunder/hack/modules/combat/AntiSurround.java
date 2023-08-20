package thunder.hack.modules.combat;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import thunder.hack.Thunderhack;
import thunder.hack.core.ModuleManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.modules.Module;
import thunder.hack.modules.client.MainSettings;
import thunder.hack.modules.player.SpeedMine;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AntiSurround extends Module {
    public AntiSurround() {
        super("AntiSurround", Category.COMBAT);
    }

    public Setting<Boolean> autodisable = new Setting<>("AutoDisable", true);
    public Setting<Boolean> switchbool = new Setting<>("Switch", true);
    public Setting<Boolean> requirepickaxe = new Setting<>("OnlyPickaxe", true);
    public Setting<Boolean> oldVers = new Setting<>("Old Version", false);
    public Setting<Boolean> rotate = new Setting<>("Rotate", false);
    public Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Strict);

    private BlockPos blockpos = null;

    public static List<BlockPos> blockPosList(BlockPos blockPos) {
        ArrayList<BlockPos> arrayList = new ArrayList<>();
        arrayList.add(blockPos.add(1, 0, 0));
        arrayList.add(blockPos.add(-1, 0, 0));
        arrayList.add(blockPos.add(0, 0, 1));
        arrayList.add(blockPos.add(0, 0, -1));
        return arrayList;
    }

    @Override
    public void onEnable() {
        blockpos = null;
    }

    @EventHandler
    public void onPreMotion(EventSync event) {
        if (!switchbool.getValue() || checkPickaxe()) {
            if (blockpos != null) {
                if (mc.world.getBlockState(blockpos).getBlock().equals(Blocks.AIR)) {
                    if (autodisable.getValue()) {
                        disable(MainSettings.isRu() ? "Сарраунд сломан! Выключаю..." : "Surround has been broken! Turning off...");
                        return;
                    }
                    blockpos = null;
                }
            }

            BlockPos blockpos2 = null;
            for (Entity obj : mc.world.getPlayers().stream().filter(player ->
                    player != mc.player && !Thunderhack.friendManager.isFriend(player) && Float.compare((float) mc.player.squaredDistanceTo(player), 36.0f) < 0).collect(Collectors.toList())) {
                BlockPos pos = BlockPos.ofFloored(obj.getPos());
                if (!checkBlockPos(pos)) continue;

                for (BlockPos pos2 : blockPosList(pos)) {
                    if (!(mc.world.getBlockState(pos2).getBlock() == Blocks.OBSIDIAN)) continue;
                    if (mc.world.getBlockState(pos2.add(0, 1, 0)).isAir() && oldVers.getValue()) continue;

                    double dist = mc.player.squaredDistanceTo(pos2.getX(), pos2.getY(), pos2.getZ());
                    if (dist < 25.0) {
                        blockpos2 = pos2;
                        break;
                    }
                }
            }

            if (blockpos2 != null) {
                SearchInvResult pickaxeResult = InventoryUtility.getPickAxe();
                if (switchbool.getValue())
                    pickaxeResult.switchTo(InventoryUtility.SwitchMode.Normal);


                InteractionUtility.BreakData bData = InteractionUtility.getBreakData(blockpos2, interact.getValue());
                if(bData == null) return;

                if (rotate.getValue()) {
                    float[] rotation = InteractionUtility.calculateAngle(bData.vector());
                    mc.player.setYaw(rotation[0]);
                    mc.player.setPitch(rotation[1]);
                }

                if (!requirepickaxe.getValue() || mc.player.getMainHandStack().getItem() instanceof PickaxeItem) {
                    if (ModuleManager.speedMine.isEnabled() && SpeedMine.progress != 0)
                        return;

                    mc.interactionManager.attackBlock(blockpos2, bData.dir());
                    mc.player.swingHand(Hand.MAIN_HAND);
                    this.blockpos = blockpos2;
                }
            }
        }
    }

    public boolean checkPickaxe() {
        Item item = mc.player.getMainHandStack().getItem();

        return item.equals(Items.DIAMOND_PICKAXE) || item.equals(Items.IRON_PICKAXE) ||
                item.equals(Items.GOLDEN_PICKAXE) || item.equals(Items.STONE_PICKAXE) ||
                item.equals(Items.WOODEN_PICKAXE);
    }


    public boolean checkValidBlock(Block block) {
        return block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.BEDROCK);
    }

    public boolean checkBlockPos(BlockPos blockPos) {
        Block block = mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock();
        Block block2 = mc.world.getBlockState(blockPos.add(0, 0, -1)).getBlock();
        Block block3 = mc.world.getBlockState(blockPos.add(1, 0, 0)).getBlock();
        Block block4 = mc.world.getBlockState(blockPos.add(0, 0, 1)).getBlock();
        Block block5 = mc.world.getBlockState(blockPos.add(-1, 0, 0)).getBlock();
        if (mc.world.isAir(blockPos)) {
            if (mc.world.isAir(blockPos.add(0, 1, 0)) || !oldVers.getValue()) {
                if (checkValidBlock(block)) {
                    if (checkValidBlock(block2)) {
                        if (checkValidBlock(block3)) {
                            if (checkValidBlock(block4)) {
                                return checkValidBlock(block5);
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
