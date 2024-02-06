package com.PuzzleU.Server.service.experience;

import com.PuzzleU.Server.common.ApiResponseDto;
import com.PuzzleU.Server.common.ResponseUtils;
import com.PuzzleU.Server.common.SuccessResponse;
import com.PuzzleU.Server.dto.experience.ExperienceDto;
import com.PuzzleU.Server.dto.user.UserRegisterOptionalDto;
import com.PuzzleU.Server.entity.enumSet.ErrorType;
import com.PuzzleU.Server.entity.experience.Experience;
import com.PuzzleU.Server.entity.user.User;
import com.PuzzleU.Server.exception.RestApiException;
import com.PuzzleU.Server.repository.ExperienceRepository;
import com.PuzzleU.Server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;


    public ApiResponseDto<SuccessResponse> createExperience(
            UserDetails loginUser,
            ExperienceDto experienceDto
    )
    {
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));
        Experience experience = new Experience();
        experience.setUser(user);
        experience.setExperienceName(experienceDto.getExperienceName());
        experience.setExperienceStartYear(experienceDto.getExperienceStartYear());
        experience.setExperienceStartMonth(experienceDto.getExperienceStartMonth());
        experience.setExperienceEndYear(experienceDto.getExperienceEndYear());
        experience.setExperienceEndMonth(experienceDto.getExperienceEndMonth());
        experience.setExperienceType(experienceDto.getExperienceType());
        experience.setExperienceStatus(experienceDto.getExperienceStatus());
        experience.setExperienceRole(experienceDto.getExperienceRole());
        experienceRepository.save(experience);
        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "경험 저장완료"), null);

    }
    public ApiResponseDto<SuccessResponse> updateExperience(
            UserDetails loginUser,
            Long experienceId,
            ExperienceDto experienceDto
    )
    {
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));
        Optional<Experience> optionalExperience = experienceRepository.findByExperienceIdAndUser(experienceId,user);
        Experience experience = optionalExperience.orElseThrow(() -> {
            System.out.println("User not found");
            return new RestApiException(ErrorType.NOT_FOUND_EXPERIENCE);
        });

        if (experienceDto.getExperienceName() != null) {
            experience.setExperienceName(experienceDto.getExperienceName());
        }

        if (experienceDto.getExperienceStartYear() != null) {
            experience.setExperienceStartYear(experienceDto.getExperienceStartYear());
        }

        if (experienceDto.getExperienceStartMonth() != null) {
            experience.setExperienceStartMonth(experienceDto.getExperienceStartMonth());
        }

        if (experienceDto.getExperienceEndYear() != null) {
            experience.setExperienceEndYear(experienceDto.getExperienceEndYear());
        }

        if (experienceDto.getExperienceEndMonth() != null) {
            experience.setExperienceEndMonth(experienceDto.getExperienceEndMonth());
        }

        if (experienceDto.getExperienceType() != null) {
            experience.setExperienceType(experienceDto.getExperienceType());
        }

        if (experienceDto.getExperienceStatus() != null) {
            experience.setExperienceStatus(experienceDto.getExperienceStatus());
        }

        if (experienceDto.getExperienceRole() != null) {
            experience.setExperienceRole(experienceDto.getExperienceRole());
        }

        experienceRepository.save(experience);
        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "경험 수정완료"), null);

    }
    public ApiResponseDto<SuccessResponse> deleteExperience(
            UserDetails loginUser,Long experienceId
    )
    {
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));
        Optional<Experience> optionalExperience = experienceRepository.findByExperienceIdAndUser(experienceId,user);
        Experience experience = optionalExperience.orElseThrow(() -> {
            System.out.println("User not found");
            return new RestApiException(ErrorType.NOT_FOUND_EXPERIENCE);
        });
        experienceRepository.delete(experience);
        return ResponseUtils.ok(SuccessResponse.of(HttpStatus.OK, "경험 삭제완료"), null);
    }
    public ApiResponseDto<List<ExperienceDto>> getExperienceList(UserDetails loginUser) {
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));

        List<ExperienceDto> experienceList = new ArrayList<>();
        List<Experience> userExperiences = experienceRepository.findByUser(user);
        System.out.println(userExperiences);
        for (Experience experience : userExperiences) {
            ExperienceDto experienceDto = new ExperienceDto();
            experienceDto.setExperienceId(experience.getExperienceId());
            experienceDto.setExperienceName(experience.getExperienceName());
            experienceDto.setExperienceStartYear(experience.getExperienceStartYear());
            experienceDto.setExperienceStartMonth(experience.getExperienceStartMonth());
            experienceDto.setExperienceEndYear(experience.getExperienceEndYear());
            experienceDto.setExperienceEndMonth(experience.getExperienceEndMonth());
            experienceDto.setExperienceType(experience.getExperienceType());
            experienceDto.setExperienceStatus(experience.getExperienceStatus());
            experienceDto.setExperienceRole(experience.getExperienceRole());
            experienceList.add(experienceDto);
        }
        return ResponseUtils.ok(experienceList, null);

    }
    public ApiResponseDto<ExperienceDto> getExperience(UserDetails loginUser, Long experienceId) {
        User user = userRepository.findByUsername(loginUser.getUsername())
                .orElseThrow(() -> new RestApiException(ErrorType.NOT_FOUND_USER));
        Optional<Experience> optionalExperience = experienceRepository.findByExperienceIdAndUser(experienceId,user);
        Experience experience = optionalExperience.orElseThrow(() -> {
            System.out.println("User not found");
            return new RestApiException(ErrorType.NOT_FOUND_EXPERIENCE);
        });
        ExperienceDto experienceDto = new ExperienceDto();
        experienceDto.setExperienceId(experience.getExperienceId());
        experienceDto.setExperienceName(experience.getExperienceName());
        experienceDto.setExperienceStartYear(experience.getExperienceStartYear());
        experienceDto.setExperienceStartMonth(experience.getExperienceStartMonth());
        experienceDto.setExperienceEndYear(experience.getExperienceEndYear());
        experienceDto.setExperienceEndMonth(experience.getExperienceEndMonth());
        experienceDto.setExperienceType(experience.getExperienceType());
        experienceDto.setExperienceStatus(experience.getExperienceStatus());
        experienceDto.setExperienceRole(experience.getExperienceRole());


        return ResponseUtils.ok(experienceDto, null);

    }
}