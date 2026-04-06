package org.instalk.cloud.instalkgroupservice.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.instalk.cloud.common.model.po.GroupMember;
import org.instalk.cloud.common.model.vo.GroupVO;

import java.util.List;

@Mapper
public interface GroupMemberMapper {
    @Insert("insert into group_member (user_id,group_id,role) values (#{ownerId},#{groupId},'OWNER')")
    void addOwner(Long ownerId, Long groupId);

    @Select("SELECT * FROM group_member WHERE user_id = #{userId} AND group_id = #{groupId}")
    GroupMember select(Long userId, Long groupId);

    @Insert("insert into group_member (user_id,group_id) values (#{userId},#{groupId})")
    void addMember(Long userId, Long groupId);

    @Select("SELECT u.id, u.username, u.signature, u.avatar, " +
            "(SELECT joined_at FROM group_member WHERE group_id = #{groupId} AND user_id = u.id) AS joinedAt " +
            "FROM user u " +
            "WHERE EXISTS " +
            "(SELECT 1 FROM group_member gm " +
            "WHERE gm.group_id = #{groupId} AND gm.user_id = u.id) " +
            "ORDER BY joinedAt ASC")
    List<GroupVO.Member> selectMembersByGroupId(Long groupId);

    @Select("SELECT group_id FROM group_member WHERE user_id=#{userId}")
    List<Long> selectGroupIdIfIam(Long userId);

    @Select("SELECT user_id FROM group_member WHERE group_id = #{id} AND role = 'ADMIN'")
    List<Long> selectAdminIdsByGroupId(Long id);

    @Select("SELECT user_id FROM group_member WHERE group_id = #{groupId}")
    List<Long> selectAllMemberIdsByGroupId(Long groupId);

    @Delete("DELETE FROM group_member WHERE group_id = #{groupId} AND user_id = #{userId}")
    void deleteMember(Long groupId, Long userId);
}
