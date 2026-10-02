package com.molu.unified_algorithm.mixin;

import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.molu.unified_algorithm.Config;

/**
 * 反双暴击 mixin。
 * <p>
 * 目标类是 Apothic Attributes（神化）的 {@code dev.shadowsoffire.apothic_attributes.impl.AttributeEvents}——
 * 一个普通事件处理类（非 mixin 类），直接注入是安全的。
 * <p>
 * 神化会通过 {@code apothCriticalStrike}（LivingIncomingDamageEvent）用
 * {@code apothic_attributes:crit_chance} 自行掷暴击；当 Critical Strike 也存在时，
 * 两套系统会对同一次攻击双重触发。本 mixin 在玩家发起攻击时取消神化的自掷暴击，
 * 让暴击由 Critical Strike 统一结算（外部加成已由 CritBridge 桥接进 CS 的属性）。
 * 怪物攻击不受影响，保留神化对怪物的暴击行为。
 * <p>
 * Apothic Attributes 未安装时，目标类不会被加载，本 mixin 自然不会生效。
 */
@Mixin(targets = "dev.shadowsoffire.apothic_attributes.impl.AttributeEvents")
public abstract class SuppressExternalCritsMixin {

    /**
     * 压制神化对玩家攻击的自掷暴击。
     *
     * @param event 伤害接收事件，attacker 为 damage source 的来源实体
     * @param ci 注入回调，cancel 后神化的暴击逻辑整体跳过
     */
    @Inject(method = "apothCriticalStrike", at = @At("HEAD"), cancellable = true, require = 1)
    private void unified$suppressPlayerCrit(LivingIncomingDamageEvent event, CallbackInfo ci) {
        if (!Config.SUPPRESS_EXTERNAL_PLAYER_CRITS.get()) {
            return;
        }
        if (!ModList.get().isLoaded("critical_strike")) {
            return;
        }
        // 仅压制玩家发起的攻击；怪物保留神化暴击。
        // 投射物由玩家射出时 source.getEntity() 也是玩家，与 CS 的远程判定口径一致。
        if (event.getSource().getEntity() instanceof Player) {
            ci.cancel();
        }
    }

    /**
     * 压制神化对原版跳劈暴击的暴击伤害覆盖（CriticalHitEvent）。
     * <p>
     * Critical Strike 默认通过 disable_vanilla_jump_criticals 禁用了原版跳劈暴击，
     * 此事件本不会触发；保留此压制是为了在用户改回启用跳劈暴击时，
     * 避免神化的暴击伤害与 Critical Strike 的暴击倍率双重叠加。
     *
     * @param event 原版暴击事件
     * @param ci 注入回调，cancel 后神化的覆盖逻辑跳过
     */
    @Inject(method = "vanillaCritDmg", at = @At("HEAD"), cancellable = true, require = 1)
    private void unified$suppressVanillaCritDmg(CriticalHitEvent event, CallbackInfo ci) {
        if (!Config.SUPPRESS_EXTERNAL_PLAYER_CRITS.get()) {
            return;
        }
        if (!ModList.get().isLoaded("critical_strike")) {
            return;
        }
        ci.cancel();
    }
}
