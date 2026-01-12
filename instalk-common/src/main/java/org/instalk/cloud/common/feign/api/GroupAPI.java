package org.instalk.cloud.common.feign.api;

import org.instalk.cloud.common.model.dto.internal.GetGroupMemberDTO;
import org.instalk.cloud.common.model.po.GroupMember;
import org.instalk.cloud.common.model.vo.GroupVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GroupAPI {

    @PostMapping("/member")
    GroupMember getMember(@RequestBody GetGroupMemberDTO getGroupMemberDTO);

    @GetMapping("/members")
    List<GroupVO.Member> getMembersByGroupId(@RequestParam Long groupId);
}
