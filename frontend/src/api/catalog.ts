import apiClient from './client';
import type { Product, ProductRequest, PageResponse, Category, CategoryRequest } from '../types';

export const productApi = {
  getAll: async (page = 0, size = 10): Promise<PageResponse<Product>> => {
    const response = await apiClient.get<PageResponse<Product>>('/products', {
      params: { page, size },
    });
    return response.data;
  },

  getById: async (id: number): Promise<Product> => {
    const response = await apiClient.get<Product>(`/products/${id}`);
    return response.data;
  },

  getByBarcode: async (barcode: string): Promise<Product> => {
    const response = await apiClient.get<Product>(`/products/barcode/${barcode}`);
    return response.data;
  },

  create: async (product: ProductRequest): Promise<Product> => {
    const response = await apiClient.post<Product>('/products', product);
    return response.data;
  },

  update: async (id: number, product: ProductRequest): Promise<Product> => {
    const response = await apiClient.put<Product>(`/products/${id}`, product);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/products/${id}`);
  },
};

export const categoryApi = {
  getAll: async (): Promise<Category[]> => {
    const response = await apiClient.get<Category[]>('/categories');
    return response.data;
  },

  getTree: async (): Promise<Category[]> => {
    const response = await apiClient.get<Category[]>('/categories/tree');
    return response.data;
  },

  getById: async (id: number): Promise<Category> => {
    const response = await apiClient.get<Category>(`/categories/${id}`);
    return response.data;
  },

  create: async (category: CategoryRequest): Promise<Category> => {
    const response = await apiClient.post<Category>('/categories', category);
    return response.data;
  },

  update: async (id: number, category: CategoryRequest): Promise<Category> => {
    const response = await apiClient.put<Category>(`/categories/${id}`, category);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/categories/${id}`);
  },
};
