package dev.kzone.portfolio.userapi.mapper;

import dev.kzone.portfolio.userapi.domain.WorkOrderActivity;
import dev.kzone.portfolio.userapi.domain.WorkOrderStatus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkOrderActivityMapper {
    @Select("""
            SELECT id, work_order_id, actor, action, from_status, to_status, detail, created_at
            FROM work_order_activities
            WHERE work_order_id = #{workOrderId}
            ORDER BY created_at DESC, id DESC
            """)
    List<WorkOrderActivity> findByWorkOrderId(@Param("workOrderId") long workOrderId);

    @Insert("""
            INSERT INTO work_order_activities (
                work_order_id, actor, action, from_status, to_status, detail
            ) VALUES (
                #{workOrderId}, #{actor}, #{action},
                #{fromStatus,jdbcType=VARCHAR}, #{toStatus,jdbcType=VARCHAR}, #{detail}
            )
            """)
    int insert(
            @Param("workOrderId") long workOrderId,
            @Param("actor") String actor,
            @Param("action") String action,
            @Param("fromStatus") WorkOrderStatus fromStatus,
            @Param("toStatus") WorkOrderStatus toStatus,
            @Param("detail") String detail
    );
}
