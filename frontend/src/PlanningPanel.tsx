import { useEffect, useMemo, useState } from 'react';
import { listWorkOrders, WorkOrder } from './api';

function dayKey(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
}

export default function PlanningPanel() {
  const [orders,setOrders] = useState<WorkOrder[]>([]);
  const [visible,setVisible] = useState(false);

  useEffect(() => {
    async function refresh() {
      await new Promise((resolve) => window.setTimeout(resolve, 120));
      try {
        const next = await listWorkOrders();
        setOrders(next);
        setVisible(true);
      } catch {
        setOrders([]);
        setVisible(false);
      }
    }
    const handler = () => void refresh();
    window.addEventListener('business-ops-auth-change', handler);
    return () => window.removeEventListener('business-ops-auth-change', handler);
  }, []);

  const summary = useMemo(() => {
    const today = dayKey(new Date());
    const open = orders.filter((order) => order.status !== 'DONE' && order.status !== 'CANCELLED');
    return {
      overdue: open.filter((order) => order.dueDate && order.dueDate < today).length,
      today: open.filter((order) => order.dueDate === today).length,
      urgent: open.filter((order) => order.priority === 'URGENT').length,
      inProgress: open.filter((order) => order.status === 'IN_PROGRESS').length,
      focus: open.filter((order) => order.priority === 'URGENT' || order.priority === 'HIGH').slice(0,4)
    };
  }, [orders]);

  if (!visible) return null;

  return (
    <aside className="planning-panel" aria-label="업무 우선순위 현황">
      <div className="planning-title"><strong>WORK PLANNING · V6</strong><span>Priority / Due date</span></div>
      <div className="planning-metrics">
        <span><b>{summary.overdue}</b>기한 초과</span>
        <span><b>{summary.today}</b>오늘 마감</span>
        <span><b>{summary.urgent}</b>긴급</span>
        <span><b>{summary.inProgress}</b>진행 중</span>
      </div>
      {summary.focus.length > 0 && <div className="planning-focus">{summary.focus.map((order) => <span key={order.id}><b>{order.priority}</b>{order.title}{order.dueDate ? ` · ${order.dueDate}` : ''}</span>)}</div>}
    </aside>
  );
}
