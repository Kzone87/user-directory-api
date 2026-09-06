import { FormEvent, useMemo, useState } from 'react';
import {
  announceDataChange,
  AuthMe,
  clearCredentials,
  createCustomer,
  createUser,
  createWorkOrder,
  Customer,
  CustomerInput,
  CustomerStatus,
  deleteUser,
  DirectoryUser,
  getCurrentUser,
  listCustomers,
  listUsers,
  listWorkOrderActivities,
  listWorkOrders,
  setBasicCredentials,
  updateCustomer,
  updateUser,
  updateWorkOrderStatus,
  UserInput,
  WorkOrder,
  WorkOrderActivity,
  WorkOrderInput,
  WorkOrderPriority,
  WorkOrderStatus
} from './api';

const emptyUserForm: UserInput = { name: '', email: '' };
const emptyCustomerForm: CustomerInput = {
  companyName: '',
  contactName: '',
  email: '',
  phone: '',
  status: 'LEAD',
  memo: ''
};
const emptyWorkOrderForm: WorkOrderInput = {
  title: '',
  customerId: 0,
  assignee: '',
  priority: 'NORMAL',
  dueDate: null
};

const statusLabels: Record<WorkOrderStatus, string> = {
  RECEIVED: '접수',
  IN_PROGRESS: '진행',
  WAITING_APPROVAL: '승인 대기',
  APPROVED: '승인 완료',
  DONE: '완료',
  CANCELLED: '취소'
};

const priorityLabels: Record<WorkOrderPriority, string> = {
  LOW: '낮음',
  NORMAL: '보통',
  HIGH: '높음',
  URGENT: '긴급'
};

const customerStatusLabels: Record<CustomerStatus, string> = {
  LEAD: '리드',
  ACTIVE: '활성',
  INACTIVE: '비활성'
};

function formatDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(new Date(value));
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
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
    return [{ label: '취소', status: 'CANCELLED' }];
  }
  if (status === 'APPROVED') {
    return [
      { label: '완료', status: 'DONE' },
      { label: '취소', status: 'CANCELLED' }
    ];
  }
  return [];
}

function isTerminal(status: WorkOrderStatus) {
  return status === 'DONE' || status === 'CANCELLED';
}

export default function App() {
  const [auth, setAuth] = useState<AuthMe | null>(null);
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [loginLoading, setLoginLoading] = useState(false);

  const [customers, setCustomers] = useState<Customer[]>([]);
  const [customerKeyword, setCustomerKeyword] = useState('');
  const [customerStatusFilter, setCustomerStatusFilter] = useState<CustomerStatus | ''>('');
  const [customerForm, setCustomerForm] = useState<CustomerInput>(emptyCustomerForm);
  const [editingCustomerId, setEditingCustomerId] = useState<number | null>(null);
  const [customerSaving, setCustomerSaving] = useState(false);
  const [selectedCustomerId, setSelectedCustomerId] = useState<number | null>(null);

  const [users, setUsers] = useState<DirectoryUser[]>([]);
  const [keyword, setKeyword] = useState('');
  const [userForm, setUserForm] = useState<UserInput>(emptyUserForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);

  const [workOrders, setWorkOrders] = useState<WorkOrder[]>([]);
  const [statusFilter, setStatusFilter] = useState<WorkOrderStatus | ''>('');
  const [workForm, setWorkForm] = useState<WorkOrderInput>(emptyWorkOrderForm);
  const [workSaving, setWorkSaving] = useState(false);
  const [activities, setActivities] = useState<WorkOrderActivity[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<WorkOrder | null>(null);
  const [activityLoading, setActivityLoading] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const isAdmin = auth?.roles.includes('ADMIN') ?? false;

  const metrics = useMemo(() => ({
    customers: customers.length,
    activeCustomers: customers.filter((item) => item.status === 'ACTIVE').length,
    received: workOrders.filter((item) => item.status === 'RECEIVED').length,
    inProgress: workOrders.filter((item) => item.status === 'IN_PROGRESS').length,
    waitingApproval: workOrders.filter((item) => item.status === 'WAITING_APPROVAL').length
  }), [customers, workOrders]);

  const visibleOrders = useMemo(
    () => selectedCustomerId === null
      ? workOrders
      : workOrders.filter((order) => order.customerId === selectedCustomerId),
    [selectedCustomerId, workOrders]
  );

  const selectedCustomer = useMemo(
    () => customers.find((customer) => customer.id === selectedCustomerId) ?? null,
    [customers, selectedCustomerId]
  );

  async function loadCustomers(search = customerKeyword, filter = customerStatusFilter) {
    setCustomers(await listCustomers(search, filter || undefined));
  }

  async function loadUsers(search = keyword) {
    setUsers(await listUsers(search));
  }

  async function loadOrders(filter: WorkOrderStatus | '' = statusFilter) {
    setWorkOrders(await listWorkOrders(filter || undefined));
  }

  async function loadOrderActivities(order: WorkOrder) {
    setSelectedOrder(order);
    setActivityLoading(true);
    setError('');
    try {
      setActivities(await listWorkOrderActivities(order.id));
    } catch (e) {
      setActivities([]);
      setError(e instanceof Error ? e.message : '업무 이력을 불러오지 못했습니다.');
    } finally {
      setActivityLoading(false);
    }
  }

  async function loadInitialData() {
    setLoading(true);
    try {
      const [nextCustomers, nextUsers, nextOrders] = await Promise.all([
        listCustomers(),
        listUsers(''),
        listWorkOrders()
      ]);
      setCustomers(nextCustomers);
      setUsers(nextUsers);
      setWorkOrders(nextOrders);
      setSelectedOrder(null);
      setActivities([]);
      setSelectedCustomerId(null);
    } finally {
      setLoading(false);
    }
  }

  async function login(event: FormEvent) {
    event.preventDefault();
    const username = loginForm.username.trim();
    if (!username || !loginForm.password) {
      setError('사용자명과 비밀번호를 입력해 주세요.');
      return;
    }

    setLoginLoading(true);
    setError('');
    setBasicCredentials(username, loginForm.password);
    try {
      const principal = await getCurrentUser();
      setAuth(principal);
      await loadInitialData();
      setLoginForm({ username: '', password: '' });
    } catch (e) {
      clearCredentials();
      setAuth(null);
      setError(e instanceof Error ? e.message : '로그인하지 못했습니다.');
    } finally {
      setLoginLoading(false);
    }
  }

  function useDemoAccount(role: 'ADMIN' | 'STAFF') {
    setLoginForm(role === 'ADMIN'
      ? { username: 'demo-admin', password: 'admin-demo' }
      : { username: 'demo-staff', password: 'staff-demo' });
    setError('');
  }

  function logout() {
    clearCredentials();
    setAuth(null);
    setCustomers([]);
    setUsers([]);
    setWorkOrders([]);
    setActivities([]);
    setSelectedOrder(null);
    setSelectedCustomerId(null);
    setCustomerKeyword('');
    setCustomerStatusFilter('');
    setKeyword('');
    setStatusFilter('');
    setEditingCustomerId(null);
    setEditingId(null);
    setCustomerForm(emptyCustomerForm);
    setUserForm(emptyUserForm);
    setWorkForm(emptyWorkOrderForm);
    setError('');
  }

  async function submitCustomer(event: FormEvent) {
    event.preventDefault();
    const payload: CustomerInput = {
      companyName: customerForm.companyName.trim(),
      contactName: customerForm.contactName.trim(),
      email: customerForm.email.trim(),
      phone: customerForm.phone.trim(),
      status: customerForm.status,
      memo: customerForm.memo.trim()
    };
    if (!payload.companyName) {
      setError('회사명을 입력해 주세요.');
      return;
    }

    setCustomerSaving(true);
    setError('');
    try {
      if (editingCustomerId === null) await createCustomer(payload);
      else await updateCustomer(editingCustomerId, payload);
      setCustomerForm(emptyCustomerForm);
      setEditingCustomerId(null);
      await loadCustomers(customerKeyword, customerStatusFilter);
      announceDataChange();
    } catch (e) {
      setError(e instanceof Error ? e.message : '고객 정보를 저장하지 못했습니다.');
    } finally {
      setCustomerSaving(false);
    }
  }

  function startCustomerEdit(customer: Customer) {
    setEditingCustomerId(customer.id);
    setCustomerForm({
      companyName: customer.companyName,
      contactName: customer.contactName ?? '',
      email: customer.email ?? '',
      phone: customer.phone ?? '',
      status: customer.status,
      memo: customer.memo ?? ''
    });
    setError('');
  }

  function cancelCustomerEdit() {
    setEditingCustomerId(null);
    setCustomerForm(emptyCustomerForm);
  }

  async function applyCustomerFilters() {
    setLoading(true);
    setError('');
    try {
      await loadCustomers(customerKeyword, customerStatusFilter);
    } catch (e) {
      setError(e instanceof Error ? e.message : '고객 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }

  async function submitWorkOrder(event: FormEvent) {
    event.preventDefault();
    const payload: WorkOrderInput = {
      title: workForm.title.trim(),
      customerId: workForm.customerId,
      assignee: workForm.assignee.trim(),
      priority: workForm.priority ?? 'NORMAL',
      dueDate: workForm.dueDate || null
    };
    if (!payload.title || payload.customerId <= 0 || !payload.assignee) {
      setError('업무명, 고객, 담당자를 모두 입력해 주세요.');
      return;
    }

    setWorkSaving(true);
    setError('');
    try {
      const created = await createWorkOrder(payload);
      setWorkForm(emptyWorkOrderForm);
      await loadOrders(statusFilter);
      await loadOrderActivities(created);
      announceDataChange();
    } catch (e) {
      setError(e instanceof Error ? e.message : '업무를 등록하지 못했습니다.');
    } finally {
      setWorkSaving(false);
    }
  }

  async function transitionOrder(order: WorkOrder, next: WorkOrderStatus) {
    setError('');
    try {
      const updated = await updateWorkOrderStatus(order.id, next);
      await loadOrders(statusFilter);
      if (selectedOrder?.id === order.id) await loadOrderActivities(updated);
      announceDataChange();
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
      if (editingId === null) await createUser(payload);
      else await updateUser(editingId, payload);
      setUserForm(emptyUserForm);
      setEditingId(null);
      await loadUsers(keyword);
    } catch (e) {
      setError(e instanceof Error ? e.message : '저장하지 못했습니다.');
    } finally {
      setSaving(false);
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

  if (!auth) {
    return (
      <main className="auth-screen">
        <section className="auth-card">
          <div className="auth-intro">
            <p className="eyebrow">BUSINESS OPS DASHBOARD · V8</p>
            <h1>고객·업무·승인·리포트를 하나의 운영 흐름으로 연결합니다.</h1>
            <p>CRM, 업무 계획, 상태 머신, 역할 권한, 승인 이력, 서버 집계 Analytics와 CSV/XLSX 리포트를 결합한 공개 포트폴리오 데모입니다.</p>
            <div className="security-points">
              <span>Customer domain + relational FK</span>
              <span>Approval-gated work order lifecycle</span>
              <span>Role-based API authorization</span>
              <span>Transactional audit + approval history</span>
              <span>Server analytics + safe CSV/XLSX export</span>
            </div>
          </div>

          <div className="login-panel">
            <p className="eyebrow">DEMO LOGIN</p>
            <h2>역할 선택 후 로그인</h2>
            <div className="account-grid">
              <button type="button" onClick={() => useDemoAccount('ADMIN')}>
                <strong>ADMIN</strong><span>승인·반려, 리포트, 디렉터리 삭제 포함</span>
              </button>
              <button type="button" onClick={() => useDemoAccount('STAFF')}>
                <strong>STAFF</strong><span>고객·업무 운영 및 승인 요청</span>
              </button>
            </div>
            <form className="editor-form auth-form" onSubmit={login}>
              <label>사용자명<input autoComplete="username" value={loginForm.username} onChange={(event) => setLoginForm({ ...loginForm, username: event.target.value })} required /></label>
              <label>비밀번호<input type="password" autoComplete="current-password" value={loginForm.password} onChange={(event) => setLoginForm({ ...loginForm, password: event.target.value })} required /></label>
              {error && <p className="error-box">{error}</p>}
              <button className="primary-button" type="submit" disabled={loginLoading}>{loginLoading ? '확인 중...' : '로그인'}</button>
            </form>
            <p className="demo-notice">공개 데모 전용 계정이며 인증정보는 브라우저 저장소에 저장하지 않습니다.</p>
          </div>
        </section>
      </main>
    );
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div>
          <p className="brand-mark">KZ</p>
          <h1>Business Ops</h1>
          <p className="sidebar-copy">고객·업무·승인 운영관리 데모</p>
        </div>
        <nav>
          <a className="active" href="#dashboard">Dashboard</a>
          <a href="#customers">Customers</a>
          <a href="#work-orders">Work orders</a>
          <a href="#directory">Directory</a>
          <a href="#delivery">Delivery scope</a>
        </nav>
        <div className="sidebar-note">
          <strong>V8 · APPROVAL & REPORTING</strong>
          <span>{auth.username} · {auth.roles.join(' / ')}</span>
        </div>
      </aside>

      <main>
        <header className="topbar">
          <div>
            <p className="eyebrow">FULL-STACK OPERATIONS PORTFOLIO</p>
            <h2>업무를 계획하고 승인한 뒤 완료하며, 전 과정을 추적합니다.</h2>
          </div>
          <div className="user-menu">
            <span className="status-pill">{isAdmin ? 'ADMIN' : 'STAFF'}</span>
            <div><strong>{auth.username}</strong><small>{auth.roles.join(' · ')}</small></div>
            <button type="button" onClick={logout}>로그아웃</button>
          </div>
        </header>

        {error && <p className="error-box global-error">{error}</p>}

        <section id="dashboard" className="metrics" aria-label="운영 현황">
          <article><span>고객</span><strong>{metrics.customers}</strong><small>현재 고객 조회 결과</small></article>
          <article><span>활성 고객</span><strong>{metrics.activeCustomers}</strong><small>ACTIVE 상태</small></article>
          <article><span>신규 접수</span><strong>{metrics.received}</strong><small>처리 대기 업무</small></article>
          <article><span>진행 중</span><strong>{metrics.inProgress}</strong><small>담당자 처리 중</small></article>
          <article><span>승인 대기</span><strong>{metrics.waitingApproval}</strong><small>ADMIN 결정 대기</small></article>
        </section>

        <section id="customers" className="workspace-grid customer-section">
          <article className="panel customer-panel">
            <div className="panel-heading customer-heading">
              <div><p className="eyebrow">CUSTOMER MODULE</p><h3>고객·거래처 관리</h3><p className="muted">회사·담당자·연락처·상태를 관리하고 등록된 고객만 업무와 연결합니다.</p></div>
              <form className="customer-filter" onSubmit={(event) => { event.preventDefault(); void applyCustomerFilters(); }}>
                <input value={customerKeyword} onChange={(event) => setCustomerKeyword(event.target.value)} placeholder="회사·담당자·이메일·전화 검색" aria-label="고객 검색" />
                <select value={customerStatusFilter} onChange={(event) => setCustomerStatusFilter(event.target.value as CustomerStatus | '')} aria-label="고객 상태 필터">
                  <option value="">전체 상태</option><option value="LEAD">리드</option><option value="ACTIVE">활성</option><option value="INACTIVE">비활성</option>
                </select>
                <button type="submit">조회</button>
              </form>
            </div>
            <div className="table-wrap">
              <table>
                <thead><tr><th>회사</th><th>담당자</th><th>연락처</th><th>상태</th><th>최근 변경</th><th>관리</th></tr></thead>
                <tbody>
                  {loading ? <tr><td colSpan={6} className="empty-state">불러오는 중...</td></tr> : customers.length === 0 ? <tr><td colSpan={6} className="empty-state">조건에 맞는 고객이 없습니다.</td></tr> : customers.map((customer) => (
                    <tr key={customer.id} className={selectedCustomerId === customer.id ? 'selected-row' : ''}>
                      <td><strong>{customer.companyName}</strong><small className="cell-note">#{customer.id}</small></td>
                      <td>{customer.contactName ?? '-'}</td>
                      <td>{customer.email ?? '-'}<small className="cell-note">{customer.phone ?? ''}</small></td>
                      <td><span className={`customer-status customer-${customer.status.toLowerCase()}`}>{customerStatusLabels[customer.status]}</span></td>
                      <td>{formatDate(customer.updatedAt)}</td>
                      <td className="actions">
                        <button className="text-button history-button" onClick={() => setSelectedCustomerId(selectedCustomerId === customer.id ? null : customer.id)}>업무 보기</button>
                        <button className="text-button" onClick={() => startCustomerEdit(customer)}>수정</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </article>

          <aside className="panel editor-panel customer-editor">
            <p className="eyebrow">{editingCustomerId === null ? 'CREATE CUSTOMER' : 'UPDATE CUSTOMER'}</p>
            <h3>{editingCustomerId === null ? '새 고객 등록' : '고객 정보 수정'}</h3>
            <form className="editor-form" onSubmit={submitCustomer}>
              <label>회사명<input value={customerForm.companyName} onChange={(event) => setCustomerForm({ ...customerForm, companyName: event.target.value })} maxLength={120} required /></label>
              <label>담당자<input value={customerForm.contactName} onChange={(event) => setCustomerForm({ ...customerForm, contactName: event.target.value })} maxLength={100} /></label>
              <label>이메일<input type="email" value={customerForm.email} onChange={(event) => setCustomerForm({ ...customerForm, email: event.target.value })} maxLength={255} /></label>
              <label>전화번호<input value={customerForm.phone} onChange={(event) => setCustomerForm({ ...customerForm, phone: event.target.value })} maxLength={30} /></label>
              <label>상태<select value={customerForm.status} onChange={(event) => setCustomerForm({ ...customerForm, status: event.target.value as CustomerStatus })}><option value="LEAD">리드</option><option value="ACTIVE">활성</option><option value="INACTIVE">비활성</option></select></label>
              <label>메모<textarea value={customerForm.memo} onChange={(event) => setCustomerForm({ ...customerForm, memo: event.target.value })} maxLength={1000} rows={3} /></label>
              <button className="primary-button" type="submit" disabled={customerSaving}>{customerSaving ? '저장 중...' : editingCustomerId === null ? '고객 등록' : '변경 저장'}</button>
              {editingCustomerId !== null && <button className="secondary-button" type="button" onClick={cancelCustomerEdit}>수정 취소</button>}
            </form>
          </aside>
        </section>

        <section id="work-orders" className="panel work-order-panel">
          <div className="panel-heading work-order-heading">
            <div>
              <p className="eyebrow">WORK ORDER MODULE · APPROVAL GATED</p>
              <h3>접수 → 진행 → 승인 요청 → 승인 → 완료</h3>
              <p className="muted">IN_PROGRESS 업무는 직접 완료할 수 없으며 승인 절차를 거쳐야 합니다. 모든 상태 변경과 승인 결정은 서버 이력에 남습니다.</p>
            </div>
            <select value={statusFilter} onChange={(event) => void applyStatusFilter(event.target.value as WorkOrderStatus | '')} aria-label="업무 상태 필터">
              <option value="">전체 상태</option>
              <option value="RECEIVED">접수</option>
              <option value="IN_PROGRESS">진행</option>
              <option value="WAITING_APPROVAL">승인 대기</option>
              <option value="APPROVED">승인 완료</option>
              <option value="DONE">완료</option>
              <option value="CANCELLED">취소</option>
            </select>
          </div>

          {selectedCustomer && <div className="customer-work-filter"><span><strong>{selectedCustomer.companyName}</strong> 업무만 표시 중</span><button type="button" onClick={() => setSelectedCustomerId(null)}>고객 필터 해제</button></div>}

          <div className="work-order-layout">
            <div className="table-wrap">
              <table>
                <thead><tr><th>업무</th><th>고객</th><th>담당자</th><th>계획</th><th>상태</th><th>관리</th></tr></thead>
                <tbody>
                  {loading ? <tr><td colSpan={6} className="empty-state">불러오는 중...</td></tr> : visibleOrders.length === 0 ? <tr><td colSpan={6} className="empty-state">조건에 맞는 업무가 없습니다.</td></tr> : visibleOrders.map((order) => (
                    <tr key={order.id} className={selectedOrder?.id === order.id ? 'selected-row' : ''}>
                      <td><strong>{order.title}</strong><small className="cell-note">{formatDate(order.updatedAt)} 갱신</small></td>
                      <td><button className="text-button customer-link" onClick={() => setSelectedCustomerId(order.customerId)}>{order.customerName}</button></td>
                      <td>{order.assignee}</td>
                      <td><strong>{priorityLabels[order.priority]}</strong><small className="cell-note">{order.dueDate ?? '마감일 없음'}</small></td>
                      <td><span className={`work-status status-${order.status.toLowerCase()}`}>{statusLabels[order.status]}</span></td>
                      <td className="actions work-actions">
                        <button className="text-button history-button" onClick={() => void loadOrderActivities(order)}>이력</button>
                        {nextActions(order.status).map((action) => <button key={action.status} className={action.status === 'CANCELLED' ? 'text-button danger' : 'text-button'} onClick={() => void transitionOrder(order, action.status)}>{action.label}</button>)}
                        {order.status === 'IN_PROGRESS' && <span className="role-note">승인 요청 필요</span>}
                        {order.status === 'WAITING_APPROVAL' && <span className="role-note">관리자 결정 대기</span>}
                        {isTerminal(order.status) && <span className="terminal-state">종료</span>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <aside className="work-create">
              <p className="eyebrow">NEW WORK ORDER</p><h3>새 업무 접수</h3>
              <form className="editor-form" onSubmit={submitWorkOrder}>
                <label>업무명<input value={workForm.title} onChange={(event) => setWorkForm({ ...workForm, title: event.target.value })} maxLength={160} required /></label>
                <label>고객<select value={workForm.customerId || ''} onChange={(event) => setWorkForm({ ...workForm, customerId: Number(event.target.value) })} required><option value="">고객 선택</option>{customers.filter((customer) => customer.status !== 'INACTIVE').map((customer) => <option key={customer.id} value={customer.id}>{customer.companyName} · {customerStatusLabels[customer.status]}</option>)}</select></label>
                <label>담당자<input value={workForm.assignee} onChange={(event) => setWorkForm({ ...workForm, assignee: event.target.value })} maxLength={100} required /></label>
                <label>우선순위<select value={workForm.priority ?? 'NORMAL'} onChange={(event) => setWorkForm({ ...workForm, priority: event.target.value as WorkOrderPriority })}><option value="LOW">낮음</option><option value="NORMAL">보통</option><option value="HIGH">높음</option><option value="URGENT">긴급</option></select></label>
                <label>마감일<input type="date" value={workForm.dueDate ?? ''} onChange={(event) => setWorkForm({ ...workForm, dueDate: event.target.value || null })} /></label>
                <button className="primary-button" type="submit" disabled={workSaving}>{workSaving ? '접수 중...' : '업무 접수'}</button>
              </form>
            </aside>
          </div>

          <div className="activity-log">
            {!selectedOrder ? (
              <div className="activity-placeholder"><div><p className="eyebrow">AUDIT HISTORY</p><h3>업무의 변경 이력을 확인합니다.</h3></div><p>업무 행의 ‘이력’을 선택하면 생성, 상태 전이, 승인 요청과 승인/반려 사용자 및 시각을 확인할 수 있습니다.</p></div>
            ) : (
              <>
                <div className="activity-heading"><div><p className="eyebrow">AUDIT HISTORY · #{selectedOrder.id}</p><h3>{selectedOrder.title}</h3><p className="muted">{selectedOrder.customerName} · {selectedOrder.assignee}</p></div><button className="secondary-button compact-button" type="button" onClick={() => void loadOrderActivities(selectedOrder)}>새로고침</button></div>
                {activityLoading ? <p className="activity-empty">이력을 불러오는 중...</p> : activities.length === 0 ? <p className="activity-empty">기록된 이력이 없습니다.</p> : <div className="activity-list">{activities.map((activity) => <article className="activity-item" key={activity.id}><span className="activity-marker" /><div className="activity-content"><div className="activity-meta"><strong>{activity.actor}</strong><span>{formatDateTime(activity.createdAt)}</span><span>{activity.action}</span></div><h4>{activity.detail}</h4>{activity.toStatus && <div className="activity-transition">{activity.fromStatus && <span className={`work-status status-${activity.fromStatus.toLowerCase()}`}>{statusLabels[activity.fromStatus]}</span>}{activity.fromStatus && <span className="transition-arrow">→</span>}<span className={`work-status status-${activity.toStatus.toLowerCase()}`}>{statusLabels[activity.toStatus]}</span></div>}</div></article>)}</div>}
              </>
            )}
          </div>
        </section>

        <section id="directory" className="workspace-grid directory-section">
          <article className="panel directory-panel">
            <div className="panel-heading">
              <div><p className="eyebrow">DIRECTORY MODULE</p><h3>사용자·연락처 관리</h3></div>
              <form className="search-form" onSubmit={(event) => { event.preventDefault(); void loadUsers(keyword).catch((e) => setError(e instanceof Error ? e.message : '검색하지 못했습니다.')); }}><input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="이름 또는 이메일 검색" aria-label="검색어" /><button type="submit">검색</button></form>
            </div>
            <div className="table-wrap"><table><thead><tr><th>이름</th><th>이메일</th><th>등록일</th><th>관리</th></tr></thead><tbody>{loading ? <tr><td colSpan={4} className="empty-state">불러오는 중...</td></tr> : users.length === 0 ? <tr><td colSpan={4} className="empty-state">조건에 맞는 데이터가 없습니다.</td></tr> : users.map((user) => <tr key={user.id}><td><strong>{user.name}</strong></td><td>{user.email}</td><td>{formatDate(user.createdAt)}</td><td className="actions"><button className="text-button" onClick={() => startEdit(user)}>수정</button>{isAdmin ? <button className="text-button danger" onClick={() => void removeUser(user)}>삭제</button> : <span className="role-note">삭제는 ADMIN만</span>}</td></tr>)}</tbody></table></div>
          </article>
          <aside className="panel editor-panel"><p className="eyebrow">{editingId === null ? 'CREATE CONTACT' : 'UPDATE CONTACT'}</p><h3>{editingId === null ? '새 연락처 등록' : '연락처 수정'}</h3><p className="muted">브라우저 입력값은 Spring Validation과 DB 제약까지 다시 검증됩니다.</p><form className="editor-form" onSubmit={submitUser}><label>이름<input value={userForm.name} onChange={(event) => setUserForm({ ...userForm, name: event.target.value })} maxLength={100} required /></label><label>이메일<input type="email" value={userForm.email} onChange={(event) => setUserForm({ ...userForm, email: event.target.value })} maxLength={255} required /></label><button className="primary-button" type="submit" disabled={saving}>{saving ? '저장 중...' : editingId === null ? '등록하기' : '변경 저장'}</button>{editingId !== null && <button className="secondary-button" type="button" onClick={cancelEdit}>수정 취소</button>}</form></aside>
        </section>

        <section id="delivery" className="delivery-panel">
          <div><p className="eyebrow">CLIENT VALUE</p><h3>V8이 증명하는 외주 범위</h3></div>
          <ul>
            <li>React/TypeScript 관리자 화면</li>
            <li>Spring Boot + MyBatis REST API</li>
            <li>고객/거래처 CRM과 관계형 업무 설계</li>
            <li>우선순위·마감일·서버 Analytics</li>
            <li>Spring Security ADMIN/STAFF RBAC</li>
            <li>승인 게이트 + 트랜잭션 Audit Log</li>
            <li>기간별 CSV/XLSX 운영 리포트</li>
          </ul>
        </section>
      </main>
    </div>
  );
}
