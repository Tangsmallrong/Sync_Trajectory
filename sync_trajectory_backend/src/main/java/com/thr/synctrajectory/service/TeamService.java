package com.thr.synctrajectory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.synctrajectory.model.domain.Team;
import com.thr.synctrajectory.model.domain.User;

/**
 * @author thr
 */
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     *
     * @param team      队伍信息
     * @param loginUser 当前登录用户
     * @return 新增的队伍编号
     */
    long addTeam(Team team, User loginUser);
}
