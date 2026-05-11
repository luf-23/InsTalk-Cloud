package org.instalk.cloud.instalkaiservice.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.instalk.cloud.common.model.enums.AiMemoryType;
import org.instalk.cloud.common.model.po.AiMemory;

import java.util.List;

@Mapper
public interface AiMemoryMapper {

    @Select("SELECT * FROM ai_memory " +
            "WHERE user_id = #{userId} AND robot_id = #{robotId} " +
            "ORDER BY created_at DESC " +
            "LIMIT #{limit}")
    List<AiMemory> selectLatest(@Param("userId") Long userId,
                                @Param("robotId") Long robotId,
                                @Param("limit") int limit);

    @Select("SELECT * FROM ai_memory " +
            "WHERE user_id = #{userId} AND robot_id = #{robotId} AND embedding IS NOT NULL " +
            "ORDER BY embedding <=> CAST(#{embeddingVector} AS vector) ASC " +
            "LIMIT #{limit}")
    List<AiMemory> selectTopByEmbedding(@Param("userId") Long userId,
                                        @Param("robotId") Long robotId,
                                        @Param("embeddingVector") String embeddingVector,
                                        @Param("limit") int limit);

    @Insert("INSERT INTO ai_memory (user_id, robot_id, type, content, embedding) " +
            "VALUES (#{userId}, #{robotId}, #{type}, #{content}, CAST(#{embeddingVector} AS vector))")
    void insert(@Param("userId") Long userId,
                @Param("robotId") Long robotId,
                @Param("type") AiMemoryType type,
                @Param("content") String content,
                @Param("embeddingVector") String embeddingVector);
}
