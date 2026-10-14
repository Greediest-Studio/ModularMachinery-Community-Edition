import mods.modularmachinery.RecipeBuilder;
import mods.modularmachinery.RecipeAdapterBuilder;

// =======================================================================
// MMCE 配方级并行控制配置示例
// =======================================================================
// 支持的方法（在 RecipePrimer 上定义，RecipeBuilder 与 RecipeAdapterBuilder 均可用）：
// 1. setMultipleParallelism(multiplier as int)
//    - 倍乘原并行数。以机器或线程的基础并行数为基准按倍率放大。
//    - 例如机器提供 4 并行，设置 multiplier 为 4 则可达 4 * 4 = 16 并行。
//
// 2. addMaxParallelism(amount as int)
//    - 对该配方的计算后最大并行数进行加减偏移（正数增加，负数减少）。
//
// 3. setMaxParallelism(maxParallelism as int)
//    - 设置该配方允许的最大并行绝对上限（超出部分会被截断；<= 0 表示不设硬上限）。
//
// 综合计算公式：
// 有效最大并行 = min(max(1, (机器原并行 * multiplier) + addAmount), setMaxParallelism)
// =======================================================================

// -----------------------------------------------------------------------
// 1. 普通配方构建器（RecipeBuilder）并行配置示例
// -----------------------------------------------------------------------

// 高效压缩配方：4倍并行，基础并行加 2，且绝对上限不超过 32
RecipeBuilder.newBuilder("super_compress", "my_compressor", 60)
    .addItemInput(<minecraft:iron_ingot> * 4)
    .addItemOutput(<minecraft:iron_block>)
    .setMultipleParallelism(4)
    .addMaxParallelism(2)
    .setMaxParallelism(32)
    .build();

// 危险/高能配方：并行倍率保持 1，但最大并行惩罚 -2（最小仍为 1）
RecipeBuilder.newBuilder("unstable_fusion", "my_compressor", 200)
    .addItemInput(<minecraft:nether_star>)
    .addItemOutput(<minecraft:beacon>)
    .addMaxParallelism(-2)
    .build();

// -----------------------------------------------------------------------
// 2. 配方适配器（RecipeAdapterBuilder）全局并行设置
// -----------------------------------------------------------------------
// 当高级机器继承低级机器的配方时，往往希望所有继承来的配方天然具备更高并行能力。
val adapter = RecipeAdapterBuilder.create("numerical_control_machinery", "minecraft:furnace");

// 全局追加材料与属性
adapter.addFluidInput(<liquid:water> * 100);

// 全局设置：所有继承来的配方均享受 4 倍原并行，且上限不超过 64
adapter.setMultipleParallelism(4)
       .addMaxParallelism(2)
       .setMaxParallelism(64);

// -----------------------------------------------------------------------
// 3. 配方适配器特定规则（RecipeAdapterRule）单独覆盖并行设置
// -----------------------------------------------------------------------
// 规则内部也可以再次调用并行方法，覆盖全局适配器的配置。

// 针对金锭产出配方：额外放大至 8 倍并行
adapter.forOutput(<minecraft:gold_ingot>)
       .setMultipleParallelism(8)
       .build();

// 针对高负荷配方（如绿宝石）：限制最大并行绝对上限不超过 4
adapter.forInput(<minecraft:emerald_ore>)
       .setMaxParallelism(4)
       .build();

// 最后统一注册适配器
adapter.build();

