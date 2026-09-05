export type DirectoryUser = {
  id: number;
  name: string;
  email: string;
  createdAt: string;
};

export type UserInput = {
  name: string;
  email: string;
};

export type CustomerStatus = 'LEAD' | 'ACTIVE' | 'INACTIVE';

export type Customer = {
  id: number;
  companyName: string;
  contactName: string | null;
  email: string | null;
  phone: string | null;
  status: CustomerStatus;
  memo: string | null;
  createdAt: string;
  updatedAt: string;
};

export type CustomerInput = {
  companyName: string;
  contactName: string;
  email: string;
  phone: string;
  status: CustomerStatus;
  memo: string;
};

export type WorkOrderStatus = 'RECEIVED' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED';

export type WorkOrder = {
  id: number;
  title: string;
  customerId: number;
  customerName: string;
  assignee: string;
  status: WorkOrderStatus;
  createdAt: string;
  updatedAt: string;
};

export type WorkOrderInput = {
  title: string;
  customerId: number;
  assignee: string;
};

export type WorkOrderActivity = {
  id: number;
  workOrderId: number;
  actor: string;
  action: 'CREATED' | 'STATUS_CHANGED';
  fromStatus: WorkOrderStatus | null;
  toStatus: WorkOrderStatus | null;
  detail: string;
  createdAt: string;
};

export type AuthMe = {
  username: string;
  roles: string[];
};

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';
let authorizationHeader: string | null = null;

export function setBasicCredentials(username: string, password: string) {
  authorizationHeader = `Basic ${btoa(`${username}:${password}`)}`;
}

export function clearCredentials() {
  authorizationHeader = null;
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json'
  };
  if (authorizationHeader) headers.Authorization = authorizationHeader;

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      ...headers,
      ...options?.headers
    }
  });

  if (!response.ok) {
    let message = `요청에 실패했습니다. (${response.status})`;
    try {
      const body = await response.json();
      message = body.message ?? body.error ?? message;
    } catch {
      // keep fallback message
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export function getCurrentUser(): Promise<AuthMe> {
  return request<AuthMe>('/api/auth/me');
}

export function listCustomers(keyword = '', status?: CustomerStatus): Promise<Customer[]> {
  const params = new URLSearchParams();
  if (keyword.trim()) params.set('keyword', keyword.trim());
  if (status) params.set('status', status);
  const query = params.toString() ? `?${params.toString()}` : '';
  return request<Customer[]>(`/api/customers${query}`);
}

export function createCustomer(input: CustomerInput): Promise<Customer> {
  return request<Customer>('/api/customers', {
    method: 'POST',
    body: JSON.stringify(input)
  });
}

export function updateCustomer(id: number, input: CustomerInput): Promise<Customer> {
  return request<Customer>(`/api/customers/${id}`, {
    method: 'PUT',
    body: JSON.stringify(input)
  });
}

export function listUsers(keyword = ''): Promise<DirectoryUser[]> {
  const query = keyword.trim() ? `?keyword=${encodeURIComponent(keyword.trim())}` : '';
  return request<DirectoryUser[]>(`/api/users${query}`);
}

export function createUser(input: UserInput): Promise<DirectoryUser> {
  return request<DirectoryUser>('/api/users', {
    method: 'POST',
    body: JSON.stringify(input)
  });
}

export function updateUser(id: number, input: UserInput): Promise<DirectoryUser> {
  return request<DirectoryUser>(`/api/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(input)
  });
}

export function deleteUser(id: number): Promise<void> {
  return request<void>(`/api/users/${id}`, { method: 'DELETE' });
}

export function listWorkOrders(status?: WorkOrderStatus): Promise<WorkOrder[]> {
  const query = status ? `?status=${encodeURIComponent(status)}` : '';
  return request<WorkOrder[]>(`/api/work-orders${query}`);
}

export function listWorkOrderActivities(id: number): Promise<WorkOrderActivity[]> {
  return request<WorkOrderActivity[]>(`/api/work-orders/${id}/activities`);
}

export function createWorkOrder(input: WorkOrderInput): Promise<WorkOrder> {
  return request<WorkOrder>('/api/work-orders', {
    method: 'POST',
    body: JSON.stringify(input)
  });
}

export function updateWorkOrderStatus(id: number, status: WorkOrderStatus): Promise<WorkOrder> {
  return request<WorkOrder>(`/api/work-orders/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  });
}
