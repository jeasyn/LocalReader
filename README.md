我的阅读是一款能运行在Android端的本地txt文件阅读器

## 实现的功能

 - 搜索
   - 自动搜索/storage/emulated/0目录或子目录下的txt文件
   - 添加本地txt文件到书架
 - 显示
   - 利用表格布局显示小说缩略图
   - 可删除添加到书架上的小说
   - 显示小说的详细信息（路径、文件大小、修改时间）
 - 阅读
 	 - 显示章节目录
 	 - 添加书签 
 	 - 夜间或日间模式
 	 - 阅读设置
 	   - 对亮度、字体大小、主题样式进行设置

### 遇到的问题

 1. 开发时用的时6.0版本，测试时10.0，搜索不到本地文件
		解决：在AndroidManifest.xml文件application中添加`android:requestLegacyExternalStorage="true"`
 2. 使用UniversalDetector获取编码导入依赖时，编译时报

	> Program type already present:
	> org.mozilla.universalchardet.CharsetListener
	

 	解决：在libs导入juniversalchardet-1.0.3.jar包即可
3. 使用ThreadFactoryBuilder线程池导入依赖时，必须使用java8，在build.gradle的android闭包中添加

	```java
	compileOptions {
		sourceCompatibility JavaVersion.VERSION_1_8
		targetCompatibility JavaVersion.VERSION_1_8
	}
	```
4. 开发时使用6.0版本，文件搜索不到，在android6.0之前，要想获取android存储权限只需在AndroidManifest.xml文件中添加

	```xml
	<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
	<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
	```
	而6.0之后，还需在activity中添加代码进行权限的请求（见MainActivity的verifyStoragePermissions和onRequestPermissionsResult方法）

这里只列出了几个对我来说较大的问题，其他就不一一列举了

### 截图
<img src="E:\ProgramData\Typora\typora-user-images/bookshelf.jpg" alt="bookshelf" style="zoom: 25%;" />

<img src="E:\ProgramData\Typora\typora-user-images/import.jpg" alt="import" style="zoom: 25%;" />

<img src="E:\ProgramData\Typora\typora-user-images/read.jpg" alt="read" style="zoom: 25%;" />

<img src="E:\ProgramData\Typora\typora-user-images/settings.jpg" alt="settings" style="zoom: 25%;" />

<img src="E:\ProgramData\Typora\typora-user-images/catalog.jpg" alt="catalog" style="zoom: 25%;" />

<img src="E:\ProgramData\Typora\typora-user-images/bookmark.jpg" alt="bookmark" style="zoom: 25%;" />