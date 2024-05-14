package com.thr.synctrajectory.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 *
 * @author thr
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 280561466299963206L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}
