package com.thr.synctrajectory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.thr.synctrajectory.mapper")
@EnableScheduling
public class SyncTrajectoryBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncTrajectoryBackendApplication.class, args);
    }

}
