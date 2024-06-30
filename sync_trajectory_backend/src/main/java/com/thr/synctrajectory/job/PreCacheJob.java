package com.thr.synctrajectory.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thr.synctrajectory.model.domain.User;
import com.thr.synctrajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
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

    // 重点用户
    // todo 不能写死
    private final List<Long> mainUserList = Arrays.asList(1L);

    /**
     * 每天执行, 预热推荐用户
     */
    @Scheduled(cron = "0 55 18 * * *")
    public void doCacheRecommendUser() {
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
}
