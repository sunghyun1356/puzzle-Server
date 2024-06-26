package com.PuzzleU.Server.position.entity;

import com.PuzzleU.Server.relations.entity.TeamPositionRelation;
import com.PuzzleU.Server.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "position")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long positionId;

    @Column(name = "position_name", length = 10)
    private String positionName; // 포지션 이름

    @Column(name = "position_url")
    private String positionUrl; // 포지션 이미지 링크

    @JsonIgnore
    @OneToMany(mappedBy = "userPosition1")
    private List<User> userList1 = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "userPosition2")
    private List<User> userList2 = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "position", cascade = CascadeType.REMOVE)
    private List<TeamPositionRelation> teamPositionRelations;
}
