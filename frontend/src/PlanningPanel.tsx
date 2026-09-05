import { FormEvent, useEffect, useMemo, useState } from 'react';
import {
  createWorkOrder,
  Customer,
  listCustomers,
  listWorkOrders,
  WorkOrder,
  WorkOrderPriority
} from './api';

function dayKey(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
}

const priorityLabels: Record<WorkOrderPriority, string> = {
  LOW: '낮음',
  NORMAL: '보통',
  HIGH: '높음',
  URGENT: '긴급'
};

export default function PlanningPanel() {
  const [orders,setOrders] = useState<WorkOrder[]>([]);
  const [customers,setCustomers] = useState<Customer[]>([]);
  const [visible,setVisible] = useState(false);
  const [creating,setCreating] = useState(false);
  const [message,setMessage] = useState('');
  const [form,setForm] = useState({
    title:'',
    customerId:0,
    assignee:'',
    priority:'NORMAL' as WorkOrderPriority,
    dueDate:''
  });

  async function refresh() {
    await new Promise((resolve) => window.setTimeout(resolve, 120));
    try {
      const [nextOrders,nextCustomers] = await Promise.all([listWorkOrders(),listCustomers()]);
      setOrders(nextOrders);
      setCustomers(nextCustomers.filter((customer) => customer.status !== 'INACTIVE'));
      setVisible(true);
    } catch {
      setOrders([]);
      setCustomers([]);
      setVisible(false);
    }
  }

  useEffect(() => {
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

  async function submit(event: FormEvent) {
    event.preventDefault();
    const title = form.title.trim();
    const assignee = form.assignee.trim();
    if (!title || form.customerId <= 0 || !assignee) {
      setMessage('업무명, 고객, 담당자를 입력해 주세요.');
      return;
    }

    setCreating(true);
    setMessage('');
    try {
      await createWorkOrder({
        title,
        customerId:form.customerId,
        assignee,
        priority:form.priority,
        dueDate:form.dueDate || null
      });
      setForm({title:'',customerId:0,assignee:'',priority:'NORMAL',dueDate:''});
      setMessage('우선순위와 마감일을 포함해 업무를 접수했습니다.');
      await refresh();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '업무를 접수하지 못했습니다.');
    } finally {
      setCreating(false);
    }
  }

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

      <details className="planning-create">
        <summary>우선순위·마감일 포함 업무 접수</summary>
        <form onSubmit={submit}>
          <input aria-label="업무명" placeholder="업무명" maxLength={160} value={form.title} onChange={(event) => setForm({...form,title:event.target.value})} required />
          <select aria-label="고객" value={form.customerId || ''} onChange={(event) => setForm({...form,customerId:Number(event.target.value)})} required>
            <option value="">고객 선택</option>
            {customers.map((customer) => <option key={customer.id} value={customer.id}>{customer.companyName}</option>)}
          </select>
          <input aria-label="담당자" placeholder="담당자" maxLength={100} value={form.assignee} onChange={(event) => setForm({...form,assignee:event.target.value})} required />
          <div className="planning-create-row">
            <select aria-label="우선순위" value={form.priority} onChange={(event) => setForm({...form,priority:event.target.value as WorkOrderPriority})}>
              {(Object.keys(priorityLabels) as WorkOrderPriority[]).map((priority) => <option key={priority} value={priority}>{priorityLabels[priority]}</option>)}
            </select>
            <input aria-label="마감일" type="date" value={form.dueDate} onChange={(event) => setForm({...form,dueDate:event.target.value})} />
          </div>
          <button type="submit" disabled={creating}>{creating ? '접수 중...' : '계획 업무 접수'}</button>
          {message && <p>{message}</p>}
        </form>
      </details>
    </aside>
  );
}
