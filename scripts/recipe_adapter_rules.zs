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
// 全局追加和其他规则追加的材料均不参与筛选，未命中的配方仅应用全局追加。
// 筛选方法请写在 addInput/addOutput 等追加方法之前；追加内容可继续设置概率、NBT、Tag 等。
