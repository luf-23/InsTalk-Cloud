package org.instalk.cloud.instalkfriendshipservice.mapper;

import org.apache.ibatis.annotations.*;
import org.instalk.cloud.common.model.po.Friendship;

import java.util.List;

@Mapper
public interface FriendshipMapper {

    @Select("SELECT * FROM friendship WHERE user_id1 = #{id1} AND user_id2 = #{id2}")
    Friendship selectByUserId1AndUserId2(Long id1, Long id2);

    @Insert("insert into friendship (user_id1,user_id2,requester_id) values (#{id1},#{id2},#{myId})")
    void addRequest(Long id1, Long id2,Long myId);

    @Update("update friendship set status = 'ACCEPTED' WHERE user_id1= #{id1} AND user_id2= #{id2} AND status = 'PENDING'")
    void acceptRequest(Long id1, Long id2);

    @Delete("delete from friendship where user_id1= #{id1} AND user_id2= #{id2} AND status='PENDING'")
    void rejectRequest(Long id1, Long id2);

    @Delete("delete from friendship where user_id1= #{id1} AND user_id2= #{id2} AND status='ACCEPTED'")
    void deleteRequest(Long id1, Long id2);


    @Select("SELECT user_id2 FROM friendship " +
            "WHERE status = 'ACCEPTED' AND user_id1 = #{myId}" +
            " UNION " +
            "SELECT user_id1 FROM friendship " +
            "WHERE status = 'ACCEPTED' AND user_id2 = #{myId}")
    List<Long> selectFriendsId(Long myId);

    @Select("SELECT user_id2 FROM friendship " +
            "WHERE status = 'PENDING' AND user_id1 = #{myId} AND requester_id != #{myId}" +
            " UNION " +
            "SELECT user_id1 FROM friendship " +
            "WHERE status = 'PENDING' AND user_id2 = #{myId} AND requester_id != #{myId}")
    List<Long> selectPendingId(Long myId);

    @Insert("insert into friendship (user_id1,user_id2,status) values (#{id1},#{id2},'ACCEPTED')")
    void makeFriendsWithRobot(Long id1, Long id2);
}
