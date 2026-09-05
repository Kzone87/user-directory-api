package dev.kzone.portfolio.userapi.service;

import dev.kzone.portfolio.userapi.domain.User;
import dev.kzone.portfolio.userapi.dto.UserCreateRequest;
import dev.kzone.portfolio.userapi.dto.UserPageResponse;
import dev.kzone.portfolio.userapi.dto.UserResponse;
import dev.kzone.portfolio.userapi.dto.UserUpdateRequest;
import dev.kzone.portfolio.userapi.exception.UserNotFoundException;
import dev.kzone.portfolio.userapi.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class UserService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORTS = Set.of("id", "name", "email", "createdAt");

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

    public UserPageResponse searchPage(
            String keyword,
            String emailDomain,
            int page,
            int size,
            String sort,
            String direction
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be 0 or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        }

        String normalizedSort = sort == null ? "id" : sort.trim();
        if (!ALLOWED_SORTS.contains(normalizedSort)) {
            throw new IllegalArgumentException("sort must be one of: id, name, email, createdAt");
        }

        String normalizedDirection = direction == null
                ? "asc"
                : direction.trim().toLowerCase(Locale.ROOT);
        if (!normalizedDirection.equals("asc") && !normalizedDirection.equals("desc")) {
            throw new IllegalArgumentException("direction must be asc or desc");
        }

        String normalizedKeyword = normalize(keyword);
        String normalizedDomain = normalize(emailDomain);
        long offset = (long) page * size;

        List<UserResponse> content = userMapper.searchPage(
                        normalizedKeyword,
                        normalizedDomain,
                        normalizedSort,
                        normalizedDirection,
                        size,
                        offset
                ).stream()
                .map(UserResponse::from)
                .toList();
        long totalElements = userMapper.countSearch(normalizedKeyword, normalizedDomain);
        int totalPages = totalElements == 0 ? 0 : (int) ((totalElements + size - 1) / size);

        return new UserPageResponse(
                content,
                page,
                size,
                totalElements,
                totalPages,
                normalizedSort,
                normalizedDirection
        );
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
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
