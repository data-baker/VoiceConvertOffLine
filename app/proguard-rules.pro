# 指定混淆时采用的算法
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# 代码混淆的压缩比例(0-7) , 默认为5 , 一般不需要改
-optimizationpasses 5

# 混淆后类名都小写
-dontusemixedcaseclassnames

# 指定不去忽略非公共的库的类(即混淆第三方, 第三方库可能自己混淆了 , 可在后面配置某些第三方库不混淆)
# 默认跳过，有些情况下编写的代码与类库中的类在同一个包下，并且持有包中内容的引用，此时就需要加入此条声明
-dontskipnonpubliclibraryclasses

# 指定不去忽略非公共的库的类的成员
-dontskipnonpubliclibraryclassmembers

# 不做预检验，preverify是proguard的四个步骤之一
# Android不需要preverify，去掉这一步可以加快混淆速度
-dontpreverify

# 有了verbose这句话，混淆后就会生成映射文件
# 包含有类名->混淆后类名的映射关系
# 然后使用printmapping指定映射文件的名称
-verbose
-printmapping proguardMapping.txt

# 保护代码中的Annotation不被混淆
# 这在JSON实体映射时非常重要，比如fastJson
-keepattributes *Annotation*,InnerClasses

# 避免混淆泛型
# 这在JSON实体映射时非常重要，比如fastJson
-keepattributes Signature

#抛出异常时保留源文件和代码行号
#-keepattributes SourceFile,LineNumberTable

#Android SDK默认的proguard-android-optimize.txt用*,但用某些版本的AS和Gradle时,不能处理内嵌enum的情况
-keepclassmembers enum ** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#讯飞
-keepattributes Signature


 -keepclasseswithmembers class * {
     public <init>(android.content.Context);
 }
 -keepclasseswithmembers class * {
     public <init>(android.content.Context, android.util.AttributeSet);
 }
 -keepclasseswithmembers class * {
     public <init>(android.content.Context, android.util.AttributeSet, int);
 }



###############################################################################
#                                                                             #
#                    离线变声sdk混淆，主要注意配置以下信息就行。                     #
#                                                                             #
###############################################################################



#如果用用到Gson解析包的，直接添加下面这几行就能成功混淆，不然会报错。
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }
##---------------End: proguard configuration for Gson  ----------


#-------------- okhttp3 start-------------
# OkHttp3
# https://github.com/square/okhttp
# okhttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.* { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# okhttp 3
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Okio
-dontwarn com.squareup.**
-dontwarn okio.**
-keep public class org.codehaus.* { *; }
-keep public class java.nio.* { *; }
#----------okhttp end--------------


-keepattributes Exceptions,InnerClasses,...
-keep class com.baker.voiceconvert.offline.bean.** { *; }
-keep public class com.baker.voiceconvert.offline.VoiceConvertOffLineManager{*;}
-keep public class com.baker.jni.voice.convert.VoiceConvertEngine{*;}
-keep public class com.baker.voiceconvert.offline.callback.OffLineConvertCallBack{*;}
-keep public class com.baker.voiceconvert.offline.util.WriteLog{*;}
-keep public class com.baker.voiceconvert.offline.callback.ConvertInnerCallBack{*;}
-keep public class com.baker.voiceconvert.offline.util.ThreadPoolUtil{*;}
-keep public class com.baker.voiceconvert.offline.util.HLogger{*;}