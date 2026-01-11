package org.instalk.cloud.instalkmessageservice.mapper;

import org.apache.ibatis.annotations.*;
import org.instalk.cloud.common.model.po.Message;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MessageMapper {
    @Select("select * from message where id = #{id}")
    Message selectById(Long id);

    @Insert("insert into message (sender_id,receiver_id,content,message_type) values (#{senderId},#{receiverId},#{content},#{messageType})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void addPrivateMessage(Message message);

    @Insert("insert into message (sender_id,group_id,content,message_type) values (#{senderId},#{groupId},#{content},#{messageType})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void addGroupMessage(Message message);

    @Select("select * from message where sender_id = #{senderId}")
    List<Message> selectBySenderId(Long senderId);

    @Select("select * from message where receiver_id = #{receiverId}")
    List<Message> selectByReceiverId(Long receiverId);

    @Select("select * from message where receiver_id = #{userId} and id > #{messageId}")
    List<Message> selectNewByReceiverId(Long userId, Long messageId);

    @Select("select * from message where sender_id != #{userId} and group_id in (select group_id from group_member where user_id=#{userId} )")
    List<Message> selectGroupMessagesAsReceiver(Long userId);

    @Select("select * from message where sender_id != #{userId} and group_id in (select group_id from group_member where user_id=#{userId}) and id > #{messageId}")
    List<Message> selectNewGroupMessagesAsReceiver(Long userId,Long messageId);

    @Select("select sent_at from message where id = #{id}")
    LocalDateTime selectSentAtById(Long id);

    @Select({
            "<script>",
            "SELECT * FROM message WHERE",
            "<if test='ids != null and !ids.isEmpty()'>",
            "id IN",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</if>",
            "<if test='ids == null or ids.isEmpty()'>",
            "1 = 0",
            "</if>",
            "ORDER BY sent_at ASC",
            "</script>"
    })
    List<Message> selectByIds(@Param("ids") List<Long> ids);

    @Delete("delete from message where id = #{id}")
    void deleteById(Long id);

    @Delete("delete from message where sender_id=#{id1} and receiver_id=#{id2} or sender_id=#{id2} and receiver_id=#{id1}")
    void deleteById1AndId2(Long id1, Long id2);
}
