package org.instalk.cloud.instalkuserservice.service;

import org.instalk.cloud.common.model.dto.UserDTO;
import org.instalk.cloud.common.model.po.User;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.model.vo.UserInfoVO;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.instalk.cloud.instalkuserservice.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User getUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    public User add(User user) {
        userMapper.add(user);
        return user;
    }

    public User addRobot(User robot) {
        userMapper.addRobot(robot);
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
        return Result.success();
    }

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    public List<User> getUsersByIds(List<Long> ids) {
        return userMapper.selectByIds(ids);
    }

    public List<User> getUserByNameLike(String username) {
        return userMapper.selectByUsernameLike(username);
    }
}
