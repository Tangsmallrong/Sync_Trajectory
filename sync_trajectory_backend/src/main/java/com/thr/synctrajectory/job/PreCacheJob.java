package com.thr.synctrajectory.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thr.synctrajectory.model.domain.User;
import com.thr.synctrajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热定时任务
 *
 * @author thr
 */
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    // 重点用户
    // todo 不能写死
    private final List<Long> mainUserList = Arrays.asList(1L);

    /**
     * 每天执行, 预热推荐用户
     */
    @Scheduled(cron = "0 55 18 * * *")
    public void doCacheRecommendUser() {
        // todo 两次用到的常量, 需要提取出来
        RLock lock = redissonClient.getLock("synctrajectory:precachejob:docache:lock");

        try {
            // 尝试获取锁, 这里一定是等待0s, 因为这个定时任务一天就执行一次, 没获取到锁的就走
            // 过期时间设置为 -1 会使用 Redisson 自带的看门狗机制(自动续期)
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getLock: " + Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    // 查数据库
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);

                    String redisKey = String.format("synctrajectory:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

                    // 写缓存 30s过期
                    try {
                        valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally {
            // 只能释放自己的锁, 判断当前锁是否是当前线程加的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();  // 释放锁
            }
        }
    }
}
