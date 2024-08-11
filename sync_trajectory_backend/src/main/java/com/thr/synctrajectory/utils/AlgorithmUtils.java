package com.thr.synctrajectory.utils;

import java.util.List;
import java.util.Objects;

/**
 * 算法工具类
 *
 * @author thr
 */
public class AlgorithmUtils {


    /**
     * 编辑距离算法(用于计算两组标签的相似度)
     *
     * @param tagList1
     * @param tagList2
     * @return
     */
    public static int minDistance(List<String> tagList1, List<String> tagList2) {
        int n = tagList1.size();
        int m = tagList2.size();

        if (n * m == 0) {
            return n + m;
        }

        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                int left = d[i - 1][j] + 1;  // 删除操作
                int down = d[i][j - 1] + 1;  // 插入操作
                int left_down = d[i - 1][j - 1];  // 替换操作（如果字符不同）

                if (!Objects.equals(tagList1.get(i - 1), (tagList2.get(j - 1))))
                    left_down += 1;

                // 选择三种操作中代价最小的一个作为当前操作的编辑距离
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }

        return d[n][m];
    }

    /**
     * 编辑距离算法(用于计算两个字符串的相似度)
     * 参考: https://blog.csdn.net/DBC_121/article/details/104198838
     *
     * @param word1
     * @param word2
     * @return
     */
    public static int minDistance(String word1, String word2) {
        int n = word1.length();
        int m = word2.length();

        // 如果其中一个字符串长度为 0，则返回另一个字符串的长度（编辑距离）
        if (n * m == 0) {
            return n + m;
        }

        // 创建一个二维数组，d[i][j] 表示 word1[0...i-1] 和 word2[0...j-1] 的最小编辑距离
        int[][] d = new int[n + 1][m + 1];
        // 初始化边界条件：word1 的前 i 个字符到空字符串的距离为 i
        for (int i = 0; i < n + 1; i++) {
            d[i][0] = i;
        }

        // 初始化边界条件：word2 的前 j 个字符到空字符串的距离为 j
        for (int j = 0; j < m + 1; j++) {
            d[0][j] = j;
        }

        // 遍历字符串 word1 和 word2，计算最小编辑距离
        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < m + 1; j++) {
                // 计算从 word1[i-1] 到 word2[j] 的最小编辑距离
                int left = d[i - 1][j] + 1;  // 删除操作
                int down = d[i][j - 1] + 1;  // 插入操作
                int left_down = d[i - 1][j - 1];  // 替换操作（如果字符不同）

                // 如果 word1[i-1] 和 word2[j-1] 不相等，则需要替换操作，编辑距离加 1
                if (word1.charAt(i - 1) != word2.charAt(j - 1))
                    left_down += 1;

                // 选择三种操作中代价最小的一个作为当前操作的编辑距离
                d[i][j] = Math.min(left, Math.min(down, left_down));
            }
        }

        // 返回 word1 和 word2 的最小编辑距离
        return d[n][m];
    }
}
