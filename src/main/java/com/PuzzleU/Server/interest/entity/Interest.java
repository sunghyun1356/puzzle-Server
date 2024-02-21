package com.PuzzleU.Server.interest.entity;

import com.PuzzleU.Server.common.enumSet.InterestTypes;
import com.PuzzleU.Server.relations.entity.CompetitionInterestRelation;
import com.PuzzleU.Server.relations.entity.UserInterestRelation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interest")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Interest {
    @Id
    //E
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interestId;

    @Column(name = "interest_name", length = 10)
    private String interestName;

    @Column(name = "interest_type")
    @Enumerated(value = EnumType.STRING)
    private InterestTypes interestType;

    @JsonIgnore
    @OneToMany(mappedBy = "interest", cascade = CascadeType.REMOVE)
    private List<UserInterestRelation> userInterestRelation = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "interest", cascade = CascadeType.REMOVE)
    private List<CompetitionInterestRelation> competitionInterestRelations = new ArrayList<>();
}
