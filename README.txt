这是一个动态服务发布模型的初体验。

在不重启hotSwap进程的前提下，可以运行期新增http服务
在不重启hotSwap进程的前提下，可以运行期新增http服务
在不重启hotSwap进程的前提下，可以运行期新增http服务

操作过程：
解开本地TestController，Person注释，本地进行编译。
将本地编译的TestController，Person字节码丢到ext-1.0-SNAPSHOT.jar中（使用解压工具打开）。
清除本地编译的字节码文件，注释掉TestController，Person两个类，启动项目
postman请求 https://127.0.0.1:8081/xxx 此时无法请求
postman请求 https://127.0.0.1:8081/jarLoad   json参数 C:/Users/Administrator/Desktop/hotSwap(ext-1.0-SNAPSHOT.jar所在目录)
postman再次请求 https://127.0.0.1:8081/xxx 成功返回


以上，在没重启hotSwap进程的前提下，在程序运行期动态提供了 https://127.0.0.1:8081/xxx 服务

字节码替换技术实现方法的逻辑替换：引入javassist，tools.jar 设置VM options
字节码替换技术实现方法的逻辑替换：引入javassist，tools.jar 设置VM options
字节码替换技术实现方法的逻辑替换：引入javassist，tools.jar 设置VM options

此时调用https://127.0.0.1:8081/xxx请求 响应的是Person信息
postman请求 https://127.0.0.1:8081/changeMethod  from-data 
参数：	p1: com.example.test.TestController
		p2: xxx
		p3: { return String.valueOf("哈哈哈");}
之后再次请求 https://127.0.0.1:8081/xxx  返回 "哈哈哈"而不是Person信息,
说明改变了com.example.test.TestController.xxx的执行逻辑

