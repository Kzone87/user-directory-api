package dev.kzone.portfolio.userapi.controller;

import dev.kzone.portfolio.userapi.dto.UserCreateRequest;
import dev.kzone.portfolio.userapi.dto.UserPageResponse;
import dev.kzone.portfolio.userapi.dto.UserResponse;
import dev.kzone.portfolio.userapi.dto.UserUpdateRequest;
import dev.kzone.portfolio.userapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String emailDomain
    ) {
        return userService.search(keyword, emailDomain);
    }

    @GetMapping("/page")
    public UserPageResponse searchPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String emailDomain,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return userService.searchPage(keyword, emailDomain, page, size, sort, direction);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable long id) {
        return userService.get(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable long id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
