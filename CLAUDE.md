# AndroidDemo 项目规范

## 项目路径
`/home/zhangzhonghao/Documents/Project/AndroidDemo/`

## JDK / SDK 版本约束（不可修改）
- **JDK**: 全局 JDK 17，不修改任何 JDK 配置
- **compileSdk**: 34
- **targetSdk**: 34
- **AGP**: 8.4.2
- **Gradle**: 8.7
- **Gradle 镜像**: https://mirrors.aliyun.com/macports/distfiles/gradle/（已配置，勿改动）

## 版本冲突处理
遇到依赖版本冲突时，提示用户，由用户决定如何处理。不要自行升级 AGP/Gradle/SDK 版本。

## 开发习惯
- 功能分组按功能类型划分（如 UI/定位/AI 等）
- 使用 BottomNavigationView 实现底部菜单
- Fragment 用于页面内容，Activity 用于容器
- 依赖优先使用 Material Components 和 AndroidX
- 保持屏幕常亮可使用 `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON`
