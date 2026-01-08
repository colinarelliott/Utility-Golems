package rehdpanda.utilitygolems;

import net.minecraft.entity.ai.goal.FollowMobGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;

public class GolemAI {
    public static void initLapisGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(
                Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE, Items.GOLDEN_PICKAXE
        ), false));
        golem.getGoalSelector().add(2, new LookAtEntityGoal(golem, PlayerEntity.class, 8.0F));
    }

    public static void initRedstoneGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new LookAtEntityGoal(golem, PlayerEntity.class, 8.0F));
        golem.getGoalSelector().add(2, new FollowMobGoal(golem, 1.0D, 3.0F, 7.0F));
    }

    public static void initEmeraldGoals(UtilityGolem golem) {
        golem.getGoalSelector().add(1, new TemptGoal(golem, 1.2D, Ingredient.ofItems(Items.EMERALD), false));
        golem.getGoalSelector().add(2, new LookAtEntityGoal(golem, VillagerEntity.class, 8.0F));
        golem.getGoalSelector().add(3, new FollowMobGoal(golem, 1.0D, 3.0F, 10.0F));
    }
}
