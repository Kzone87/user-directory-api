import { useEffect, useMemo, useState } from 'react';
import { AnalyticsBucket, getOperationsAnalytics, OperationsAnalytics } from './api';

function maxCount(items: AnalyticsBucket[]) {
  return Math.max(1, ...items.map((item) => item.count));
}

function BarList({ items }: { items: AnalyticsBucket[] }) {
  const max = useMemo(() => maxCount(items), [items]);
  if (!items.length) return <p className="analytics-empty">표시할 데이터가 없습니다.</p>;
  return (
    <div className="analytics-bars">
      {items.map((item) => (
        <div className="analytics-bar-row" key={item.label}>
          <span>{item.label}</span>
          <div className="analytics-track"><i style={{ width: `${Math.max(8, (item.count / max) * 100)}%` }} /></div>
          <b>{item.count}</b>
        </div>
      ))}
    </div>
  );
}

export default function AnalyticsPanel() {
  const [data, setData] = useState<OperationsAnalytics | null>(null);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  async function refresh() {
    setLoading(true);
    setMessage('');
    await new Promise((resolve) => window.setTimeout(resolve, 100));
    try {
      setData(await getOperationsAnalytics());
    } catch {
      setData(null);
      setMessage('로그인 후 서버 집계 Analytics를 확인할 수 있습니다.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const handler = () => void refresh();
    window.addEventListener('business-ops-auth-change', handler);
    return () => window.removeEventListener('business-ops-auth-change', handler);
  }, []);

  if (!data) {
    return message ? <aside className="analytics-shell analytics-locked">{message}</aside> : null;
  }

  return (
    <section className="analytics-shell" aria-label="운영 Analytics">
      <div className="analytics-heading">
        <div><strong>OPERATIONS ANALYTICS · V7</strong><span>Server-side aggregation</span></div>
        <button type="button" onClick={() => void refresh()} disabled={loading}>{loading ? '집계 중...' : '새로고침'}</button>
      </div>

      <div className="analytics-kpis">
        <article><span>전체 고객</span><b>{data.totalCustomers}</b><small>활성 {data.activeCustomers}</small></article>
        <article><span>진행 가능 업무</span><b>{data.openWorkOrders}</b><small>RECEIVED + IN_PROGRESS</small></article>
        <article className={data.overdueWorkOrders ? 'danger' : ''}><span>기한 초과</span><b>{data.overdueWorkOrders}</b><small>종료/취소 제외</small></article>
        <article><span>이번 달 완료</span><b>{data.doneThisMonth}</b><small>DONE 기준</small></article>
      </div>

      <div className="analytics-grid">
        <article><h3>업무 상태</h3><BarList items={data.statusDistribution} /></article>
        <article><h3>우선순위</h3><BarList items={data.priorityDistribution} /></article>
        <article><h3>담당자별 열린 업무</h3><BarList items={data.workloadByAssignee} /></article>
        <article><h3>최근 14일 완료 추이</h3><BarList items={data.completedTrend} /></article>
      </div>
    </section>
  );
}
