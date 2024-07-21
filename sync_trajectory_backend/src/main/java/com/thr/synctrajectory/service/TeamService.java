package com.thr.synctrajectory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.synctrajectory.model.domain.Team;
import com.thr.synctrajectory.model.domain.User;
import com.thr.synctrajectory.model.dto.TeamQuery;
import com.thr.synctrajectory.model.vo.TeamUserVO;

import java.util.List;

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

    /**
     * 搜索队伍
     *
     * @param teamQuery 队伍查询对象
     * @param isAdmin 是否为管理员
     * @return 符合查询条件的队伍列表
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);
}
