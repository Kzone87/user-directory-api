package dev.kzone.portfolio.userapi.mapper;

import dev.kzone.portfolio.userapi.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<User> findById(@Param("id") long id);

    List<User> search(
            @Param("keyword") String keyword,
            @Param("emailDomain") String emailDomain
    );

    List<User> searchPage(
            @Param("keyword") String keyword,
            @Param("emailDomain") String emailDomain,
            @Param("sort") String sort,
            @Param("direction") String direction,
            @Param("size") int size,
            @Param("offset") long offset
    );

    long countSearch(
            @Param("keyword") String keyword,
            @Param("emailDomain") String emailDomain
    );

    int insert(User user);

    int update(User user);

    int deleteById(@Param("id") long id);
}
