package dev.kzone.portfolio.userapi.mapper;

import dev.kzone.portfolio.userapi.domain.WorkOrder;
import dev.kzone.portfolio.userapi.domain.WorkOrderStatus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface WorkOrderMapper {
    @Select("""
            SELECT wo.id,
                   wo.title,
                   wo.customer_id,
                   c.company_name AS customer_name,
                   wo.assignee,
                   wo.status,
                   wo.priority,
                   wo.due_date,
                   wo.created_at,
                   wo.updated_at
            FROM work_orders wo
            JOIN customers c ON c.id = wo.customer_id
            WHERE wo.id = #{id}
            """)
    Optional<WorkOrder> findById(@Param("id") long id);

    @Select("""
            <script>
            SELECT wo.id,
                   wo.title,
                   wo.customer_id,
                   c.company_name AS customer_name,
                   wo.assignee,
                   wo.status,
                   wo.priority,
                   wo.due_date,
                   wo.created_at,
                   wo.updated_at
            FROM work_orders wo
            JOIN customers c ON c.id = wo.customer_id
            <where>
              <if test='status != null'>wo.status = #{status}</if>
            </where>
            ORDER BY
              CASE wo.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'NORMAL' THEN 3 ELSE 4 END,
              CASE WHEN wo.due_date IS NULL THEN 1 ELSE 0 END,
              wo.due_date ASC,
              wo.updated_at DESC,
              wo.id DESC
            </script>
            """)
    List<WorkOrder> findAll(@Param("status") WorkOrderStatus status);

    @Select("""
            <script>
            SELECT wo.id,
                   wo.title,
                   wo.customer_id,
                   c.company_name AS customer_name,
                   wo.assignee,
                   wo.status,
                   wo.priority,
                   wo.due_date,
                   wo.created_at,
                   wo.updated_at
            FROM work_orders wo
            JOIN customers c ON c.id = wo.customer_id
            <where>
              <if test='from != null'>CAST(wo.created_at AS DATE) &gt;= #{from}</if>
              <if test='to != null'>AND CAST(wo.created_at AS DATE) &lt;= #{to}</if>
            </where>
            ORDER BY wo.created_at ASC, wo.id ASC
            </script>
            """)
    List<WorkOrder> findForReport(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Insert("""
            INSERT INTO work_orders (title, customer_id, assignee, status, priority, due_date)
            VALUES (#{title}, #{customerId}, #{assignee}, #{status}, #{priority}, #{dueDate})
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
