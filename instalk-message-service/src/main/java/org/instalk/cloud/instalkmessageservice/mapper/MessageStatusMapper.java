package org.instalk.cloud.instalkmessageservice.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageStatusMapper {
    @Insert("insert into message_status (message_id,user_id) values (#{messageId},#{receiverId})")
    void add(Long messageId, Long receiverId);

    @Insert("insert into message_status (message_id,user_id,is_read) values (#{messageId},#{receiverId},TRUE)")
    void addAndRead(Long messageId, Long receiverId);

    @Update("update message_status set is_read = TRUE,read_at=now() where user_id = #{receiverId} and message_id = #{messageId}")
    Integer updateToRead(Long messageId, Long receiverId);

    @Update({
            "<script>" +
                    " UPDATE message_status SET is_read = TRUE, read_at = NOW() " +
                    "WHERE user_id = #{receiverId} " +
                    "AND message_id IN " +
                    "<foreach collection='messageIds' item='id' open='(' close=')' separator=','> #{id} </foreach>" +
                    "</script>"
    })
    Integer updateListToRead(Long receiverId, List<Long> messageIds);

    @Select("select is_read from message_status where message_id=#{messageId} and user_id=#{receiverId}")
    Boolean select(Long messageId, Long receiverId);
}
