package org.instalk.cloud.instalkmessageservice.controller;

import org.instalk.cloud.common.feign.client.GroupFeignClient;
import org.instalk.cloud.common.model.dto.MessageDTO;
import org.instalk.cloud.common.model.dto.internal.GetGroupMemberDTO;
import org.instalk.cloud.common.model.po.Message;
import org.instalk.cloud.common.model.vo.MessageVO;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.instalk.cloud.instalkmessageservice.mapper.MessageMapper;
import org.instalk.cloud.instalkmessageservice.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class FrontController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private GroupFeignClient groupFeignClient;

    @Autowired
    private MessageMapper messageMapper;

    @PostMapping("/send")
    public Result<MessageVO> sendMessage(@RequestBody MessageDTO messageDTO){
        if (messageDTO == null) return Result.error("参数为空");
        return messageService.sendMessage(messageDTO);
    }

    @GetMapping("/messageList")
    public Result<List<MessageVO>> getMessageList(){
        return messageService.getMessageList();
    }


    @PostMapping("/newMessageList")
    public Result<List<MessageVO>> getNewMessageList(@RequestBody MessageVO lastMessage){
        if (lastMessage == null) Result.success();
        Long myId = ThreadLocalUtil.getId();
        if (lastMessage.getSenderId()!=myId &&
                lastMessage.getReceiverId()!=myId &&
                groupFeignClient.getMember(new GetGroupMemberDTO(myId,lastMessage.getGroupId()))==null
        ) Result.error("参数错误");
        return messageService.getNewMessageList(lastMessage.getId());
    }

    @PostMapping("/read")
    public Result readMessage(@RequestParam Long messageId){
        if (messageId == null) return Result.error("参数错误");
        if (messageMapper.selectById(messageId)==null) return Result.error("消息不存在");
        return messageService.readMessage(messageId);
    }

    @PostMapping("/readList")
    public Result readMessageList(@RequestParam List<Long> messageIds){
        if (messageIds==null) return Result.error("参数错误");
        return messageService.readMessageList(messageIds);
    }


    @PostMapping("/revoke")
    public Result revokeMessage(@RequestParam Long messageId){
        if (messageId == null) return Result.error("参数错误");
        Message message = messageMapper.selectById(messageId);
        if (message==null) return Result.error("消息不存在");
        if (!message.getSenderId().equals(ThreadLocalUtil.getId())) return Result.error("您没有权限");
        return messageService.revokeMessage(message);
    }
}
