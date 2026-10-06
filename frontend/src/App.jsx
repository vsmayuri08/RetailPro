import { Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import SuperAdminDashboard from './pages/SuperAdminDashboard';
import Branches from './pages/Branches';
import Categories from './pages/Categories';
import Suppliers from './pages/Suppliers';
import Users from './pages/Users';
import Staff from './pages/Staff';
import BillingSettings from './pages/BillingSettings';
import ExpiryDiscountRules from './pages/ExpiryDiscountRules';
import Reports from './pages/Reports';
import BranchManagerDashboard from './pages/BranchManagerDashboard';
import Products from './pages/Products';
import Inventory from './pages/Inventory';
import Customers from './pages/Customers';
import Employees from './pages/Employees';
import Sales from './pages/Sales';
import AbcAnalysis from './pages/AbcAnalysis';
import DemandForecast from './pages/DemandForecast';
import MarketBasketAnalysis from './pages/MarketBasketAnalysis';
import RfmSegmentation from './pages/RfmSegmentation';
import ReorderSuggestions from './pages/ReorderSuggestions';
import PurchaseOrders from './pages/PurchaseOrders';
import CashierDashboard from './pages/CashierDashboard';
import NewSale from './pages/NewSale';
import Bills from './pages/Bills';
import Returns from './pages/Returns';
import DigitalReceipt from './pages/DigitalReceipt';
import CashierLeaderboard from './pages/CashierLeaderboard';
import ActivityLogs from './pages/ActivityLogs';

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/receipt/:invoiceNumber" element={<DigitalReceipt />} />

      {/* Super Admin Routes */}
      <Route path="/admin" element={
        <ProtectedRoute roles={['SUPER_ADMIN']}>
          <Layout />
        </ProtectedRoute>
      }>
        <Route path="dashboard" element={<SuperAdminDashboard />} />
        <Route path="branches" element={<Branches />} />
        <Route path="categories" element={<Categories />} />
        <Route path="suppliers" element={<Suppliers />} />
        <Route path="users" element={<Users />} />
        <Route path="staff" element={<Staff />} />
        <Route path="billing-settings" element={<BillingSettings />} />
        <Route path="expiry-discounts" element={<ExpiryDiscountRules />} />
        <Route path="reports" element={<Reports />} />
        <Route path="cashier-leaderboard" element={<CashierLeaderboard />} />
        <Route path="activity-log" element={<ActivityLogs />} />
        <Route index element={<Navigate to="dashboard" replace />} />
      </Route>

      {/* Branch Manager Routes */}
      <Route path="/manager" element={
        <ProtectedRoute roles={['BRANCH_MANAGER']}>
          <Layout />
        </ProtectedRoute>
      }>
        <Route path="dashboard" element={<BranchManagerDashboard />} />
        <Route path="products" element={<Products />} />
        <Route path="inventory" element={<Inventory />} />
        <Route path="customers" element={<Customers />} />
        <Route path="employees" element={<Employees />} />
        <Route path="staff" element={<Staff />} />
        <Route path="sales" element={<Sales />} />
        <Route path="abc-analysis" element={<AbcAnalysis />} />
        <Route path="demand-forecast" element={<DemandForecast />} />
        <Route path="reports/market-basket" element={<MarketBasketAnalysis />} />
        <Route path="rfm-segmentation" element={<RfmSegmentation />} />
        <Route path="reorder-suggestions" element={<ReorderSuggestions />} />
        <Route path="purchase-orders" element={<PurchaseOrders />} />
        <Route path="cashier-leaderboard" element={<CashierLeaderboard />} />
        <Route path="activity-log" element={<ActivityLogs />} />
        <Route index element={<Navigate to="dashboard" replace />} />
      </Route>

      {/* Cashier Routes */}
      <Route path="/cashier" element={
        <ProtectedRoute roles={['CASHIER']}>
          <Layout />
        </ProtectedRoute>
      }>
        <Route path="dashboard" element={<CashierDashboard />} />
        <Route path="sale" element={<NewSale />} />
        <Route path="bills" element={<Bills />} />
        <Route path="returns" element={<Returns />} />
        <Route path="leaderboard" element={<CashierLeaderboard />} />
        <Route index element={<Navigate to="dashboard" replace />} />
      </Route>

      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App;
