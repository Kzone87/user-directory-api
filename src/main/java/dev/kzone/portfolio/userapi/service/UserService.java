package dev.kzone.portfolio.userapi.service;

import dev.kzone.portfolio.userapi.domain.User;
import dev.kzone.portfolio.userapi.dto.UserCreateRequest;
import dev.kzone.portfolio.userapi.dto.UserResponse;
import dev.kzone.portfolio.userapi.dto.UserUpdateRequest;
import dev.kzone.portfolio.userapi.exception.UserNotFoundException;
import dev.kzone.portfolio.userapi.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserResponse get(long id) {
        return UserResponse.from(requireUser(id));
    }

    public List<UserResponse> search(String keyword, String emailDomain) {
        String normalizedKeyword = normalize(keyword);
        String normalizedDomain = normalize(emailDomain);
        return userMapper.search(normalizedKeyword, normalizedDomain).stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        User user = new User(null, request.name().trim(), request.email().trim().toLowerCase(), null);
        userMapper.insert(user);
        return UserResponse.from(requireUser(user.getId()));
    }

    @Transactional
    public UserResponse update(long id, UserUpdateRequest request) {
        User user = requireUser(id);
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        userMapper.update(user);
        return UserResponse.from(requireUser(id));
    }

    @Transactional
    public void delete(long id) {
        requireUser(id);
        userMapper.deleteById(id);
    }

    private User requireUser(long id) {
        return userMapper.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
