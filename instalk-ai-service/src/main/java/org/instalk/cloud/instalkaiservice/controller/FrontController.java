package org.instalk.cloud.instalkaiservice.controller;

import org.instalk.cloud.instalkaiservice.mapper.UserAiConfigMapper;
import org.instalk.cloud.instalkaiservice.service.AiChatService;
import org.instalk.cloud.common.feign.client.FriendshipFeignClient;
import org.instalk.cloud.common.model.dto.AiChatDTO;
import org.instalk.cloud.common.model.dto.UserAiConfigDTO;
import org.instalk.cloud.common.model.dto.internal.FriendshipDTO;
import org.instalk.cloud.common.model.po.Friendship;
import org.instalk.cloud.common.model.vo.Result;
import org.instalk.cloud.common.model.vo.UserAiConfigVO;
import org.instalk.cloud.common.util.ThreadLocalUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ai")
public class FrontController {

    private final AiChatService aiChatService;
    private final UserAiConfigMapper userAiConfigMapper;
    private final FriendshipFeignClient friendshipFeignClient;

    public FrontController(AiChatService aiChatService,
                           UserAiConfigMapper userAiConfigMapper,
                           FriendshipFeignClient friendshipFeignClient) {
        this.aiChatService = aiChatService;
        this.userAiConfigMapper = userAiConfigMapper;
        this.friendshipFeignClient = friendshipFeignClient;
    }

    @GetMapping("/credential")
    public Result<String> getCredential() {
        return aiChatService.getCredential();
    }

    @GetMapping("/config")
    public Result<UserAiConfigVO> getAiConfig(@RequestParam Long robotId) {
        if (robotId == null) {
            return Result.error("参数错误");
        }
        Long userId = ThreadLocalUtil.getId();
        Long minId = Long.min(userId, robotId);
        Long maxId = Long.max(userId, robotId);
        Friendship friendship = friendshipFeignClient.getByUserId1AndUserId2(new FriendshipDTO(minId, maxId));
        if (friendship == null || !friendship.getStatus().equals("ACCEPTED")) {
            return Result.error("请先添加机器人为好友");
        }
        return aiChatService.getAiConfig(robotId);
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody UserAiConfigDTO userAiConfigDTO) {
        Long userId = ThreadLocalUtil.getId();
        if (userAiConfigDTO == null) {
            return Result.error("参数错误");
        }
        if (userAiConfigDTO.getRobotId() == null) {
            return Result.error("参数错误");
        }
        if (userAiConfigMapper.isOwner(userId, userAiConfigDTO.getRobotId()) == null) {
            return Result.error("无权限");
        }
        return aiChatService.update(userAiConfigDTO);
    }

    @PostMapping("/chat-stream")
    public SseEmitter streamChat(@RequestBody AiChatDTO aiChatDTO) {
        return aiChatService.streamChat(aiChatDTO);
    }
}
