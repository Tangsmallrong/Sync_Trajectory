package com.thr.synctrajectory.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        // list: 数据存储在本地 JVM 内存中
        List<String> list = new ArrayList<>();
        list.add("thr");
        System.out.println("list:" + list.get(0));  // list:thr
        list.remove(0);

        // rlist: 数据存储在 redis 的内存中, 此处指定的 name 即为 key
        // 实现上继承了 List
        RList<String> rList = redissonClient.getList("test-list");
        rList.add("thr");
        System.out.println("rlist:" + rList.get(0));  // rlist:thr
        rList.remove(0);

        // map
        Map<String, Integer> map = new HashMap<>();
        map.put("thr", 20);
        map.get("thr");

        // rmap
        RMap<Object, Object> rmap = redissonClient.getMap("test-map");
        rmap.put("thr", 20);

        // set

        // stack
    }

    @Test
    void testWatchDog() {
        RLock lock = redissonClient.getLock("synctrajectory:precachejob:docache:lock");

        try {
            // 尝试获取锁, 这里一定是等待0s, 因为这个定时任务一天就执行一次, 没获取到锁的就走
            // 过期时间设置为 -1 会使用 Redisson 自带的看门狗机制(自动续期)
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                Thread.sleep(100000);
                System.out.println("getLock: " + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            // 只能释放自己的锁, 判断当前锁是否是当前线程加的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();  // 释放锁
            }
        }
    }
}
