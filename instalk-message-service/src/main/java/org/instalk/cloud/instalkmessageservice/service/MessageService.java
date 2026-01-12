package org.instalk.cloud.instalkmessageservice.service;

import org.instalk.cloud.common.feign.client.FriendshipFeignClient;
import org.instalk.cloud.common.feign.client.GroupFeignClient;
import org.instalk.cloud.common.feign.client.WebSocketFeignClient;
import org.instalk.cloud.common.model.dto.MessageDTO;
import org.instalk.cloud.common.model.dto.internal.*;
import org.instalk.cloud.common.model.po.Message;
import org.instalk.cloud.common.model.vo.GroupVO;
import org.instalk.cloud.common.model.vo.MessageVO;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.instalk.cloud.instalkmessageservice.mapper.MessageMapper;
import org.instalk.cloud.instalkmessageservice.mapper.MessageStatusMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private MessageStatusMapper messageStatusMapper;

    @Autowired
    private WebSocketFeignClient webSocketFeignClient;

    @Autowired
    private FriendshipFeignClient friendshipFeignClient;

    @Autowired
    private GroupFeignClient groupFeignClient;

    public void deleteById1AndId2(DeleteMessageDTO deleteMessageDTO) {
        messageMapper.deleteById1AndId2(deleteMessageDTO.getId1(), deleteMessageDTO.getId2());
    }

     
    public Result<MessageVO> sendMessage(MessageDTO messageDTO) {
        Long senderId = ThreadLocalUtil.getId();
        Message message = new Message();
        message.setSenderId(senderId);
        message.setContent(messageDTO.getContent());
        message.setMessageType(messageDTO.getMessageType());
        message.setReceiverId(messageDTO.getReceiverId());
        message.setGroupId(messageDTO.getGroupId());

        if (message.getReceiverId()!= null){
            if (!friendshipFeignClient.getByUserId1AndUserId2(new FriendshipDTO(Long.min(senderId,message.getReceiverId()),Long.max(senderId,message.getReceiverId()))).getStatus().equals("ACCEPTED")){
                return Result.error("请先添加对方为好友");
            }
            messageMapper.addPrivateMessage(message);
            messageStatusMapper.add(message.getId(),message.getReceiverId());

            // 通过 WebSocket 推送消息给接收者
            message.setSentAt(messageMapper.selectSentAtById(message.getId()));
            MessageVO messageVO = new MessageVO(message, Boolean.FALSE); // 对接收者来说是未读
            webSocketFeignClient.sendMessageToUser(new WsSendPrivateMessageDTO(message.getReceiverId(), messageVO));
        }
        if (message.getGroupId()!= null){
            if(groupFeignClient.getMember(new GetGroupMemberDTO(senderId,message.getGroupId()))== null){
                return Result.error("您不是群成员");
            }
            messageMapper.addGroupMessage(message);
            List<GroupVO.Member> receiverIds = groupFeignClient.getMembersByGroupId(message.getGroupId());
            List<Long> receiverIdList = new ArrayList<>();
            for (GroupVO.Member member : receiverIds) {
                if (member.getId()==senderId) continue;
                messageStatusMapper.add(message.getId(),member.getId());
                receiverIdList.add(member.getId());
            }

            // 通过 WebSocket 推送消息给所有群成员（除发送者外）
            message.setSentAt(messageMapper.selectSentAtById(message.getId()));
            MessageVO messageVO = new MessageVO(message, Boolean.FALSE); // 对接收者来说是未读
            webSocketFeignClient.broadcastMessageToUsers(new WsBroadcastMessageDTO(receiverIdList, messageVO));
        }
        message.setSentAt(messageMapper.selectSentAtById(message.getId()));
        MessageVO messageVO = new MessageVO(message,Boolean.TRUE);
        return Result.success(messageVO);
    }

     
    public Result<List<MessageVO>> getMessageList() {
        Long userId = ThreadLocalUtil.getId();
        List<MessageVO> messageVOS1 = messageMapper.selectBySenderId(userId).stream().map(message -> {
            MessageVO messageVO = new MessageVO();
            messageVO.setId(message.getId());
            messageVO.setSenderId(message.getSenderId());
            messageVO.setReceiverId(message.getReceiverId());
            messageVO.setGroupId(message.getGroupId());
            messageVO.setContent(message.getContent());
            messageVO.setMessageType(message.getMessageType());
            messageVO.setSentAt(message.getSentAt());
            messageVO.setIsRead(Boolean.TRUE);
            return messageVO;
        }).toList();
        List<MessageVO> messageVOS2 = messageMapper.selectByReceiverId(userId).stream().map(message -> {
            MessageVO messageVO = new MessageVO();
            messageVO.setId(message.getId());
            messageVO.setSenderId(message.getSenderId());
            messageVO.setReceiverId(message.getReceiverId());
            messageVO.setGroupId(message.getGroupId());
            messageVO.setContent(message.getContent());
            messageVO.setMessageType(message.getMessageType());
            messageVO.setSentAt(message.getSentAt());
            messageVO.setIsRead(messageStatusMapper.select(message.getId(),userId));
            return messageVO;
        }).toList();
        List<MessageVO> messageVOS3 = messageMapper.selectGroupMessagesAsReceiver(userId).stream().map(message -> {
            MessageVO messageVO = new MessageVO();
            messageVO.setId(message.getId());
            messageVO.setSenderId(message.getSenderId());
            messageVO.setReceiverId(message.getReceiverId());
            messageVO.setGroupId(message.getGroupId());
            messageVO.setContent(message.getContent());
            messageVO.setMessageType(message.getMessageType());
            messageVO.setSentAt(message.getSentAt());
            messageVO.setIsRead(messageStatusMapper.select(message.getId(),userId));
            return messageVO;
        }).toList();
        List<MessageVO> messageVOS = new ArrayList<>();
        messageVOS.addAll(messageVOS1);
        messageVOS.addAll(messageVOS2);
        messageVOS.addAll(messageVOS3);
        return Result.success(messageVOS);
    }

     
    public Result<List<MessageVO>> getNewMessageList(Long messageId) {
        Long userId = ThreadLocalUtil.getId();
        List<MessageVO> messageVOS1 = messageMapper.selectNewByReceiverId(userId,messageId).stream().map(message -> {
            MessageVO messageVO = new MessageVO();
            messageVO.setId(message.getId());
            messageVO.setSenderId(message.getSenderId());
            messageVO.setReceiverId(message.getReceiverId());
            messageVO.setGroupId(message.getGroupId());
            messageVO.setContent(message.getContent());
            messageVO.setMessageType(message.getMessageType());
            messageVO.setSentAt(message.getSentAt());
            messageVO.setIsRead(messageStatusMapper.select(message.getId(),userId));
            return messageVO;
        }).toList();
        List<MessageVO> messageVOS2 = messageMapper.selectNewGroupMessagesAsReceiver(userId,messageId).stream().map(message -> {
            MessageVO messageVO = new MessageVO();
            messageVO.setId(message.getId());
            messageVO.setSenderId(message.getSenderId());
            messageVO.setReceiverId(message.getReceiverId());
            messageVO.setGroupId(message.getGroupId());
            messageVO.setContent(message.getContent());
            messageVO.setMessageType(message.getMessageType());
            messageVO.setSentAt(message.getSentAt());
            messageVO.setIsRead(messageStatusMapper.select(message.getId(),userId));
            return messageVO;
        }).toList();
        List<MessageVO> messageVOS = new ArrayList<>();
        messageVOS.addAll(messageVOS1);
        messageVOS.addAll(messageVOS2);
        return Result.success(messageVOS);
    }

     
    public Result<Void> readMessage(Long messageId) {
        Long userId = ThreadLocalUtil.getId();
        Integer count = messageStatusMapper.updateToRead(messageId,userId);
        if (count == 0) messageStatusMapper.addAndRead(messageId,userId);
        return Result.success();
    }

     
    public Result<Void> readMessageList(List<Long> messageIds) {
        Long userId = ThreadLocalUtil.getId();
        Integer count = messageStatusMapper.updateListToRead(userId,messageIds);
        if (count < messageIds.size()) {
            for (Long messageId : messageIds){
                if (messageStatusMapper.select(messageId,userId) == null){
                    messageStatusMapper.addAndRead(messageId,userId);
                }
            }
        }
        return Result.success();
    }

     
    public Result<Void> revokeMessage(Message message) {
        Long messageId = message.getId();
        messageMapper.deleteById(messageId);

        // 通过 WebSocket 通知相关用户消息已被撤回
        if (message.getReceiverId() != null) {
            // 私聊消息：通知接收者
            webSocketFeignClient.sendMessageRecallNotification(new WsRevokeMessageDTO(message.getReceiverId(), messageId));
        } else if (message.getGroupId() != null) {
            // 群聊消息：通知所有群成员（除了发送者）
            List<GroupVO.Member> members = groupFeignClient.getMembersByGroupId(message.getGroupId());
            List<Long> receiverIds = new ArrayList<>();
            for (GroupVO.Member member : members) {
                if (!member.getId().equals(message.getSenderId())) {
                    receiverIds.add(member.getId());
                }
            }
            webSocketFeignClient.broadcastMessageRecallNotification(new WsBroadcastRevokeDTO(receiverIds, messageId));
        }

        return Result.success();
    }

}
