# AI-Enlightenment-Time
AI-Enlightenment-Time
# AI启蒙时光 — 最终需求文档（交付版）

版本：1.0  
日期：2025-08-27  
作者：项目业务分析师

摘要  
本文件为客户端仅运行的 Android 应用“AI启蒙时光”的最终需求文档（Markdown）。文档整合了需求、设计与任务拆分资料，且已按用户要求将指定 AI 模型集成方案（GEMINI-2.5-PRO、Qwen3-Embedding-8B（BAAI/bge-m3）、BAAI/bge-reranker-v2-m3、GPT-5-PRO、grok-4-imageGen）完整纳入实现与验收标准；UI 色彩以“红色优先”为主题以贴合目标用户偏好。源材料参考见文末。

目录
1. 项目定位与目标
2. 目标用户与场景
3. 核心功能与用户故事
4. 非功能性需求
5. 技术架构与模块映射（含模型-功能映射）
6. AI 模型调用策略、降级与隐私/成本评估概要
7. UI/UX 设计要点（“红色优先”）
8. 验收标准与可测试项
9. 需求追踪矩阵（概要）
10. 交付物与下一步计划
附录：参考文档列表

1. 项目定位与目标
- 产品定位：面向 3-6 岁小男孩的客户端仅 Android 应用，提供每日 15 分钟 AI 引导探索学习体验，强调“科技感”与“陪伴感”。
- 目标：将 AI 能力（多模态与检索增强）最大化融入体验，确保离线优先、隐私安全、流畅低耗电。

2. 目标用户与场景
- 用户画像：3-6 岁小男孩，家长为主要购买/配置者。偏好颜色：红色。
- 核心场景：每日早/晚 15 分钟的探索引导（故事、游戏、观察任务、拍照互动、语音问答、成就鼓励）。

3. 核心功能与用户故事（精要）
- 日常探索流程：应用每天生成一套 15 分钟任务（故事 + 互动任务 + 拍照/说话反馈），用户故事覆盖学习点与情感引导。
- 故事生成：基于 GEMINI-2.5-PRO / GPT-5-PRO 生成短故事，含角色、目标、简单问题引导。
- 图像生成功能（奖励/卡片）：使用 grok-4-imageGen 生成趣味插画或成就卡。
- 照片分析：本地或受控调用模型检索/嵌入（Qwen3-Embedding-8B + reranker）用于识别图像中物体并提供友好反馈（注意隐私与仅需时上传策略）。
- 语音交互：STT 与 TTS 集成（本地 TTS 或云 TTS 优选），对话生成由 GEMINI-2.5-PRO / GPT-5-PRO 驱动。
- 成就体系与成长档案：本地使用 Room 存储，展示成长进度与家长总结。
- 配置与插件化：插件式主题与成长阶段扩展，支持未来接入新的模型或内容源。

4. 非功能性需求
- 平台：Android 7.0+，性能优化（内存、CPU、网络、低耗电），支持离线体验优先。
- 隐私：严格本地首选，最小化上传；若需云调用，仅在家长同意并启用情况下进行；所有外发数据须进行加密与脱敏。
- 可维护性：模块化（Presentation / Domain / Data）、插件式扩展点。
- 可用性：适配儿童交互（大按钮、语音优先、声光反馈）、高可访问性。

5. 技术架构与模块映射（含模型-功能映射）
- 架构概览：Clean Architecture（Presentation / Domain / Data）。核心模块：AI Service、Multimedia Service、UI Shell、Persistence、Plugin Manager、Telemetry（本地/可选上报）。
- 模型-功能映射（建议）：
  - GEMINI-2.5-PRO：主要用于多轮对话、故事生成与多模态理解（优先对话逻辑与多轮故事）。
  - GPT-5-PRO：用于复杂创作类任务与高质量文本生成（备选/富文本生成场景，如家长报告、深度故事）。
  - Qwen3-Embedding-8B（BAAI/bge-m3）：用于本地/云端图片与文本的向量化，支持近似检索与相似性匹配（照片识别索引、内容检索）。
  - BAAI/bge-reranker-v2-m3：用于在检索或多候选生成时对结果进行排序与置信度增强（照片识别候选、多模型生成结果选择）。
  - grok-4-imageGen：用于奖励图片、卡片和插图生成，提升“科技感”视觉产出。
- 模型调用层设计：
  - 抽象接口：IAIService，支持按模型配置 appKey + apiBaseUrl、本地缓存、优先本地（若支持）-> 家长授权云调用 -> 降级文本/本地规则。
  - 调用策略：按场景分配主模型与轻量替代模型（见第6节）。
  - 运行时配置：支持热更新各模型 appKey 与 apiBaseUrl；更新后新建连接并对在途请求不打断；所有变更写入审计日志（不含明文）。
- 存储与密钥管理：
  - 每个模型的 appKey 与 apiBaseUrl 存放于 Jetpack DataStore；appKey 使用 Android Keystore（AES-GCM）加密封装，apiBaseUrl 明文但需校验。
  - apiBaseUrl 校验：必须为 https，禁止查询参数/片段；可选域名白名单以降低 SSRF 风险。
  - 环境区分：支持开发/测试/生产三套配置，一键切换与回滚；默认随构建类型选择环境。
  - 敏感日志仅本地保存，外发需脱敏并获家长授权（仅记录 host，不记录 Key 明文）。

6. AI 模型调用策略、降级与隐私/成本评估概要
- 首要原则：隐私优先、成本可控、响应可接受。
- 调用优先级示例：
  - 对话/故事：主用 GEMINI-2.5-PRO，成本或延迟不可接受时退回 GPT-5-PRO（如果已购买低延迟计划）或本地模版化内容。
  - 图像识别：先尝试本地 vision 模型或轻量识别，复杂场景使用 Qwen3-Embedding-8B 向量化 + reranker。若无网络或未授权，则用离线标签建议或提示家长。
  - 图片生成功能：默认在云调用 grok-4-imageGen；家长可选择关闭生成功能以节省成本。
- 降级策略：
  - 网络不可用：提供离线模版、播放预生成故事、提示稍后重试。
  - 模型调用失败：切换到本地简短规则或缓存上次成功结果，并记录 Telemetry。
- 隐私评估要点：
  - 严禁在未经家长明确同意下上传儿童可识别信息。
  - 所有上传数据需最小化（仅发送必要裁剪/特征），并在文档中指明保留期与删除机制。
- 成本评估（概要，详见可选报告）：
  - 建议对高频低价值请求（例如频繁同类短对话）使用本地缓存或轻量模型；对高价值创作（奖励图、家长报告）使用高质量模型并记录消耗。

7. UI/UX 设计要点（“红色优先”）
- 色彩策略：主色调“活力红”（示例：#E53935），辅以柔和中性色与暖色渐变，儿童界面避免过度刺激。
- 交互设计：
  - 大触控目标（最小 48dp），圆角视觉元素，简洁引导文案（语音优先）。
  - 首页：大卡片式今日任务；显著红色“开始”按钮；卡通角色引导栏。
  - 成就页：卡片与动态特效（由 grok-4-imageGen 生成的卡片为可选视觉奖励）。
- 无障碍与护眼：
  - 夜间/护眼模式、字体大小调节、自动朗读优化。
- 家长入口：权限、模型开关、隐私设置、调用记录与开销概览。
 - 家长入口：权限、模型开关、隐私设置、调用记录与开销概览；模型配置中心（逐模型 appKey + apiBaseUrl 管理、https 校验、域名白名单、保存前“测试连接”）。

8. 验收标准与可测试项（精要）
- 每日流程：每次启动能生成并播放 15 分钟内的探索内容（文本/语音/视觉），并记录完成度（Acceptance: 90% 正常生成成功或有合理降级提示）。
- 故事质量：生成故事包含主角、目标与两个简单问题，故事长度控制在 150-300 字（可被 TTS 清晰读出）。
- 图像识别准确率（离线可测场景）：在标准数据集上达到预期阈值（需定义基线 N）。
- 模型降级：在网络断开时能自动降级并给出可替代内容（Acceptance: 无崩溃并显示降级提示）。
- 隐私合规：未经家长授权不上传可识别图像（验收通过检查上传日志）。
 - 配置可测：设置页可分别为各模型配置 appKey 与 apiBaseUrl；apiBaseUrl 必须为 https、通过白名单校验且无查询/片段；“测试连接”按钮成功率 ≥ 99%；更新后新发起请求使用新配置且不影响在途请求；误配时显示可读错误并自动降级。
 - 审计与安全：配置变更记录时间戳、模型、字段类型（Key/URL）与操作者；不记录 Key 明文，URL 仅记录 host。

9. 需求追踪矩阵（概要）
- Requirement -> Task -> Acceptance Criteria（示例）：
  - R1: 每日 15 分钟探索 -> T1: 实现日历与任务调度 -> AC1: 自动在规定时间生成并展示任务。
  - R2: 故事生成（AI） -> T2: 集成 GEMINI-2.5-PRO 接口 -> AC2: 生成故事满足结构与长度限制。
  - R3: 照片互动 -> T3: 实现拍照流程 + 嵌入索引 -> AC3: 成功识别并反馈或合理降级。
（完整矩阵见附件 / 开发交付清单）

10. 交付物与下一步计划
- 交付物：
  - 本文档：docs/final_requirements_last.md（当前文件） — 主需求文档与交付版。
  - 需求追踪矩阵（CSV/Markdown 表）：可生成供 JIRA 或相似工具导入。
  - 可选：docs/model_cost_privacy_assessment.md（按需生成）。
- 下一步（待确认并执行）：
  - 生成完整需求追踪矩阵并写入仓库。
  - 生成逐模型“成本与隐私风险评估”单页（建议优先）。
  - 与架构/开发确认 Kotlin vs Java 技术栈决策（若需要我可给出建议）。

附录：参考文档（可点击）
- [`.kiro/specs/ai-enlightenment-time/requirements.md`](.kiro/specs/ai-enlightenment-time/requirements.md:1)  
- [`.kiro/specs/ai-enlightenment-time/design.md`](.kiro/specs/ai-enlightenment-time/design.md:1)  
- [`.kiro/specs/ai-enlightenment-time/tasks.md`](.kiro/specs/ai-enlightenment-time/tasks.md:1)  
- [`docs/final_requirements.md`](docs/final_requirements.md:1)  
- [`docs/e2e_test_plan.md`](docs/e2e_test_plan.md:1)  
- [`docs/architecture_blueprint.md`](docs/architecture_blueprint.md:1)

结束语  
本文件为交付准备的最终需求稿，已将你要求的模型集成、科技感与“红色优先”策略完整纳入。如需我继续将需求追踪矩阵写成 CSV/Markdown 表或生成“模型调用成本与隐私风险评估”报告，请指示（我将根据选择写入相应文件并更新任务状态）。



# AI启蒙时光 — 最终需求（开发视角修订版）

版本：1.1  
日期：2025-08-27  
作者：项目业务分析师（开发视角修订）

说明  
本文件基于已交付的最终需求稿 [&`docs/final_requirements_last.md`](docs/final_requirements_last.md:1) 进行开发可执行性的补充与量化修订，重点包括：验收指标量化、模型调用接口与调用策略细节、配置与密钥管理流程、安全/隐私实现要点、以及若干实现建议以便开发快速落地。

目录
1. 关键变更摘要
2. 量化验收指标（可测试项）
3. AI 模型调用接口与控制点（工程实现细节）
4. 配置与密钥管理流程
5. 隐私与数据脱敏实现细则
6. 性能与成本控制建议
7. 文档引用与交付清单

1. 关键变更摘要
- 将验收标准中若干模糊项量化为可测指标（见第2节）。  
- 增补 IAIService 接口规范与异步调用模式，明确主/备模型选择与超时策略（见第3节）。  
- 明确 API Key 存储、加密与家长授权流程（见第4节）。  
- 增加上传最小化、向量 TTL 与家长可删机制（见第5节）。  

2. 量化验收指标（可测试项）
- 日常流程成功率：在典型网络环境（4G/家庭WiFi）下，生成并展示 15 分钟探索流程的成功率 ≥ 95%。  
- 故事生成质量：
  - 结构：包含主角、目标、≥2 个互动问题（自动检查关键词与问题标点）通过率 ≥ 98%。  
  - 字数：150–300 字（允许 ±10% 波动）。  
  - TTS 可读时长 ≤ 3 分钟（通过 TTS 预估时长测试）。  
- 语音识别（STT）准确率：安静环境下（SNR ≥ 20dB）儿童短句识别准确率 ≥ 85%。  
- 图像识别/检索：在内部验证集上 Top-1 准确率 ≥ 80%，Top-3 ≥ 92%（具体基线由测试团队与产品确认）。  
- 降级响应：当云调用失败或超过超时（默认 5s）时，界面应在 1s 内展示降级提示并启用本地缓存内容。  
- 计费监控：模型调用计量误差 ≤ 2%（累计 token / image 次计量与实际结算对齐）。  

3. AI 模型调用接口与控制点（工程实现细节）
- 抽象接口 IAIService（示例方法）
  - generateStory(context: StoryContext): Future<StoryResult>
  - generateDialogue(context: DialogueContext): Future<DialogueResult>
  - embedImage(image: ImageBlob): Future<Vector>
  - rerank(candidates: List<Candidate>): Future<List<Candidate>>
  - generateImage(spec: ImageSpec): Future<ImageResult>
- 调用模式
  - 主调用采用异步 Future/Promise 模式，UI 层使用可取消的请求（以防页面退出仍在继续处理）。
  - 超时策略：普通对话/故事 5s，图像生成 15s，embedding 3s。超过超时触发降级逻辑。  
  - 并发限制：单设备同时向云发起模型调用数量限制为 3（避免突发并发引起计费/延迟问题）。  
- 本地优先与缓存
  - 对短文本回复与常见引导问题，优先查询本地 LRU 缓存（TTL 24 小时）。  
  - 对故事类生成，若上次相似主题可复用，则从缓存或预生成包中即时返回。  
- 模型选择策略（示例）
  - 对话/短故事 → GEMINI-2.5-PRO 主用；当成本或延迟阈值触发时，使用本地模板或较轻量模型。  
  - 高质量文案（家长报告）→ GPT-5-PRO（异步）并在家长同意后执行。  
  - 图片向量化 → Qwen3-Embedding-8B（本地优先；若本地不可用且获得授权则云端调用）。  
  - Reranker → bge-reranker-v2-m3 仅在候选数 > N（默认 N=5）且初筛置信度 < 0.7 时调用。  
  - 图像生成 → grok-4-imageGen，异步提示并允许家长审批/计费确认。  
 - 配置模型（新增）
   - Config 数据结构（示例）
     - ModelConfig: { model: String, appKey: EncryptedString, apiBaseUrl: String, environment: Enum(dev/test/prod), updatedAt: Long }
     - GlobalConfig: { activeEnv: Enum, domainWhitelist: List<String> }
   - Config API（示例）
     - updateConfig(model: String, appKey: CharArray?, apiBaseUrl: String?): Result
     - testConnection(model: String): Result<Health>
     - switchEnvironment(env: Enum): Result
   - 校验与应用
     - apiBaseUrl 必须 https、不得包含查询或片段；可选域名白名单校验。
     - 成功更新后重建对应模型的 HTTP 客户端；在途请求不被中断。
     - 失败时返回具体错误码（INVALID_URL / NOT_HTTPS / DOMAIN_NOT_ALLOWED / KEY_MISSING / NETWORK_FAIL）。

4. 配置与密钥管理流程
- 存储位置：每个模型维护 appKey（加密）与 apiBaseUrl（明文校验）；统一存放在 Jetpack DataStore。appKey 以 Android Keystore (AES-GCM) 封装存取。通过 KeyManager 封装读写与轮换，并提供审计接口（仅记录时间/模型/操作类型）。  
- 家长授权：首次触发云调用（上传图像/语音或使用付费模型）时弹出明确授权对话，记录（家长ID、时间戳、授权范围、到期日）。授权可在家长设置中随时撤回。  
- 配置回滚与环境区分：支持开发/测试/生产三套配置；可一键切换活跃环境与回滚；Debug 使用 MockKeyStore。  
- URL 校验与白名单：apiBaseUrl 必须为 https 且无查询/片段；可选启用域名白名单；不符合时拒绝保存并提示。  
- 最小权限原则：仅保存所需最小 appKey 权限；支持 Key 轮换与失效检测（如每 90 天提醒）。  

5. 隐私与数据脱敏实现细则
- 上传最小化：
  - 图像上传前自动进行裁切（仅保留中心物体区域）与模糊化元数据（去除 EXIF、GPS、时间戳、设备ID）；只上传必要的像素或特征摘要。  
  - 语音上传前做 VAD（Voice Activity Detection）并仅上传截取的语音片段与转录文本，不上传原始长录音（除非家长授权）。  
- 向量/索引管理：
  - 本地索引默认启用，云端索引仅在家长授权并在确认保留期（默认 30 天）后上传。客户端定期（如 30 天）自动清理过期向量。  
  - 向量/索引的删除权必须对家长开放（立即删除并返回操作结果）。  
- 审计与日志：
  - 所有上传动作、模型调用（含模型名、调用类型、估算消耗）记录可供家长查看的汇总日志。原始儿童数据不包含在外发日志中。  
- 同意记录：
  - 每次家长授权动作需记录时间戳、授权内容、同意文本及哈希，便于合规审计。  

6. 性能与成本控制建议（便于工程实现）
- 本地能力评估：在 CI 测试矩阵中增加典型低端设备（2GB RAM，Android 7）与中端设备（4GB RAM）上的基准测试，用于决定是否可在本地运行 embedding。  
- 缓存策略
  - LRU 缓存用于短文本/短对话（最大条数 500，TTL 24h）。  
  - 大型生成（图片/长文本）使用持久缓存并限制总占用（默认 200MB）。  
- 计费预警
  - 在客户端实现估算函数（基于 token 或 image 次）并在家长端显示本月消耗预估；当超出阈值（预设 80%）时触发提示与自动降级。  
- 测试覆盖
  - 必须为 IAIService 的降级路径、超时以及缓存命中/失效路径编写单元与集成测试。  

7. 文档引用与交付清单
- 本修订基于并引用： [&`docs/final_requirements_last.md`](docs/final_requirements_last.md:1)、[&`docs/model_cost_privacy_assessment.md`](docs/model_cost_privacy_assessment.md:1)、[&`docs/requirement_trace_matrix.md`](docs/requirement_trace_matrix.md:1)。  
- 输出文件：本文件为 [&`docs/final_requirements_rev.md`](docs/final_requirements_rev.md:1)。后续可根据需要导出成 Word 或进一步拆分为工程任务卡（JIRA CSV）。

结束语  
已将文档按产品与开发视角补充量化验收指标及实现细节，建议下一步由开发/架构团队评审 IAIService 接口签名与 KeyManager 接口后，分配实现任务并在 sprint 计划中加入性能基准与隐私合规检查项。
