# 项目相关速记

## 远程开发：`Deployment`

### 程序的运行、编译构建[打包]、部署
> `mvn spring-boot:run`：运行SpringBoot项目的命令
> `mvn package`：打包项目（打包为一个jar包）
> `java -jar xxx.jar --spring.profiles.active=prod`：选择配置，并运行指定的jar包

### 远程开发之Debug
1.  
2. `java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar xxx.jar --spring.profiles.active=prod`：启用远程开发Debug模式

