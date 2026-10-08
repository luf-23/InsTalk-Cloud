package org.instalk.cloud.common.model.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastGroupDeleteDTO;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastMessageDTO;
import org.instalk.cloud.common.model.dto.internal.WsBroadcastRevokeDTO;
import org.instalk.cloud.common.model.dto.internal.WsDeleteFriendDTO;
import org.instalk.cloud.common.model.dto.internal.WsRevokeMessageDTO;
import org.instalk.cloud.common.model.dto.internal.WsSendPrivateMessageDTO;
import org.instalk.cloud.common.model.vo.MessageVO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessagePushMQ implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessagePushType pushType;
    private MessageVO messageVO;
    private Long receiverId;
    private List<Long> receiverIds;
    private Long messageId;
    private Long friendId;
    private Long groupId;
    private Integer retryCount = 0;

    public static MessagePushMQ fromPrivateMessage(MessageMQ messageMQ) {
        MessagePushMQ push = new MessagePushMQ();
        push.setPushType(MessagePushType.PRIVATE_MESSAGE);
        push.setMessageVO(messageMQ.getMessageVO());
        push.setReceiverId(messageMQ.getMessageVO().getReceiverId());
        push.setReceiverIds(privateMessageReceivers(messageMQ.getMessageVO()));
        push.setRetryCount(messageMQ.getRetryCount());
        return push;
    }

    public static MessagePushMQ fromGroupMessage(MessageMQ messageMQ) {
        MessagePushMQ push = new MessagePushMQ();
        push.setPushType(MessagePushType.GROUP_MESSAGE);
        push.setMessageVO(messageMQ.getMessageVO());
        push.setReceiverIds(withSender(messageMQ.getReceiverIds(), messageMQ.getMessageVO()));
        push.setRetryCount(messageMQ.getRetryCount());
        return push;
    }

    public static MessagePushMQ fromSendPrivateMessage(WsSendPrivateMessageDTO dto) {
        MessagePushMQ push = new MessagePushMQ();
        push.setPushType(MessagePushType.PRIVATE_MESSAGE);
        push.setMessageVO(dto.getMessageVO());
        push.setReceiverId(dto.getReceiverId());
        push.setReceiverIds(privateMessageReceivers(dto.getMessageVO()));
        return push;
    }

    private static List<Long> privateMessageReceivers(MessageVO messageVO) {
        return withSender(List.of(messageVO.getReceiverId()), messageVO);
    }

    private static List<Long> withSender(List<Long> originalReceiverIds, MessageVO messageVO) {
        List<Long> receiverIds = new ArrayList<>();
        if (originalReceiverIds != null) {
            receiverIds.addAll(originalReceiverIds);
        }
        if (messageVO != null && messageVO.getSenderId() != null
                && !receiverIds.contains(messageVO.getSenderId())) {
            receiverIds.add(messageVO.getSenderId());
        }
        return receiverIds;
    }

    public static MessagePushMQ fromBroadcastMessage(WsBroadcastMessageDTO dto) {
        MessagePushMQ push = new MessagePushMQ();
        push.setPushType(MessagePushType.GROUP_MESSAGE);
        push.setMessageVO(dto.getMessageVO());
        push.setReceiverIds(withSender(dto.getReceiverIds(), dto.getMessageVO()));
        return push;
    }

    public static MessagePushMQ fromDeleteFriend(WsDeleteFriendDTO dto) {
        MessagePushMQ push = new MessagePushMQ();
        push.setPushType(MessagePushType.FRIEND_DELETED);
        push.setReceiverId(dto.getId());
        push.setFriendId(dto.getMyId());
        return push;
    }

    public static MessagePushMQ fromRevokeMessage(WsRevokeMessageDTO dto) {
        MessagePushMQ push = new MessagePushMQ();
        push.setPushType(MessagePushType.MESSAGE_RECALL);
        push.setReceiverId(dto.getReceiverId());
        push.setMessageId(dto.getMessageId());
        return push;
    }

    public static MessagePushMQ fromBroadcastRevoke(WsBroadcastRevokeDTO dto) {
        MessagePushMQ push = new MessagePushMQ();
        push.setPushType(MessagePushType.BROADCAST_RECALL);
        push.setReceiverIds(dto.getReceiverIds());
        push.setMessageId(dto.getMessageId());
        return push;
    }

    public static MessagePushMQ fromBroadcastGroupDelete(WsBroadcastGroupDeleteDTO dto) {
        MessagePushMQ push = new MessagePushMQ();
        push.setPushType(MessagePushType.GROUP_DELETED);
        push.setReceiverIds(dto.getMemberIds());
        push.setGroupId(dto.getGroupId());
        return push;
    }

    public MessagePushMQ forReceivers(List<Long> receiverIds) {
        MessagePushMQ copy = new MessagePushMQ();
        copy.pushType = pushType;
        copy.messageVO = messageVO;
        copy.receiverId = receiverId;
        copy.receiverIds = receiverIds;
        copy.messageId = messageId;
        copy.friendId = friendId;
        copy.groupId = groupId;
        copy.retryCount = retryCount;
        return copy;
    }
}
