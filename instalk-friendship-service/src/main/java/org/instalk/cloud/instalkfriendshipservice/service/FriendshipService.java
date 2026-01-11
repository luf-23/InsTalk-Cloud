package org.instalk.cloud.instalkfriendshipservice.service;

import org.instalk.cloud.common.feign.client.MessageFeignClient;
import org.instalk.cloud.common.feign.client.UserFeignClient;
import org.instalk.cloud.common.feign.client.WebSocketFeignClient;
import org.instalk.cloud.common.model.dto.internal.DeleteMessageDTO;
import org.instalk.cloud.common.model.dto.internal.WebSocketDeleteFriendDTO;
import org.instalk.cloud.common.model.po.Friendship;
import org.instalk.cloud.common.model.po.User;
import org.instalk.cloud.common.model.vo.FriendVO;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.instalk.cloud.instalkfriendshipservice.mapper.FriendshipMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipMapper friendshipMapper;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private MessageFeignClient messageFeignClient;

    @Autowired
    private WebSocketFeignClient webSocketFeignClient;

    public void makeFriendsWithRobot(Long minId, Long maxId) {
        friendshipMapper.makeFriendsWithRobot(minId,maxId);
    }


    public Result sendFriendshipRequest(Long id) {
        Long myId = ThreadLocalUtil.getId();
        if (myId == id) return Result.error("不能添加自己为好友");
        Long id1 = Math.min(myId,id);
        Long id2 = Math.max(myId,id);
        Friendship friendship = friendshipMapper.selectByUserId1AndUserId2(id1,id2);
        if (friendship != null){
            if (friendship.getStatus().equals("PENDING")) return Result.error("好友申请已存在");
            else if (friendship.getStatus().equals("BLOCKED")) return Result.error("你已被拉入黑名单或对方被你拉入了黑名单");
            else return Result.error("已经是好友");
        }
        friendshipMapper.addRequest(id1,id2,myId);
        return Result.success();
    }


    public Result<FriendVO> acceptFriendshipRequest(Long id) {
        Long myId = ThreadLocalUtil.getId();
        if (myId == id) return Result.error("不能添加自己为好友");
        Long id1 = Math.min(myId,id);
        Long id2 = Math.max(myId,id);
        Friendship friendship = friendshipMapper.selectByUserId1AndUserId2(id1,id2);
        if (friendship == null) return Result.error("好友申请不存在");
        else{
            if (friendship.getRequesterId()==myId) return Result.error("操作失败");
            if (friendship.getStatus().equals("BLOCKED")) return Result.error("你已被拉入黑名单或对方被你拉入了黑名单");
            else if (friendship.getStatus().equals("ACCEPTED")) return Result.error("已经是好友");
        }
        friendshipMapper.acceptRequest(id1,id2);
        User friendInfo = userFeignClient.getUserById(id);
        FriendVO friendVO = new FriendVO(friendInfo,friendship.getCreatedAt());
        return Result.success(friendVO);
    }

    public Result rejectFriendshipRequest(Long id) {
        Long myId = ThreadLocalUtil.getId();
        if (myId == id) return Result.error("操作失败");
        Long id1 = Math.min(myId,id);
        Long id2 = Math.max(myId,id);
        Friendship friendship = friendshipMapper.selectByUserId1AndUserId2(id1,id2);
        if (friendship == null) return Result.error("好友申请不存在");
        else{
            if (friendship.getRequesterId()==myId) return Result.error("操作失败");
            if (friendship.getStatus().equals("ACCEPTED")) return Result.error("已经是好友");
            else if (friendship.getStatus().equals("BLOCKED")) return Result.error("你已被拉入黑名单或对方被你拉入了黑名单");
        }
        friendshipMapper.rejectRequest(id1,id2);
        return  Result.success();
    }

    public Result deleteFriendship(Long id) {
        Long myId = ThreadLocalUtil.getId();
        if (myId == id) return Result.error("操作失败");
        Long id1 = Math.min(myId,id);
        Long id2 = Math.max(myId,id);
        Friendship friendship = friendshipMapper.selectByUserId1AndUserId2(id1,id2);
        if (friendship == null) return Result.error("好友不存在");
        else{
            if (friendship.getStatus().equals("PENDING")) return Result.error("好友不存在");
            else if (friendship.getStatus().equals("BLOCKED")) return Result.error("你已被拉入黑名单或对方被你拉入了黑名单");
        }
        friendshipMapper.deleteRequest(id1,id2);
        messageFeignClient.deleteById1AndId2(new DeleteMessageDTO(id1, id2));


        // 通过 WebSocket 通知对方好友已被删除
        webSocketFeignClient.sendFriendDeletedNotification(new WebSocketDeleteFriendDTO(id,myId));

        return  Result.success();
    }

    public Result<List<FriendVO>> getFriendList() {
        Long myId = ThreadLocalUtil.getId();
        List<Long> Ids = friendshipMapper.selectFriendsId(myId);
        List<FriendVO> friendList = userFeignClient.getUsersByIds(Ids).stream().map(user -> {
            FriendVO friendVO = new FriendVO();
            friendVO.setId(user.getId());
            friendVO.setUsername(user.getUsername());
            friendVO.setSignature(user.getSignature());
            friendVO.setAvatar(user.getAvatar());
            friendVO.setRole(user.getRole().equals("ROBOT") ? "ROBOT" : "USER");
            friendVO.setCreatedAt(user.getCreatedAt());
            return friendVO;
        }).toList();
        return Result.success(friendList);
    }

    public Result<List<FriendVO>> getPendingList() {
        Long myId = ThreadLocalUtil.getId();
        List<Long> Ids = friendshipMapper.selectPendingId(myId);
        List<FriendVO> pendingList = userFeignClient.getUsersByIds(Ids).stream().map(user -> {
            FriendVO friendVO = new FriendVO();
            friendVO.setId(user.getId());
            friendVO.setUsername(user.getUsername());
            friendVO.setSignature(user.getSignature());
            friendVO.setAvatar(user.getAvatar());
            friendVO.setRole(user.getRole().equals("ROBOT") ? "ROBOT" : "USER");
            friendVO.setCreatedAt(user.getCreatedAt());
            return friendVO;
        }).toList();
        return Result.success(pendingList);
    }

    public Result<List<FriendVO>> searchByUsername(String username) {
        List<FriendVO> friendVOS = userFeignClient.getUserByNameLike(username).stream().map(user -> {
            FriendVO friendVO = new FriendVO();
            friendVO.setId(user.getId());
            friendVO.setUsername(user.getUsername());
            friendVO.setSignature(user.getSignature());
            friendVO.setAvatar(user.getAvatar());
            friendVO.setRole(user.getRole().equals("ROBOT") ? "ROBOT" : "USER");
            friendVO.setCreatedAt(user.getCreatedAt());
            return friendVO;
        }).filter(friendVO -> !friendVO.getId().equals(ThreadLocalUtil.getId())).toList();
        return Result.success(friendVOS);
    }
}
