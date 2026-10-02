package com.unified_algorithm.unified_algorithm;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Unified Algorithm 配置类。
 * <p>
 * 管理暴击桥接与反双暴击的全部开关，所有配置走 NeoForge 的 ModConfigSpec，
 * 可通过 Mods 界面 > 本模组 > config 在游戏内修改。
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 是否启用暴击桥接（把外部暴击属性加成汇入 Critical Strike 的暴击判定） */
    public static final ModConfigSpec.BooleanValue BRIDGE_ENABLED = BUILDER
            .comment("Enable bridging external crit attributes into Critical Strike's crit system.",
                    "When enabled, bonuses on the attributes listed below are added to Critical Strike's innate crit chance / crit damage.")
            .define("bridgeEnabled", true);

    /**
     * 桥接属性列表，格式：{@code <attribute_id>;<chance|damage>}。
     * 属性值超出其默认值的部分会作为系数（小数）加到 Critical Strike 的
     * 先天暴击概率（chance）或暴击伤害倍率（damage）上。
     * 未安装的模组对应条目会被自动跳过。
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BRIDGE_ATTRIBUTES = BUILDER
            .comment("External attributes bridged into Critical Strike's crit calculation.",
                    "Format: <attribute_id>;<chance|damage>",
                    "The attribute's value above its default is treated as a fraction and added to",
                    "Critical Strike's innate crit chance (chance) or crit damage multiplier (damage).",
                    "Entries for mods that are not installed are skipped automatically.",
                    "Example: apothic_attributes:crit_chance;chance")
            .defineListAllowEmpty("bridgeAttributes", List.of(
                    "apothic_attributes:crit_chance;chance",
                    "apothic_attributes:crit_damage;damage"), () -> "", Config::validateEntry);

    /**
     * 当 Critical Strike 存在时，压制其他模组对玩家攻击的自带暴击判定，
     * 让暴击由 Critical Strike 统一结算（避免双重暴击）。怪物不受影响。
     */
    public static final ModConfigSpec.BooleanValue SUPPRESS_EXTERNAL_PLAYER_CRITS = BUILDER
            .comment("When Critical Strike is present, suppress other mods' own crit rolls on player attacks",
                    "so that crits on players are governed by Critical Strike alone (prevents double crits).",
                    "Mob attacks are not affected.")
            .define("suppressExternalPlayerCrits", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    /**
     * 校验桥接条目格式。
     *
     * @param obj 待校验的对象，应为 {@code <attribute_id>;<chance|damage>} 格式的字符串
     * @return 格式合法返回 true
     */
    private static boolean validateEntry(final Object obj) {
        if (!(obj instanceof String entry)) {
            return false;
        }
        String[] parts = entry.split(";");
        if (parts.length != 2) {
            return false;
        }
        return parts[1].equals("chance") || parts[1].equals("damage");
    }
}
