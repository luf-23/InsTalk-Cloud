package org.instalk.cloud.instalkaiconfigservice.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.instalk.cloud.common.model.po.AiChatSummary;

@Mapper
public interface AiChatSummaryMapper {

    @Select("SELECT * FROM ai_chat_summary WHERE user_id = #{userId} AND robot_id = #{robotId}")
    AiChatSummary selectByUserAndRobot(@Param("userId") Long userId, @Param("robotId") Long robotId);

    @Insert("INSERT INTO ai_chat_summary (user_id, robot_id, summary, last_message_id) " +
            "VALUES (#{userId}, #{robotId}, #{summary}, #{lastMessageId}) " +
            "ON CONFLICT (user_id, robot_id) DO UPDATE SET summary = EXCLUDED.summary, " +
            "last_message_id = EXCLUDED.last_message_id, updated_at = CURRENT_TIMESTAMP")
    void upsert(@Param("userId") Long userId,
                @Param("robotId") Long robotId,
                @Param("summary") String summary,
                @Param("lastMessageId") Long lastMessageId);
}
