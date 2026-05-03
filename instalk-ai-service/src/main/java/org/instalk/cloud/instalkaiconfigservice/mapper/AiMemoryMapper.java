package org.instalk.cloud.instalkaiconfigservice.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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
            "ORDER BY DOT_PRODUCT(embedding, CAST(#{embeddingJson} AS VECTOR(1536))) DESC " +
            "LIMIT #{limit}")
    List<AiMemory> selectTopByEmbedding(@Param("userId") Long userId,
                                        @Param("robotId") Long robotId,
                                        @Param("embeddingJson") String embeddingJson,
                                        @Param("limit") int limit);

    @Insert("INSERT INTO ai_memory (user_id, robot_id, type, content, embedding_json, embedding) " +
            "VALUES (#{userId}, #{robotId}, #{type}, #{content}, #{embeddingJson}, CAST(#{embeddingJson} AS VECTOR(1536)))")
    void insert(@Param("userId") Long userId,
                @Param("robotId") Long robotId,
                @Param("type") String type,
                @Param("content") String content,
                @Param("embeddingJson") String embeddingJson);
}
