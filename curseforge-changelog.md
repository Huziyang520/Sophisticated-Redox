## 0.6.0-fix

- Fixed the mod failing to load on NeoForge 26.3.0.77: the loader renamed the config type this mod registers (`SERVER` became `SYNCED`), which made mod construction fail with a `NoSuchFieldError`
- The config type is now resolved by name, so the jar loads on both 26.3 loader generations

- 修复在 NeoForge 26.3.0.77 上无法加载的问题：加载器改掉了本模组使用的配置类型名（`SERVER` 变为 `SYNCED`），导致模组构造阶段抛 `NoSuchFieldError`
- 配置类型改为按名称查找，26.3 线的两代加载器都能正常加载

## 0.6.0

- Added Minecraft 26.3 support
- Added a NeoForge configuration screen: the mod now has a Config button in the mod list, which opens the server config
- Simplified the mod description to a single line

- 新增 Minecraft 26.3 支持
- 新增 NeoForge 配置界面：模组列表里本模组多了「配置」按钮，点开就是服务端配置
- 模组简介精简为一行
