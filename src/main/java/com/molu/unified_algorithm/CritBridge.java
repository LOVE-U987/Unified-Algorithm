package com.molu.unified_algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.critical_strike.CriticalStrikeMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 暴击桥接核心。
 * <p>
 * Critical Strike 的暴击完全由它自己的两个属性驱动：
 * <ul>
 *   <li>{@code critical_strike:chance}（基础 100，概率 = (值-100)/100，即每 +1 为 +1% 暴击概率）</li>
 *   <li>{@code critical_strike:damage}（基础 100，伤害倍率 = 值/100）</li>
 * </ul>
 * 这两个属性在玩家身上各有一个先天持久修正器（ID {@code critical_strike:innate_bonus}，
 * 操作类型 ADD_MULTIPLIED_BASE，默认暴击概率 +0.05、暴击伤害 +0.5）。
 * <p>
 * 本类在每个玩家 tick 时把外部暴击属性（如神化的 {@code apothic_attributes:crit_chance}）
 * 超出其默认值的部分，累加进上述先天修正器：
 * {@code 先天修正 = CS自身先天值 + Σ(外部属性值 - 外部属性默认值)}。
 * 由于 CS 的概率/倍率计算为 value = base × (1 + 先天值)，该加法恰好等于
 * 「CS 先天 + 外部加成」的精确加法语义，无需修改 CS 任何代码，
 * 近战、远程（投射物 shooter 判定）、批量模式全部自然生效。
 */
public final class CritBridge {

    /** CS 暴击概率属性 ID */
    private static final ResourceLocation CS_CHANCE_ID = ResourceLocation.fromNamespaceAndPath("critical_strike", "chance");
    /** CS 暴击伤害属性 ID */
    private static final ResourceLocation CS_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath("critical_strike", "damage");
    /** CS 先天修正器 ID（两个属性共用同一 ID，各自实例互不影响） */
    private static final ResourceLocation INNATE_BONUS_ID = ResourceLocation.fromNamespaceAndPath("critical_strike", "innate_bonus");

    /** 桥接贡献类型：暴击概率 or 暴击伤害 */
    private enum Kind { CHANCE, DAMAGE }

    /**
     * 已解析的桥接条目。
     *
     * @param attribute 外部属性引用（用于读取玩家当前值）
     * @param kind 贡献类型（chance / damage）
     * @param defaultValue 外部属性的默认值，贡献 = 当前值 - 默认值
     */
    private record BridgeEntry(Holder<Attribute> attribute, Kind kind, double defaultValue) {}

    /** 已解析的桥接条目缓存 */
    private static List<BridgeEntry> bridgeEntries = List.of();
    /** 缓存是否已解析（配置重载后需重新解析） */
    private static boolean resolved = false;

    private CritBridge() {}

    /**
     * 玩家 tick 回调（仅服务端执行）：同步外部暴击属性贡献到 CS 的先天修正器。
     * 幂等：贡献无变化时不做任何写入；桥接关闭时会把先天修正器还原为 CS 配置值。
     *
     * @param event 玩家 tick 事件（Post 阶段）
     */
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        if (!ModList.get().isLoaded("critical_strike")) {
            return;
        }
        ensureResolved();

        double extChance = 0.0;
        double extDamage = 0.0;
        if (Config.BRIDGE_ENABLED.get()) {
            for (BridgeEntry entry : bridgeEntries) {
                double contribution = player.getAttributeValue(entry.attribute()) - entry.defaultValue();
                if (contribution == 0.0) {
                    continue;
                }
                if (entry.kind() == Kind.CHANCE) {
                    extChance += contribution;
                } else {
                    extDamage += contribution;
                }
            }
        }

        applyInnate(player, CS_CHANCE_ID, csInnateChance() + extChance);
        applyInnate(player, CS_DAMAGE_ID, csInnateDamage() + extDamage);
    }

    /**
     * 配置重载回调：使桥接条目缓存失效，下次 tick 时重新解析。
     *
     * @param event 配置重载事件
     */
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        resolved = false;
    }

    /**
     * 惰性解析配置中的桥接条目为属性引用（属性注册表在服务端启动后稳定，首次 tick 时解析是安全的）。
     * 无法解析的条目（未安装的模组、拼错的 ID）记一次警告后跳过。
     */
    private static void ensureResolved() {
        if (resolved) {
            return;
        }
        List<BridgeEntry> entries = new ArrayList<>();
        for (String raw : Config.BRIDGE_ATTRIBUTES.get()) {
            String[] parts = raw.split(";");
            if (parts.length != 2) {
                UnifiedAlgorithm.LOGGER.warn("[Unified Algorithm] 忽略格式非法的桥接条目: {}", raw);
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(parts[0]);
            if (id == null) {
                UnifiedAlgorithm.LOGGER.warn("[Unified Algorithm] 忽略非法的属性 ID: {}", raw);
                continue;
            }
            Optional<Holder.Reference<Attribute>> holder = BuiltInRegistries.ATTRIBUTE.getHolder(id);
            if (holder.isEmpty()) {
                UnifiedAlgorithm.LOGGER.warn("[Unified Algorithm] 属性 {} 不存在（对应模组可能未安装），已跳过", id);
                continue;
            }
            Attribute attribute = holder.get().value();
            entries.add(new BridgeEntry(holder.get(), "damage".equals(parts[1]) ? Kind.DAMAGE : Kind.CHANCE, attribute.getDefaultValue()));
        }
        bridgeEntries = List.copyOf(entries);
        resolved = true;
        if (!bridgeEntries.isEmpty()) {
            UnifiedAlgorithm.LOGGER.info("[Unified Algorithm] 已解析 {} 条暴击桥接条目", bridgeEntries.size());
        }
    }

    /**
     * 读取 CS 配置中的暴击概率先天加成。
     *
     * @return CS 的 attribute_crit_chance_innate_bonus 配置值
     */
    private static double csInnateChance() {
        return CriticalStrikeMod.config.value.attribute_crit_chance_innate_bonus;
    }

    /**
     * 读取 CS 配置中的暴击伤害先天加成。
     *
     * @return CS 的 attribute_crit_damage_innate_bonus 配置值
     */
    private static double csInnateDamage() {
        return CriticalStrikeMod.config.value.attribute_crit_damage_innate_bonus;
    }

    /**
     * 把总先天加成写入 CS 属性实例（幂等：当前值与目标一致时不做任何操作）。
     *
     * @param player 目标玩家
     * @param csAttrId CS 属性 ID（critical_strike:chance / critical_strike:damage）
     * @param total 目标先天加成（CS 自身先天值 + 外部贡献）
     */
    private static void applyInnate(Player player, ResourceLocation csAttrId, double total) {
        Optional<Holder.Reference<Attribute>> holder = BuiltInRegistries.ATTRIBUTE.getHolder(csAttrId);
        if (holder.isEmpty()) {
            return;
        }
        AttributeInstance instance = player.getAttribute(holder.get());
        if (instance == null) {
            return;
        }
        AttributeModifier current = instance.getModifier(INNATE_BONUS_ID);
        if (current != null && Math.abs(current.amount() - total) < 1.0E-9) {
            return;
        }
        // addOrReplacePermanentModifier 会按 ID 原子替换已有修正器
        instance.addOrReplacePermanentModifier(new AttributeModifier(INNATE_BONUS_ID, total, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }
}
