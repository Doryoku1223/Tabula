1. 核心用户流程 (User Flow)
启动与权限： 首次启动请求 READ_MEDIA_IMAGES 权限。

卡片堆叠 (The Session)：

系统从相册加载 15 张图片（有两种算法）。

用户看到一张圆角卡片。

交互 (Interaction)：

左/右滑： 切换查看上一张/下一张（保留图片）。

上滑： 标记删除（Mark for Deletion）。图片飞出屏幕，加入待删除列表，并在底部显示一个小计数器（如 "3/15 to delete"）。

撤销： 点击底部的计数器/回收站图标，打开暂存区，点击图片可撤销标记。

结算 (The Settlement)：

当 15 张卡片全部滑完，展示“结算页”。

显示：“本组标记了 X 张废片”。

按钮 A：“一键清理” (Burn X photos)。

按钮 B：“保留并开始下一组” (Keep & Next)。

物理删除： 点击“一键清理” -> 触发 Android 系统级弹窗 -> 确认 -> 图片从手机相册消失。

2. UI/UX 详细规范
设计风格： Minimalist / Art Gallery。

配色方案：

Background: #FFFFFF (纯白)

Card Shadow: 极淡的灰色阴影，制造悬浮感。

Text/Icons: #1A1A1A (近黑)。

Accent (Selected/Active): #000000 (纯黑)。

卡片样式：

圆角：24dp。

比例：自适应，但在屏幕中保持固定边距，像画框一样。

动画物理： 使用弹簧阻尼（Spring Spec），拒绝生硬的线性移动。

3. 技术约束 (Android Specifics)
架构： MVVM (Model-View-ViewModel)。

UI 框架： Jetpack Compose (必须)。

图片加载： Coil (性能最好)。

权限管理：

必须适配 Android 10+ (Scoped Storage)。

删除必须使用 ContentResolver 和 RecoverableSecurityException 处理系统弹窗。