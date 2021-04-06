这是一个动态服务发布模型的初体验。

在不重启hotSwap进程的前提下，可以运行期新增http服务
在不重启hotSwap进程的前提下，可以运行期新增http服务
在不重启hotSwap进程的前提下，可以运行期新增http服务

重点需要关注的：
1. ModuleClassLoader作为自定义类加载器加载外部依赖包，ModuleClassLoader会先寻找外部依赖包下的class, 未找到才委托给父类加载器寻找。
2. 发送http请求https://127.0.0.1:8081/refresh后 RefreshClass将加载给定目录下jar包，获得jar包内的class，完成类的控制反转和依赖注入。
3. CommonController接收http服务请求，根据http请求路径找到容器中的对象和方法，根据http请求参数进行对象反射调用。
4. 外部依赖包 ext-1.0-SNAPSHOT.jar，当前包含com.example.service.impl.BankServiceImpl和com.example.utils.DBUtils



初体验过程：
1. 数据库的连接更改成你本地数据库的连接，本地库新增表和数据，开发工具启动程序(打成jar包部署才能更真实的模拟,某些情况下IDEA不容易出现NoClassDefFoundException)
CREATE TABLE "bank" (
"id" int8 NOT NULL,
"money" int8,
"name" varchar(255) COLLATE "default"
)
WITH (OIDS=FALSE)
;
INSERT INTO "bank" VALUES ('666', '666', 'xxx');
2. 发送 post 请求：curl -H "Content-Type:application/json" -X POST -d '{"java.lang.Integer":666}' http://127.0.0.1:8081/bankServiceImpl/getBank
    报错 No bean named 'bankServiceImpl'，因为还没把com.example.service.impl.BankServiceImpl和com.example.utils.DBUtils刷到spring容器中
3. 发送 post 请求：curl -H "Content-Type:application/json" -X POST -d 'ext-1.0-SNAPSHOT.jar父文件夹路径' https://127.0.0.1:8081/refresh
4. 此时外部依赖包中的class文件已经在spring容器完成了控制反转和依赖注入
5. 发送 post 请求：curl -H "Content-Type:application/json" -X POST -d '{"java.lang.Integer":666}' http://127.0.0.1:8081/bankServiceImpl/getBank
   能够得到正常结果

以上，在没重启hotSwap进程的前提下，在程序运行期动态提供了 http://127.0.0.1:8081/bankServiceImpl/getBank的服务