package com.PuzzleU.Server.common.oauth;

import com.PuzzleU.Server.common.api.ApiResponseDto;
import com.PuzzleU.Server.common.jwt.TokenDto;
import com.PuzzleU.Server.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
// 테스트용

@RestController
@AllArgsConstructor
@RequestMapping("/api/oauth")
public class OAuthController {
    final private OAuthService oAuthService;
    // 테스트용
    final private UserRepository userRepository;

    /**
     * 카카오 callback
     * [GET] /oauth/kakao
     * 이 과정은 클라이언트에서 처리! 테스트용으로 만들어 두었음.
     */

    @PostMapping("/kakao")
    public ApiResponseDto<TokenDto> kakaoLogin(@RequestParam("kakaoAccessToken") String kakaoAccessToken) {

        return oAuthService.kakaoLogin(kakaoAccessToken);
    }

    @PostMapping("/kakao/refresh")
    public ApiResponseDto<TokenDto> kakaorefreshToken(
            @RequestHeader(value="accessToken") String accessToken,
            @RequestHeader(value="refreshToken") String refreshToken
    ) {
        return oAuthService.refreshKakaoToken(accessToken, refreshToken);
    }

    @PostMapping("/apple")
    public ApiResponseDto<TokenDto> appleLogin(@RequestParam("appleAccessToken")String appleAccessToken)
    {
        return oAuthService.appleLogin((appleAccessToken));
    }
    @PostMapping("/apple/refresh")
    public ApiResponseDto<TokenDto> applerefreshToken(
            @RequestHeader(value="accessToken") String accessToken,
            @RequestHeader(value="refreshToken") String refreshToken
    ) {
        return oAuthService.refreshAppleToken(accessToken, refreshToken);
    }

}
