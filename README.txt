这是一个动态服务发布模型的初体验。

在不重启hotSwap进程的前提下，可以运行期新增http服务
在不重启hotSwap进程的前提下，可以运行期新增http服务
在不重启hotSwap进程的前提下，可以运行期新增http服务

操作过程：
解开本地TestController，Person注释，本地进行编译。
将本地编译的TestController，Person字节码丢到ext-1.0-SNAPSHOT.jar中（使用解压工具打开）。
清除本地编译的字节码文件，注释掉TestController，Person两个类，启动项目
postman请求 http://127.0.0.1:8081/xxx 此时无法请求
postman请求 http://127.0.0.1:8081/jarLoad?dir=C:/Users/Administrator/Desktop/hotSwap (dir参数为jar所在目录)
postman再次请求 http://127.0.0.1:8081/xxx 成功返回


以上，在没重启hotSwap进程的前提下，在程序运行期动态提供了 http://127.0.0.1:8081/xxx 服务

字节码替换技术实现方法的逻辑替换：引入javassist，tools.jar 设置VM options
字节码替换技术实现方法的逻辑替换：引入javassist，tools.jar 设置VM options
字节码替换技术实现方法的逻辑替换：引入javassist，tools.jar 设置VM options
VM options -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

此时调用http://127.0.0.1:8081/xxx请求 响应的是Person信息
postman请求 http://127.0.0.1:8081/changeMethod  from-data 
参数：	className: com.example.test.TestController
		methodName: xxx
		logic: { return String.valueOf("哈哈哈");}
之后再次请求 http://127.0.0.1:8081/xxx  返回 "哈哈哈"而不是Person信息,
说明改变了com.example.test.TestController.xxx的执行逻辑


动态编译技术：解压当前jar包，构建 -classpath编译环境并动态编译，加载编译后的class文件并使用spring的bean注册，使@RestController注解生效
动态编译技术：解压当前jar包，构建 -classpath编译环境并动态编译，加载编译后的class文件并使用spring的bean注册，使@RestController注解生效
动态编译技术：解压当前jar包，构建 -classpath编译环境并动态编译，加载编译后的class文件并使用spring的bean注册，使@RestController注解生效

先试试postman请求 http://远程ip:8081/xxx 此时无法访问
上传java源文件到指定目录 TestController，Person两个java文件
postman请求 http://远程ip:8081/upload?dir=E:/code  form-data file 选中要上传的文件
postman请求 http://远程ip:8081/javaFileLoad?dir=E:/code
这时访问链接 http://远程ip:8081/xxx 可以访问
可以上传其他功能的@RestController源文件,取决于你的脑洞有多大