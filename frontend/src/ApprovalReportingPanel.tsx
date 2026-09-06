import { FormEvent, useEffect, useMemo, useState } from 'react';
import {
  announceDataChange,
  AuthMe,
  decideWorkOrderApproval,
  downloadWorkOrderReport,
  getCurrentUser,
  listWorkOrderApprovals,
  listWorkOrders,
  requestWorkOrderApproval,
  updateWorkOrderStatus,
  WorkOrder,
  WorkOrderApproval,
  WorkOrderStatus
} from './api';

const statusLabels: Record<WorkOrderStatus, string> = {
  RECEIVED: '접수',
  IN_PROGRESS: '진행',
  WAITING_APPROVAL: '승인 대기',
  APPROVED: '승인 완료',
  DONE: '완료',
  CANCELLED: '취소'
};

function saveBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  URL.revokeObjectURL(url);
}

export default function ApprovalReportingPanel() {
  const [principal, setPrincipal] = useState<AuthMe | null>(null);
  const [orders, setOrders] = useState<WorkOrder[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [approvals, setApprovals] = useState<WorkOrderApproval[]>([]);
  const [comment, setComment] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');

  const isAdmin = principal?.roles.includes('ADMIN') ?? false;

  async function refresh() {
    try {
      const [nextPrincipal, nextOrders] = await Promise.all([getCurrentUser(), listWorkOrders()]);
      setPrincipal(nextPrincipal);
      setOrders(nextOrders);
      const candidate = nextOrders.find((order) => order.id === selectedId)
        ?? nextOrders.find((order) => ['IN_PROGRESS', 'WAITING_APPROVAL', 'APPROVED'].includes(order.status))
        ?? null;
      setSelectedId(candidate?.id ?? null);
      if (candidate) setApprovals(await listWorkOrderApprovals(candidate.id));
      else setApprovals([]);
    } catch {
      setPrincipal(null);
      setOrders([]);
      setSelectedId(null);
      setApprovals([]);
    }
  }

  useEffect(() => {
    const handler = () => void refresh();
    window.addEventListener('business-ops-auth-change', handler);
    window.addEventListener('business-ops-data-change', handler);
    return () => {
      window.removeEventListener('business-ops-auth-change', handler);
      window.removeEventListener('business-ops-data-change', handler);
    };
  }, [selectedId]);

  async function chooseOrder(id: number) {
    setSelectedId(id);
    try {
      setApprovals(await listWorkOrderApprovals(id));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '승인 이력을 읽지 못했습니다.');
    }
  }

  async function mutate(action: () => Promise<unknown>, success: string) {
    setBusy(true);
    setMessage('');
    try {
      await action();
      setComment('');
      setMessage(success);
      await refresh();
      announceDataChange();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '요청을 처리하지 못했습니다.');
    } finally {
      setBusy(false);
    }
  }

  async function exportReport(format: 'csv' | 'xlsx') {
    if (from && to && from > to) {
      setMessage('시작일은 종료일보다 늦을 수 없습니다.');
      return;
    }
    setBusy(true);
    setMessage('');
    try {
      const blob = await downloadWorkOrderReport(format, from, to);
      saveBlob(blob, `work-orders-report.${format}`);
      setMessage(`${format.toUpperCase()} 리포트를 내려받았습니다.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : '리포트를 내려받지 못했습니다.');
    } finally {
      setBusy(false);
    }
  }

  const selected = useMemo(() => orders.find((order) => order.id === selectedId) ?? null, [orders, selectedId]);
  const actionable = orders.filter((order) => ['IN_PROGRESS', 'WAITING_APPROVAL', 'APPROVED'].includes(order.status));

  if (!principal) return null;

  return (
    <section className="approval-report-panel" aria-label="승인 및 운영 리포트">
      <div className="approval-report-heading">
        <div><strong>APPROVAL & REPORTING · V8</strong><span>Human approval / operational export</span></div>
        <span className="approval-role">{isAdmin ? 'ADMIN' : 'STAFF'}</span>
      </div>

      <div className="approval-report-grid">
        <article className="approval-card">
          <h3>업무 승인</h3>
          <p>진행 중 업무는 승인 요청을 거쳐야 완료 상태로 갈 수 있습니다.</p>
          <select aria-label="승인 업무 선택" value={selectedId ?? ''} onChange={(event) => void chooseOrder(Number(event.target.value))}>
            <option value="">업무 선택</option>
            {actionable.map((order) => <option key={order.id} value={order.id}>{statusLabels[order.status]} · {order.title}</option>)}
          </select>

          {selected && <div className="approval-selected">
            <strong>{selected.title}</strong>
            <span>{selected.customerName} · {selected.assignee}</span>
            <em>{statusLabels[selected.status]}</em>
          </div>}

          {selected && selected.status === 'IN_PROGRESS' && <form onSubmit={(event: FormEvent) => {
            event.preventDefault();
            void mutate(() => requestWorkOrderApproval(selected.id, comment), '승인 요청을 생성했습니다.');
          }}>
            <textarea value={comment} onChange={(event) => setComment(event.target.value)} maxLength={500} placeholder="승인 요청 메모 (선택)" />
            <button disabled={busy} type="submit">승인 요청</button>
          </form>}

          {selected && selected.status === 'WAITING_APPROVAL' && <div className="approval-actions">
            <textarea value={comment} onChange={(event) => setComment(event.target.value)} maxLength={500} placeholder={isAdmin ? '승인/반려 메모 (선택)' : '관리자 결정을 기다리는 중입니다.'} disabled={!isAdmin} />
            {isAdmin ? <div>
              <button disabled={busy} type="button" onClick={() => void mutate(() => decideWorkOrderApproval(selected.id, 'APPROVE', comment), '업무를 승인했습니다.')}>승인</button>
              <button disabled={busy} className="danger" type="button" onClick={() => void mutate(() => decideWorkOrderApproval(selected.id, 'REJECT', comment), '업무를 반려했습니다.')}>반려</button>
            </div> : <p>ADMIN만 승인 또는 반려할 수 있습니다.</p>}
          </div>}

          {selected && selected.status === 'APPROVED' && <button disabled={busy} type="button" onClick={() => void mutate(() => updateWorkOrderStatus(selected.id, 'DONE'), '승인된 업무를 완료 처리했습니다.')}>완료 처리</button>}

          <div className="approval-history">
            {approvals.slice(0, 5).map((approval) => <div key={approval.id}>
              <strong>{approval.decision}</strong>
              <span>{approval.requestedBy} · {approval.requestedAt.slice(0, 16).replace('T', ' ')}</span>
              {approval.decisionComment && <p>{approval.decisionComment}</p>}
            </div>)}
            {selected && approvals.length === 0 && <p>승인 이력이 없습니다.</p>}
          </div>
        </article>

        <article className="approval-card report-card">
          <h3>운영 리포트</h3>
          <p>ADMIN은 업무 데이터를 기간별 CSV/XLSX로 내려받을 수 있습니다.</p>
          {isAdmin ? <>
            <label>시작일<input type="date" value={from} onChange={(event) => setFrom(event.target.value)} /></label>
            <label>종료일<input type="date" value={to} onChange={(event) => setTo(event.target.value)} /></label>
            <div className="report-actions">
              <button disabled={busy} type="button" onClick={() => void exportReport('csv')}>CSV Report</button>
              <button disabled={busy} type="button" onClick={() => void exportReport('xlsx')}>XLSX Report</button>
            </div>
            <small>CSV/XLSX 모두 spreadsheet formula injection 방어를 적용합니다.</small>
          </> : <p className="report-denied">운영 리포트는 ADMIN 전용입니다.</p>}
        </article>
      </div>
      {message && <p className="approval-message" role="status">{message}</p>}
    </section>
  );
}
