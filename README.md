# car_o2o
一个练手的项目

## 技术选型:
 - 核心框架：Spring Boot 
 - 权限框架：Apache Shiro 
 - 模板引擎：Freemarker 
 - 持久层框架：MyBatis 
 - 数据库连接池：Alibaba Druid 
 - 日志管理：Logf4j 
 - 视图框架：Spring MVC 
 - 定时器：Quart 
 - 数据库连接池：Druid 
 - 页面交互：layui 
 - 下拉框：bootstrap-select 
 - 文件上传：Bootstrap File Input 

## 项目特点:
 - 简单,易上手的后台管理系统 
 - 友好的代码注释,便于阅读 
 - 灵活的权限管理 
    
## 项目结构
 - car.base: 系统管理 
 - car.business: 业务管理 
 - car.common: 共用板块 
 - car.frontend: 前段控制 
 - car.shiro: 权限管理 
  
## 本地部署
 - 运行: 启动类CarO2O的主方法启动 
 - 登陆页面: http://localhost/index 
 - 登陆账号密码: admin/admin123 
  
## 软件需求
 - JDK: 8+ 
 - Mysql: 5.0+ 
 - Maven: 3.0+ 
 - TomCat: 8+ 

## 功能板块
  - 系统管理:
      -  用户管理: 增删改查, 导出 , 导入 
      -  角色管理: 增删改查, 导出 , 权限分配 
      -  菜单管理 , 部门管理 , 字典管理 , 参数管理 , 公告通知 : 增删改查 
      -  岗位管理: 前端页面存在bug 
  - 业务管理: 
      -  养修预约业务: 增删改查, 用户到店 , 用户结算明细 , 取消预约 
      -  养修服务单项: 增改查 , 发起审核 , 商品的上架与下架 
      -  服务结算单: 增删改查 , 服务结算明细 
      -  套餐审核: 列表 , 我的待办 , 我的已办 
           - 审批审核, 查看业务流程图 , 查看业务进程 
      -  请假流程审核: 列表 , 我的待办 , 我的已办 
           - 发起申请 , 请假审批 
            
### 功能实现流程图
      详见开发文档(未上传) 
