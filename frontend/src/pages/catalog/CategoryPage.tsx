import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { categoryApi } from '../../api/catalog';
import type { Category } from '../../types';
import { CategoryTree } from '../../components/catalog/CategoryTree';
import { useAuthStore } from '../../stores/authStore';
import type { AxiosError } from 'axios';

const categorySchema = z.object({
  name: z.string().min(1, 'El nombre es requerido'),
  description: z.string().optional(),
  parentCategoryId: z.string().optional(),
});

type CategoryFormData = z.infer<typeof categorySchema>;

export function CategoryPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [flatCategories, setFlatCategories] = useState<Category[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [showForm, setShowForm] = useState(false);
  const { hasRole } = useAuthStore();

  const canEdit = hasRole(['ADMIN', 'WAREHOUSE']);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CategoryFormData>({
    resolver: zodResolver(categorySchema),
  });

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    setIsLoading(true);
    try {
      const [tree, flat] = await Promise.all([
        categoryApi.getTree(),
        categoryApi.getAll(),
      ]);
      setCategories(tree);
      setFlatCategories(flat);
    } catch (err) {
      setError('Error al cargar las categorías');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleNew = () => {
    setEditingCategory(null);
    reset({ name: '', description: '', parentCategoryId: '' });
    setShowForm(true);
  };

  const handleEdit = (category: Category) => {
    setEditingCategory(category);
    reset({
      name: category.name,
      description: category.description || '',
      parentCategoryId: category.parentCategoryId?.toString() || '',
    });
    setShowForm(true);
  };

  const handleDelete = async (category: Category) => {
    if (!window.confirm(`¿Está seguro de desactivar la categoría "${category.name}"?`)) {
      return;
    }
    try {
      await categoryApi.delete(category.id);
      loadCategories();
    } catch (err) {
      setError('Error al desactivar la categoría');
      console.error(err);
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingCategory(null);
    reset();
  };

  const onSubmit = async (data: CategoryFormData) => {
    setIsSaving(true);
    setError(null);

    const categoryData = {
      name: data.name,
      description: data.description || undefined,
      parentCategoryId: data.parentCategoryId ? parseInt(data.parentCategoryId) : undefined,
    };

    try {
      if (editingCategory) {
        await categoryApi.update(editingCategory.id, categoryData);
      } else {
        await categoryApi.create(categoryData);
      }
      setShowForm(false);
      setEditingCategory(null);
      reset();
      loadCategories();
    } catch (err) {
      const axiosError = err as AxiosError<{ message: string }>;
      setError(axiosError.response?.data?.message || 'Error al guardar la categoría');
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Cargando categorías...</div>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      {/* Category Tree */}
      <div>
        <div className="flex justify-between items-center mb-4">
          <h1 className="text-2xl font-bold text-gray-900">Categorías</h1>
          {canEdit && (
            <button
              onClick={handleNew}
              className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
            >
              Nueva Categoría
            </button>
          )}
        </div>

        {error && !showForm && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        <div className="bg-white shadow rounded-lg p-4">
          <CategoryTree
            categories={categories}
            onEdit={canEdit ? handleEdit : undefined}
            onDelete={canEdit ? handleDelete : undefined}
          />
        </div>
      </div>

      {/* Category Form */}
      {showForm && canEdit && (
        <div>
          <h2 className="text-xl font-bold text-gray-900 mb-4">
            {editingCategory ? 'Editar Categoría' : 'Nueva Categoría'}
          </h2>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="bg-white shadow rounded-lg p-6 space-y-4">
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700">
                Nombre *
              </label>
              <input
                id="name"
                type="text"
                {...register('name')}
                className={`mt-1 block w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                  errors.name ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Nombre de la categoría"
              />
              {errors.name && (
                <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                Descripción
              </label>
              <textarea
                id="description"
                rows={3}
                {...register('description')}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Descripción de la categoría"
              />
            </div>

            <div>
              <label htmlFor="parentCategoryId" className="block text-sm font-medium text-gray-700">
                Categoría Padre
              </label>
              <select
                id="parentCategoryId"
                {...register('parentCategoryId')}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">Sin categoría padre (raíz)</option>
                {flatCategories
                  .filter((c) => c.id !== editingCategory?.id)
                  .map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
              </select>
            </div>

            <div className="flex justify-end space-x-3 pt-4 border-t">
              <button
                type="button"
                onClick={handleCancel}
                className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={isSaving}
                className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isSaving ? 'Guardando...' : editingCategory ? 'Actualizar' : 'Crear'}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
