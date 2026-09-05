import { FormEvent, useEffect, useMemo, useState } from 'react';
import {
  createUser,
  createWorkOrder,
  deleteUser,
  DirectoryUser,
  listUsers,
  listWorkOrders,
  updateUser,
  updateWorkOrderStatus,
  UserInput,
  WorkOrder,
  WorkOrderInput,
  WorkOrderStatus
} from './api';

const emptyUserForm: UserInput = { name: '', email: '' };
const emptyWorkOrderForm: WorkOrderInput = { title: '', customerName: '', assignee: '' };

const statusLabels: Record<WorkOrderStatus, string> = {
  RECEIVED: '접수',
  IN_PROGRESS: '진행',
  DONE: '완료',
  CANCELLED: '취소'
};

function formatDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(new Date(value));
}

function nextActions(status: WorkOrderStatus): Array<{ label: string; status: WorkOrderStatus }> {
  if (status === 'RECEIVED') {
    return [
      { label: '진행 시작', status: 'IN_PROGRESS' },
      { label: '취소', status: 'CANCELLED' }
    ];
  }
  if (status === 'IN_PROGRESS') {
    return [
      { label: '완료', status: 'DONE' },
      { label: '취소', status: 'CANCELLED' }
    ];
  }
  return [];
}

export default function App() {
  const [users, setUsers] = useState<DirectoryUser[]>([]);
  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<WorkOrderStatus | ''>('');
  const [userForm, setUserForm] = useState<UserInput>(emptyUserForm);
  const [workForm, setWorkForm] = useState<WorkOrderInput>(emptyWorkOrderForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [workSaving, setWorkSaving] = useState(false);
  const [error, setError] = useState('');

  const domains = useMemo(
    () => new Set(users.map((user) => user.email.split('@')[1]).filter(Boolean)).size,
    [users]
  );

  const workMetrics = useMemo(() => {
    return {
      received: workOrders.filter((item) => item.status === 'RECEIVED').length,
      inProgress: workOrders.filter((item) => item.status === 'IN_PROGRESS').length,
      done: workOrders.filter((item) => item.status === 'DONE').length
    };
  }, [workOrders]);

  async function loadUsers(search = keyword) {
    setUsers(await listUsers(search));
  }

  async function loadOrders(filter: WorkOrderStatus | '' = statusFilter) {
    setWorkOrders(await listWorkOrders(filter || undefined));
  }

  async function loadInitial() {
    setLoading(true);
    setError('');
    try {
      const [nextUsers, nextOrders] = await Promise.all([listUsers(''), listWorkOrders()]);
      setUsers(nextUsers);
      setWorkOrders(nextOrders);
    } catch (e) {
      setError(e instanceof Error ? e.message : '데이터를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadInitial();
  }, []);

  async function submitUser(event: FormEvent) {
    event.preventDefault();
    const payload = { name: userForm.name.trim(), email: userForm.email.trim() };
    if (!payload.name || !payload.email) {
      setError('이름과 이메일을 모두 입력해 주세요.');
      return;
    }

    setSaving(true);
    setError('');
    try {
      if (editingId === null) {
        await createUser(payload);
      } else {
        await updateUser(editingId, payload);
      }
      setUserForm(emptyUserForm);
      setEditingId(null);
      await loadUsers(keyword);
    } catch (e) {
      setError(e instanceof Error ? e.message : '저장하지 못했습니다.');
    } finally {
      setSaving(false);
    }
  }

  async function submitWorkOrder(event: FormEvent) {
    event.preventDefault();
    const payload = {
      title: workForm.title.trim(),
      customerName: workForm.customerName.trim(),
      assignee: workForm.assignee.trim()
    };
    if (!payload.title || !payload.customerName || !payload.assignee) {
      setError('업무명, 고객명, 담당자를 모두 입력해 주세요.');
      return;
    }

    setWorkSaving(true);
    setError('');
    try {
      await createWorkOrder(payload);
      setWorkForm(emptyWorkOrderForm);
      await loadOrders(statusFilter);
    } catch (e) {
      setError(e instanceof Error ? e.message : '업무를 등록하지 못했습니다.');
    } finally {
      setWorkSaving(false);
    }
  }

  function startEdit(user: DirectoryUser) {
    setEditingId(user.id);
    setUserForm({ name: user.name, email: user.email });
    setError('');
  }

  function cancelEdit() {
    setEditingId(null);
    setUserForm(emptyUserForm);
  }

  async function removeUser(user: DirectoryUser) {
    if (!window.confirm(`${user.name} 항목을 삭제하시겠습니까?`)) return;
    setError('');
    try {
      await deleteUser(user.id);
      if (editingId === user.id) cancelEdit();
      await loadUsers(keyword);
    } catch (e) {
      setError(e instanceof Error ? e.message : '삭제하지 못했습니다.');
    }
  }

  async function transitionOrder(order: WorkOrder, next: WorkOrderStatus) {
    setError('');
    try {
      await updateWorkOrderStatus(order.id, next);
      await loadOrders(statusFilter);
    } catch (e) {
      setError(e instanceof Error ? e.message : '상태를 변경하지 못했습니다.');
    }
  }

  async function applyStatusFilter(value: WorkOrderStatus | '') {
    setStatusFilter(value);
    setLoading(true);
    setError('');
    try {
      await loadOrders(value);
    } catch (e) {
      setError(e instanceof Error ? e.message : '업무 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div>
          <p className="brand-mark">KZ</p>
          <h1>Business Ops</h1>
          <p className="sidebar-copy">중소기업 업무관리 풀스택 데모</p>
        </div>
        <nav>
          <a className="active" href="#dashboard">Dashboard</a>
          <a href="#work-orders">Work orders</a>
          <a href="#directory">Directory</a>
          <a href="#delivery">Delivery scope</a>
        </nav>
        <div className="sidebar-note">
          <strong>V2</strong>
          <span>React UI ↔ Spring REST API ↔ state machine</span>
        </div>
      </aside>

      <main>
        <header className="topbar">
          <div>
            <p className="eyebrow">FULL-STACK OPERATIONS PORTFOLIO</p>
            <h2>고객 데이터와 업무 상태를 한 화면에서 관리합니다.</h2>
          </div>
          <span className="status-pill">SPRING API CONNECTED</span>
        </header>

        {error && <p className="error-box global-error">{error}</p>}

        <section id="dashboard" className="metrics" aria-label="운영 현황">
          <article>
            <span>연락처</span>
            <strong>{users.length}</strong>
            <small>현재 검색 결과</small>
          </article>
          <article>
            <span>신규 접수</span>
            <strong>{workMetrics.received}</strong>
            <small>처리 대기</small>
          </article>
          <article>
            <span>진행 중</span>
            <strong>{workMetrics.inProgress}</strong>
            <small>담당자 처리 중</small>
          </article>
          <article>
            <span>완료</span>
            <strong>{workMetrics.done}</strong>
            <small>현재 조회 범위</small>
          </article>
        </section>

        <section id="work-orders" className="panel work-order-panel">
          <div className="panel-heading work-order-heading">
            <div>
              <p className="eyebrow">WORK ORDER MODULE</p>
              <h3>업무 접수 · 진행 · 완료 상태관리</h3>
              <p className="muted">허용된 상태 전이만 서버에서 처리하며 동시 변경은 409로 보호합니다.</p>
            </div>
            <select
              value={statusFilter}
              onChange={(event) => void applyStatusFilter(event.target.value as WorkOrderStatus | '')}
              aria-label="업무 상태 필터"
            >
              <option value="">전체 상태</option>
              <option value="RECEIVED">접수</option>
              <option value="IN_PROGRESS">진행</option>
              <option value="DONE">완료</option>
              <option value="CANCELLED">취소</option>
            </select>
          </div>

          <div className="work-order-layout">
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>업무</th>
                    <th>고객</th>
                    <th>담당자</th>
                    <th>상태</th>
                    <th>변경</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr><td colSpan={5} className="empty-state">불러오는 중...</td></tr>
                  ) : workOrders.length === 0 ? (
                    <tr><td colSpan={5} className="empty-state">조건에 맞는 업무가 없습니다.</td></tr>
                  ) : (
                    workOrders.map((order) => (
                      <tr key={order.id}>
                        <td>
                          <strong>{order.title}</strong>
                          <small className="cell-note">{formatDate(order.updatedAt)} 갱신</small>
                        </td>
                        <td>{order.customerName}</td>
                        <td>{order.assignee}</td>
                        <td><span className={`work-status status-${order.status.toLowerCase()}`}>{statusLabels[order.status]}</span></td>
                        <td className="actions work-actions">
                          {nextActions(order.status).map((action) => (
                            <button
                              key={action.status}
                              className={action.status === 'CANCELLED' ? 'text-button danger' : 'text-button'}
                              onClick={() => void transitionOrder(order, action.status)}
                            >
                              {action.label}
                            </button>
                          ))}
                          {nextActions(order.status).length === 0 && <span className="terminal-state">종료</span>}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            <aside className="work-create">
              <p className="eyebrow">NEW WORK ORDER</p>
              <h3>새 업무 접수</h3>
              <form className="editor-form" onSubmit={submitWorkOrder}>
                <label>
                  업무명
                  <input value={workForm.title} onChange={(event) => setWorkForm({ ...workForm, title: event.target.value })} maxLength={160} required />
                </label>
                <label>
                  고객명
                  <input value={workForm.customerName} onChange={(event) => setWorkForm({ ...workForm, customerName: event.target.value })} maxLength={120} required />
                </label>
                <label>
                  담당자
                  <input value={workForm.assignee} onChange={(event) => setWorkForm({ ...workForm, assignee: event.target.value })} maxLength={100} required />
                </label>
                <button className="primary-button" type="submit" disabled={workSaving}>
                  {workSaving ? '접수 중...' : '업무 접수'}
                </button>
              </form>
            </aside>
          </div>
        </section>

        <section id="directory" className="workspace-grid directory-section">
          <article className="panel directory-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">DIRECTORY MODULE</p>
                <h3>사용자·연락처 관리</h3>
              </div>
              <form
                className="search-form"
                onSubmit={(event) => {
                  event.preventDefault();
                  void loadUsers(keyword).catch((e) => setError(e instanceof Error ? e.message : '검색하지 못했습니다.'));
                }}
              >
                <input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="이름 또는 이메일 검색" aria-label="검색어" />
                <button type="submit">검색</button>
              </form>
            </div>

            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>이름</th>
                    <th>이메일</th>
                    <th>등록일</th>
                    <th>관리</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr><td colSpan={4} className="empty-state">불러오는 중...</td></tr>
                  ) : users.length === 0 ? (
                    <tr><td colSpan={4} className="empty-state">조건에 맞는 데이터가 없습니다.</td></tr>
                  ) : (
                    users.map((user) => (
                      <tr key={user.id}>
                        <td><strong>{user.name}</strong></td>
                        <td>{user.email}</td>
                        <td>{formatDate(user.createdAt)}</td>
                        <td className="actions">
                          <button className="text-button" onClick={() => startEdit(user)}>수정</button>
                          <button className="text-button danger" onClick={() => void removeUser(user)}>삭제</button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </article>

          <aside className="panel editor-panel">
            <p className="eyebrow">{editingId === null ? 'CREATE CONTACT' : 'UPDATE CONTACT'}</p>
            <h3>{editingId === null ? '새 연락처 등록' : '연락처 수정'}</h3>
            <p className="muted">브라우저 입력값은 Spring Validation과 DB unique 제약까지 다시 검증됩니다.</p>

            <form className="editor-form" onSubmit={submitUser}>
              <label>
                이름
                <input value={userForm.name} onChange={(event) => setUserForm({ ...userForm, name: event.target.value })} maxLength={100} required />
              </label>
              <label>
                이메일
                <input type="email" value={userForm.email} onChange={(event) => setUserForm({ ...userForm, email: event.target.value })} maxLength={255} required />
              </label>
              <button className="primary-button" type="submit" disabled={saving}>
                {saving ? '저장 중...' : editingId === null ? '등록하기' : '변경 저장'}
              </button>
              {editingId !== null && <button className="secondary-button" type="button" onClick={cancelEdit}>수정 취소</button>}
            </form>
          </aside>
        </section>

        <section id="delivery" className="delivery-panel">
          <div>
            <p className="eyebrow">CLIENT VALUE</p>
            <h3>이 프로젝트가 증명하는 외주 범위</h3>
          </div>
          <ul>
            <li>React/TypeScript 관리자 화면</li>
            <li>Spring Boot + MyBatis REST API</li>
            <li>고객 CRUD · 검색 · Excel export</li>
            <li>업무 접수와 서버 상태 머신</li>
            <li>조건부 상태 UPDATE를 이용한 동시 변경 방어</li>
            <li>400 / 404 / 409 오류 계약 · OpenAPI · CI</li>
          </ul>
        </section>
      </main>
    </div>
  );
}
