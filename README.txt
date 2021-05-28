这是一个动态服务发布模型的初体验。

在不重启hotSwap进程的前提下，可以运行期新增http服务
在不重启hotSwap进程的前提下，可以运行期新增http服务
在不重启hotSwap进程的前提下，可以运行期新增http服务

操作过程：
解开本地TestController，Person注释，本地进行编译。
将本地编译的TestController，Person字节码丢到ext-1.0-SNAPSHOT.jar中（使用解压工具打开）。
清除本地编译的字节码文件，启动项目，注释掉TestController，Person两个类
postman请求 https://127.0.0.1:8081/xxx 此时无法请求
postman请求 https://127.0.0.1:8081/refresh   json参数 C:/Users/Administrator/Desktop/hotSwap(ext-1.0-SNAPSHOT.jar所在目录)
postman再次请求 https://127.0.0.1:8081/xxx 成功返回


以上，在没重启hotSwap进程的前提下，在程序运行期动态提供了 https://127.0.0.1:8081/xxx 服务