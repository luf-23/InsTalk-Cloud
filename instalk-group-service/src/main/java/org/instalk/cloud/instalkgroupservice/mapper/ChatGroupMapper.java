package org.instalk.cloud.instalkgroupservice.mapper;

import org.apache.ibatis.annotations.*;
import org.instalk.cloud.common.model.po.ChatGroup;

import java.util.List;

@Mapper
public interface ChatGroupMapper {
    @Select("SELECT * FROM chat_group WHERE owner_id = #{userId}")
    List<ChatGroup> selectByOwnerId(Long userId);

    @Select("SELECT * FROM chat_group WHERE id = #{groupId}")
    ChatGroup selectById(Long groupId);

    @Insert({
            "<script>",
            "INSERT INTO chat_group (name, description, owner_id",
            "<if test='avatar != null and avatar != \"\"'>, avatar</if>",
            ") VALUES (#{name}, #{description}, #{ownerId}",
            "<if test='avatar != null and avatar != \"\"'>, #{avatar}</if>",
            ")",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void add(ChatGroup chatGroup);

    @Select("SELECT * FROM chat_group WHERE name LIKE CONCAT('%',#{nameLike},'%')")
    List<ChatGroup> selectByNameLike(String nameLike);

    @Update("UPDATE chat_group SET name=#{name},description=#{description},avatar=#{avatar} WHERE id=#{id}")
    void update(ChatGroup newChatGroup);

    @Delete("DELETE FROM chat_group WHERE owner_id = #{ownerId} AND id = #{groupId}")
    void delete(Long ownerId, Long groupId);
}
