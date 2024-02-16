package com.PuzzleU.Server.relations.entity;

import com.PuzzleU.Server.university.entity.University;
import com.PuzzleU.Server.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUniversityRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userUniversityId;

    // 의존 관계 매핑 (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;

    // 의존관계 매핑 (Skillset)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;
}