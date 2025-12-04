# Bank App - 现代化银行应用

这是一个采用现代化设计的银行应用前端项目，基于 Vue 3 + TypeScript + Vant UI + Tailwind CSS 构建。

## ✨ 特性

- 🎨 **现代化 UI 设计** - 参考 Airbnb、Instagram、Spotify 等主流应用的设计风格
- 🌓 **深色/浅色模式** - 支持主题切换，提供舒适的视觉体验
- 📱 **响应式设计** - 完美适配手机、平板和桌面端
- ⚡ **流畅动画** - 精心设计的过渡动画和交互反馈
- 🎯 **卡片式布局** - 清晰的信息层次和视觉焦点
- 🔤 **优质字体** - 使用 Roboto 和 Open Sans 字体
- 🎭 **Material Design** - 遵循 Material Design 设计规范

## 🚀 技术栈

- **框架**: Vue 3 + TypeScript
- **UI 组件**: Vant 4.x (移动端 UI 组件库)
- **CSS 框架**: Tailwind CSS 3.x
- **状态管理**: Pinia
- **路由**: Vue Router
- **国际化**: Vue I18n
- **构建工具**: Vite

## 📦 安装依赖

```bash
npm install
```

## 🏃 运行开发服务器

```bash
npm run dev
```

应用将在 `http://localhost:5173/` 启动

## 🏗️ 构建生产版本

```bash
npm run build
```

## 🎨 主题切换

应用支持深色/浅色模式切换：

1. 点击顶部导航栏右侧的 **月亮/太阳** 图标即可切换主题
2. 主题设置会自动保存到 localStorage，下次访问时会记住你的选择
3. 所有页面和组件都完美支持深色模式

## 🎯 主要功能

### 首页 (Home)
- 资产总览卡片
- 快捷功能入口（扫码、转账、外币兑换等）
- 账户列表

### 账户 (Account)
- 卡片列表展示
- 交易流水查询
- 外币兑换功能

### 转账 (Transfer)
- 快速转账
- 跨境汇款
- AA收款/红包

### 个人中心 (Profile)
- 个人信息管理
- 密码设置
- 消息中心
- 电子政务链接

## 🎨 设计特色

### 配色方案
- **主色调**: 蓝色渐变 (#007bff → #0056b3)
- **辅助色**: 紫色渐变 (#6a11cb → #2575fc)
- **深色模式**: 深蓝灰色调 (#1a1a2e → #16213e)
- **亮色模式**: 浅蓝灰色调 (#f5f7fa → #e4edf9)

### 动画效果
- 页面淡入淡出
- 卡片上滑动画
- 元素缩放弹性动画
- 悬停交互效果
- 渐变背景旋转动画

### 响应式断点
- 移动端: < 768px
- 平板端: 768px - 1199px  
- 桌面端: ≥ 1200px

## 📱 浏览器支持

- Chrome (推荐)
- Firefox
- Safari
- Edge

## 📄 许可证

MIT License

## 🙏 致谢

感谢以下开源项目：
- [Vue.js](https://vuejs.org/)
- [Vant UI](https://vant-ui.github.io/vant/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Pinia](https://pinia.vuejs.org/)
