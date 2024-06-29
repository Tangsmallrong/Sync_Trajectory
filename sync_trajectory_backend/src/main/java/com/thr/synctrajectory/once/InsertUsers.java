package com.thr.synctrajectory.once;

import com.thr.synctrajectory.mapper.UserMapper;
import com.thr.synctrajectory.model.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@Component
public class InsertUsers {
    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入用户
     */
    // 只执行一次的定时任务, 延迟5秒执行, 该方法有点取巧, 尽量别用, 防止不小心误用
    // @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final int INSERT_NUM = 1000;

        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假用户");
            user.setUserAccount("fake-thr");
            user.setAvatarUrl("https://tangsmallrong.github.io/img/avatar.jpg");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setTags("[]");
            user.setProfile("");
            user.setUserRole(0);
            user.setPlanetCode("1111111");

            userMapper.insert(user);
        }

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
