package com.unified_algorithm.unified_algorithm;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// modId 需与 META-INF/neoforge.mods.toml 中的条目保持一致
@Mod(UnifiedAlgorithm.MODID)
public class UnifiedAlgorithm {
    // 模组 ID，统一在此定义供全模组引用
    public static final String MODID = "unified_algorithm";
    // 直接引用一个 slf4j 日志器
    public static final Logger LOGGER = LogUtils.getLogger();

    // 模组类构造器是模组加载时最先执行的代码。
    // FML 会自动识别 IEventBus、ModContainer 等参数类型并自动注入。
    public UnifiedAlgorithm(IEventBus modEventBus, ModContainer modContainer) {
        // 注册 commonSetup 方法到模组生命周期事件
        modEventBus.addListener(this::commonSetup);

        // 注册暴击桥接：玩家 tick 时同步外部暴击属性到 Critical Strike 的先天修正器
        NeoForge.EVENT_BUS.addListener(CritBridge::onPlayerTickPost);
        // 配置重载时使桥接条目缓存失效（ModConfigEvent 走模组事件总线）
        modEventBus.addListener(CritBridge::onConfigReload);

        // 将自身注册到游戏事件总线。
        // 仅当本类需要直接响应事件（如 onServerStarting）时才需要这一行。
        NeoForge.EVENT_BUS.register(this);

        // 注册本模组的 ModConfigSpec，FML 会为我们创建并加载配置文件
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // 模组公共初始化回调
    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[Unified Algorithm] common setup: crit compat environment loaded");
    }

    // 使用 SubscribeEvent 注解，由事件总线自动发现并调用
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 服务器启动时的逻辑
        LOGGER.info("[Unified Algorithm] server starting: crit compat ready");
    }
}
