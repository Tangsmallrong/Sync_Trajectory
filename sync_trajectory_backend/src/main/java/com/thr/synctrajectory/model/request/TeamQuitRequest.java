package com.thr.synctrajectory.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 退出队伍请求体
 *
 * @author thr
 */
@Data
public class TeamQuitRequest implements Serializable {
    private static final long serialVersionUID = -4551947326066702761L;

    /**
     * id
     */
    private Long teamId;
}
