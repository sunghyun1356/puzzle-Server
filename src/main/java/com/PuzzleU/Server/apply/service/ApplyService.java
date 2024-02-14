package com.PuzzleU.Server.apply.service;

import com.PuzzleU.Server.apply.dto.ApplyDetailDto;
import com.PuzzleU.Server.apply.dto.ApplyPostDto;
import com.PuzzleU.Server.apply.entity.Apply;
import com.PuzzleU.Server.apply.repository.ApplyRepository;
import com.PuzzleU.Server.common.api.ApiResponseDto;
import com.PuzzleU.Server.common.api.ErrorResponse;
import com.PuzzleU.Server.common.api.ResponseUtils;
import com.PuzzleU.Server.common.api.SuccessResponse;
import com.PuzzleU.Server.common.enumSet.ApplyStatus;
import com.PuzzleU.Server.common.enumSet.ErrorType;
import com.PuzzleU.Server.common.exception.RestApiException;
import com.PuzzleU.Server.competition.entity.Competition;
import com.PuzzleU.Server.position.dto.PositionDto;
import com.PuzzleU.Server.position.entity.Position;
import com.PuzzleU.Server.position.repository.PositionRepository;
import com.PuzzleU.Server.relations.entity.PositionApplyRelation;
import com.PuzzleU.Server.relations.entity.TeamLocationRelation;
import com.PuzzleU.Server.relations.entity.TeamUserRelation;
import com.PuzzleU.Server.relations.repository.PositionApplyRelationRepository;
import com.PuzzleU.Server.relations.repository.TeamLocationRelationRepository;
import com.PuzzleU.Server.relations.repository.TeamUserRepository;
import com.PuzzleU.Server.team.dto.ApplyTeamDto;
import com.PuzzleU.Server.team.dto.ApplyTeamListDto;
import com.PuzzleU.Server.team.dto.TeamAbstractDto;
import com.PuzzleU.Server.team.entity.Team;
import com.PuzzleU.Server.team.repository.TeamRepository;
import com.PuzzleU.Server.user.entity.User;
import com.PuzzleU.Server.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final PositionApplyRelationRepository positionApplyRelationRepository;
    private final PositionRepository positionRepository;
    private final ApplyRepository applyRepository;
    private final TeamUserRepository teamUserRepository;
    private final TeamLocationRelationRepository teamLocationRelationRepository;
    // 팀에 대한 지원서 작성
    @Transactional
    public ApiResponseDto<SuccessResponse> postApply(UserDetails loginUser, Long teamId, ApplyPostDto applyPostDto) {
        Team team = teamRepository.findByTeamId(teamId)
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_TEAM));
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));

        Apply apply = Apply.builder()
                .applyTitle(applyPostDto.getApplyTitle())
                .applyContent(applyPostDto.getApplyContent())
                .applyStatus(ApplyStatus.WAITING)
                .team(team)
                .user(user)
                .build();

        applyRepository.save(apply);

        for (Long positionId : applyPostDto.getApplyPositionIdList()) {
            Position position = positionRepository.findByPositionId(positionId)
                    .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_POSITION));

            PositionApplyRelation positionApplyRelation = PositionApplyRelation.builder()
                    .apply(apply)
                    .position(position).build();

            positionApplyRelationRepository.save(positionApplyRelation);
        }

        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "지원서 저장 완료"), null);
    }

    // 지원서 상세
    @Transactional
    public ApiResponseDto<ApplyDetailDto> applyDetail(Long applyId) {

        Apply apply = applyRepository.findByApplyId(applyId)
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_APPLY));

        Team team = apply.getTeam();

        Competition competition  = team.getCompetition();

        List<PositionApplyRelation> positionApplyRelationList = positionApplyRelationRepository.findByApply(apply);

        System.out.println(positionApplyRelationList);

        List<PositionDto> positionDtoList = new ArrayList<>();
        for (PositionApplyRelation rel : positionApplyRelationList) {
            PositionDto positionDto = PositionDto.builder()
                    .PositionId(rel.getPosition().getPositionId())
                    .PositionName(rel.getPosition().getPositionName()).build();
            positionDtoList.add(positionDto);
        }


        ApplyDetailDto applyDetailDto = new ApplyDetailDto();
        applyDetailDto.setCompetitionPoster(competition.getCompetitionPoster());
        applyDetailDto.setCompetitionTitle(competition.getCompetitionName());
        applyDetailDto.setTeamTitle(team.getTeamTitle());
        applyDetailDto.setPositionList(positionDtoList);
        applyDetailDto.setApplyTitle(apply.getApplyTitle());
        applyDetailDto.setApplyContent(apply.getApplyContent());

        return ResponseUtils.ok(applyDetailDto, null);
    }
    // type에 따라 내가 지원한 팀들을 볼 수 있는 기능
    @Transactional
    public ApiResponseDto<ApplyTeamListDto> getApplyType(UserDetails loginUser, int pageNo, int pageSize, String sortBy, String type) {
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
        Page<Team> teamPage;
        if ("total".equals(type)) {
            teamPage = new PageImpl<>(applyRepository.findByUser(user, pageable));
        } else if ("wait".equals(type)) {
            teamPage = new PageImpl<>(applyRepository.findByUserAndApplyStatusIsWaiting(user, pageable));
        } else if ("end".equals(type)) {
            teamPage = new PageImpl<>(applyRepository.findByUserAndApplyStatusIsFinished(user, pageable));
        } else {
            throw new RestApiException(ErrorType.NAME_NOT_PROVIDED);
        }

        ApplyTeamListDto teamListDto = new ApplyTeamListDto();
        List<TeamAbstractDto> teamAbstractDtos = new ArrayList<>();
        teamAbstractDtos = teamPage.getContent().stream()
                .map(team ->
                {
                    TeamAbstractDto teamAbstractDto = new TeamAbstractDto();
                    List<TeamUserRelation> teamUserRelation = teamUserRepository.findByTeam(team);
                    for (TeamUserRelation teamuserRelation : teamUserRelation) {
                        if (teamuserRelation.getIsWriter())
                        {
                            teamAbstractDto.setTeamWriter(teamuserRelation.getUser().getUserKoreaName());
                            break;
                        }
                    }
                    List<TeamLocationRelation> teamLocationRelation = teamLocationRelationRepository.findByTeam(team);
                    List<String> locationList = new ArrayList<>();
                    for (TeamLocationRelation teamLocationRelation1 : teamLocationRelation) {
                        String location = teamLocationRelation1.getLocation().getLocationName();
                        locationList.add(location);
                    }
                    Optional<Apply> applyOptional = applyRepository.findByUserAndTeam(user, team);
                    Apply apply = applyOptional.orElseThrow(
                            ()-> new RestApiException(ErrorType.NOT_FOUND_APPLY)
                    );
                    teamAbstractDto.setApplyId(apply.getApplyId());
                    teamAbstractDto.setTeamId(team.getTeamId());
                    teamAbstractDto.setTeamNeed(team.getTeamMemberNeed());
                    teamAbstractDto.setTeamNowMember(team.getTeamMemberNow());
                    teamAbstractDto.setTeamTitle(team.getTeamTitle());
                    teamAbstractDto.setTeamPoster(team.getCompetition().getCompetitionPoster());
                    teamAbstractDto.setTeamLocations(locationList);
                    List<String> PositionList = new ArrayList<>();
                    for(Position position : team.getPositionList())
                    {
                        PositionList.add(position.getPositionName());
                    }
                    teamAbstractDto.setPositionList(PositionList);
                    return teamAbstractDto;
                })
                .collect(Collectors.toList());
        teamListDto.setTotalTeam(teamAbstractDtos.size());
        teamListDto.setTeamList(teamAbstractDtos);
        teamListDto.setLast(teamPage.isLast());
        teamListDto.setTotalPages(teamPage.getTotalPages());
        teamListDto.setTotalElements(teamPage.getTotalElements());
        teamListDto.setPageNo(pageNo);
        teamListDto.setPageSize(pageSize);


        return ResponseUtils.ok(teamListDto, null);
    }
    // 내가 지원한 지원서나 팀을 볼 수 있음
    @Transactional
    public ApiResponseDto<ApplyTeamDto> getApply(UserDetails loginUser) {
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));
        ApplyTeamDto applyTeamDto = new ApplyTeamDto();

        for (int i = 0; i <= 1; i++) {
            List<Team> team1;
            Team team2;
            if (i == 0) {
                team1 = applyRepository.findFirstByUserAndApplyStatusIsWaitingOne(user);
                if (team1.isEmpty()) {
                    team2 = new Team(); // 빈 팀 생성
                } else {
                    team2 = team1.get(0);
                }
            } else {
                team1 = applyRepository.findFirstByUserAndApplyStatusIsFinishedOne(user);
                if (team1.isEmpty()) {
                    team2 = new Team(); // 빈 팀 생성
                } else {
                    team2 = team1.get(0);
                }
            }
            TeamAbstractDto teamAbstractDto1 = new TeamAbstractDto();
            if (!team1.isEmpty()) {
                List<TeamUserRelation> teamUserRelation1 = teamUserRepository.findByTeam(team2);
                for (TeamUserRelation teamuserRelation : teamUserRelation1) {
                    if (teamuserRelation.getIsWriter()) {
                        teamAbstractDto1.setTeamWriter(teamuserRelation.getUser().getUserKoreaName());
                        break;
                    }
                }
                List<TeamLocationRelation> teamLocationRelation = teamLocationRelationRepository.findByTeam(team2);
                List<String> locationList1 = new ArrayList<>();
                for (TeamLocationRelation teamLocationRelation1 : teamLocationRelation) {
                    String location = teamLocationRelation1.getLocation().getLocationName();
                    locationList1.add(location);
                }
                Optional<Apply> applyOptional = applyRepository.findByUserAndTeam(user, team2);
                Apply apply = applyOptional.orElseThrow(
                        ()-> new RestApiException(ErrorType.NOT_FOUND_APPLY)
                );
                teamAbstractDto1.setApplyId(apply.getApplyId());
                teamAbstractDto1.setTeamId(team2.getTeamId());
                teamAbstractDto1.setTeamNeed(team2.getTeamMemberNeed());
                teamAbstractDto1.setTeamNowMember(team2.getTeamMemberNow());
                teamAbstractDto1.setTeamTitle(team2.getTeamTitle());
                teamAbstractDto1.setTeamPoster(team2.getCompetition().getCompetitionPoster());
                teamAbstractDto1.setTeamLocations(locationList1);
                List<String> PositionList1 = new ArrayList<>();
                for (Position position : team2.getPositionList()) {
                    PositionList1.add(position.getPositionName());
                }
                teamAbstractDto1.setPositionList(PositionList1);
            }
            if (i == 0) {
                applyTeamDto.setTeamListDto1(teamAbstractDto1);
            } else {
                applyTeamDto.setTeamListDto2(teamAbstractDto1);
            }
        }
        return ResponseUtils.ok(applyTeamDto, null);
    }
    // 나의 지원을 삭제함
    @Transactional
    public ApiResponseDto<SuccessResponse> deleteApply(UserDetails loginUser, Long apply_id)
    {
        Optional<Apply> applyOptional = applyRepository.findById(apply_id);
        Apply apply = applyOptional.orElseThrow(
                () -> new RestApiException(ErrorType.NOT_FOUND_APPLY)
        );
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));
        if(apply.getUser() == user)
        {
            applyRepository.delete(apply);
            return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "지원서 삭제완료"),null);
        }
        else
        {
            return ResponseUtils.error(ErrorResponse.of(HttpStatus.NOT_ACCEPTABLE,"권한이 없습니다"));
        }

    }


}