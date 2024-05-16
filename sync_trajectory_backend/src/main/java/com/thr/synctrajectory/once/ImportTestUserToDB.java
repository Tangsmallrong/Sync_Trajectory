package com.thr.synctrajectory.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入用户到数据库-未完成
 */
public class ImportTestUserToDB {
    public static void main(String[] args) {
        String fileName = "src/main/resources/testExcel.xlsx";

        // 使用同步读取的方式 读取所有数据
        List<TestTableUserInfo> userInfoList =
                EasyExcel.read(fileName).head(TestTableUserInfo.class).sheet().doReadSync();
        System.out.println("总数 = " + userInfoList.size());

        // 将用户呢称作为键, 相同昵称的用户信息对象封装为 list 值
        Map<String, List<TestTableUserInfo>> listMap =
                userInfoList.stream()
                        .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUsername()))
                        .collect(Collectors.groupingBy(TestTableUserInfo::getUsername));

        // 遍历 map 的 entry, 查找值 list 内部元素数量 > 1 的行数
        for (Map.Entry<String, List<TestTableUserInfo>> stringListEntry : listMap.entrySet()) {
            if (stringListEntry.getValue().size() > 1) {  // 查找重复的呢称
                System.out.println("username = " + stringListEntry.getKey());
                System.out.println("1");
            }
        }

        System.out.println("不重复昵称数 = " + listMap.keySet().size());
    }
}