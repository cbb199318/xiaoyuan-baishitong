package com.campus.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.platform.common.BusinessException;
import com.campus.platform.dto.LoginRequest;
import com.campus.platform.dto.RegisterRequest;
import com.campus.platform.entity.User;
import com.campus.platform.entity.UserProfile;
import com.campus.platform.enums.RoleType;
import com.campus.platform.enums.UserStatus;
import com.campus.platform.mapper.UserMapper;
import com.campus.platform.mapper.UserProfileMapper;
import com.campus.platform.security.JwtTokenService;
import com.campus.platform.security.JwtUserPrincipal;
import com.campus.platform.vo.AuthLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    public AuthLoginVO register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入密码不一致");
        }
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (existing != null) {
            throw new BusinessException("手机号已注册");
        }
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname("用户" + request.getPhone().substring(7));
        user.setRole(RoleType.USER.name());
        user.setStatus(UserStatus.ACTIVE.name());
        user.setReportRestricted(0);
        userMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setContactPhone(user.getPhone());
        userProfileMapper.insert(profile);

        JwtUserPrincipal principal = new JwtUserPrincipal(user.getId(), user.getPhone(), user.getPassword(), user.getRole(), user.getStatus());
        return new AuthLoginVO(jwtTokenService.generateToken(principal), userService.getProfile(user.getId()));
    }

    public AuthLoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, request.getPhone()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "手机号或密码错误");
        }
        if (!UserStatus.canLogin(user.getStatus())) {
            throw new BusinessException(403, "当前账号已被限制登录，请联系管理员处理");
        }
        JwtUserPrincipal principal = new JwtUserPrincipal(user.getId(), user.getPhone(), user.getPassword(), user.getRole(), user.getStatus());
        return new AuthLoginVO(jwtTokenService.generateToken(principal), userService.getProfile(user.getId()));
    }
}
