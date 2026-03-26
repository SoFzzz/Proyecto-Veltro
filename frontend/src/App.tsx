import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthGuard, RoleGuard } from './components/auth';
import { MainLayout } from './components/layout';
import { LoginPage } from './pages/auth';
import { ProductListPage, ProductFormPage, CategoryPage } from './pages/catalog';
import { UnauthorizedPage, NotFoundPage } from './pages/ErrorPages';

// Placeholder pages for future phases
function DashboardPage() {
  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-gray-900">Panel de Control</h1>
      <p className="text-gray-600 mt-2">Bienvenido al sistema Veltro ERP/POS</p>
    </div>
  );
}

function PosPage() {
  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-gray-900">Punto de Venta</h1>
      <p className="text-gray-600 mt-2">Módulo de ventas (Fase 2)</p>
    </div>
  );
}

function InventoryPage() {
  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-gray-900">Inventario</h1>
      <p className="text-gray-600 mt-2">Gestión de inventario (Fase 2)</p>
    </div>
  );
}

function App() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/unauthorized" element={<UnauthorizedPage />} />

      {/* Protected routes */}
      <Route
        path="/"
        element={
          <AuthGuard>
            <MainLayout />
          </AuthGuard>
        }
      >
        {/* Default redirect based on role handled by LoginPage */}
        <Route index element={<Navigate to="/dashboard" replace />} />

        {/* Dashboard - All authenticated users */}
        <Route path="dashboard" element={<DashboardPage />} />

        {/* POS - ADMIN and CASHIER only */}
        <Route
          path="pos"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'CASHIER']}>
              <PosPage />
            </RoleGuard>
          }
        />

        {/* Inventory - ADMIN and WAREHOUSE only */}
        <Route
          path="inventory"
          element={
            <RoleGuard allowedRoles={['ADMIN', 'WAREHOUSE']}>
              <InventoryPage />
            </RoleGuard>
          }
        />

        {/* Catalog - ADMIN only */}
        <Route
          path="catalog"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <Navigate to="/catalog/products" replace />
            </RoleGuard>
          }
        />
        <Route
          path="catalog/products"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <ProductListPage />
            </RoleGuard>
          }
        />
        <Route
          path="catalog/products/new"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <ProductFormPage />
            </RoleGuard>
          }
        />
        <Route
          path="catalog/products/:id/edit"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <ProductFormPage />
            </RoleGuard>
          }
        />
        <Route
          path="catalog/categories"
          element={
            <RoleGuard allowedRoles={['ADMIN']}>
              <CategoryPage />
            </RoleGuard>
          }
        />
      </Route>

      {/* 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

export default App;
