package com.thr.synctrajectory.service;

import com.thr.synctrajectory.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        // 操作 redis 字符串的操作集合
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 操作 redis 列表的操作集合
        // ListOperations listOperations = redisTemplate.opsForList();

        // 增
        valueOperations.set("thrString", "hhh");
        valueOperations.set("thrInt", 1);
        valueOperations.set("thrDouble", 2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("thr");
        valueOperations.set("thrUser", user);

        // 查
        Object thr = valueOperations.get("thrString");
        Assertions.assertEquals("hhh", (String) thr);
        thr = valueOperations.get("thrInt");
        Assertions.assertEquals(1, (Integer) thr);
        thr = valueOperations.get("thrDouble");
        Assertions.assertEquals(2.0, (Double) thr);
        System.out.println(valueOperations.get("thrUser"));

        // 删
        // redisTemplate.delete("thrString");
    }
}
