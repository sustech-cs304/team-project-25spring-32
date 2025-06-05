# Final-Report-32

# 1. Metrics

使用CodeMR工具和MetricsReloaded插件进行静态分析。

项目总体

MetricsReloaded分析：

Lines of Code：11202
Number of source files：96
Cyclomatic complexity：1464
Number of dependencies：18

CodeMR报告:[team-project-25spring-32/codemr at main · sustech-cs304/team-project-25spring-32 (github.com)](https://github.com/sustech-cs304/team-project-25spring-32/tree/main/codemr)

# 2. Documentation

用户文档已在应用中配置，可打开应用点击左上角汉堡菜单，并点击快速入门查看

开发者文档已上传至GitHub，链接：[team-project-25spring-32/README.md at main · sustech-cs304/team-project-25spring-32 (github.com)](https://github.com/sustech-cs304/team-project-25spring-32/blob/main/README.md)

开发者WIKI:[Home · sustech-cs304/team-project-25spring-32 Wiki (github.com)](https://github.com/sustech-cs304/team-project-25spring-32/wiki)

# 3. Tests

普通代码方法使用JUnit和Mock进行测试，分为Andriod环境测试（InstrumentTest）和普通测试（UnitTest），安卓环境测试报告由gradle测试配置和jacoco依赖生成，可在app/build/reports中找到。普通测试结果可直接在Andriod Studio中查阅。报告提供测试结果说明和覆盖率说明。

InstrumentTest：[https://github.com/sustech-cs304/team-project-25spring-32/tree/main/app/src/androidTest/java/com/example/pa](https://github.com/sustech-cs304/team-project-25spring-32/tree/main/app/src/androidTest/java/com/example/pa)

UnitTest：[https://github.com/sustech-cs304/team-project-25spring-32/tree/main/app/src/test/java/com/example/pa](https://github.com/sustech-cs304/team-project-25spring-32/tree/main/app/src/test/java/com/example/pa)

UI测试及其他不便于自动化测试的部分采用人工测试，人工测试说明已写在测试手册.pdf文件中。

# 4. Build

项目采用Gradle进行自动化构建，构建过程执行的具体任务如下：

> Task :app:preBuild
Task :app:preDebugBuild
Task :app:mergeDebugNativeDebugMetadata
Task :app:dataBindingMergeDependencyArtifactsDebug
Task :app:generateDebugResValues
Task :app:generateDebugResources
Task :app:mergeDebugResources
Task :app:packageDebugResources
Task :app:parseDebugLocalResources
Task :app:dataBindingGenBaseClassesDebug
Task :app:dataBindingTriggerDebug
Task :app:javaPreCompileDebug
Task :app:checkDebugAarMetadata
Task :app:mapDebugSourceSetPaths
Task :app:createDebugCompatibleScreenManifests
Task :app:extractDeepLinksDebug
Task :app:processDebugMainManifest
Task :app:processDebugManifest
Task :app:mergeDebugShaders
Task :app:compileDebugShaders
Task :app:generateDebugAssets
Task :app:mergeDebugAssets
Task :app:generateDebugJacocoPropertiesFile
Task :app:compressDebugAssets
Task :app:checkDebugDuplicateClasses
Task :app:desugarDebugFileDependencies
Task :app:mergeExtDexDebug
Task :app:mergeLibDexDebug
Task :app:mergeDebugJniLibFolders
Task :app:mergeDebugNativeLibs
Task :app:stripDebugDebugSymbols
Task :app:validateSigningDebug
Task :app:writeDebugAppMetadata
Task :app:writeDebugSigningConfigVersions
Task :app:processDebugManifestForPackage
Task :app:processDebugResources
Task :app:compileDebugJavaWithJavac
Task :app:processDebugJavaRes 
Task :app:mergeDebugJavaResource 
Task :app:jacocoDebug
Task :app:dexBuilderDebug
Task :app:mergeProjectDexDebug
Task :app:packageDebug
Task :app:createDebugApkListingFileRedirect
Task :app:assembleDebug
> 

构建完成后，主要生成的文件有项目报告和**/app/build/outputs/apk/debug/** 下的.apk文件，其余构建中间件及构建日志等文件均在**/app/build/** 下可找到。

主要项目构建配置文件：[https://github.com/sustech-cs304/team-project-25spring-32/blob/lkq/app/build.gradle.kts](https://github.com/sustech-cs304/team-project-25spring-32/blob/lkq/app/build.gradle.kts)

# 5. Deployment

项目采用安卓虚拟机部署，由于安卓各个应用互相独立，因此可看作是一个独立的容器，且共同资源（如安卓虚拟机内部的照片）可共享，可保证数据一致性。应用内部数据则由应用本身搭建数据库进行存储，且各个应用数据库互不干扰。

所有应用均在安卓环境下运行。

Github action执行CI流程，每一次都能够将构建好的工件提交，主要的构建工件为.apk文件，但由于GitHub Action本身内部虚拟机运行较慢，因此人工将工件下载并部署比自动化部署效率要高得多
