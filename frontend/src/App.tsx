import { FormEvent, useEffect, useMemo, useState } from 'react';
import {
  createUser,
  deleteUser,
  DirectoryUser,
  listUsers,
  updateUser,
  UserInput
} from './api';

const emptyForm: UserInput = { name: '', email: '' };

function formatDate(value: string) {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(new Date(value));
}

export default function App() {
  const [users, setUsers] = useState<DirectoryUser[]>([]);
  const [keyword, setKeyword] = useState('');
  const [form, setForm] = useState<UserInput>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const domains = useMemo(
    () => new Set(users.map((user) => user.email.split('@')[1]).filter(Boolean)).size,
    [users]
  );

  const newest = useMemo(() => {
    return [...users].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )[0];
  }, [users]);

  async function load(search = keyword) {
    setLoading(true);
    setError('');
    try {
      setUsers(await listUsers(search));
    } catch (e) {
      setError(e instanceof Error ? e.message : '데이터를 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load('');
  }, []);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const payload = { name: form.name.trim(), email: form.email.trim() };
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
      setForm(emptyForm);
      setEditingId(null);
      await load(keyword);
    } catch (e) {
      setError(e instanceof Error ? e.message : '저장하지 못했습니다.');
    } finally {
      setSaving(false);
    }
  }

  function startEdit(user: DirectoryUser) {
    setEditingId(user.id);
    setForm({ name: user.name, email: user.email });
    setError('');
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(emptyForm);
  }

  async function remove(user: DirectoryUser) {
    if (!window.confirm(`${user.name} 항목을 삭제하시겠습니까?`)) return;
    setError('');
    try {
      await deleteUser(user.id);
      if (editingId === user.id) cancelEdit();
      await load(keyword);
    } catch (e) {
      setError(e instanceof Error ? e.message : '삭제하지 못했습니다.');
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div>
          <p className="brand-mark">KZ</p>
          <h1>Business Ops</h1>
          <p className="sidebar-copy">외주형 업무관리 시스템 데모</p>
        </div>
        <nav>
          <a className="active" href="#dashboard">Dashboard</a>
          <a href="#directory">Directory</a>
          <a href="#delivery">Delivery scope</a>
        </nav>
        <div className="sidebar-note">
          <strong>V1</strong>
          <span>React UI ↔ Spring REST API</span>
        </div>
      </aside>

      <main>
        <header className="topbar">
          <div>
            <p className="eyebrow">FULL-STACK PORTFOLIO CASE</p>
            <h2>운영 데이터를 한 화면에서 관리합니다.</h2>
          </div>
          <span className="status-pill">API CONNECTED</span>
        </header>

        <section id="dashboard" className="metrics" aria-label="운영 현황">
          <article>
            <span>등록 항목</span>
            <strong>{users.length}</strong>
            <small>현재 검색 결과 기준</small>
          </article>
          <article>
            <span>이메일 도메인</span>
            <strong>{domains}</strong>
            <small>연락처 데이터 분포</small>
          </article>
          <article>
            <span>최근 등록</span>
            <strong className="metric-text">{newest?.name ?? '-'}</strong>
            <small>{newest ? formatDate(newest.createdAt) : '데이터 없음'}</small>
          </article>
        </section>

        <section id="directory" className="workspace-grid">
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
                  void load(keyword);
                }}
              >
                <input
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                  placeholder="이름 또는 이메일 검색"
                  aria-label="검색어"
                />
                <button type="submit">검색</button>
              </form>
            </div>

            {error && <p className="error-box">{error}</p>}

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
                          <button className="text-button danger" onClick={() => void remove(user)}>삭제</button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </article>

          <aside className="panel editor-panel">
            <p className="eyebrow">{editingId === null ? 'CREATE' : 'UPDATE'}</p>
            <h3>{editingId === null ? '새 항목 등록' : '항목 수정'}</h3>
            <p className="muted">브라우저 입력값은 Spring Validation과 DB unique 제약까지 다시 검증됩니다.</p>

            <form className="editor-form" onSubmit={submit}>
              <label>
                이름
                <input
                  value={form.name}
                  onChange={(event) => setForm({ ...form, name: event.target.value })}
                  maxLength={100}
                  required
                />
              </label>
              <label>
                이메일
                <input
                  type="email"
                  value={form.email}
                  onChange={(event) => setForm({ ...form, email: event.target.value })}
                  maxLength={255}
                  required
                />
              </label>
              <button className="primary-button" type="submit" disabled={saving}>
                {saving ? '저장 중...' : editingId === null ? '등록하기' : '변경 저장'}
              </button>
              {editingId !== null && (
                <button className="secondary-button" type="button" onClick={cancelEdit}>수정 취소</button>
              )}
            </form>
          </aside>
        </section>

        <section id="delivery" className="delivery-panel">
          <div>
            <p className="eyebrow">CLIENT VALUE</p>
            <h3>이 모듈이 증명하는 외주 범위</h3>
          </div>
          <ul>
            <li>React/TypeScript 관리 화면</li>
            <li>Spring Boot REST API 연동</li>
            <li>검색·CRUD·입력 검증</li>
            <li>오류 상태 사용자 피드백</li>
            <li>DB unique 제약과 API 계약</li>
          </ul>
        </section>
      </main>
    </div>
  );
}
