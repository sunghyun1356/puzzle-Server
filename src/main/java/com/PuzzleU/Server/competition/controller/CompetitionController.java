package com.PuzzleU.Server.competition.controller;

import com.PuzzleU.Server.common.api.ApiResponseDto;
import com.PuzzleU.Server.competition.dto.CompetitionHomeTotalDto;
import com.PuzzleU.Server.competition.dto.CompetitionSpecificDto;
import com.PuzzleU.Server.common.enumSet.CompetitionType;
import com.PuzzleU.Server.competition.service.CompetitionService;
import com.PuzzleU.Server.team.dto.TeamListDto;
import com.PuzzleU.Server.team.dto.TeamSpecificDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/competition")
public class CompetitionController {

    private final CompetitionService competitionService;

    @GetMapping("/homepage")
    public ApiResponseDto<CompetitionHomeTotalDto> homepage(
            @RequestParam(value = "competitionType", required = false) CompetitionType competitionType,
            @RequestParam(value = "search", defaultValue = "None", required = false) String search,
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "6", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "CompetitionId", required = false) String sortBy
    ) {
        return competitionService.getHomepage(pageNo, pageSize, sortBy, search, competitionType);
    }
    @GetMapping("/homepage/{competition_id}")
    public ApiResponseDto<CompetitionSpecificDto> specific(@Valid
            @PathVariable Long competition_id)
    {
        return competitionService.getSpecific(competition_id);
    }
    @GetMapping("/homepage/{competition_id}/team")
    public ApiResponseDto<TeamListDto> teamList(@Valid
    @PathVariable Long competition_id,
    @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
    @RequestParam(value = "pageSize", defaultValue = "6", required = false) int pageSize,
    @RequestParam(value = "sortBy", defaultValue = "teamId", required = false) String sortBy)
    {
        return competitionService.getTeamList(competition_id, pageNo, pageSize, sortBy);
    }
    @GetMapping("/homepage/{competition_id}/team/{team_id}")
    public ApiResponseDto<TeamSpecificDto> teamSpecific(@Valid @PathVariable Long competition_id, @PathVariable Long team_id)
    {
        return competitionService.getTeamSpecific(competition_id, team_id);
    }
    @GetMapping("/homepage/team")
    public ApiResponseDto<TeamListDto> teamSearch(@Valid
                                                  @RequestParam(value = "search", defaultValue = "None", required = false) String search,
                                                  @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
                                                @RequestParam(value = "pageSize", defaultValue = "6", required = false) int pageSize,
                                                @RequestParam(value = "sortBy", defaultValue = "teamId", required = false) String sortBy)
    {
        return competitionService.getTeamSearchList(pageNo, pageSize, sortBy,search);
    }


}