package com.thr.synctrajectory.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页参数请求对象
 *
 * @author thr
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -473780791340223340L;

    /**
     * 页面大小
     */
    protected int pageSize = 10;

    /**
     * 当前是第几页
     */
    protected int pageNum = 1;
}
