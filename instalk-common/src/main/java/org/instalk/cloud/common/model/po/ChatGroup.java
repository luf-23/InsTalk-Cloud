package org.instalk.cloud.common.model.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.instalk.cloud.common.model.dto.GroupDTO;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroup {
    private Long id;
    private String name;
    private String description;
    private String avatar;
    private Long ownerId;
    private LocalDateTime createdAt;

    public ChatGroup(ChatGroup chatGroup, GroupDTO groupDTO){
        this.id = chatGroup.getId();
        this.name = groupDTO.getName() !=null ? groupDTO.getName() : chatGroup.getName();
        this.description = groupDTO.getDescription() != null ? groupDTO.getDescription() : chatGroup.getDescription();
        this.avatar = groupDTO.getAvatar() != null ? groupDTO.getAvatar() : chatGroup.getAvatar();
    }
}
