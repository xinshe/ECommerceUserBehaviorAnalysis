package com.sx.dao;

import com.sx.domain.Task;

/**
 * 任务管理DAO接口
 */
public interface TaskDAO {

    /**
     * 根据主键查询业务
     */
    Task findById(long taskid);

}
