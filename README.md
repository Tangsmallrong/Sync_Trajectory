# Sync_Trajectory - 开发中
基于 SpringBoot + Vue3 同步轨迹-同伴匹配项目，通过标签匹配来引导用户找到相似兴趣和思维模式的伙伴，在这个由标签绘制的精准轨迹中，为你描绘出通往志同道合伙伴的路径。

## 1. 需求分析

> 目标：帮助找到志同道合的伙伴

- 用户添加/修改标签

- 允许用户根据标签去搜索其他用户

- 组队

- 相似度计算算法进行推荐

## 2. 技术选型

### 2.1 前端

- Vue 3 开发框架（提高页面开发的效率）

- Vant UI 组件库（基于 Vue 的移动端组件）（React 版 Zent）

- TypeScript 

- Vite 2 脚⼿架 （打包工具，快！）

- Axios 请求库

- Nginx 来单机部署

### 2.2 后端

- SpringBoot 2.7.x 框架 

- SpringMVC + MyBatis + MyBatis Plus（提高开发效率）

- MySQL 数据库 

-  Redis 缓存
  
- Swagger + Knife4j 接⼝⽂档 （前后端协调更⽅便）

- 相似度匹配算法
