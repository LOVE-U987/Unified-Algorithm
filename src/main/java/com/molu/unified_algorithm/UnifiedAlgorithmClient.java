package com.molu.unified_algorithm;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// 本类不会在专用服务器上加载，在这里访问客户端代码是安全的
@Mod(value = UnifiedAlgorithm.MODID, dist = Dist.CLIENT)
// 使用 EventBusSubscriber 自动注册本类中所有带 @SubscribeEvent 注解的静态方法
@EventBusSubscriber(modid = UnifiedAlgorithm.MODID, value = Dist.CLIENT)
public class UnifiedAlgorithmClient {
    /**
     * 客户端模组构造器。
     *
     * @param container 客户端模组容器，用于注册扩展点
     */
    public UnifiedAlgorithmClient(ModContainer container) {
        // 让 NeoForge 能为本模组的配置创建配置界面。
        // 入口：Mods 界面 > 点击本模组 > 点击 config。
        // 别忘了在 en_us.json 中为配置项添加翻译。
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    // 客户端初始化事件回调
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // 客户端初始化逻辑
        UnifiedAlgorithm.LOGGER.info("[Unified Algorithm] client setup");
    }
}
