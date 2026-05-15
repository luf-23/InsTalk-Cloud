package org.instalk.cloud.instalksocialservice.group.controller;

import org.instalk.cloud.common.model.dto.GroupDTO;
import org.instalk.cloud.common.model.vo.GroupVO;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.instalk.cloud.instalksocialservice.group.mapper.ChatGroupMapper;
import org.instalk.cloud.instalksocialservice.group.mapper.GroupMemberMapper;
import org.instalk.cloud.instalksocialservice.group.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("groupFrontController")
@RequestMapping("/group")
public class FrontController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private ChatGroupMapper chatGroupMapper;

    @PostMapping("/create")
    public Result<GroupVO> createGroup(@RequestBody GroupDTO groupDTO){
        return groupService.createGroup(groupDTO);
    }

    @PostMapping("/join")
    public Result<GroupVO> joinGroup(@RequestParam Long groupId){
        return groupService.joinGroup(groupId);
    }

    @GetMapping("/myGroupList")
    public Result<List<GroupVO>> getGroupListAndMembers(){
        return groupService.getMyGroupListAndMembers();
    }

    @GetMapping("/groupList")
    public Result<List<GroupVO>> getGroupList(){
        return groupService.getGroupListAndMembers();
    }

    @GetMapping("/search")
    public Result<List<GroupVO>> search(@RequestParam String nameLike){
        return groupService.search(nameLike);
    }

    @PostMapping("/update")
    public Result update(@RequestBody GroupDTO groupDTO){
        if (groupDTO==null || groupDTO.getId()==null) return Result.error("参数错误");
        return groupService.update(groupDTO);
    }

    @PostMapping("/leave")
    public Result exit(@RequestParam Long groupId){
        Long userId = ThreadLocalUtil.getId();
        if (groupMemberMapper.select(userId, groupId)==null) return Result.error("您已退出该�?);
        return groupService.leaveGroup(groupId, userId);
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam Long groupId){
        Long ownerId = ThreadLocalUtil.getId();
        if (chatGroupMapper.selectByOwnerId(ownerId)==null) return Result.error("您没有权限删除该�?);
        return groupService.delete(ownerId,groupId);
    }

}
