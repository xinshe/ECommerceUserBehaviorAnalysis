package com.sx.jdbc;

import com.sx.conf.Configuration;
import com.sx.constant.Constants;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * JDBC辅助组件
 *
 * 在正式项目的代码编写中，不要出现任何hard code（硬编码）的字符
 * 比如'李四'，'com.mysql.jdbc.Driver'
 * 所有这些东西都要通过常量来封装和使用
 */

public class JDBCHelper {

    /**
     *  第一步：在静态代码块中，直接加载数据库驱动
     *  加载驱动，不要硬编码
     *
     *  项目要尽量做成可配置的，配置项与代码分离
     *  在my.properties中添加
     *     jdbc.driver=com.mysql.jdbc.Driver
     *  在Constants.java中添加
     *     String JDBC_DRIVER = "jdbc.driver";
     */
    static {
        try {
            String driver = Configuration.getProperty(Constants.JDBC_DRIVER);
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  第二步，实现JDBCHelper的单例化
     *     因为它的内部要封装一个简单的内部的数据库连接池
     *     为了保证数据库连接池有且仅有一份，所以就通过单例的方式
     *     保证JDBCHelper只有一个实例，实例中只有一份数据库连接池
     */
    private static JDBCHelper instance = null;

    // 获取单例
    public static JDBCHelper getInstance() {
        if(instance == null) {
            synchronized (JDBCHelper.class) {
                if(instance == null)
                    instance = new JDBCHelper();
            }
        }
        return instance;
    }

    // 数据库连接池
    private LinkedList<Connection> datasource = new LinkedList<>();

    /**
     * 第三步：实现单例的过程中，创建唯一的数据库连接池
     *
     * 私有化构造方法
     *
     * JDBCHelper在整个程序运行生命周期中，只会创建一次实例
     * 在这一次创建实例的过程中，就会调用JDBCHelper（）的方法
     * 此时，就可以在构造方法中，去创建自己唯一的一个数据库连接池
     */
    private JDBCHelper() {
        // 数据库连接池的大小（要放多少个数据库连接），可以通过在配置文件中配置的方式来灵活设定
        int datasourceSize = Configuration.getInteger(Constants.JDBC_DATASOURCE_SIZE);

        // 数据库连接的相关信息
        String url = Configuration.getProperty(Constants.JDBC_URL);
        String user = Configuration.getProperty(Constants.JDBC_USER);
        String password = Configuration.getProperty(Constants.JDBC_PASSWORD);

        // 创建指定数量的数据库连接，并放入数据库连接池中
        for (int i = 0; i < datasourceSize; i++) {
            try {
                Connection connection = DriverManager.getConnection(url,user,password);
                datasource.push(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 第四步，提供获取数据库连接的方法
     * 有可能，获取的时候连接池已经用光了，暂时获取不到数据库连接
     * 所以要编写一个简单的等待机制，等待获取到数据库连接
     *
     * synchronized设置多线程并发访问限制
     */
    private synchronized Connection getConnection() {
        while (datasource.size() == 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return datasource.poll();
    }

    /**
     * 第五步：开发增删改查的方法
     * 1.执行增删改SQL语句的方法
     * 2.执行查询SQL语句的方法
     * 3.批量执行SQL语句的方法
     */

    /**
     * 执行增删改SQL语句
     * @param sql
     * @param params
     * @return 影响的行数
     */
    public int executeUpdate(String sql, Object[] params) {
        int rtn = 0;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i+1, params[i]);
            }

            rtn = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(connection != null) {
                datasource.push(connection);
            }
        }
        return rtn;
    }

    // 执行查询SQL语句
    public void executeQuery(String sql, Object[] params,
                             QueryCallback callback) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i+1, params[i]);
            }
            rs = preparedStatement.executeQuery();

            callback.process(rs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if(preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if(connection != null)
                datasource.push(connection);
        }
    }

    /**
     * 内部类：查询回调接口
     */
    public interface QueryCallback {
        //处理查询结果
        void process(ResultSet rs) throws Exception;
    }

    /**
     * 批量执行SQL语句
     *
     * 批量执行SQL语句，是JDBC中的一个高级功能
     * 默认情况下，每次执行一条SQL语句，就会通过网络连接，向MySQL发送一次请求
     * 但是，如果在短时间内要执行多条结构完全一样的SQL，只是参数不同
     * 虽然使用PreparedStatment这种方式，可以只编译一次SQL，提高性能，
     * 但是，还是对于每次SQL都要向MySQL发送一次网络请求
     *
     * 可以通过批量执行SQL语句的功能优化这个性能
     * 一次性通过PreparedStatement发送多条SQL语句，可以几百几千甚至几万条
     * 执行的时候，也仅仅编译一次就可以
     * 这种批量执行SQL语句的方式，可以大大提升性能
     *
     * @param sql
     * @param paramsList
     * @return每条SQL语句影响的行数
     */

    public int[] executeBatch (String sql, List<Object[]> paramsList) {
        int[] rtn = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = getConnection();
            // 第一步：使用Connection对象，取消自动提交
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(sql);

            // 第二步：使用PreparedStatement.addBatch()方法加入批量的SQL参数
            for (Object[] params: paramsList) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i+1, params[i]);
                }
                preparedStatement.addBatch();

                // 第三步：使用PreparedStatement.executeBatch（）方法，执行批量SQL语句
                rtn = preparedStatement.executeBatch();
            }
            // 最后一步，使用Connection对象，提交批量的SQL语句
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null)
                datasource.push(connection);
        }
        return rtn;
    }
}
