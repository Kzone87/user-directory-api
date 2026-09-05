(() => {
    const state = {
        page: 0,
        size: 20,
        sort: 'id',
        direction: 'asc',
        keyword: '',
        emailDomain: '',
        totalPages: 0,
        totalElements: 0,
        editingId: null,
    };

    const elements = {
        health: document.getElementById('healthState'),
        totalUsers: document.getElementById('totalUsers'),
        currentPage: document.getElementById('currentPage'),
        currentSize: document.getElementById('currentSize'),
        notice: document.getElementById('notice'),
        searchForm: document.getElementById('searchForm'),
        keyword: document.getElementById('keywordInput'),
        domain: document.getElementById('domainInput'),
        sort: document.getElementById('sortInput'),
        direction: document.getElementById('directionInput'),
        size: document.getElementById('sizeInput'),
        resetSearch: document.getElementById('resetSearchButton'),
        exportLink: document.getElementById('exportLink'),
        tableBody: document.getElementById('userTableBody'),
        emptyState: document.getElementById('emptyState'),
        prev: document.getElementById('prevPageButton'),
        next: document.getElementById('nextPageButton'),
        pageLabel: document.getElementById('pageLabel'),
        userForm: document.getElementById('userForm'),
        editorTitle: document.getElementById('editorTitle'),
        editingId: document.getElementById('editingId'),
        name: document.getElementById('nameInput'),
        email: document.getElementById('emailInput'),
        nameError: document.getElementById('nameError'),
        emailError: document.getElementById('emailError'),
        save: document.getElementById('saveButton'),
        cancelEdit: document.getElementById('cancelEditButton'),
    };

    function setNotice(message, kind = 'info') {
        elements.notice.textContent = message;
        elements.notice.dataset.kind = kind;
    }

    async function request(url, options = {}) {
        const response = await fetch(url, {
            headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
            ...options,
        });

        const contentType = response.headers.get('content-type') || '';
        const payload = contentType.includes('application/json')
            ? await response.json()
            : await response.text();

        if (!response.ok) {
            const error = new Error(payload?.message || `Request failed (${response.status})`);
            error.status = response.status;
            error.payload = payload;
            throw error;
        }

        return payload;
    }

    function buildPageUrl() {
        const params = new URLSearchParams({
            page: String(state.page),
            size: String(state.size),
            sort: state.sort,
            direction: state.direction,
        });
        if (state.keyword) params.set('keyword', state.keyword);
        if (state.emailDomain) params.set('emailDomain', state.emailDomain);
        return `/api/users/page?${params.toString()}`;
    }

    function updateExportLink() {
        const params = new URLSearchParams();
        if (state.keyword) params.set('keyword', state.keyword);
        if (state.emailDomain) params.set('emailDomain', state.emailDomain);
        elements.exportLink.href = params.size > 0
            ? `/api/users/export?${params.toString()}`
            : '/api/users/export';
    }

    function formatCreatedAt(value) {
        if (!value) return '–';
        const date = new Date(value);
        return Number.isNaN(date.getTime()) ? value : date.toLocaleString('ko-KR');
    }

    function createCell(text) {
        const cell = document.createElement('td');
        cell.textContent = text;
        return cell;
    }

    function renderRows(users) {
        const fragment = document.createDocumentFragment();

        for (const user of users) {
            const row = document.createElement('tr');
            row.appendChild(createCell(String(user.id)));
            row.appendChild(createCell(user.name));
            row.appendChild(createCell(user.email));
            row.appendChild(createCell(formatCreatedAt(user.createdAt)));

            const actionCell = document.createElement('td');
            const actions = document.createElement('div');
            actions.className = 'row-actions';

            const editButton = document.createElement('button');
            editButton.type = 'button';
            editButton.textContent = '수정';
            editButton.dataset.action = 'edit';
            editButton.addEventListener('click', () => beginEdit(user.id));

            const deleteButton = document.createElement('button');
            deleteButton.type = 'button';
            deleteButton.textContent = '삭제';
            deleteButton.dataset.action = 'delete';
            deleteButton.addEventListener('click', () => deleteUser(user));

            actions.append(editButton, deleteButton);
            actionCell.appendChild(actions);
            row.appendChild(actionCell);
            fragment.appendChild(row);
        }

        elements.tableBody.replaceChildren(fragment);
        elements.emptyState.hidden = users.length > 0;
    }

    function renderPagination() {
        const visiblePage = state.totalPages === 0 ? 0 : state.page + 1;
        elements.pageLabel.textContent = `${visiblePage} / ${state.totalPages}`;
        elements.prev.disabled = state.page <= 0;
        elements.next.disabled = state.totalPages === 0 || state.page >= state.totalPages - 1;
        elements.totalUsers.textContent = state.totalElements.toLocaleString();
        elements.currentPage.textContent = String(visiblePage);
        elements.currentSize.textContent = String(state.size);
    }

    async function loadUsers({ quiet = false } = {}) {
        if (!quiet) setNotice('사용자 목록을 불러오는 중입니다.', 'working');
        try {
            const page = await request(buildPageUrl());
            state.totalPages = page.totalPages;
            state.totalElements = page.totalElements;

            if (state.page > 0 && state.page >= page.totalPages && page.totalPages > 0) {
                state.page = page.totalPages - 1;
                return loadUsers({ quiet });
            }

            renderRows(page.content);
            renderPagination();
            updateExportLink();
            if (!quiet) {
                setNotice(`사용자 ${page.totalElements.toLocaleString()}건 중 ${page.content.length}건을 표시했습니다.`, 'success');
            }
        } catch (error) {
            console.error(error);
            renderRows([]);
            setNotice(error.message || '사용자 목록을 불러오지 못했습니다.', 'error');
        }
    }

    async function checkHealth() {
        try {
            const health = await request('/actuator/health', { headers: {} });
            elements.health.textContent = health.status || 'UP';
            elements.health.dataset.state = 'up';
        } catch (error) {
            elements.health.textContent = 'DOWN';
            elements.health.dataset.state = 'down';
        }
    }

    function readSearchForm() {
        state.keyword = elements.keyword.value.trim();
        state.emailDomain = elements.domain.value.trim();
        state.sort = elements.sort.value;
        state.direction = elements.direction.value;
        state.size = Number(elements.size.value);
        state.page = 0;
    }

    function clearFieldErrors() {
        elements.nameError.textContent = '';
        elements.emailError.textContent = '';
    }

    function showValidationErrors(payload) {
        clearFieldErrors();
        if (!payload?.fieldErrors) return;
        elements.nameError.textContent = payload.fieldErrors.name || '';
        elements.emailError.textContent = payload.fieldErrors.email || '';
    }

    function resetEditor() {
        state.editingId = null;
        elements.editingId.value = '';
        elements.userForm.reset();
        clearFieldErrors();
        elements.editorTitle.textContent = '사용자 등록';
        elements.save.textContent = '등록';
        elements.cancelEdit.hidden = true;
    }

    async function beginEdit(id) {
        setNotice(`사용자 #${id} 정보를 불러오는 중입니다.`, 'working');
        try {
            const user = await request(`/api/users/${id}`);
            state.editingId = user.id;
            elements.editingId.value = String(user.id);
            elements.name.value = user.name;
            elements.email.value = user.email;
            clearFieldErrors();
            elements.editorTitle.textContent = `사용자 #${user.id} 수정`;
            elements.save.textContent = '변경 저장';
            elements.cancelEdit.hidden = false;
            elements.name.focus();
            setNotice(`사용자 #${id}을 수정 모드로 열었습니다.`);
        } catch (error) {
            console.error(error);
            setNotice(error.message || '사용자 정보를 불러오지 못했습니다.', 'error');
        }
    }

    async function saveUser(event) {
        event.preventDefault();
        clearFieldErrors();

        const body = {
            name: elements.name.value.trim(),
            email: elements.email.value.trim(),
        };

        const editing = state.editingId !== null;
        const url = editing ? `/api/users/${state.editingId}` : '/api/users';
        const method = editing ? 'PUT' : 'POST';
        setNotice(editing ? '사용자 변경사항을 저장하는 중입니다.' : '새 사용자를 등록하는 중입니다.', 'working');
        elements.save.disabled = true;

        try {
            const saved = await request(url, { method, body: JSON.stringify(body) });
            resetEditor();
            await loadUsers({ quiet: true });
            setNotice(`${saved.name} 사용자를 ${editing ? '수정' : '등록'}했습니다.`, 'success');
        } catch (error) {
            console.error(error);
            showValidationErrors(error.payload);
            setNotice(error.message || '사용자를 저장하지 못했습니다.', 'error');
        } finally {
            elements.save.disabled = false;
        }
    }

    async function deleteUser(user) {
        if (!window.confirm(`${user.name} (${user.email}) 사용자를 삭제할까요?`)) return;
        setNotice(`사용자 #${user.id}을 삭제하는 중입니다.`, 'working');
        try {
            await request(`/api/users/${user.id}`, { method: 'DELETE' });
            if (state.editingId === user.id) resetEditor();
            await loadUsers({ quiet: true });
            setNotice(`${user.name} 사용자를 삭제했습니다.`, 'success');
        } catch (error) {
            console.error(error);
            setNotice(error.message || '사용자를 삭제하지 못했습니다.', 'error');
        }
    }

    elements.searchForm.addEventListener('submit', event => {
        event.preventDefault();
        readSearchForm();
        loadUsers();
    });

    elements.resetSearch.addEventListener('click', () => {
        elements.searchForm.reset();
        state.page = 0;
        state.size = 20;
        state.sort = 'id';
        state.direction = 'asc';
        state.keyword = '';
        state.emailDomain = '';
        loadUsers();
    });

    elements.prev.addEventListener('click', () => {
        if (state.page <= 0) return;
        state.page -= 1;
        loadUsers();
    });

    elements.next.addEventListener('click', () => {
        if (state.totalPages === 0 || state.page >= state.totalPages - 1) return;
        state.page += 1;
        loadUsers();
    });

    elements.userForm.addEventListener('submit', saveUser);
    elements.cancelEdit.addEventListener('click', resetEditor);

    checkHealth();
    loadUsers();
})();
