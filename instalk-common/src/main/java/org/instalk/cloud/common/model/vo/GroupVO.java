package org.instalk.cloud.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.instalk.cloud.common.model.po.ChatGroup;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupVO {
    private Long id;
    private String name;
    private String description;
    private String avatar;
    private Long ownerId;
    private LocalDateTime createdAt;
    private List<Long> adminIds;
    private List<Member> members;
    @Data
    public static class Member{
        private Long id;
        private String username;
        private String signature;
        private String avatar;
        private LocalDateTime joinedAt;
    }

    public GroupVO(ChatGroup chatGroup, List<Long> adminIds, List<Member> members){
        this.id = chatGroup.getId();
        this.name = chatGroup.getName();
        this.description = chatGroup.getDescription();
        this.avatar = chatGroup.getAvatar();
        this.ownerId = chatGroup.getOwnerId();
        this.createdAt = chatGroup.getCreatedAt();
        this.adminIds = adminIds;
        this.members = members;
    }
}
