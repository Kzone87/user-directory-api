package dev.kzone.portfolio.userapi.mapper;

import dev.kzone.portfolio.userapi.domain.ApprovalDecision;
import dev.kzone.portfolio.userapi.domain.WorkOrderApproval;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface WorkOrderApprovalMapper {
    @Insert("""
            INSERT INTO work_order_approvals (work_order_id, requested_by, request_comment, decision)
            VALUES (#{workOrderId}, #{requestedBy}, #{requestComment}, 'PENDING')
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WorkOrderApproval approval);

    @Select("""
            SELECT id, work_order_id, requested_by, requested_at, request_comment,
                   decision, decided_by, decided_at, decision_comment
            FROM work_order_approvals
            WHERE work_order_id = #{workOrderId}
            ORDER BY requested_at DESC, id DESC
            """)
    List<WorkOrderApproval> findByWorkOrderId(@Param("workOrderId") long workOrderId);

    @Select("""
            SELECT id, work_order_id, requested_by, requested_at, request_comment,
                   decision, decided_by, decided_at, decision_comment
            FROM work_order_approvals
            WHERE work_order_id = #{workOrderId} AND decision = 'PENDING'
            ORDER BY requested_at DESC, id DESC
            LIMIT 1
            """)
    Optional<WorkOrderApproval> findLatestPending(@Param("workOrderId") long workOrderId);

    @Update("""
            UPDATE work_order_approvals
            SET decision = #{decision},
                decided_by = #{decidedBy},
                decided_at = CURRENT_TIMESTAMP,
                decision_comment = #{comment}
            WHERE id = #{id} AND decision = 'PENDING'
            """)
    int decide(
            @Param("id") long id,
            @Param("decision") ApprovalDecision decision,
            @Param("decidedBy") String decidedBy,
            @Param("comment") String comment
    );
}
