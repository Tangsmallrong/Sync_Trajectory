package com.thr.synctrajectory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.thr.synctrajectory.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 * @author thr
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    用户编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      客户端的请求
     * @return 返回脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser 原始用户对象
     * @return 删除密码字段后的用户对象
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request 请求对象
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 标签列表
     * @return 符合条件的用户
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     *
     * @param user      用户信息
     * @param loginUser 当前登录用户
     * @return 成功返回正数
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户信息
     *
     * @param request 请求对象
     * @return 用户信息对象
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request 请求
     * @return 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser 登录用户
     * @return 是否为管理员
     */
    boolean isAdmin(User loginUser);

    /**
     * 获取匹配的用户
     *
     * @param num       用户个数
     * @param loginUser 登录用户
     * @return 匹配的用户列表
     */
    List<User> matchUsers(long num, User loginUser);
}
