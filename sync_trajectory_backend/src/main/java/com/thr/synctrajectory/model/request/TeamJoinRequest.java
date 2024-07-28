package com.thr.synctrajectory.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 加入队伍请求体
 *
 * @author thr
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 8653745363586323592L;

    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
