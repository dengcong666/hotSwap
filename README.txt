这是一个动态服务发布模型的初体验。

重点需要关注的：
1.ModuleClassLoader作为自定义类加载器加载外部依赖包，ModuleClassLoader能找到的class不会委托给父类加载器，找不到才委托给父类加载器。
2.发送http请求https://127.0.0.1/refresh后 RefreshClass将加载参数所给定的目录下外部依赖jar包，获得相应的字节码后完成类的控制反转和依赖注入
3.CommonController接收http服务请求，解析路径和参数，找到容器中的对象和方法，进行反射调用
4.外部依赖包已经上传，ext-1.0-SNAPSHOT.jar包含com.example.service.impl.BankServiceImpl和com.example.utils.DBUtils



使用方式：
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
3. 发送 post 请求：curl -H "Content-Type:application/json" -X POST -d '动态加载的外部依赖包路径' https://127.0.0.1/refresh
4. 此时外部依赖包中的class文件已经在spring容器完成了控制反转和依赖注入
5. 发送 post 请求：curl -H "Content-Type:application/json" -X POST -d '{"java.lang.Integer":666}' http://127.0.0.1:8081/bankServiceImpl/getBank
   能够得到正常结果

