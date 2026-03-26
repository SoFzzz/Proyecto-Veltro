// API Response wrapper
export interface ApiResponse<T> {
  data: T;
  message?: string;
  timestamp: string;
}

// Paginated response
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
  first: boolean;
  last: boolean;
}

// User roles
export type UserRole = 'ADMIN' | 'CASHIER' | 'WAREHOUSE';

// Auth types
export interface User {
  id: number;
  username: string;
  email: string;
  role: UserRole;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface RefreshResponse {
  accessToken: string;
  refreshToken: string;
}

// Product types
export interface Product {
  id: number;
  name: string;
  barcode: string | null;
  sku: string | null;
  description: string | null;
  costPrice: string;
  salePrice: string;
  categoryId: number | null;
  categoryName: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string | null;
}

export interface ProductRequest {
  name: string;
  barcode?: string;
  sku?: string;
  description?: string;
  costPrice: string;
  salePrice: string;
  categoryId?: number;
}

// Category types
export interface Category {
  id: number;
  name: string;
  description: string | null;
  parentCategoryId: number | null;
  parentCategoryName: string | null;
  active: boolean;
  children?: Category[];
}

export interface CategoryRequest {
  name: string;
  description?: string;
  parentCategoryId?: number;
}

// Inventory types
export interface Inventory {
  id: number;
  productId: number;
  productName: string;
  currentStock: number;
  minStock: number;
  maxStock: number;
}

// Error response
export interface ApiError {
  message: string;
  code: string;
  timestamp: string;
  details?: Record<string, string>;
}
