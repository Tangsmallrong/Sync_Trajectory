package com.thr.synctrajectory.once;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * 导入 excel 数据
 */
public class ImportExcel {
    /**
     * 读取数据
     */
    public static void main(String[] args) {
        // 文件路径
        String fileName = "src/main/resources/testExcel.xlsx";
        // readByListener(fileName);  // 监听器读取
        synchronousRead(fileName);  // 测试数据不多, 这里直接调用同步读取的方法
    }

    /**
     * 方法一: 监听器读取 清晰易维护,一条一条处理
     *
     * @param fileName 文件路径
     */
    public static void readByListener(String fileName) {
        // 指定读用哪个 class 去读, 然后读取第一个 sheet, 文件流会自动关闭
        // 底层每次会读取100条数据然后返回, 直接调用使用数据就行
        EasyExcel.read(fileName, TestTableUserInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 方法二: 同步读取 一次性获取完整数据 适用于小数据量
     *
     * @param fileName 文件路径
     */
    public static void synchronousRead(String fileName) {
        // 需要指定读用哪个 class 去读, 然后读取第一个 sheet, 同步读取会自动 finish
        List<TestTableUserInfo> list = EasyExcel.read(fileName)
                .head(TestTableUserInfo.class).sheet().doReadSync();
        for (TestTableUserInfo planetQiuTableUserInfo : list) {
            System.out.println(planetQiuTableUserInfo);
        }
    }
}