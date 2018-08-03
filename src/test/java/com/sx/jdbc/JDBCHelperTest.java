package com.sx.jdbc;

import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JDBCHelperTest {

    JDBCHelper jdbcHelper = null;

    @Before
    public void before() {
        jdbcHelper = JDBCHelper.getInstance();
    }

    @Test
    public void getInstance() {
        JDBCHelper tmp = JDBCHelper.getInstance();
        assert(tmp == jdbcHelper);
    }

    @Test
    public void executeUpdate() {
        String sql = "insert into test_user(id, name) values(?,?)";
        jdbcHelper.executeUpdate(sql, new Object[]{3,"李四"});
    }

    /**
     *  设计一个内部接口QueryCallback
     *  那么在执行查询语句的时候，我们就可以封装和指定自己的查询结果的处理逻辑
     *  封装在一个内部接口的匿名内部类对象中，传入JDBCHelper的方法
     *  在方法内部，可以回调我们定义的逻辑，处理查询结果
     *  并将查询结果放入外部的变量中
     */
    @Test
    public void executeQuery() {
        final Map<Integer, String> testUser = new HashMap<>();
        jdbcHelper.executeQuery("select id,name from test_user where id=?",
                new Object[]{2},
                new JDBCHelper.QueryCallback() {

            public void process(ResultSet rs) throws Exception {
                if(rs.next()) {
                    int id = rs.getInt(1);
                    String name = rs.getString(2);

                    // 匿名内部类的使用，有一个很重要的知识点：如果要访问外部类中的一些成员，比如方法内的局部变量
                    // 那么，必须将局部变量声明为final类型才能访问，否则是访问不了的
                    testUser.put(id, name);
                }
            }
        });
        System.out.println(testUser);
    }

    @Test
    public void executeBatch() {
        String sql = "insert into test_user(id,name) value(?,?)";
        List<Object[]> paramsList = new ArrayList<>();
        paramsList.add(new Object[]{1,"麻子"});
        paramsList.add(new Object[]{2,"王五"});

        jdbcHelper.executeBatch(sql, paramsList);
    }
}