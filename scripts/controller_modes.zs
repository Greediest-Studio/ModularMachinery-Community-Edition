import mods.modularmachinery.MachineModifier;
import mods.modularmachinery.ControllerMode;
import mods.modularmachinery.MMEvents;
import mods.modularmachinery.ControllerGUIRenderEvent;
import mods.modularmachinery.RecipeAdapterBuilder;

// 用此段替换原来的模式注册和配方适配器定义，避免重复注册。
MachineModifier.addControllerMode(
    "elysia_forger",
    ControllerMode.create("模式", 0)
        .addMode(0, "板材")
        .addMode(1, "齿轮")
        .setControllerButtonVisible(true)
        .setControllerButtonTooltip(
            "§e按下按钮切换运行模式",
            "§a当前模式：§f%s"
        )
);

// 按钮默认隐藏；设为 true 后显示齿轮图标，单按钮无角标。
// 多个可见按钮按模式组注册顺序显示 1、2、3……，不受模式数值影响。
// 点击弹出模式列表，Esc 或点击列表外关闭；列表较长时可滚轮翻看。
// 模式无需智能数据接口；每台控制器独立保存，结构未成型时按钮禁用。
// 默认值必须通过 addMode 注册；同一控制器的不同模式组互不影响。
// 模式按机器类型保存，重进游戏或临时拆除结构后重组仍保留。
// 切换只影响后续配方，已开工配方继续完成。
// 在服务端事件中也可调用 event.controller.setControllerMode("模式", 1)。
// setControllerMode 返回是否实际切换；隐藏按钮的模式组也能由脚本切换。
// addModeSelect 声明配方所需模式，多次调用时必须全部满足。
// RecipeBuilder.newBuilder(...) 和 RecipeAdapterBuilder.create(...) 均支持此方法。

MMEvents.onControllerGUIRender("elysia_forger", function(event as ControllerGUIRenderEvent) {
    var info as string[] = [
        "§e///大型铸造单元控制面板///",
        "§a机器名称：§eELYSIA单元 - 大型铸造单元"
    ];
    event.extraInfo = info;
});

RecipeAdapterBuilder.create("elysia_forger", "thermalexpansion:compactor_plate")
    .addModeSelect("模式", 0)
    .addRecipeTooltip("§d铸造配方支持模块化电容升级，详情请查询“模块化电容”")
    .addRecipeTooltip("§e需求运行模式：板材（0）")
    .setMaxThreads(1)
    .build();

RecipeAdapterBuilder.create("elysia_forger", "thermalexpansion:compactor_gear")
    .addModeSelect("模式", 1)
    .addRecipeTooltip("§d铸造配方支持模块化电容升级，详情请查询“模块化电容”")
    .addRecipeTooltip("§e需求运行模式：齿轮（1）")
    .setMaxThreads(1)
    .build();

// 在游戏内执行 /ct syntax 检查脚本；修改模式声明后重启游戏。
