package com.thr.synctrajectory.service;

import com.thr.synctrajectory.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 测试批量插入用户
 * 注意打包时要删掉或忽略该测试类, 不然打一次包就插入一次
 */
@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000,
            10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 批量插入用户
     */
    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final int INSERT_NUM = 100;
        List<User> userList = new ArrayList<>();
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

            userList.add(user);
        }
        userService.saveBatch(userList, 20);

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 并发批量插入用户
     */
    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final int INSERT_NUM = 100;
        // 分成10组 i=10 每组10个(如果是10万条数据就每组1万个)
        int j = 0;
        int batchSize = 10;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;

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
                userList.add(user);

                if (j % batchSize == 0) {
                    break;
                }
            }

            // 异步执行 注意执行的先后顺序
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName: " + Thread.currentThread().getName());
                userService.saveBatch(userList, 10);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
