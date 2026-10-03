## 构建

构建环境基于 [CleanroomMC/ForgeDevEnv](https://github.com/CleanroomMC/ForgeDevEnv/tree/8f63f346dde23de759b01398917a19719efd2f43)：Gradle 9.7.0、RetroFuturaGradle 2.0.3。

使用 JDK 25 启动 Gradle，并将 IDE 的 Gradle JVM 设置为 JDK 25。

```powershell
# JAVA_HOME 指向本机 JDK 25；编译工具链由 Gradle 自动获取。
./gradlew.bat build
./gradlew.bat runClient
```

项目设置位于 `gradle.properties`，依赖位于 `gradle/scripts/dependencies.gradle`，项目适配位于 `gradle/scripts/extra.gradle`。`libs/lumenized-1.0.3-dev.jar` 是本地依赖。
