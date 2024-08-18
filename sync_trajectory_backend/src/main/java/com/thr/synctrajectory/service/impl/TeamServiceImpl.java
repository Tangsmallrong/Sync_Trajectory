package com.thr.synctrajectory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.thr.synctrajectory.common.ErrorCode;
import com.thr.synctrajectory.exception.BusinessException;
import com.thr.synctrajectory.mapper.TeamMapper;
import com.thr.synctrajectory.model.domain.Team;
import com.thr.synctrajectory.model.domain.User;
import com.thr.synctrajectory.model.domain.UserTeam;
import com.thr.synctrajectory.model.dto.TeamQuery;
import com.thr.synctrajectory.model.enums.TeamStatusEnum;
import com.thr.synctrajectory.model.request.TeamJoinRequest;
import com.thr.synctrajectory.model.request.TeamQuitRequest;
import com.thr.synctrajectory.model.request.TeamUpdateRequest;
import com.thr.synctrajectory.model.vo.TeamUserVO;
import com.thr.synctrajectory.model.vo.UserVO;
import com.thr.synctrajectory.service.TeamService;
import com.thr.synctrajectory.service.UserService;
import com.thr.synctrajectory.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thr
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        // todo 代码圈复杂度太高
        // 1. 请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍信息为空");
        }

        // 2. 是否登录, 未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN, "未登录");
        }
        final long userId = loginUser.getId();

        // 3. 校验信息
        // 3.1 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        // 3.2 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }

        // 3.3 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }

        // 3.4 status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }

        // 3.5 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }

        // 3.6 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间大于当前时间");
        }

        // 3.7 校验用户最多创建 5 个队伍
        // todo 有 bug, 用户可能同时创建多个(需要加锁)
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建和加入 5 个队伍");
        }

        // todo 要用到事务, 队伍信息和用户-队伍关系信息同时有/无 (手写事务)
        // 4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        // 5. 插入用户-队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
//        Date joinTime = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));
//        userTeam.setJoinTime(joinTime);

        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, User loginUser) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();

        // 组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }

            // 若需要获取当前用户加入的队伍
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }

            // 同时查询队伍名称和描述
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 查询队伍名称
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            // 查询队伍描述
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            // 查询最大人数相等的
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            // 根据创建人来查询
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
//            // 根据状态来查询
//            Integer status = teamQuery.getStatus();
//            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
//            if (statusEnum == null) {
//                statusEnum = TeamStatusEnum.PUBLIC;
//            }
//            // 只有管理员才能查看加密还有非公开的房间
//            if (!loginUser && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
//                throw new BusinessException(ErrorCode.NO_AUTH);
//            }
            // 如果是查询当前用户创建或者加入的队伍, 不进行状态限制
            boolean isCreator = userId != null && userId.equals(loginUser != null ? loginUser.getId() : null);
            boolean isJoiner = CollectionUtils.isNotEmpty(idList);

            if (!isCreator && !isJoiner) {
                // 通用的状态逻辑
                Integer status = teamQuery.getStatus();
                TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
                if (statusEnum == null) {
                    statusEnum = TeamStatusEnum.PUBLIC;
                }

                // 未登录用户只能查看公开的队伍
                if (loginUser == null && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                    throw new BusinessException(ErrorCode.NO_AUTH);
                }

                queryWrapper.eq("status", statusEnum.getValue());
            }
        }

        // 不展示已过期队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));

        // 符合查询条件的队伍
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }

        // todo 关联查询用户信息(自己写 SQL)
        // (1) 查询队伍和创建人的信息
        // select * from team t left join user u on t.userId = u.id
        // (2) 查询队伍和已加入队伍成员的信息
        // select *
        // from team t
        //         left join user_team ut on t.id = ut.teamId
        //         left join user u on ut.userId = u.id
        // 关联查询创建人信息
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }

        // 设置队伍的已加入人数
        enrichTeamsWithJoinCounts(teamUserVOList);

        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍信息为空");
        }

        // 查询队伍是否存在
        Long id = teamUpdateRequest.getId();
        Team oldTeam = getTeamById(id);

        // 只有队伍的创建者和管理员有权限修改队伍信息
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // todo 比较新值和旧值，只有在有差异时才更新

        // 如果队伍状态改为加密, 必须要有密码
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (TeamStatusEnum.SECRET.equals(statusEnum) && StringUtils.isBlank(teamUpdateRequest.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须设置密码");
        }

        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        // todo 考虑并发重复加入队伍问题 => 加锁
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 所加入的队伍必须存在
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);

        // 只能加入未过期的队伍
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR, "队伍已过期");
        }

        // 禁止加入私有的队伍
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "禁止加入私有队伍");
        }

        // 所加入队伍为加密的, 必须密码匹配
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }

        // 用户最多加入 5 个队伍
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        long hasJoinNum = userTeamService.count(userTeamQueryWrapper);  // 用户已加入的队伍个数
        if (hasJoinNum > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建和加入 5 个队伍");
        }

        // 不能重复加入已加入的队伍
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teamId", teamId);
        long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);  // 已加入该队伍的人数
        if (hasUserJoinTeam > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
        }

        // 只能加入未满的队伍
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);  // 已加入该队伍的人数
        if (teamHasJoinNum >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
        }

        // 新增队伍-用户关联信息
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 判断队伍是否存在
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);

        // 判断用户是否已加入队伍
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }

        // 队伍人数查询
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        if (teamHasJoinNum == 1) {
            // 队伍只剩一人, 直接解散(删除队伍信息)
            this.removeById(teamId);
        } else {
            // 队伍至少还剩两人, 判断是否是队长退出队伍
            if (team.getUserId() == userId) {
                // 权限转移给最早加入的用户
                // 1. 查询已加入队伍的所有用户和加入时间, 原则上少取数据
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() < 2) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                // 2. 获取权限转移的用户对象作为下一个队长
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 3. 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队长失败");
                }
            }
        }

        // 删除用户-队伍关联信息
        return userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        // 1. 校验队伍是否存在
        Team team = getTeamById(id);
        long teamId = team.getId();

        // 2. 校验当前用户是否是队长
        if (team.getUserId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }

        // 3. 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }

        // 4. 删除队伍
        return this.removeById(teamId);
    }

    @Override
    public TeamUserVO getTeamUserById(long id, User loginUser) {
        Team team = this.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR, "队伍不存在");
        }

        // 将 Team 转换为 TeamUserVO
        TeamUserVO teamUserVO = new TeamUserVO();
        BeanUtils.copyProperties(team, teamUserVO);

        // 查询当前队伍的加入人数和当前用户是否加入该队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", id);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);

        // 获取队伍当前加入人数
        int teamHasJoinNum = userTeamList.size();
        teamUserVO.setHasJoinNum(teamHasJoinNum);

        // 判断当前用户是否加入队伍
        boolean hasJoin = userTeamList.stream()
                .anyMatch(userTeam -> userTeam.getUserId().equals(loginUser.getId()));
        teamUserVO.setHasJoin(hasJoin);

        // 获取并设置队伍所有成员的用户信息
        List<Long> userIdList = userTeamList.stream()
                .map(UserTeam::getUserId)
                .collect(Collectors.toList());
        List<User> userList = userService.listByIds(userIdList)
                .stream()
                .map(userService::getSafetyUser)  // 脱敏处理
                .collect(Collectors.toList());
        teamUserVO.setUserVOList(userList);

        return teamUserVO;
    }

    /**
     * 根据队伍id查询队伍人数
     *
     * @param teamId 队伍id
     * @return 队伍人数
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);  // 已加入该队伍的人数
    }

    /**
     * 根据id获取队伍信息
     *
     * @param teamId 队伍id
     * @return 队伍信息
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 查询并设置加入队伍的用户信息(人数)
     *
     * @param teamList 队伍列表
     */
    private void enrichTeamsWithJoinCounts(List<TeamUserVO> teamList) {
        if (CollectionUtils.isEmpty(teamList)) {
            return;
        }

        // 获取队伍 ID 列表
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());

        // 查询加入队伍的用户信息(人数)
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 根据 teamId 进行分组, 获取每个队伍当前已加入的人数
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));

        // 设置队伍的加入人数
        teamList.forEach(team
                -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
    }
}




