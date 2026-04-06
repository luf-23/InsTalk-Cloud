package org.instalk.cloud.instalkgroupservice.service;

import org.instalk.cloud.common.feign.client.WebSocketFeignClient;
import org.instalk.cloud.common.model.dto.GroupDTO;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastGroupDeleteDTO;
import org.instalk.cloud.common.model.po.ChatGroup;
import org.instalk.cloud.common.model.po.GroupMember;
import org.instalk.cloud.common.model.vo.GroupVO;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.instalk.cloud.instalkgroupservice.mapper.ChatGroupMapper;
import org.instalk.cloud.instalkgroupservice.mapper.GroupMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private ChatGroupMapper chatGroupMapper;

    @Autowired
    private WebSocketFeignClient webSocketFeignClient;

    public GroupMember getMember(Long senderId, Long groupId) {
        return groupMemberMapper.select(senderId, groupId);
    }

    public List<GroupVO.Member> getMembersByGroupId(Long groupId) {
        return groupMemberMapper.selectMembersByGroupId(groupId);
    }


    public Result<GroupVO> createGroup(GroupDTO groupDTO) {
        Long ownerId = ThreadLocalUtil.getId();
        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setName(groupDTO.getName());
        chatGroup.setDescription(groupDTO.getDescription());
        chatGroup.setOwnerId(ownerId);
        chatGroup.setAvatar(groupDTO.getAvatar());
        chatGroupMapper.add(chatGroup);
        groupMemberMapper.addOwner(ownerId, chatGroup.getId());
        GroupVO groupVO = new GroupVO(chatGroupMapper.selectById(chatGroup.getId()), groupMemberMapper.selectAdminIdsByGroupId(chatGroup.getId()), groupMemberMapper.selectMembersByGroupId(chatGroup.getId()));
        return Result.success(groupVO);
    }


    public Result<GroupVO> joinGroup(Long groupId) {
        Long userId = ThreadLocalUtil.getId();
        if (groupMemberMapper.select(userId, groupId)!=null) return Result.error("您已加入该群");
        groupMemberMapper.addMember(userId, groupId);
        GroupVO groupVO = new GroupVO(chatGroupMapper.selectById(groupId), groupMemberMapper.selectAdminIdsByGroupId(groupId), groupMemberMapper.selectMembersByGroupId(groupId));
        return Result.success(groupVO);
    }


    public Result<List<GroupVO>> getMyGroupListAndMembers() {
        Long userId = ThreadLocalUtil.getId();
        List<GroupVO> groupVOS = chatGroupMapper.selectByOwnerId(userId).stream().map(chatGroup -> {
            GroupVO groupVO = new GroupVO();
            groupVO.setId(chatGroup.getId());
            groupVO.setName(chatGroup.getName());
            groupVO.setDescription(chatGroup.getDescription());
            groupVO.setAvatar(chatGroup.getAvatar());
            groupVO.setOwnerId(chatGroup.getOwnerId());
            groupVO.setCreatedAt(chatGroup.getCreatedAt());
            groupVO.setAdminIds(groupMemberMapper.selectAdminIdsByGroupId(chatGroup.getId()));
            groupVO.setMembers(groupMemberMapper.selectMembersByGroupId(chatGroup.getId()));
            return groupVO;
        }).toList();
        return Result.success(groupVOS);
    }


    public Result<List<GroupVO>> getGroupListAndMembers() {
        Long userId = ThreadLocalUtil.getId();
        List<GroupVO> groupVOS = groupMemberMapper.selectGroupIdIfIam(userId).stream().map(groupId -> {
            GroupVO groupVO = new GroupVO();
            groupVO.setId(groupId);
            ChatGroup chatGroup = chatGroupMapper.selectById(groupId);
            groupVO.setName(chatGroup.getName());
            groupVO.setDescription(chatGroup.getDescription());
            groupVO.setAvatar(chatGroup.getAvatar());
            groupVO.setOwnerId(chatGroup.getOwnerId());
            groupVO.setCreatedAt(chatGroup.getCreatedAt());
            groupVO.setAdminIds(groupMemberMapper.selectAdminIdsByGroupId(groupId));
            groupVO.setMembers(groupMemberMapper.selectMembersByGroupId(groupId));
            return groupVO;
        }).toList();
        return Result.success(groupVOS);
    }


    public Result<List<GroupVO>> search(String nameLike) {
        List<GroupVO> groupVOS = chatGroupMapper.selectByNameLike(nameLike).stream().map(chatGroup -> {
            GroupVO groupVO = new GroupVO();
            groupVO.setId(chatGroup.getId());
            groupVO.setName(chatGroup.getName());
            groupVO.setDescription(chatGroup.getDescription());
            groupVO.setAvatar(chatGroup.getAvatar());
            groupVO.setOwnerId(chatGroup.getOwnerId());
            groupVO.setCreatedAt(chatGroup.getCreatedAt());
            groupVO.setAdminIds(groupMemberMapper.selectAdminIdsByGroupId(chatGroup.getId()));
            groupVO.setMembers(groupMemberMapper.selectMembersByGroupId(chatGroup.getId()));
            return groupVO;
        }).toList();
        return Result.success(groupVOS);
    }


    public Result update(GroupDTO groupDTO) {
        ChatGroup chatGroup = chatGroupMapper.selectById(groupDTO.getId());
        ChatGroup newChatGroup = new ChatGroup(chatGroup,groupDTO);
        chatGroupMapper.update(newChatGroup);
        return Result.success();
    }


    public Result leaveGroup(Long groupId, Long userId) {
        groupMemberMapper.deleteMember(groupId, userId);
        return Result.success();
    }


    public Result delete(Long ownerId, Long groupId) {
        // 在删除群组之前，先获取所有群成员ID（用于通知）
        List<Long> memberIds = groupMemberMapper.selectAllMemberIdsByGroupId(groupId);

        // 从成员列表中移除群主自己（群主不需要收到通知，因为是他主动解散的）
        memberIds.remove(ownerId);

        // 删除群组（数据库中通过 ON DELETE CASCADE 会自动删除相关记录）
        chatGroupMapper.delete(ownerId, groupId);

        // 通过 WebSocket 通知所有群成员（除了群主）群组已被解散
        if (!memberIds.isEmpty()) {
            webSocketFeignClient.broadcastGroupDeletedNotification(new WsBroadcastGroupDeleteDTO(memberIds, groupId));
        }

        return Result.success();
    }

}
