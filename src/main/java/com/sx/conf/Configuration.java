package com.sx.conf;

import java.io.InputStream;
import java.util.Properties;

/**
 * 配置管理组件
 *
 * 配置管理组件可以复杂也可以简单，对于简单的配置管理组件来说，只要开发一个类，可以在第一次访问它的时候，就从对应的properties文件中
 * 读取配置项，并提供外界获取某个位置key对应的value的方法
 * 如果是特别复杂的配置管理组件，那么可能需要使用一些设计模式，比如单例模式、解释器模式，可能需要管理多个不同的properties，甚至是xml
 * 类型的配置文件
 * 此处开发一个简单的配置管理组件
 *
 */
public class Configuration {

    private static Properties prop = new Properties();

    /**
     * 静态代码块
     *
     * Java中，类第一次使用的时候，会被JVM中的类加载器从磁盘上的.class文件中加载出来，然后为类创建一个Class对象，代表这个类
     *
     * 类在第一次加载时，会进行自身的初始化，初始化类的时候就会执行类的静态代码块
     * 因此，对于配置管理组件，就在静态代码块中，编写读取配置文件的代码，这样的话，外界代码第一次调用这个Configuration类的静态方法
     * 时，就会加载配置文件中的数据
     * 而且，类的初始化在整个JVM生命周期内有且仅有一次，也就是说配置文件只会加载一次，以后就是重复使用
     */
    static {
        try {
            // 通过“类名.class”的方式，就可以获取到这个类在JVM中对应的class对象
            // 然后再通过这个class对象的getClassLoader()方法，就可以获取到当初加载这个类的JVM中的类加载器（ClassLoader）
            // 然后调用ClassLoader的getResourceAsStream()方法就可以用类加载器，去加载 类加载路径中指定的文件
            // 最终可以获取到一个针对指定文件的输入流（InputStream）
            InputStream in = Configuration.class
                    .getClassLoader().getResourceAsStream("my.properties");
            // 可以把key value对加载进去了
            prop.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回字符串类型
     *
     * 第一次外界代码，调用ConfigurationManager类的getProperty静态方法时，JVM内部会发现
     * ConfigurationManager类还不在JVM的内存中
     *
     * 此时JVM，就会使用自己的ClassLoader（类加载器），去对应的类所在的磁盘文件（.class文件）中
     * 去加载ConfigurationManager类，到JVM内存中来，并根据类内部的信息，去创建一个Class对象
     * Class对象中，就包含了类的元信息，包括类有哪些field（Properties prop）；有哪些方法（getProperty）
     *
     * 加载ConfigurationManager类的时候，还会初始化这个类，那么此时就执行类的static静态代码块
     * 此时咱们自己编写的静态代码块中的代码，就会加载application.properites文件的内容，到Properties对象中来
     *
     * 下一次外界代码，再调用ConfigurationManager的getProperty()方法时，就不会再次加载类，不会再次初始化
     * 类，和执行静态代码块了，所以也印证了，我们上面所说的，类只会加载一次，配置文件也仅仅会加载一次
     * @param key
     * @return 字符串类型的value
     */
    public static String getProperty(String key) {
        return prop.getProperty(key);
    }


    /**
     * 返回布尔类型
     * @param key
     * @return 布尔类型的value
     */
    public static Boolean getBoolean(String key) {
        String value = getProperty(key);
        try {
            return Boolean.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 返回long类型
     * @param key
     * @return long类型的value
     */
    public static Long getLong(String key) {
        String value = getProperty(key);
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * 返回int类型
     * @param key
     * @return
     */
    public static Integer getInteger(String key) {
        String value = getProperty(key);
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
