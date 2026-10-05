## 构建

构建环境基于 [CleanroomMC/ForgeDevEnv](https://github.com/CleanroomMC/ForgeDevEnv/tree/8f63f346dde23de759b01398917a19719efd2f43)：Gradle 9.7.0、RetroFuturaGradle 2.0.3。

使用 JDK 25 启动 Gradle，并将 IDE 的 Gradle JVM 设置为 JDK 25。

```powershell
# JAVA_HOME 指向本机 JDK 25；编译工具链由 Gradle 自动获取。
./gradlew.bat build
./gradlew.bat runClient
```

项目设置位于 `gradle.properties`，依赖位于 `gradle/scripts/dependencies.gradle`，项目适配位于 `gradle/scripts/extra.gradle`。`libs/lumenized-1.0.3-dev.jar` 是本地依赖。

## MMCE-Addons 融合

联动仓口已直接并入本体。

从原 MMCE + Addons 迁移 `.zs` 脚本，见 [CraftTweaker 迁移指南](CraftTweaker迁移指南.md)。

## 高级搭建工具

`modularmachinery:advanced_builder_tool` 已并入本体，两根木棍竖排合成。

- 普通右键打开配置；潜行右键控制器开始搭建或拆卸，有进行中的任务时优先取消。潜行右键空气取消自己的任务。
- 木棍仍沿用原来的材料检查、执行间隔和 NBT 配置；两种入口共用任务表，同一控制器不会重复施工。原高级扳手仍用于结构导出。
- 工具支持主副手，每把独立保存设置。动态长度默认 `1`，按机器定义限制；默认关闭所有开关。`auto-assembly.advancedTickInterval=1`、`advancedOperationsPerTick=64` 控制执行节奏。
- 方块选择页使用服务端的结构别名，支持分页、恢复默认、全局选择和 JEI/HEI 拖入。优先级为全局选择、对应别名、结构默认；直接声明的方块不被别名选项改写。
- 背包优先供料；安装 AE2 后可启用无线终端供料、回收及缺料合成，合成仍须在 AE 确认页手动确认。AE2 Fluid Crafting 提供流体合成，Baubles 提供饰品栏终端查找，MMCE Complement 提供附属模块入口。无 AE 也可使用基础搭建和拆卸。
- 拆卸保留控制器，并只回收匹配的结构方块；流体没有回收空间时保留原方块。AE 退款的零散流体无法放回网络或容器时，会保存在 `modularmachinery:builder_fluid_return` 回收容器中。

运行时移除独立 MMCE-AdvancedBuilderTool JAR；融合功能不依赖 ModularUI。旧扩展物品 ID 和 NBT 不迁移，新设置保存在工具的 `modularmachinery:builder` 子标签。

Complement 需要适配本体新包名的版本。当前编译用 `8830125` 仍引用旧包 `github.kasuminova.mmce.common.block.appeng.BlockMEPatternProvider`，实际启动验证失败，不能直接作为本分支的运行时依赖。

实现与资源来源为 [exLi-wai/MMCE-AdvancedBuilderTool](https://github.com/exLi-wai/MMCE-AdvancedBuilderTool)（参考提交 `09e1b7a`），保留 GPL-3.0 许可，与本体同一许可证。

```powershell
./gradlew.bat test build
# 使用 build/ 下的隔离世界；依赖组合：base、ae、full、complement。
./gradlew.bat -I gradle/scripts/builder-smoke.init.gradle runServer -PbuilderSmoke=base
./gradlew.bat -I gradle/scripts/builder-smoke.init.gradle runClient -PbuilderSmoke=full
# 自动验证结果位于 build/builder-smoke-<组合>-<任务>/builder-smoke-result.txt。

# Windows JDK 25 若出现 Unable to establish loopback connection，可在当前终端临时回退 TCP：
$env:JAVA_TOOL_OPTIONS='-Djdk.net.unixdomain.tmpdir=D:/__mmce_no_unix_socket_directory__'
```
