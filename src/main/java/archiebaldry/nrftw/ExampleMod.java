package archiebaldry.nrftw;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ExampleMod.MOD_ID)
public class ExampleMod
{
    public static final String MOD_ID = "nrftw";

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final float PHANTOM_MEMBRANE_CHANCE = 0.25f;

    public ExampleMod()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getName().equals(EntityType.DROWNED.getDefaultLootTable())) {
            LootPool.Builder poolBuilder = LootPool.lootPool()
                    .add(LootItem.lootTableItem(Items.PHANTOM_MEMBRANE))
                    .when(LootItemKilledByPlayerCondition.killedByPlayer())
                    .when(LootItemRandomChanceCondition.randomChance(PHANTOM_MEMBRANE_CHANCE));

            event.getTable().addPool(poolBuilder.build());
        }
    }

    @SubscribeEvent
    public void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();

        Level world = event.getLevel();

        BlockHitResult hitResult = event.getHitVec();

        if (!world.isClientSide) {
            BlockPos pos = hitResult.getBlockPos();

            BlockState state = world.getBlockState(pos);

            Block block = state.getBlock();

            if (block instanceof BedBlock) {
                player.displayClientMessage(Component.literal("There ain't no rest for the wicked"), true);

                LOGGER.info("{} tried to use a bed but there ain't no rest for the wicked.", player.getName().getString());

                event.setCancellationResult(InteractionResult.SUCCESS);

                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        for (ServerLevel world : event.getServer().getAllLevels()) {
            world.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOINSOMNIA).set(false, event.getServer());

            LOGGER.info("Disabled insomnia in {}.", world);
        }
    }
}
