package com.sx.dao.impl;

import com.sx.dao.TaskDAO;
import com.sx.domain.Task;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 任务管理DAO测试类
 */
public class TaskDAOImplTest {

    @Test
    public void findById() {
        TaskDAO taskDAO = DAOFactory.getTaskDAO();
        Task task1 = taskDAO.findById(1);
        assert (task1.getTaskName().equals("测试任务001"));

        Task task2 = taskDAO.findById(2);
        assert (task2.getTaskName() == null);


    }
}