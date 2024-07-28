package com.thr.synctrajectory.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thr.synctrajectory.common.BaseResponse;
import com.thr.synctrajectory.common.ErrorCode;
import com.thr.synctrajectory.common.ResultUtils;
import com.thr.synctrajectory.exception.BusinessException;
import com.thr.synctrajectory.model.domain.Team;
import com.thr.synctrajectory.model.domain.User;
import com.thr.synctrajectory.model.dto.TeamQuery;
import com.thr.synctrajectory.model.request.TeamAddRequest;
import com.thr.synctrajectory.model.request.TeamJoinRequest;
import com.thr.synctrajectory.model.request.TeamUpdateRequest;
import com.thr.synctrajectory.model.vo.TeamUserVO;
import com.thr.synctrajectory.service.TeamService;
import com.thr.synctrajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
     * 删除队伍
     *
     * @param id 要删除的队伍编号
     * @return 删除成功/失败
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean result = teamService.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }
        return ResultUtils.success(true);
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
    public BaseResponse<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR, "队伍信息不存在");
        }
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

        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
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
}