pluginManagement {
    repositories {
        // 你也可以在这里为插件添加国内镜像，如果插件下载也慢的话
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") } // 阿里云的 Google 镜像
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") } // 阿里云的 Gradle 插件镜像
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 首先尝试国内镜像
        maven { url = uri("https://maven.aliyun.com/repository/public") } // 阿里云公共仓库 (包含了 Central)
        maven { url = uri("https://maven.aliyun.com/repository/google") } // 阿里云的 Google 镜像

        google()
        mavenCentral()
    }
}

rootProject.name = "PA"
include(":app")
 