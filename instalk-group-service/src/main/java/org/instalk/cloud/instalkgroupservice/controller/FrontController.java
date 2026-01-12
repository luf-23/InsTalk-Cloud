package org.instalk.cloud.instalkgroupservice.controller;

import org.instalk.cloud.common.model.dto.GroupDTO;
import org.instalk.cloud.common.model.vo.GroupVO;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.instalk.cloud.instalkgroupservice.mapper.ChatGroupMapper;
import org.instalk.cloud.instalkgroupservice.mapper.GroupMemberMapper;
import org.instalk.cloud.instalkgroupservice.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
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
        if (groupMemberMapper.select(userId, groupId)==null) return Result.error("您已退出该群");
        return groupService.leaveGroup(groupId, userId);
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam Long groupId){
        Long ownerId = ThreadLocalUtil.getId();
        if (chatGroupMapper.selectByOwnerId(ownerId)==null) return Result.error("您没有权限删除该群");
        return groupService.delete(ownerId,groupId);
    }

}
