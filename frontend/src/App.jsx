import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AuthProvider, { useAuth } from './context/AuthContext';
import Login from './pages/Login';
import Register from './pages/Register';
import ConsumerLayout from './layouts/ConsumerLayout';
import PartnerLayout from './layouts/PartnerLayout';
import Products from './pages/consumer/Products';
import Checkout from './pages/consumer/Checkout';
import Tracking from './pages/consumer/Tracking';
import Support from './pages/consumer/Support';
import AvailableOrders from './pages/partner/AvailableOrders';
import ActiveOrder from './pages/partner/ActiveOrder';

const ProtectedRoute = ({ children, roleRequired }) => {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" />;
  if (roleRequired && user.role !== roleRequired) {
    return <Navigate to={user.role === 'CUSTOMER' ? '/consumer' : '/partner'} />;
  }
  return children;
};

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          
          <Route path="/consumer" element={<ProtectedRoute roleRequired="CUSTOMER"><ConsumerLayout /></ProtectedRoute>}>
            <Route index element={<Products />} />
            <Route path="checkout" element={<Checkout />} />
            <Route path="tracking/:id" element={<Tracking />} />
            <Route path="support" element={<Support />} />
          </Route>
          
          <Route path="/partner" element={<ProtectedRoute roleRequired="RIDER"><PartnerLayout /></ProtectedRoute>}>
            <Route index element={<AvailableOrders />} />
            <Route path="order/:id" element={<ActiveOrder />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
