# Changelog - Unified Algorithm

## v1.0.0 (2026-10-02，未发布)

### 暴击兼容实现（路线 B：Critical Strike 接受标准属性系统加成）

- **CritBridge 暴击桥接**：每个玩家 tick（服务端）把外部暴击属性超出默认值的部分，
  累加进 CS 的先天修正器（`critical_strike:innate_bonus`，ADD_MULTIPLIED_BASE）：
  - `apothic_attributes:crit_chance` → CS 暴击概率（小数加法，精确语义）
  - `apothic_attributes:crit_damage` → CS 暴击伤害倍率
  - 幂等写入（无变化不操作）；桥接关闭时自动还原 CS 配置先天值
  - 不修改 CS 任何代码，近战 / 远程 / 批量模式自然生效；CS 配置的先天值运行时读取
- **SuppressExternalCritsMixin 反双暴击**：注入神化的 `AttributeEvents#apothCriticalStrike`
  与 `#vanillaCritDmg`（HEAD cancellable），玩家发起攻击时取消神化自掷暴击，
  由 CS 统一结算；怪物保留神化暴击。神化未安装时目标类不加载，mixin 自然不生效
- **配置**（`bridgeEnabled` / `bridgeAttributes` / `suppressExternalPlayerCrits`），
  桥接列表数据驱动可扩展（格式 `<attribute_id>;<chance|damage>`），支持其他暴击模组
- **依赖**：新增 `apothic-attributes 1.21.1-2.11.0`、`tiny-config 3.1.0-neoforge`（compileOnly）
- **NeoForge 21.1.219 → 21.1.252**（初版升至 21.1.235 满足 Apotheosis 8.9.0 硬性要求，后按新环境迁移至 21.1.252）
- **文档**：README.md 重写为中英双语模组介绍；新增 PUBLISHING.md（Modrinth Markdown + CurseForge BBCode 发布页描述）
- **文档美化**：README 增加 shields 徽章、emoji 分区、FAQ 与快速自测；PUBLISHING 增加亮点引导语、FAQ 与一句话摘要
- **许可证**：All Rights Reserved → MIT（新增 LICENSE 文件，版权人 LOVE_U987，2026；TEMPLATE_LICENSE.txt 保留为模板来源许可）
- `gradlew build` 验证通过
