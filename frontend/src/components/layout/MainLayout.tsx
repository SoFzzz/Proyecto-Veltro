import { Link, useNavigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';

export function MainLayout() {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getRoleLabel = (role: string): string => {
    switch (role) {
      case 'ADMIN':
        return 'Administrador';
      case 'CASHIER':
        return 'Cajero';
      case 'WAREHOUSE':
        return 'Almacén';
      default:
        return role;
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-8">
              <Link to="/" className="text-xl font-bold text-blue-600">
                Veltro
              </Link>
              <nav className="hidden md:flex space-x-4">
                {user?.role === 'ADMIN' && (
                  <>
                    <Link
                      to="/dashboard"
                      className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                    >
                      Panel
                    </Link>
                    <Link
                      to="/products"
                      className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                    >
                      Productos
                    </Link>
                    <Link
                      to="/categories"
                      className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                    >
                      Categorías
                    </Link>
                    <Link
                      to="/inventory"
                      className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                    >
                      Inventario
                    </Link>
                  </>
                )}
                {user?.role === 'WAREHOUSE' && (
                  <>
                    <Link
                      to="/products"
                      className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                    >
                      Productos
                    </Link>
                    <Link
                      to="/categories"
                      className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                    >
                      Categorías
                    </Link>
                    <Link
                      to="/inventory"
                      className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                    >
                      Inventario
                    </Link>
                    <Link
                      to="/purchase-orders"
                      className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                    >
                      Órdenes de Compra
                    </Link>
                  </>
                )}
                {user?.role === 'CASHIER' && (
                  <>
                    <Link
                      to="/pos"
                      className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                    >
                      Punto de Venta
                    </Link>
                    <Link
                      to="/products"
                      className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
                    >
                      Productos
                    </Link>
                  </>
                )}
              </nav>
            </div>
            <div className="flex items-center space-x-4">
              <div className="text-sm">
                <span className="text-gray-500">Usuario: </span>
                <span className="font-medium text-gray-900">{user?.username}</span>
                <span className="ml-2 px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded">
                  {user && getRoleLabel(user.role)}
                </span>
              </div>
              <button
                onClick={handleLogout}
                className="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
              >
                Cerrar Sesión
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Outlet />
      </main>
    </div>
  );
}
