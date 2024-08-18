package com.thr.synctrajectory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.synctrajectory.model.domain.Team;
import com.thr.synctrajectory.model.domain.User;
import com.thr.synctrajectory.model.dto.TeamQuery;
import com.thr.synctrajectory.model.request.TeamJoinRequest;
import com.thr.synctrajectory.model.request.TeamQuitRequest;
import com.thr.synctrajectory.model.request.TeamUpdateRequest;
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
     * @param loginUser 已登录用户
     * @return 符合查询条件的队伍列表
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, User loginUser);

    /**
     * 修改队伍信息
     *
     * @param teamUpdateRequest 修改对象
     * @param loginUser         已登录用户
     * @return 是否修改成功
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 用户加入队伍
     *
     * @param teamJoinRequest 加入队伍请求对象
     * @param loginUser       已登录用户
     * @return 是否加入成功
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 用户退出队伍
     *
     * @param teamQuitRequest 退出队伍请求对象
     * @param loginUser       已登录用户
     * @return 是否退出成功
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 队长解散队伍
     *
     * @param id        要解散的队伍id
     * @param loginUser 已登录用户
     * @return 是否解散成功
     */
    boolean deleteTeam(long id, User loginUser);

    /**
     * 根据队伍 id 获取队伍详细信息(包括当前队伍人数以及当前登录用户是否加入该队伍的状态)
     *
     * @param id        队伍 id
     * @param loginUser 已登录用户
     * @return 队伍详细信息
     */
    TeamUserVO getTeamUserById(long id, User loginUser);
}
