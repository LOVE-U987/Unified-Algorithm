<div align="center">

# Unified Algorithm

**统一暴击 —— 让 Critical Strike 与神化 (Apotheosis) 及其他暴击相关模组协同工作。**

[中文](#中文) | [English](#english)

</div>

---

<a id="中文"></a>

## 这是什么？

**Unified Algorithm** 是一个 Minecraft 1.21.1（NeoForge）兼容模组，解决
[Critical Strike](https://modrinth.com/mod/critical-strike) 与
[Apotheosis（神化）](https://modrinth.com/mod/apotheosis) 等暴击模组之间
**暴击概率 / 暴击伤害不互通**的问题。

Critical Strike 用它自己的两个属性驱动暴击；神化等模组则把暴击加成写在**各自的属性**上。
两套系统互不相认：一起安装时要么双重暴击打架，要么暴击词条白给。

本模组的方案是**让 Critical Strike 接受标准属性系统的加成**：
把外部暴击属性桥接进 Critical Strike 的暴击判定，同时压制其他模组的重复暴击，
让 **Critical Strike 成为唯一的暴击结算者**——它的粒子、音效、附魔与配置全部保留。

## 工作原理

1. **暴击桥接（CritBridge）**——每个玩家 tick（仅服务端、幂等写入）：
   把桥接列表中外部属性「超出其默认值的部分」累加进 Critical Strike 的先天暴击修正器
   （`critical_strike:innate_bonus`）。不修改 Critical Strike 任何代码，
   近战、远程（投射物持有者判定）与批量暴击模式全部自然生效。
2. **反双暴击（Mixin）**——当 Critical Strike 存在时，压制神化（Apothic Attributes）
   对**玩家攻击**的自带暴击判定，避免同一刀双重暴击；**怪物保留**神化的暴击行为。
3. **安全降级**——Critical Strike 未安装时桥接自动休眠；
   神化未安装时反双暴击 mixin 自然不生效。

## 数学效果示例

| 场景 | 暴击概率 | 暴击伤害 |
|---|---|---|
| 只装 Critical Strike（默认） | 5% | 1.5x |
| + 神化词条 +10% 概率 / +50% 伤害 | **15%** | **2.0x** |

加法语义精确（无双重叠加）：外部词条卸下后，下一个游戏 tick 自动还原 Critical Strike 原生数值。

## 兼容性

| 模组 | 状态 |
|---|---|
| [Critical Strike](https://modrinth.com/mod/critical-strike) | ✅ 主要目标（暴击结算者） |
| [Apotheosis](https://modrinth.com/mod/apotheosis) 8.9+ / Apothic Attributes 2.10+ | ✅ 默认桥接 + 反双暴击 |
| 其他暴击模组 | 🔧 把它们的暴击属性加进配置即可桥接 |

## 配置

游戏内：**Mods 界面 > Unified Algorithm > Config**，或直接编辑 `config/unified_algorithm-common.toml`。

| 选项 | 默认值 | 说明 |
|---|---|---|
| `bridgeEnabled` | `true` | 是否启用暴击桥接 |
| `bridgeAttributes` | 神化两条 | 桥接属性列表，格式 `<attribute_id>;<chance\|damage>`；属性值超出默认值的部分按小数累加进 CS 的先天暴击概率 / 伤害倍率。未安装模组对应条目自动跳过 |
| `suppressExternalPlayerCrits` | `true` | Critical Strike 存在时，压制其他模组对玩家攻击的自带暴击判定（怪物不受影响） |

扩展示例：给其他暴击模组接桥，只需在 `bridgeAttributes` 添加一行，例如：

```toml
[[bridgeAttributes]]
    "某模组:crit_chance;chance"
```

## 版本要求

| 依赖 | 版本 | 必需 |
|---|---|---|
| Minecraft | 1.21.1 | ✅ |
| NeoForge | 21.1.252+ | ✅ |
| Critical Strike | 1.0.4+ | 推荐（未装则本模组休眠） |
| Apotheosis | 8.9.0+ | 可选 |
| Apothic Attributes | 2.10.0+ | 可选（随神化提供） |

## 构建

需要 JDK 21。

```bash
./gradlew build      # 构建 jar（输出到 build/libs/）
./gradlew runClient  # 启动开发客户端（已配置 CS + 神化运行时依赖）
```

## 许可证

[MIT](LICENSE)（Copyright (c) 2026 LOVE_U987；`TEMPLATE_LICENSE.txt` 为 NeoForged MDK 模板自身的许可，适用于模板来源文件）。

---

<a id="english"></a>

## What is this?

**Unified Algorithm** is a Minecraft 1.21.1 (NeoForge) compatibility mod that fixes
**critical hit chance / damage incompatibility** between
[Critical Strike](https://modrinth.com/mod/critical-strike) and
[Apotheosis](https://modrinth.com/mod/apotheosis) (or any other crit-related mod).

Critical Strike drives crits with its own two attributes, while other mods put crit
bonuses on **their own** attributes — the systems ignore each other: installed together,
they either double-dip into crits or the affixes do nothing at all.

This mod makes **Critical Strike accept standard attribute-system bonuses**: external
crit attributes are bridged into Critical Strike's crit calculation, while duplicate
crit rolls from other mods are suppressed — so **Critical Strike remains the single
crit resolver**, keeping its particles, sounds, enchantments and config intact.

## How it works

1. **Crit bridge (CritBridge)** — per-player tick (server-side, idempotent): the part of
   each bridged attribute above its default value is added into Critical Strike's innate
   crit modifier (`critical_strike:innate_bonus`). No Critical Strike code is touched;
   melee, ranged (projectile owner) and batched crit modes all work naturally.
2. **Anti-double-crit (Mixin)** — when Critical Strike is present, Apothic Attributes'
   own crit rolls on **player attacks** are suppressed to prevent double crits;
   **mob crits keep** their Apotheosis behavior.
3. **Safe degradation** — without Critical Strike the bridge sleeps; without
   Apothic Attributes the anti-double-crit mixin simply never applies.

## Math example

| Scenario | Crit chance | Crit damage |
|---|---|---|
| Critical Strike only (defaults) | 5% | 1.5x |
| + Apotheosis affixes +10% chance / +50% damage | **15%** | **2.0x** |

Exact additive semantics (no double-dipping): removing the affixes restores Critical
Strike's native values on the next game tick.

## Configuration

In game: **Mods screen > Unified Algorithm > Config**, or edit
`config/unified_algorithm-common.toml`.

| Option | Default | Description |
|---|---|---|
| `bridgeEnabled` | `true` | Enable the crit bridge |
| `bridgeAttributes` | Apotheosis entries | Bridged attributes, format `<attribute_id>;<chance\|damage>`; the part above the attribute's default is added as a fraction to CS's innate crit chance / damage. Entries for missing mods are skipped |
| `suppressExternalPlayerCrits` | `true` | When Critical Strike is present, suppress other mods' own crit rolls on player attacks (mobs unaffected) |

## Requirements

| Dependency | Version | Required |
|---|---|---|
| Minecraft | 1.21.1 | ✅ |
| NeoForge | 21.1.252+ | ✅ |
| Critical Strike | 1.0.4+ | Recommended (mod sleeps without it) |
| Apotheosis | 8.9.0+ | Optional |
| Apothic Attributes | 2.10.0+ | Optional (comes with Apotheosis) |

## Building

Requires JDK 21.

```bash
./gradlew build      # Build the jar (outputs to build/libs/)
./gradlew runClient  # Launch a dev client (CS + Apotheosis runtime deps configured)
```

## License

[MIT](LICENSE) (Copyright (c) 2026 LOVE_U987; `TEMPLATE_LICENSE.txt` is the NeoForged MDK template's own license, applying to the template files).
