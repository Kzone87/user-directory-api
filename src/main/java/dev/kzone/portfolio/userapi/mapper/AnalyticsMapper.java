package dev.kzone.portfolio.userapi.mapper;

import dev.kzone.portfolio.userapi.dto.AnalyticsBucket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnalyticsMapper {
    @Select("SELECT COUNT(*) FROM customers")
    long countCustomers();

    @Select("SELECT COUNT(*) FROM customers WHERE status = 'ACTIVE'")
    long countActiveCustomers();

    @Select("SELECT COUNT(*) FROM work_orders WHERE status IN ('RECEIVED', 'IN_PROGRESS')")
    long countOpenWorkOrders();

    @Select("""
            SELECT COUNT(*)
            FROM work_orders
            WHERE due_date IS NOT NULL
              AND due_date < CURRENT_DATE
              AND status NOT IN ('DONE', 'CANCELLED')
            """)
    long countOverdueWorkOrders();

    @Select("""
            SELECT COUNT(*)
            FROM work_orders
            WHERE status = 'DONE'
              AND YEAR(updated_at) = YEAR(CURRENT_DATE)
              AND MONTH(updated_at) = MONTH(CURRENT_DATE)
            """)
    long countDoneThisMonth();

    @Select("""
            SELECT status AS label, COUNT(*) AS count
            FROM work_orders
            GROUP BY status
            ORDER BY CASE status
              WHEN 'RECEIVED' THEN 1
              WHEN 'IN_PROGRESS' THEN 2
              WHEN 'DONE' THEN 3
              ELSE 4
            END
            """)
    List<AnalyticsBucket> statusDistribution();

    @Select("""
            SELECT priority AS label, COUNT(*) AS count
            FROM work_orders
            GROUP BY priority
            ORDER BY CASE priority
              WHEN 'URGENT' THEN 1
              WHEN 'HIGH' THEN 2
              WHEN 'NORMAL' THEN 3
              ELSE 4
            END
            """)
    List<AnalyticsBucket> priorityDistribution();

    @Select("""
            SELECT assignee AS label, COUNT(*) AS count
            FROM work_orders
            WHERE status IN ('RECEIVED', 'IN_PROGRESS')
            GROUP BY assignee
            ORDER BY count DESC, assignee ASC
            """)
    List<AnalyticsBucket> workloadByAssignee();

    @Select("""
            SELECT CAST(CAST(updated_at AS DATE) AS VARCHAR) AS label, COUNT(*) AS count
            FROM work_orders
            WHERE status = 'DONE'
              AND updated_at >= DATEADD('DAY', -13, CURRENT_TIMESTAMP)
            GROUP BY CAST(updated_at AS DATE)
            ORDER BY CAST(updated_at AS DATE)
            """)
    List<AnalyticsBucket> completedTrend();
}
