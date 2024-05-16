package com.thr.synctrajectory.once;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 监听器
 */
@Slf4j
public class TableListener implements ReadListener<TestTableUserInfo> {

    /**
     * 每读一条数据都会触发该方法
     */
    @Override
    public void invoke(TestTableUserInfo data, AnalysisContext context) {
        System.out.println(data);
    }

    /**
     * 所有数据解析完成后调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        System.out.println("已解析完成");
    }
}