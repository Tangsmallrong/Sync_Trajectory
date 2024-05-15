package com.thr.synctrajectory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thr.synctrajectory.common.ErrorCode;
import com.thr.synctrajectory.exception.BusinessException;
import com.thr.synctrajectory.mapper.UserMapper;
import com.thr.synctrajectory.model.domain.User;
import com.thr.synctrajectory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.thr.synctrajectory.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author thr
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值, 混淆密码
     * 快捷键 prsf 打出 private static final
     */
    private static final String SALT = "thr";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1. 校验, 使用 apache.commons 库
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }

        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户编号过长");
        }

        // 账户不能包含特殊字符, 正则表达式
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {  // 如果找到特殊字符?
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }

        // 账户不能重复, 查数据库是否有相同账户的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能重复");
        }

        // 用户编号不能重复, 查数据库是否有相同账户的用户
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户编号不能重复");
        }

        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和校验密码不同");
        }

        // 2. 加密, md5 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.USER_SAVE_ERROR, "保存新用户失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验, 使用 apache.commons 库
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号密码不能为空");
        }

        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }

        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过长");
        }

        // 账户不能包含特殊字符, 正则表达式
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {  // 如果找到特殊字符?
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }

        // 2. 加密, md5 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在或密码错误
        if (user == null) {  // 两种情况, 要么用户不存在, 要么密码错了
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 3. 用户脱敏, 密码不返回
        // 三击鼠标可快速选中一行
        User safetyUser = getSafetyUser(user);

        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        // 返回脱敏后的用户信息
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser 原始用户对象
     * @return 删除密码字段后的用户对象
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setTags(originUser.getTags());

        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request 请求对象
     * @return 注销成功
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 标签列表
     * @return 符合条件的用户
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        // 先判空
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签不能为空");
        }

        // 查询方法一: SQL 模糊匹配
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        // 拼接 and 查询 like '%Java%' and like '%Python%'
//        // 注意: MySQL中, 默认情况下, 字符串比较不区分大小写
//        for (String tagName : tagNameList) {
//            queryWrapper = queryWrapper.like("tags", tagName);
//        }
//
//        List<User> userList = userMapper.selectList(queryWrapper);
//        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());

        // 查询方法二: 内存查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // (1) 先查询所有用户
        List<User> userList = userMapper.selectList(queryWrapper);

        Gson gson = new Gson();
        // (2) 在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            if (StringUtils.isBlank(tagsStr)) {  // 如果这个用户的 tags 字段为空, 直接返回 false
                return false;
            }
            // 使用 set 集合加快查询速度
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {  // 如果通过反序列化得到的 Set 里不包含 List 中的所有标签
                    return false;  // filter 会过滤掉返回 false 的对象
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }
}




