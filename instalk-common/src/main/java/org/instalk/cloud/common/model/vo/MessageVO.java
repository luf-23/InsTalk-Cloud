package org.instalk.cloud.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.instalk.cloud.common.model.po.Message;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageVO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long groupId;
    private String content;
    private String messageType;
    private LocalDateTime sentAt;
    private Boolean isRead;
    //1.senderId为自己
    //2.receiverId为自己
    //3.groupId群成员有自己且senderId不为自己

    public MessageVO(Message message, Boolean isRead){
        this.id = message.getId();
        this.senderId = message.getSenderId();
        this.receiverId = message.getReceiverId();
        this.groupId = message.getGroupId();
        this.content = message.getContent();
        this.messageType = message.getMessageType();
        this.sentAt = message.getSentAt();
        this.isRead = isRead;
    }
}
