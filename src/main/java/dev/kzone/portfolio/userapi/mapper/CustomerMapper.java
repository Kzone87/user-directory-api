package dev.kzone.portfolio.userapi.mapper;

import dev.kzone.portfolio.userapi.domain.Customer;
import dev.kzone.portfolio.userapi.domain.CustomerStatus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CustomerMapper {
    @Select("""
            SELECT id, company_name, contact_name, email, phone, status, memo, created_at, updated_at
            FROM customers
            WHERE id = #{id}
            """)
    Optional<Customer> findById(@Param("id") long id);

    @Select("""
            <script>
            SELECT id, company_name, contact_name, email, phone, status, memo, created_at, updated_at
            FROM customers
            <where>
              <if test='status != null'>status = #{status}</if>
              <if test='keyword != null and keyword != ""'>
                AND (
                  LOWER(company_name) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                  OR LOWER(COALESCE(contact_name, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                  OR LOWER(COALESCE(email, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                  OR REPLACE(COALESCE(phone, ''), '-', '') LIKE CONCAT('%', REPLACE(#{keyword}, '-', ''), '%')
                )
              </if>
            </where>
            ORDER BY updated_at DESC, id DESC
            </script>
            """)
    List<Customer> findAll(
            @Param("keyword") String keyword,
            @Param("status") CustomerStatus status
    );

    @Insert("""
            INSERT INTO customers (company_name, contact_name, email, phone, status, memo)
            VALUES (#{companyName}, #{contactName}, #{email}, #{phone}, #{status}, #{memo})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Customer customer);

    @Update("""
            UPDATE customers
            SET company_name = #{companyName},
                contact_name = #{contactName},
                email = #{email},
                phone = #{phone},
                status = #{status},
                memo = #{memo},
                updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int update(Customer customer);
}
