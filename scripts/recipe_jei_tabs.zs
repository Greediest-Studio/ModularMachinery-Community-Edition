import mods.modularmachinery.RecipeBuilder;
import mods.modularmachinery.RecipeAdapterBuilder;
import mods.modularmachinery.MachineModifier;
import mods.modularmachinery.MachineJEICategory;

// =======================================================================
// MMCE 机械配方 JEI 多标签页（Recipe Categories / Tabs）示例
// =======================================================================

// -----------------------------------------------------------------------
// 1. 默认标签页行为：不调用 setJEICategory
// -----------------------------------------------------------------------
// 如果创建配方时不加 setJEICategory，该配方将自动进入机械的【默认标签页】。
// 默认标签页标题为机械本身的名称，图标为机械蓝图与控制器。
RecipeBuilder.newBuilder("default_smelting", "elysia_forger", 100)
    .addItemInput(<minecraft:iron_ingot>)
    .addItemOutput(<minecraft:gold_ingot>)
    .build();

// -----------------------------------------------------------------------
// 2. 配方级别指定标签页（setJEICategory）
// -----------------------------------------------------------------------
// 支持多种重载：
// - setJEICategory(key) : 仅指定分类标识，标题回退为 "机械名 - key"，图标使用默认蓝图
// - setJEICategory(key, displayName) : 指定分类标识与显示标题（支持样式代码）
// - setJEICategory(key, icon) : 指定分类标识与专属图标
// - setJEICategory(key, displayName, icon) : 指定分类标识、显示标题与专属图标

// 板材加工标签页：
RecipeBuilder.newBuilder("plate_iron", "elysia_forger", 60)
    .setJEICategory("plate", "§6板材压延", <minecraft:iron_bars>)
    .addItemInput(<minecraft:iron_ingot> * 2)
    .addItemOutput(<minecraft:iron_bars>)
    .build();

// 齿轮加工标签页：
RecipeBuilder.newBuilder("gear_iron", "elysia_forger", 120)
    .setJEICategory("gear", "§b齿轮铸造", <minecraft:compass>)
    .addItemInput(<minecraft:iron_ingot> * 4)
    .addItemOutput(<minecraft:compass>)
    .build();

// -----------------------------------------------------------------------
// 3. 配方适配器（RecipeAdapterBuilder）批量指定标签页
// -----------------------------------------------------------------------
// 该适配器引入的所有配方，统一归入 "thermal_plate" 标签页。
RecipeAdapterBuilder.create("elysia_forger", "thermalexpansion:compactor_plate")
    .setJEICategory("thermal_plate", "TE板材加工", <minecraft:iron_trapdoor>)
    .build();

// -----------------------------------------------------------------------
// 4. 适配器规则（RecipeAdapterRule）单独重定向到不同标签页
// -----------------------------------------------------------------------
val adapter = RecipeAdapterBuilder.create("elysia_forger", "thermalexpansion:compactor_gear")
    .setJEICategory("thermal_gear", "常规齿轮铸造", <minecraft:clock>);

// 单独将产出含特定物品的配方，划分到专属的“特种工艺”标签页：
adapter.forOutput(<minecraft:gold_ingot>)
    .setJEICategory("precious_metal", "§e黄金特种工艺", <minecraft:gold_block>)
    .build();

adapter.build();

// -----------------------------------------------------------------------
// 5. 使用 MachineModifier 预先定义机械的标签页元数据（可选规范化管理）
// -----------------------------------------------------------------------
// 提前为机械注册标签页的名称、专属图标和排序优先级（priority 越小越靠前）。
// 注册后，配方中只需写入 .setJEICategory("circuit") 即可自动继承这里的标题与图标：
MachineModifier.addJEICategory(
    "elysia_forger",
    "circuit",
    "§d电路蚀刻单元",
    <minecraft:redstone>,
    10
);

RecipeBuilder.newBuilder("circuit_basic", "elysia_forger", 200)
    .setJEICategory("circuit")
    .addItemInput(<minecraft:paper>)
    .addItemInput(<minecraft:redstone> * 4)
    .addItemOutput(<minecraft:repeater>)
    .build();

// -----------------------------------------------------------------------
// 6. 为特定配方取消 JEI 显示（setLoadJEI）
// -----------------------------------------------------------------------
// 当某些配方为隐藏配方、特殊工艺或不希望在 JEI 中查看到时，可通过 .setLoadJEI(false) 取消显示：

// (1) RecipeBuilder 新建配方时取消 JEI 显示：
RecipeBuilder.newBuilder("secret_forging", "elysia_forger", 100)
    .setLoadJEI(false)
    .addItemInput(<minecraft:dirt>)
    .addItemOutput(<minecraft:diamond>)
    .build();

// (2) 配方适配器（RecipeAdapterBuilder）批量取消引入配方的 JEI 显示：
RecipeAdapterBuilder.create("elysia_forger", "minecraft:furnace")
    .setLoadJEI(false)
    .build();

// (3) 配方适配器规则（RecipeAdapterRule）单独取消符合条件的配方的 JEI 显示：
val customAdapter = RecipeAdapterBuilder.create("elysia_forger", "thermalexpansion:pulverizer");
customAdapter.forOutput(<minecraft:sand>)
    .setLoadJEI(false) // 仅将产出为沙子的磨粉配方在 JEI 中取消显示，其余配方正常显示
    .build();
customAdapter.build();



