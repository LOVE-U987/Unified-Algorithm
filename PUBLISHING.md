# Unified Algorithm 发布页描述

> 本文件供 Modrinth / CurseForge 发布时复制粘贴使用，包含两种格式：
> Modrinth 用 **Markdown** 版；CurseForge 用 **BBCode** 版（CurseForge 编辑器不支持 Markdown）。

---

## 一、Modrinth 版（Markdown）

**Unified Algorithm** 让 [Critical Strike](https://modrinth.com/mod/critical-strike) 与
[Apotheosis（神化）](https://modrinth.com/mod/apotheosis) 等暴击模组协同工作：
把外部暴击属性的加成桥接进 Critical Strike 的暴击判定，并压制其他模组的重复暴击，
让 **Critical Strike 成为唯一的暴击结算者**。

### 问题背景

- [Critical Strike](https://modrinth.com/mod/critical-strike) 用自己的属性
  （`critical_strike:chance` / `critical_strike:damage`）驱动暴击；
- [Apotheosis](https://modrinth.com/mod/apotheosis) 的暴击词条写在
  **Apothic Attributes**（`apothic_attributes:crit_chance` / `crit_damage`）上；
- 两套系统互不相认：一起安装时要么**双重暴击**打架，要么暴击词条**白给**。

### 工作原理

1. **暴击桥接**：每个玩家 tick（服务端、幂等）把桥接属性「超出默认值的部分」
   累加进 Critical Strike 的先天暴击修正器。不修改 Critical Strike 任何代码，
   近战 / 远程 / 批量暴击模式全部生效。
2. **反双暴击**：Critical Strike 存在时，压制神化对**玩家攻击**的自带暴击判定；
   **怪物保留**神化暴击。

### 效果示例

| 场景 | 暴击概率 | 暴击伤害 |
|---|---|---|
| 只装 Critical Strike | 5% | 1.5x |
| + 神化词条 +10% / +50% | **15%** | **2.0x** |

加法精确、无双重叠加；卸下词条后自动还原。

### 配置

| 选项 | 默认 | 说明 |
|---|---|---|
| `bridgeEnabled` | `true` | 启用暴击桥接 |
| `bridgeAttributes` | 神化两条 | 桥接属性列表，格式 `<attribute_id>;<chance\|damage>`，可扩展接入其他暴击模组 |
| `suppressExternalPlayerCrits` | `true` | 压制其他模组对玩家攻击的自带暴击判定 |

### 版本要求

| 依赖 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.252+ |
| Critical Strike | 1.0.4+ |
| Apotheosis | 8.9.0+（可选） |

---

## 二、CurseForge 版（BBCode）

[h2]Unified Algorithm[/h2]
让 [url=https://www.curseforge.com/minecraft/mc-mods/critical-strike]Critical Strike[/url] 与 [url=https://www.curseforge.com/minecraft/mc-mods/apotheosis]Apotheosis（神化）[/url] 等暴击模组协同工作：把外部暴击属性的加成桥接进 Critical Strike 的暴击判定，并压制其他模组的重复暴击，让 [b]Critical Strike 成为唯一的暴击结算者[/b]。

[h3]问题背景[/h3]
[list]
[*]Critical Strike 用自己的属性（critical_strike:chance / critical_strike:damage）驱动暴击
[*]Apotheosis 的暴击词条写在 Apothic Attributes（apothic_attributes:crit_chance / crit_damage）上
[*]两套系统互不相认：一起安装时要么[b]双重暴击[/b]打架，要么暴击词条[b]白给[/b]
[/list]

[h3]工作原理[/h3]
[list]
[*][b]暴击桥接[/b]：每个玩家 tick（服务端、幂等）把桥接属性「超出默认值的部分」累加进 Critical Strike 的先天暴击修正器。不修改 Critical Strike 任何代码，近战 / 远程 / 批量暴击模式全部生效
[*][b]反双暴击[/b]：Critical Strike 存在时，压制神化对[b]玩家攻击[/b]的自带暴击判定；[b]怪物保留[/b]神化暴击
[/list]

[h3]效果示例[/h3]
[table]
[tr][th]场景[/th][th]暴击概率[/th][th]暴击伤害[/th][/tr]
[tr][td]只装 Critical Strike[/td][td]5%[/td][td]1.5x[/td][/tr]
[tr][td]+ 神化词条 +10% / +50%[/td][td]15%[/td][td]2.0x[/td][/tr]
[/table]
加法精确、无双重叠加；卸下词条后自动还原。

[h3]配置[/h3]
[list]
[*][b]bridgeEnabled[/b]（默认 true）：启用暴击桥接
[*][b]bridgeAttributes[/b]（默认神化两条）：桥接属性列表，格式 [i]<attribute_id>;<chance|damage>[/i]，可扩展接入其他暴击模组
[*][b]suppressExternalPlayerCrits[/b]（默认 true）：压制其他模组对玩家攻击的自带暴击判定
[/list]

[h3]版本要求[/h3]
[list]
[*]Minecraft 1.21.1
[*]NeoForge 21.1.252+
[*]Critical Strike 1.0.4+
[*]Apotheosis 8.9.0+（可选）
[/list]

[h3]一句话简介（列表摘要，200 字符内）[/h3]
让 Critical Strike 接受神化等模组的暴击加成，统一暴击结算，告别双重暴击与词条白给。

---

## 附：发布信息速查

- **项目名**：Unified Algorithm
- **modId**：`unified_algorithm`
- **版本**：1.0.0（未发布）
- **许可证**：All Rights Reserved
- **简短摘要**：Unifies critical hit chance and damage between Critical Strike, Apotheosis affixes and vanilla attribute bonuses.
- **CurseForge 分类建议**：Adventure and RPG / Magic；辅助类可选 "API and Library"
- **Modrinth 分类建议**：adventure、utility
- **环境标签**：Forge 系（NeoForge），客户端 + 服务端均需安装
