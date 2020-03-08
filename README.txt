1.生成壳aar
 shell module执行 assembleDebug生成Shell-debug.aar
2.生成原始app
 app module执行 assembleDebug生成app-debug.apk
3.生成加固的app
 将Shell-debu.aar与app-debug.apk拷贝到dexshelltool module中的resource文件夹中，其中已经放入了签名jks文件
 执行MyClass的main函数，会在resource/outputs下面生成app-unsigned.apk与app-signed.apk