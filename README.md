# Sophisticated Redox

> Minecraft 26.2 · NeoForge · MIT

An addon for **Sophisticated Backpacks**. Five backpack upgrades that convert copper blocks **the moment they are inserted** — oxidize, scrape back, or wax. Your backpack becomes a sorting station for copper oxidation stages.

This is a small, flavour-driven utility mod, not a tech mod: no machines, no new ores, no automation chains. It just keeps your weathered, oxidized and waxed copper blocks in the state you want them, so builders and collectors stop ferrying stacks back and forth.

## The five upgrades

| Upgrade | Effect | Options |
|---|---|---|
| **Oxidation Upgrade** | Oxidizes one stage | — |
| **Advanced Oxidation Upgrade** | Oxidizes several stages in one go; level Ⅲ reaches fully oxidized | Level Ⅰ–Ⅲ, shape match |
| **Reduction Upgrade** | Scrapes one stage off | Allow dewaxing |
| **Advanced Reduction Upgrade** | Scrapes several stages; level Ⅲ returns to unaffected | Level Ⅰ–Ⅳ, allow dewaxing, shape match |
| **Waxing Upgrade** | Waxes the block, locking it at its current stage | — |

**A level is a maximum number of steps.** One step does exactly one thing: one oxidation stage, one reduction stage, or one dewaxing. With dewaxing allowed, the advanced reduction upgrade goes up to **Ⅳ** — one step to strip the wax plus up to three reduction steps, which turns a waxed oxidized block into a clean unaffected one. Switching dewaxing off drops a Ⅳ setting back to Ⅲ.

## How it behaves

- **Triggers on:** GUI drag / shift-click, hopper or pipe input, pickup and magnet upgrades, and installing the upgrade itself (it sweeps what is already in the backpack, no re-inserting needed).
- **Order:** oxidation / reduction upgrades run in the order you installed them. Waxing always runs last, whatever slot it is in — that is what makes "oxidize, then lock it in" work.
- **Waxed blocks stop weathering** (vanilla semantics), so oxidation and reduction skip them. Turn on "allow dewaxing" if you want them handled anyway.
- **Filters:** one per upgrade, whitelist or blacklist, using Sophisticated Backpacks' own filter UI. Default is blacklist with an empty list, i.e. **all copper blocks are processed** out of the box.
- **Shape match** (advanced upgrades): one filter entry then covers every oxidation stage and waxed variant of that block shape. Put an oxidized copper block in the filter and all four stages plus their waxed versions match — cut copper, being a different shape, does not.
- **Coverage:** nothing is hardcoded. Everything is read from the game's own weathering chain and NeoForge's data map, so the whole vanilla copper family is covered, and modded copper blocks that register through the vanilla weathering API work with no compat patch.

## Install

Requires [Sophisticated Backpacks](https://modrinth.com/mod/sophisticated-backpacks) and [Sophisticated Core](https://modrinth.com/mod/sophisticated-core), `26.2` builds. Drop the jar into `mods/`. Install it on **both client and server** (the logic runs server side, the settings screen is client side).

## Config

`<world>/serverconfig/sophisticated_redox-server.toml`

| Key | Default | Purpose |
|---|---|---|
| `diagnostics.debugLogging` | `false` | Writes every conversion step to the log. Turn this on if an upgrade seems to do nothing |
| `steps.defaultSteps` | `3` | Level a freshly crafted advanced upgrade starts at |
| `steps.maxStepsWithDewax` | `4` | Level cap for advanced reduction with dewaxing allowed |

## Languages

`en_us`, `en_gb`, `zh_cn`, `zh_tw`, `zh_hk`, `lzh`, `ru_ru`, `de_de`, `ja_jp`, `fr_fr`, `es_es`, `pt_br`, `ko_kr`.

## FAQ

**An upgrade seems to do nothing.** Check that it sits in an upgrade slot (not a storage slot) and that it is switched on in its settings screen, then enable `debugLogging` — the log states exactly which step stopped and why.

**Why does waxing stop oxidation?** Vanilla semantics: wax stops weathering. That is also why the waxing upgrade always runs last.

## Links

- GitHub: https://github.com/Huziyang520/Sophisticated-Redox
- Issues: https://github.com/Huziyang520/Sophisticated-Redox/issues
- Backup issue tracker: https://issue.mengcai.online/

MIT licensed. Thanks to P3pp3rF1y for Sophisticated Backpacks and Sophisticated Core.

---

# 精妙背包：氧化还原

> Minecraft 26.2 · NeoForge · MIT

**精妙背包的附属模组。** 五种升级，铜质方块**存入背包的那一刻**自动氧化、除锈或涂蜡——背包成了铜块氧化状态的整理台。

这是个趣味向的小功能模组，不是科技模组：不新增机器、不新增矿物、不新增维度。它只做一件事——让你那些斑驳铜块、锈蚀铜块、涂蜡铜块保持在你想要的状态，省掉来回跑工地的功夫。

## 五种升级

| 升级 | 作用 | 可调项 |
|---|---|---|
| **氧化升级** | 氧化一级 | — |
| **高级氧化升级** | 一次连续氧化多级，Ⅲ 档直达"氧化" | 档位 Ⅰ–Ⅲ、匹配方块形状 |
| **还原升级** | 除锈一级 | 允许脱蜡 |
| **高级还原升级** | 一次连续除锈多级，Ⅲ 档回到"未氧化" | 档位 Ⅰ–Ⅳ、允许脱蜡、匹配方块形状 |
| **涂蜡升级** | 涂蜡，把方块锁在当前档位 | — |

**档位 = 最多执行几步。** 一步只做一件事：一次一级氧化、一次一级除锈，或一次脱蜡。高级还原开启"允许脱蜡"后可调到 **Ⅳ**——脱蜡占一步，再加最多三次除锈，正好把"涂蜡的氧化铜块"收拾成"干净的未氧化铜块"。关掉脱蜡时，Ⅳ 会自动落回 Ⅲ。

## 行为规则

- **触发时机**：GUI 拖拽 / Shift+左键、漏斗与管道输入、拾取与磁吸升级，以及**刚把升级装进背包时**（自动扫一遍已有物品，不用重新拿进拿出）。
- **顺序**：氧化 / 还原类升级按安装顺序依次执行；涂蜡升级恒定排在最后，无论装在哪个槽——这正是"先氧化再锁住"能成立的原因。
- **涂蜡后的方块不再风化**（原版语义），氧化与还原会跳过它；想让它们继续处理，请打开"允许脱蜡"。
- **过滤**：每个升级独立，白名单 / 黑名单，面板沿用精妙背包原版。默认黑名单且列表为空，即**所有铜质方块都会被处理**，装上就能用。
- **匹配方块形状**（仅高级升级）：过滤器里放一个该形状的任意档位物品，就能命中同形状的全部氧化档位与涂蜡变体。放"氧化铜块"会命中四个档位及各自的涂蜡版；"切制铜块"形状不同，不命中。
- **覆盖范围**：不硬编码清单，全部读自游戏自身的风化链与 NeoForge 数据图，因此原版全铜器家族自动覆盖，通过原版风化 API 注册的模组方块也无需兼容补丁。

## 安装

需要 [精妙背包](https://modrinth.com/mod/sophisticated-backpacks) 与 [精妙核心](https://modrinth.com/mod/sophisticated-core) 的 `26.2` 版本。把 jar 放进 `mods/`，**客户端与服务端都要装**（逻辑在服务端，设置界面在客户端）。

## 配置

`<存档>/serverconfig/sophisticated_redox-server.toml`

| 键 | 默认 | 说明 |
|---|---|---|
| `diagnostics.debugLogging` | `false` | 把转换链每一步写进日志。升级"好像没反应"时打开它排查 |
| `steps.defaultSteps` | `3` | 高级升级刚合成时的档位 |
| `steps.maxStepsWithDewax` | `4` | 高级还原开脱蜡后的档位上限 |

## 本地化

`en_us`、`en_gb`、`zh_cn`、`zh_tw`、`zh_hk`、`lzh`、`ru_ru`、`de_de`、`ja_jp`、`fr_fr`、`es_es`、`pt_br`、`ko_kr`。

## 常见问题

**升级好像没反应。** 先确认它装在**升级槽**里（不是普通储物格）、且设置页里没被关掉；再把 `debugLogging` 打开，日志会写明断在哪一步以及原因。

**为什么涂蜡之后就不氧化了？** 原版语义如此：涂蜡阻止风化。也正因如此，涂蜡升级恒定排在最后。

## 链接

- GitHub：https://github.com/Huziyang520/Sophisticated-Redox
- 问题反馈：https://github.com/Huziyang520/Sophisticated-Redox/issues
- 备用反馈站：https://issue.mengcai.online/

MIT 协议开源。感谢 P3pp3rF1y 开发并维护精妙背包与精妙核心。
