package com.thr.synctrajectory.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author thr
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -7140828216606742040L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户校验密码
     */
    private String checkPassword;

    /**
     * 用户编号
     */
    private String planetCode;
}
