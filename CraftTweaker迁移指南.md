# CraftTweaker 迁移指南

只记录迁移时必须修改的差异，每个附属一节；未列出的方法沿用原写法。

## 通用

- 用本仓库版本替换旧 MMCE，移除已经融合的附属 JAR；保留其需要的实际联动模组。
- `mods.modularmachinery` 的 import、机器名、配方名和 `.build()` 不需要改。

## MMCE-Addons

| 搜索旧写法 | 修改方式 |
| --- | --- |
| `#modloaded modularmachineryaddons` | 删除；保留 `bloodmagic`、`thaumcraft` 等实际联动条件 |
| `modularmachineryaddons:物品或方块名` | 已保留内容改为 `modularmachinery:原名`，meta 和 NBT 保持 |
| Addons 的 `addWillInput` / `addWillOutput` | 改为 `addWillMultiChunkInput` / `addWillMultiChunkOutput`，参数顺序不变 |
| `modularmachineryaddons:willMultiChunk` | 改为 `modularmachinery:will_multi_chunk` |
| `modularmachineryaddons:potentialEnergy` | 改为 `modularmachinery:potential_energy` |
| `modularmachineryaddons:dragonBreath` | 改为 `modularmachinery:dragon_breath` |
| 其他已保留需求的 `modularmachineryaddons:` 前缀 | 改为 `modularmachinery:`；同时检查 Modifier 和 JSON 中的字符串 |

**意志接口只改数字在前、类型字符串在最后的 Addons 重载。** 类型字符串在前的单区块接口（如 `addWillInput("DEFAULT", 100)`）保持不变。

**未保留：** ME 源质输入输出仓、两种辐射海绵、Addons 装配/拆解工具、奇点物品仓。清理其物品引用及结构定义。
