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

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers
    },
    ...options
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
