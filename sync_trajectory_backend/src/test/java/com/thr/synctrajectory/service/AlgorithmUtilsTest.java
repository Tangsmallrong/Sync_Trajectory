package com.thr.synctrajectory.service;

import com.thr.synctrajectory.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * 算法工具类测试
 */
public class AlgorithmUtilsTest {

    @Test
    void test() {
        String str1 = "life is simple";
        String str2 = "life is not simple";
        String str3 = "life is difficult or simple";
        int score1 = AlgorithmUtils.minDistance(str1, str2);
        int score2 = AlgorithmUtils.minDistance(str1, str3);
        System.out.println(score1);  // 4
        System.out.println(score2);  // 13
    }

    @Test
    void testCompareTags() {
        List<String> tagList1 = Arrays.asList("java", "大一", "男");
        List<String> tagList2 = Arrays.asList("java", "大一", "女");
        List<String> tagList3 = Arrays.asList("python", "大二", "女");
        int score1 = AlgorithmUtils.minDistance(tagList1, tagList2);
        int score2 = AlgorithmUtils.minDistance(tagList1, tagList3);
        System.out.println(score1);  // 1
        System.out.println(score2);  // 3
    }
}
