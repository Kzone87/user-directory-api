package dev.kzone.portfolio.userapi.mapper;

import dev.kzone.portfolio.userapi.domain.WorkOrder;
import dev.kzone.portfolio.userapi.domain.WorkOrderStatus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface WorkOrderMapper {
    @Select("""
            SELECT id, title, customer_name, assignee, status, created_at, updated_at
            FROM work_orders
            WHERE id = #{id}
            """)
    Optional<WorkOrder> findById(@Param("id") long id);

    @Select("""
            <script>
            SELECT id, title, customer_name, assignee, status, created_at, updated_at
            FROM work_orders
            <where>
              <if test='status != null'>status = #{status}</if>
            </where>
            ORDER BY updated_at DESC, id DESC
            </script>
            """)
    List<WorkOrder> findAll(@Param("status") WorkOrderStatus status);

    @Insert("""
            INSERT INTO work_orders (title, customer_name, assignee, status)
            VALUES (#{title}, #{customerName}, #{assignee}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WorkOrder workOrder);

    @Update("""
            UPDATE work_orders
            SET status = #{status}, updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id} AND status = #{expectedStatus}
            """)
    int updateStatus(
            @Param("id") long id,
            @Param("expectedStatus") WorkOrderStatus expectedStatus,
            @Param("status") WorkOrderStatus status
    );
}
