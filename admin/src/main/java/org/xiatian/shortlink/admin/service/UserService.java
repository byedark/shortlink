package org.xiatian.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiatian.shortlink.admin.dao.entity.UserDO;
import org.xiatian.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.xiatian.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.xiatian.shortlink.admin.dto.resp.UserLoginRespDTO;
import org.xiatian.shortlink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     *
     * @param username 用户名
     * @return 用户返回实体
     */
    Boolean hasUsername(String username);

    /**
     * 注册用户
     *
     * @param requestParam 注册用户请求参数
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 修改用户信息
     *
     * @param requestParam 用户信息
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     * 用户登陆
     * @param requestParam 用户账号密码
     * @return token
     */
    UserLoginRespDTO login(UserRegisterReqDTO requestParam);

    /**
     * 检查用户是否登录
     *
     * @param username 用户名
     * @param token    用户登录 Token
     * @return 用户是否登录标识
     */
    Boolean checkLogin(String username, String token);

    /**
     * 退出登录
     *
     * @param username 用户名
     * @param token    用户登录 Token
     */
    void logout(String username, String token);
}
