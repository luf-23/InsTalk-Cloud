package org.instalk.cloud.instalkgroupservice.controller;

import org.instalk.cloud.common.feign.api.GroupAPI;
import org.instalk.cloud.common.model.dto.internal.GetGroupMemberDTO;
import org.instalk.cloud.common.model.po.GroupMember;
import org.instalk.cloud.common.model.vo.GroupVO;
import org.instalk.cloud.instalkgroupservice.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/group")
public class InternalController implements GroupAPI {

    @Autowired
    private GroupService groupService;

    @Override
    public GroupMember getMember(@RequestBody GetGroupMemberDTO getGroupMemberDTO) {
        return groupService.getMember(getGroupMemberDTO.getSenderId(), getGroupMemberDTO.getGroupId());
    }

    @Override
    public List<GroupVO.Member> getMembersByGroupId(@RequestParam Long groupId) {
        return groupService.getMembersByGroupId(groupId);
    }
}
