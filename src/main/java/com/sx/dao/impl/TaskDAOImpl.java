package com.sx.dao.impl;

import com.sx.dao.TaskDAO;
import com.sx.domain.Task;
import com.sx.jdbc.JDBCHelper;

import java.sql.ResultSet;

/**
 * 任务管理DAO实现类
 */
public class TaskDAOImpl implements TaskDAO {

    /**
     * 根据主键查询任务
     */
    @Override
    public Task findById(long taskid) {
        final Task task = new Task();

        String sql = "select * from task where task_id = ?";
        Object[] params = new Object[]{taskid};

        JDBCHelper instance = JDBCHelper.getInstance();
        instance.executeQuery(sql,params, new JDBCHelper.QueryCallback() {

            @Override
            public void process(ResultSet rs) throws Exception {
                if (rs.next()) {
                    long taskid = rs.getLong(1);
                    String taskName = rs.getString(2);
                    String createTime = rs.getString(3);
                    String startTime = rs.getString(4);
                    String finishTime = rs.getString(5);
                    String taskType = rs.getString(6);
                    String taskStatus = rs.getString(7);
                    String taskParam = rs.getString(8);

                    task.setTaskid(taskid);
                    task.setTaskName(taskName);
                    task.setCreateTime(createTime);
                    task.setStartTime(startTime);
                    task.setFinishTime(finishTime);
                    task.setTaskType(taskType);
                    task.setTaskStatus(taskStatus);
                    task.setTaskParam(taskParam);
                }
            }
        });
        return task;
    }
}
