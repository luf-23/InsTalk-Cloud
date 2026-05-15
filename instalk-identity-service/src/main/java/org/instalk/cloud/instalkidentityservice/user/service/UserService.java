package org.instalk.cloud.instalkidentityservice.user.service;

import org.instalk.cloud.common.model.dto.UserDTO;
import org.instalk.cloud.common.model.po.User;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.model.vo.UserInfoVO;
import org.instalk.cloud.common.util.RedisUtil;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.instalk.cloud.infrastructure.redis.RedisUtilImpl;
import org.instalk.cloud.instalkidentityservice.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@ComponentScan(basePackageClasses = {RedisUtilImpl.class})
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    // 缓存key前缀
    private static final String USER_CACHE_PREFIX = "user:info:";
    // 缓存过期时间�?0分钟
    private static final long CACHE_EXPIRE_TIME = 30;
    private static final TimeUnit CACHE_EXPIRE_UNIT = TimeUnit.MINUTES;

    public User getUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    public User add(User user) {
        userMapper.add(user);
        
        // 新增用户后立即缓�?
        if (user.getId() != null) {
            String cacheKey = USER_CACHE_PREFIX + user.getId();
            redisUtil.setObject(cacheKey, user, CACHE_EXPIRE_TIME, CACHE_EXPIRE_UNIT);
        }
        
        return user;
    }

    public User addRobot(User robot) {
        userMapper.addRobot(robot);
        
        // 新增机器人后立即缓存
        if (robot.getId() != null) {
            String cacheKey = USER_CACHE_PREFIX + robot.getId();
            redisUtil.setObject(cacheKey, robot, CACHE_EXPIRE_TIME, CACHE_EXPIRE_UNIT);
        }
        
        return robot;
    }

    public Result<UserInfoVO> getInfo(User user) {
        UserInfoVO userInfoVO = new UserInfoVO(user);
        return Result.success(userInfoVO);
    }

    public Result update(UserDTO userDTO) {
        Long id = ThreadLocalUtil.getId();
        User user = userMapper.selectById(id);
        User newUser = new User(user,userDTO);
        userMapper.update(newUser);
        
        // 更新后删除缓�?
        String cacheKey = USER_CACHE_PREFIX + id;
        redisUtil.delete(cacheKey);
        
        return Result.success();
    }

    public User getUserById(Long id) {
        if (id == null) {
            return null;
        }
        
        // 1. 先从缓存中获�?
        String cacheKey = USER_CACHE_PREFIX + id;
        User cachedUser = redisUtil.getObject(cacheKey, User.class);
        
        if (cachedUser != null) {
            // 缓存命中
            return cachedUser;
        }
        
        // 2. 缓存未命中，从数据库查询
        User user = userMapper.selectById(id);
        
        // 3. 如果用户存在，写入缓�?
        if (user != null) {
            redisUtil.setObject(cacheKey, user, CACHE_EXPIRE_TIME, CACHE_EXPIRE_UNIT);
        }
        
        return user;
    }

    public List<User> getUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        
        // 批量查询时优化：先从缓存获取，未命中的再查询数据�?
        // TODO: 可以进一步优化为批量缓存查询，减少Redis访问次数
        return userMapper.selectByIds(ids);
    }

    public List<User> getUserByNameLike(String usernameLike) {
        return userMapper.selectByUsernameLike(usernameLike);
    }
}
