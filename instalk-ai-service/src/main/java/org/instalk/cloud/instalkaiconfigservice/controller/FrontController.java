package org.instalk.cloud.instalkaiconfigservice.controller;

import org.instalk.cloud.common.feign.client.FriendshipFeignClient;
import org.instalk.cloud.common.model.dto.AiChatDTO;
import org.instalk.cloud.common.model.dto.UserAiConfigDTO;
import org.instalk.cloud.common.model.dto.internal.FriendshipDTO;
import org.instalk.cloud.common.model.po.Friendship;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.model.vo.UserAiConfigVO;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.instalk.cloud.instalkaiconfigservice.mapper.UserAiConfigMapper;
import org.instalk.cloud.instalkaiconfigservice.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ai")
public class FrontController {

    @Autowired
    private AiService aiService;

    @Autowired
    private UserAiConfigMapper userAiConfigMapper;

    @Autowired
    private FriendshipFeignClient friendshipFeignClient;

    @GetMapping("/credential")
    public Result<String> getCredential() {
        return aiService.getCredential();
    }

    @GetMapping("/config")
    public Result<UserAiConfigVO> getAiConfig(@RequestParam Long robotId){
        if (robotId == null) return Result.error("参数错误");
        Long userId = ThreadLocalUtil.getId();
        Long minId = Long.min(userId,robotId);
        Long maxId = Long.max(userId,robotId);
        Friendship friendship = friendshipFeignClient.getByUserId1AndUserId2(new FriendshipDTO(minId,maxId));
        if (friendship==null || !friendship.getStatus().equals("ACCEPTED")) return Result.error("请先添加机器人为好友");
        return aiService.getAiConfig(robotId);
    }

    @PostMapping("/update")
    public Result update(@RequestBody UserAiConfigDTO userAiConfigDTO){
        Long userId = ThreadLocalUtil.getId();
        if (userAiConfigDTO == null) return Result.error("参数错误");
        if (userAiConfigDTO.getRobotId() == null) return Result.error("参数错误");
        if (userAiConfigMapper.isOwner(userId,userAiConfigDTO.getRobotId())==null) return Result.error("无权限");
        return aiService.update(userAiConfigDTO);
    }


    @PostMapping("/chat-stream")
    public SseEmitter streamChat(@RequestBody AiChatDTO aiChatDTO){
        return aiService.streamChat(aiChatDTO);
    }
}
