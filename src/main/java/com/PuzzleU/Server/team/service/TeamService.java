package com.PuzzleU.Server.team.service;

import com.PuzzleU.Server.common.api.ApiResponseDto;
import com.PuzzleU.Server.common.api.ErrorResponse;
import com.PuzzleU.Server.common.api.ResponseUtils;
import com.PuzzleU.Server.common.api.SuccessResponse;
import com.PuzzleU.Server.competition.repository.CompetitionRepository;
import com.PuzzleU.Server.competition.dto.CompetitionSearchDto;
import com.PuzzleU.Server.competition.dto.CompetitionSearchTotalDto;
import com.PuzzleU.Server.friendship.dto.FriendShipSearchResponseDto;
import com.PuzzleU.Server.location.entity.Location;
import com.PuzzleU.Server.location.repository.LocationRepository;
import com.PuzzleU.Server.position.entity.Position;
import com.PuzzleU.Server.position.repository.PositionRepository;
import com.PuzzleU.Server.relations.entity.TeamLocationRelation;
import com.PuzzleU.Server.relations.entity.TeamUserRelation;
import com.PuzzleU.Server.relations.repository.TeamLocationRelationRepository;
import com.PuzzleU.Server.relations.repository.TeamUserRepository;
import com.PuzzleU.Server.team.dto.TeamCreateDto;
import com.PuzzleU.Server.friendship.repository.FriendshipRepository;
import com.PuzzleU.Server.team.entity.Team;
import com.PuzzleU.Server.team.repository.TeamRepository;
import com.PuzzleU.Server.user.dto.UserSimpleDto;
import com.PuzzleU.Server.competition.entity.Competition;
import com.PuzzleU.Server.common.enumSet.ErrorType;
import com.PuzzleU.Server.friendship.entity.FriendShip;
import com.PuzzleU.Server.user.entity.User;
import com.PuzzleU.Server.common.exception.RestApiException;
import com.PuzzleU.Server.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;
    private final FriendshipRepository friendshipRepository;
    private final LocationRepository locationRepository;
    private final TeamRepository teamRepository;
    private final TeamUserRepository teamUserRepository;
    private final TeamLocationRelationRepository teamLocationRelationRepository;
    private final PositionRepository positionRepository;

    // 팀 구인글을 등록하는 API
    @Transactional
    public ApiResponseDto<SuccessResponse> teamcreate(
            TeamCreateDto teamCreateDto,
            Long competitionId,
            List<Long> teamMember,
            UserDetails loginUser
            ,List<Long> locations,
            List<Long> positions
    )
    {

        User loginuser = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));
        Optional<Competition> competitionOptional = competitionRepository.findById(competitionId);
        Competition competition = competitionOptional.orElseThrow(
                ()-> new RestApiException(ErrorType.NOT_FOUND_COMPETITION)
        );
        List<Position>positionList = new ArrayList<>();
        for(Long positionId : positions)
        {
            Optional<Position> positionOptional = positionRepository.findById(positionId);
            Position position = positionOptional.orElseThrow(()-> new RestApiException(ErrorType.NOT_FOUND_POSITION));
            positionList.add(position);
        }
        Team team = new Team();
        team.setTeamTitle(teamCreateDto.getTeamTitle());
        team.setTeamMemberNeed(teamCreateDto.getTeamMemberNeed());
        team.setTeamUntact(teamCreateDto.getTeamUntact());
        team.setTeamUrl(teamCreateDto.getTeamUrl());
        team.setTeamIntroduce(teamCreateDto.getTeamIntroduce());
        team.setTeamContent(teamCreateDto.getTeamContent());
        team.setTeamStatus(teamCreateDto.getTeamStatus());
        team.setCompetition(competition);
        team.setTeamMemberNow(teamMember.size()+1);
        team.setPositionList(positionList);
        teamRepository.save(team);
        TeamUserRelation teamUserRelationWriter=  TeamUserRelation.builder()
                .team(team)
                .user(loginuser)
                .isWriter(true)
                .build();
        teamUserRepository.saveAndFlush(teamUserRelationWriter);
        for(Long userId : teamMember)
        {
            Optional<User> userOptional = userRepository.findById(userId);
                    User user = userOptional.orElseThrow(
                    ()-> new RestApiException(ErrorType.NOT_FOUND_USER)
            );
            TeamUserRelation teamUserRelation=  TeamUserRelation.builder()
                    .team(team)
                    .user(user)
                    .isWriter(false)
                    .build();
            teamUserRepository.saveAndFlush(teamUserRelation);
        }
        for(Long locationId : locations)
        {
            Optional<Location> locationOptional = locationRepository.findById(locationId);
            Location location = locationOptional.orElseThrow(
                    () -> new RestApiException(ErrorType.NOT_FOUND_LOCATION)
            );
            TeamLocationRelation teamLocationRelation = TeamLocationRelation.builder()
                    .team(team)
                    .location(location)
                    .build();
            teamLocationRelationRepository.saveAndFlush(teamLocationRelation);
        }



        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK,"팀 구인글 생성완료"), null);
    }
    public ApiResponseDto<SuccessResponse> teamUpdate(TeamCreateDto teamCreateDto, Long competitionId,
                                                      List<Long> teamMember, UserDetails loginUser,
                                                      List<Long> locations, List<Long> positions, Long teamId) {
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_COMPETITION));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_TEAM));

        TeamUserRelation teamUserRelation = teamUserRepository.findByUserAndTeam(user, team)
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_RELATION));

        if (teamUserRelation.getIsWriter()) {
            List<Position> positionList = new ArrayList<>();
            for (Long positionId : positions) {
                Position position = positionRepository.findById(positionId)
                        .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_POSITION));
                positionList.add(position);
            }

            team.setTeamTitle(teamCreateDto.getTeamTitle());
            team.setTeamMemberNeed(teamCreateDto.getTeamMemberNeed());
            team.setTeamUntact(teamCreateDto.getTeamUntact());
            team.setTeamUrl(teamCreateDto.getTeamUrl());
            team.setTeamIntroduce(teamCreateDto.getTeamIntroduce());
            team.setTeamContent(teamCreateDto.getTeamContent());
            team.setTeamStatus(teamCreateDto.getTeamStatus());
            team.setCompetition(competition);
            team.setTeamMemberNow(teamMember.size()+1);
            team.setPositionList(positionList);
            teamRepository.save(team);

            List<TeamUserRelation> existingRelations = teamUserRepository.findByTeam(team);

            for (Long userId : teamMember) {
                User memberUser = userRepository.findById(userId)
                        .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));

                TeamUserRelation newRelation = TeamUserRelation.builder()
                        .team(team)
                        .user(memberUser)
                        .isWriter(false)
                        .build();

                boolean relationExists = existingRelations.stream()
                        .anyMatch(relation -> relation.getUser().getId().equals(userId));

                if (!relationExists) {
                    teamUserRepository.saveAndFlush(newRelation);
                }
            }

            existingRelations.forEach(existingRelation -> {
                Long existingUserId = existingRelation.getUser().getId();
                if (!teamMember.contains(existingUserId)) {
                    teamUserRepository.delete(existingRelation);
                }
            });

            for (Long locationId : locations) {
                Location location = locationRepository.findById(locationId)
                        .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_LOCATION));
                TeamLocationRelation teamLocationRelation = TeamLocationRelation.builder()
                        .team(team)
                        .location(location)
                        .build();
                teamLocationRelationRepository.saveAndFlush(teamLocationRelation);
            }
            TeamUserRelation teamUserRelation1 = new TeamUserRelation();
            teamUserRelation1.setUser(user);
            teamUserRelation1.setTeam(team);
            teamUserRelation1.setIsWriter(true);
            teamUserRepository.save(teamUserRelation1);

            return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "팀 구인글 수정완료"), null);
        } else {
            return ResponseUtils.error(ErrorResponse.of(HttpStatus.NOT_ACCEPTABLE, "유저의 권한이 없습니다."));
        }
    }
    public ApiResponseDto<SuccessResponse> teamdelete(
            Long teamId,
            UserDetails loginUser
    )
    {
        System.out.println(loginUser);
        User user = userRepository.findByUsername(loginUser.getUsername()).orElseThrow(
                ()-> new RestApiException(ErrorType.NOT_FOUND_USER)
        );
        Optional<Team>teamOptional = teamRepository.findById(teamId);
        Team team = teamOptional.orElseThrow(()->
                new RestApiException(ErrorType.NOT_FOUND_TEAM));

        Optional<TeamUserRelation> teamUserRelationOptional = teamUserRepository.findByUserAndTeam(user, team);
        if(teamUserRelationOptional.isPresent())
        {
            TeamUserRelation teamUserRelation = teamUserRelationOptional.orElseThrow(()-> new RestApiException(ErrorType.NOT_FOUND_RELATION));
            if (teamUserRelation.getIsWriter())
            {
                teamRepository.delete(team);

            }
            else{
                return ResponseUtils.error(ErrorResponse.of(HttpStatus.NOT_ACCEPTABLE,"유저의 권한이 없습니다."));
            }
        }
        else{
            return ResponseUtils.error(ErrorResponse.of(HttpStatus.NOT_FOUND,"유저가 속한 팀이 아닙니다."));
        }

        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK,"팀 구인글 삭제완료"), null);

    }

    // 공모전 팀 등록시 공모전검색을 해서 값을 가져오는 API
    @Transactional
    public ApiResponseDto<CompetitionSearchTotalDto> competitionTeamSearch(int pageNo, int pageSize, String sortBy, String keyword) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        Page<Competition> competitionPage;
        competitionPage = new PageImpl<>(competitionRepository.findByCompetitionName(keyword, pageable));
        List<CompetitionSearchDto> competitionSearchDtos = competitionPage.getContent().stream()
                .map(competition -> {
                    CompetitionSearchDto competitionSearchDto = new CompetitionSearchDto();
                    competitionSearchDto.setCompetitionId(competition.getCompetitionId());
                    competitionSearchDto.setCompetitionName(competition.getCompetitionName());
                    return competitionSearchDto;
                })
                .toList();
        CompetitionSearchTotalDto competitionSearchTotalDto = new CompetitionSearchTotalDto();
        competitionSearchTotalDto.setCompetitionSearchDtoList(competitionSearchDtos);
        competitionSearchTotalDto.setTotalPages(competitionPage.getTotalPages());
        competitionSearchTotalDto.setTotalElements(competitionPage.getTotalElements());
        competitionSearchTotalDto.setLast(competitionPage.isLast());
        competitionSearchTotalDto.setPageSize(pageSize);
        competitionSearchTotalDto.setPageNo(pageNo);

        return ResponseUtils.ok(competitionSearchTotalDto, null);
    }


    // 유저 정보 리스트를 가져오고 여기에는 유저의 이름, id, 한줄소개가 있어야한다.
    // 공모전글을 등록할때 내 친구들만 데이터를 가져오는 API
    @Transactional
    public ApiResponseDto<FriendShipSearchResponseDto> friendRegister(String keyword, UserDetails loginUser,int pageNo, int pageSize, String sortBy)
    {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));

        Page<FriendShip> friendShips = friendshipRepository.findActiveFriendshipsForUserAndKeyword(user, keyword, pageable);
        List<FriendShip> friendShipList = friendShips.getContent();
        FriendShipSearchResponseDto friendShipSearchResponseDto = new FriendShipSearchResponseDto();
        // 각각의 friendshipe에 대해서 user가 아닌 user에 대한 정보를 저장하고 리스트로 만들어준다

        List<UserSimpleDto> userSimpleDtoList = new ArrayList<>();
        for(FriendShip friendShip:friendShipList)
        {
            User user1 = friendShip.getUser1();
            User user2 = friendShip.getUser2();
            if (user1 == user)
            {
                UserSimpleDto userSimpleDto = new UserSimpleDto();
                userSimpleDto.setUserProfile(user2.getUserProfile());
                userSimpleDto.setUserKoreaName(user2.getUserKoreaName());
                userSimpleDto.setUserRepresentativeProfileSentence(user2.getUserRepresentativeProfileSentence());
                userSimpleDto.setUserId(user2.getId());
                userSimpleDtoList.add(userSimpleDto);

            }
            else {
                UserSimpleDto userSimpleDto = new UserSimpleDto();
                userSimpleDto.setUserProfile(user1.getUserProfile());
                userSimpleDto.setUserKoreaName(user1.getUserKoreaName());
                userSimpleDto.setUserRepresentativeProfileSentence(user1.getUserRepresentativeProfileSentence());
                userSimpleDto.setUserId(user1.getId());
                userSimpleDtoList.add(userSimpleDto);

            }
        }
        friendShipSearchResponseDto.setUserSimpleDtoList(userSimpleDtoList);

     return ResponseUtils.ok(friendShipSearchResponseDto, null)  ;
    }
}