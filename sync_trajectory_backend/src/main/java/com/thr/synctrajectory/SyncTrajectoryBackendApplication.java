package com.thr.synctrajectory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.thr.usercenter.mapper")
public class SyncTrajectoryBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncTrajectoryBackendApplication.class, args);
    }

}
