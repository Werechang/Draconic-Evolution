package com.brandon3055.draconicevolution.items.equipment;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.utils.EnergyUtils;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.capability.PropertyProvider;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.api.modules.data.AOEData;
import com.brandon3055.draconicevolution.api.modules.data.SpeedData;
import com.brandon3055.draconicevolution.init.EquipCfg;
import com.brandon3055.draconicevolution.items.tools.old.IAOEWeapon;
import com.brandon3055.draconicevolution.utils.LogHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

import static com.brandon3055.draconicevolution.api.capability.DECapabilities.MODULE_HOST_CAPABILITY;

/**
 * Created by brandon3055 on 5/7/20
 */
public interface IModularMelee extends IModularTieredItem {

    @Override
    default boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity target) {
        ModuleHost host = stack.getCapability(MODULE_HOST_CAPABILITY).orElseThrow(IllegalStateException::new);
        float damage = (float) getAttackDamage(host, stack);
        long energyPerHit = (long) (EquipCfg.energyAttack * damage);
        extractEnergy(player, stack, energyPerHit);

        double aoe = host.getModuleData(ModuleTypes.AOE, new AOEData(0)).getAOE() * 1.5;
        if (host instanceof PropertyProvider && ((PropertyProvider) host).hasDecimal("attack_aoe")) {
            aoe = ((PropertyProvider) host).getDecimal("attack_aoe").getValue();
        }

        float attackStrength = player.getAttackStrengthScale(0.5F);
        if (aoe > 0 && attackStrength > 0.9) {
            damage = damage * (0.2F + (attackStrength * attackStrength * 0.8F));
            dealAOEDamage(player, target, stack, energyPerHit, damage, aoe);
        }
        return false;
    }

    default void dealAOEDamage(PlayerEntity player, Entity target, ItemStack stack, long energyPerHit, float damage, double aoe) {
        List<LivingEntity> entities = player.level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(aoe, 0.25D, aoe));
        double aoeAngle = 100;
        double yaw = player.yRot - 180;
        int fireAspect = EnchantmentHelper.getFireAspect(player);

        for (LivingEntity entity : entities) {
            if (getEnergyStored(stack) < energyPerHit && !player.abilities.instabuild) break;
            float distance = player.distanceTo(entity);
            if (entity == player || entity == target || player.isAlliedTo(entity) || distance < 1 || entity.distanceTo(target) > aoe) continue;
            double angle = Math.atan2(player.getX() - entity.getX(), player.getZ() - entity.getZ()) * MathHelper.todeg;
            double relativeAngle = Math.abs((angle + yaw) % 360);
            if (relativeAngle <= aoeAngle / 2 || relativeAngle > 360 - (aoeAngle / 2)) {
                boolean lit = false;
                float health = entity.getHealth();
                if (fireAspect > 0 && !entity.isOnFire()) {
                    lit = true;
                    entity.setSecondsOnFire(1);
                }

                if (entity.hurt(DamageSource.playerAttack(player), damage)) {
                    float damageDealt = health - entity.getHealth();
                    entity.knockback(0.4F, MathHelper.sin(player.yRot * MathHelper.torad), (-MathHelper.cos(player.yRot * MathHelper.torad)));

                    if (fireAspect > 0) {
                        entity.setSecondsOnFire(fireAspect * 4);
                    }

                    if (player.level instanceof ServerWorld && damageDealt > 2.0F) {
                        int k = (int)((double)damage * 0.5D);
                        ((ServerWorld)player.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getY(0.5D), entity.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                    }

                    player.awardStat(Stats.DAMAGE_DEALT, Math.round(damageDealt * 10.0F));
                    if (player.level instanceof ServerWorld && damageDealt > 2.0F) {
                        int k = (int) ((double) damageDealt * 0.5D);
                        ((ServerWorld) player.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getY(0.5D), entity.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                    }
                } else if (lit) {
                    entity.clearFire();
                }
            }
            extractEnergy(player, stack, energyPerHit);
        }
    }

}
