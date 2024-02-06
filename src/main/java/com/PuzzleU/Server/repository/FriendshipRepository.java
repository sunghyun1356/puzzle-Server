package com.PuzzleU.Server.repository;

import com.PuzzleU.Server.entity.friendship.FriendShip;
import com.PuzzleU.Server.entity.user.User;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<FriendShip, Long> {


    @Query("SELECT f FROM FriendShip f WHERE ((f.user1 = :user AND f.user2.userKoreaName LIKE %:keyword%) OR (f.user2 = :user AND f.user1.userKoreaName LIKE %:keyword%)) AND f.userStatus = true")
    Page<FriendShip> findActiveFriendshipsForUserAndKeyword(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);


}
