package com.thr.synctrajectory.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thr.synctrajectory.common.BaseResponse;
import com.thr.synctrajectory.common.ErrorCode;
import com.thr.synctrajectory.common.ResultUtils;
import com.thr.synctrajectory.exception.BusinessException;
import com.thr.synctrajectory.model.domain.Team;
import com.thr.synctrajectory.model.domain.User;
import com.thr.synctrajectory.model.domain.UserTeam;
import com.thr.synctrajectory.model.dto.TeamQuery;
import com.thr.synctrajectory.model.enums.TeamStatusEnum;
import com.thr.synctrajectory.model.request.TeamAddRequest;
import com.thr.synctrajectory.model.request.TeamJoinRequest;
import com.thr.synctrajectory.model.request.TeamQuitRequest;
import com.thr.synctrajectory.model.request.TeamUpdateRequest;
import com.thr.synctrajectory.model.vo.TeamUserVO;
import com.thr.synctrajectory.service.TeamService;
import com.thr.synctrajectory.service.UserService;
import com.thr.synctrajectory.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 队伍控制器
 *
 * @author thr
 */
@RestController
@RequestMapping("/team")
@Slf4j
//@CrossOrigin(origins = {"http://user.tang0103.com"}, allowCredentials = "true")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    /**
     * 新增队伍
     *
     * @param teamAddRequest 队伍信息
     * @return 新增的队伍的id(mybatis自动生成的)
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍信息为空");
        }

        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    /**
     * 修改队伍信息
     *
     * @param teamUpdateRequest 队伍信息
     * @return 修改成功/失败
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,
                                            HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍信息为空");
        }

        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍信息失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 查询队伍信息
     *
     * @param id 要修改的队伍信息编号
     * @return 修改成功/失败
     */
    @GetMapping("/get")
    public BaseResponse<TeamUserVO> getTeamById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        TeamUserVO team = teamService.getTeamUserById(id, loginUser);
        return ResultUtils.success(team);
    }

    /**
     * 查询队伍信息列表
     *
     * @param teamQuery 队伍信息查询对象
     * @return 修改成功/失败
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = null;
        try {
            loginUser = userService.getLoginUser(request);
        } catch (Exception e) {
            // 未登录的用户只能查看公开的队伍
            teamQuery.setStatus(TeamStatusEnum.PUBLIC.getValue());
        }
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, loginUser);

        // 判断当前用户是否已加入队伍
        // 获取查询结果中所有队伍的 ID 列表
//        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
//        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
//        // 将 getLoginUser 包裹在 try-catch 中, 确保未登录用户可以正常使用此接口
//        try {
//            User loginUser = userService.getLoginUser(request);
//            userTeamQueryWrapper.eq("userId", loginUser.getId());
//            userTeamQueryWrapper.in("teamId", teamIdList);
//            // 查询当前登录用户已加入的队伍
//            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
//            // 提取已加入的队伍 ID, 放入 Set 集合
//            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
//            // 遍历队伍列表, 判断每个队伍是否已被当前用户加入
//            teamList.forEach(team -> {
//                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
//                team.setHasJoin(hasJoin);  // 设置加入状态, 方便前端按钮展示
//            });
//        } catch (Exception e) {}

        return ResultUtils.success(teamList);
    }

    /**
     * 分页查询队伍信息列表
     *
     * @param teamQuery 队伍信息查询对象
     * @return 修改成功/失败
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);

        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        // queryWrapper 自动根据 team 里的字段去搜索, 但是不支持模糊查询
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(teamPage);
    }

    /**
     * 用户加入队伍
     *
     * @param teamJoinRequest 加入队伍请求
     * @return 是否加入成功
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 用户退出队伍
     *
     * @param teamQuitRequest 退出队伍请求
     * @return 是否退出成功
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 队长解散队伍
     *
     * @param id 要解散的队伍编号
     * @return 解散成功/失败
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户创建的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, loginUser);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取当前用户加入的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 查询当前用户加入的队伍编号列表
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // 去除不重复的队伍 id
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, loginUser);
        return ResultUtils.success(teamList);
    }
}