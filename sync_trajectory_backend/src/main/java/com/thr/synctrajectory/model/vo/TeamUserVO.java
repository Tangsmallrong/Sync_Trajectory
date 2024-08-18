package com.thr.synctrajectory.model.vo;

import com.thr.synctrajectory.model.domain.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍和用户信息封装类(脱敏)
 *
 * @author thr
 */
@Data
public class TeamUserVO implements Serializable {

    private static final long serialVersionUID = -7421957134803249352L;

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人用户信息
     */
    private UserVO createUser;

    /**
     * 已加入用户数
     */
    private Integer hasJoinNum;

    /**
     * 当前用户是否已加入该队伍
     */
    private boolean hasJoin = false;

    /**
     * 已加入的所有用户信息
     */
    private List<User> userVOList;
}
