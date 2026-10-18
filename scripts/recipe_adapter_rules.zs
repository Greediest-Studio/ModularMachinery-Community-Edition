import mods.modularmachinery.RecipeAdapterBuilder;

// 将 my_machine 替换为目标机械的注册名。
val adapter = RecipeAdapterBuilder.create("my_machine", "minecraft:furnace");

// 全局追加：每条适配后的配方都额外消耗煤、产出石头。
adapter.addItemInput(<minecraft:coal>);
adapter.addItemOutput(<minecraft:stone>);

// 单独调整：铁矿石配方额外产出砂砾。
adapter.forInput(<minecraft:iron_ore>)
    .addItemOutput(<minecraft:gravel>)
    .build();

// 同一适配器可以定义多条规则；也可以按原有输出筛选。
adapter.forOutput(<minecraft:gold_ingot>)
    .addItemInput(<minecraft:redstone>)
    .addFluidInput(<liquid:water> * 1000)
    .build();

// 输入和输出条件可以组合；所有条件必须同时满足。
// 这条与铁矿石规则同时命中时，额外砂砾和概率沙子会叠加。
adapter.forInput(<minecraft:iron_ore>)
    .forOutput(<minecraft:iron_ingot>)
    .addItemOutput(<minecraft:sand>)
    .setChance(0.25)
    .build();

// 按物品输入/输出的物品种数筛选：每种物品、矿辞或物品组需求各计一种，
// 流体、气体、能量和可选催化剂不计入，物品组内部的可选材料不重复计数。
// 只有一个物品输入的配方（例如熔炉、单输入研磨）：
adapter.forInputItemTypes(1)
    .addItemOutput(<minecraft:gravel>)
    .build();

// 物品输入种类数在 [min, max] 区间内的配方（含边界）：
adapter.forInputItemTypes(2, 3)
    .addFluidInput(<liquid:water> * 500)
    .build();

// 没有任何物品输入的配方（例如只消耗流体）：
adapter.forInputItemTypes(0)
    .addItemOutput(<minecraft:cobblestone>)
    .build();

// 物品输出种类数恰好为 2 的配方：
adapter.forOutputItemTypes(2)
    .setChance(0.5)
    .build();

// 没有任何物品输出、只产出流体的配方：
adapter.forOutputItemTypes(0)
    .addItemOutput(<minecraft:stone>)
    .build();

// 物品种数条件同样可以和 forInput/forOutput 组合，且多次设置时取交集：
adapter.forInputItemTypes(1)
    .forOutputItemTypes(1)
    .forInput(<minecraft:iron_ore>)
    .build();

// 移除命中的配方：不注册、不进入 JEI，其余配方照常注册。
// remove() 必须搭配至少一个筛选条件，否则会移除适配器的全部配方；
// 移除优先于追加，同一配方即使被其他规则追加过也不会注册。
// 例如删除只有一种物品输入的配方：
// adapter.forInputItemTypes(1)
//     .remove()
//     .build();

// 也可以按具体材料移除：
// adapter.forInput(<minecraft:dirt>).remove().build();
// adapter.forOutput(<minecraft:gravel>).remove().build();

// 规则的 build() 保存规则；最后只调用一次 adapter.build() 注册整批配方。
adapter.build();

// 多材料筛选示例：适用于原配方本来就包含这些输入的其他适配器。
// 支持物品、矿辞、流体混用，参数列表和数组写法等价。
// otherAdapter.forInput(<ore:ingotIron>, <minecraft:coal>, <liquid:water>)
//     .addItemOutput(<minecraft:gravel>)
//     .build();
// otherAdapter.forInput([<ore:ingotIron>, <minecraft:coal>, <liquid:water>])
//     .forOutput(<minecraft:iron_ingot>)
//     .addFluidOutput(<liquid:lava> * 1000)
//     .build();

// 匹配忽略数量，流体按种类匹配；配方允许含有其他材料。
// 物品组中的候选材料也参与匹配；概率产物和每 tick 流体也参与对应方向的匹配。
// 物品种数筛选按需求条数计数：每个物品、矿辞或物品组需求各算一种，流体/气体/能量/燃料和可选催化剂不算。
// 全局追加和其他规则追加的材料均不参与筛选，未命中的配方仅应用全局追加。
// 筛选方法请写在 addInput/addOutput 等追加方法之前；追加内容可继续设置概率、NBT、Tag 等。
