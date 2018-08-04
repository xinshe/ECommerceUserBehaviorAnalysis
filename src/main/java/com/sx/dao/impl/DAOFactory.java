package com.sx.dao.impl;

import com.sx.dao.TaskDAO;

/**
 * DAO工厂类
 */
public class DAOFactory {

    /**
     * 获取任务管理DAO
     */
    public static TaskDAO getTaskDAO() {
        return new TaskDAOImpl();
    }
}
