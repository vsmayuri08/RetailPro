import React, { useEffect, useMemo, useState } from 'react';
import { Outlet, NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import RetailProAi from './RetailProAi';
import '../styles/layout.css';

import logo from '../assets/img/logo.svg';
import logoutIcon from '../assets/icons/logout.svg';
import dashboardO from '../assets/icons/dashboard-o.svg';
import dashboardF from '../assets/icons/dashboard-f.svg';
import packageO from '../assets/icons/package-o.svg';
import packageF from '../assets/icons/package-f.svg';
import profileO from '../assets/icons/profile-o.svg';
import profileF from '../assets/icons/profile-f.svg';
import documentationO from '../assets/icons/documentation-o.svg';
import documentationF from '../assets/icons/documentation-f.svg';
import coreO from '../assets/icons/core-o.svg';
import coreF from '../assets/icons/core-f.svg';
import formsO from '../assets/icons/forms-o.svg';
import formsF from '../assets/icons/forms-f.svg';
import chartsO from '../assets/icons/charts-o.svg';
import chartsF from '../assets/icons/charts-f.svg';
import gridO from '../assets/icons/grid-o.svg';
import gridF from '../assets/icons/grid-f.svg';
import tablesO from '../assets/icons/tables-o.svg';
import tablesF from '../assets/icons/tables-f.svg';
import mapsO from '../assets/icons/maps-o.svg';
import mapsF from '../assets/icons/maps-f.svg';
import settingsO from '../assets/icons/settings-o.svg';
import settingsF from '../assets/icons/settings-f.svg';
import ecommerceO from '../assets/icons/ecommerce-o.svg';
import ecommerceF from '../assets/icons/ecommerce-f.svg';

const ICONS = {
  dashboard: [dashboardO, dashboardF],
  package: [packageO, packageF],
  profile: [profileO, profileF],
  docs: [documentationO, documentationF],
  core: [coreO, coreF],
  forms: [formsO, formsF],
  charts: [chartsO, chartsF],
  grid: [gridO, gridF],
  tables: [tablesO, tablesF],
  maps: [mapsO, mapsF],
  settings: [settingsO, settingsF],
  shop: [ecommerceO, ecommerceF],
};

// Same routes and role visibility as before — only grouped and given icons.
const NAV_BY_ROLE = {
  SUPER_ADMIN: [
    { title: 'Overview', links: [
      { name: 'Dashboard', path: '/admin/dashboard', icon: 'dashboard' },
    ] },
    { title: 'Management', links: [
      { name: 'Branches', path: '/admin/branches', icon: 'maps' },
      { name: 'Categories', path: '/admin/categories', icon: 'grid' },
      { name: 'Suppliers', path: '/admin/suppliers', icon: 'package' },
      { name: 'Users', path: '/admin/users', icon: 'profile' },
      { name: 'Staff & Payroll', path: '/admin/staff', icon: 'forms' },
    ] },
    { title: 'Configuration', links: [
      { name: 'Billing Settings', path: '/admin/billing-settings', icon: 'settings' },
      { name: 'Expiry Discounts', path: '/admin/expiry-discounts', icon: 'core' },
    ] },
    { title: 'Insights', links: [
      { name: 'Reports', path: '/admin/reports', icon: 'docs' },
      { name: 'Cashier Leaderboard', path: '/admin/cashier-leaderboard', icon: 'charts' },
      { name: 'Activity Log', path: '/admin/activity-log', icon: 'tables' },
    ] },
  ],
  BRANCH_MANAGER: [
    { title: 'Overview', links: [
      { name: 'Dashboard', path: '/manager/dashboard', icon: 'dashboard' },
    ] },
    { title: 'Catalog & Stock', links: [
      { name: 'Products', path: '/manager/products', icon: 'package' },
      { name: 'Inventory', path: '/manager/inventory', icon: 'tables' },
      { name: 'Reorder Suggestions', path: '/manager/reorder-suggestions', icon: 'grid' },
      { name: 'Purchase Orders', path: '/manager/purchase-orders', icon: 'docs' },
    ] },
    { title: 'People', links: [
      { name: 'Customers', path: '/manager/customers', icon: 'profile' },
      { name: 'Employees', path: '/manager/employees', icon: 'profile' },
      { name: 'Staff & Payroll', path: '/manager/staff', icon: 'forms' },
    ] },
    { title: 'Sales & Analytics', links: [
      { name: 'Sales', path: '/manager/sales', icon: 'shop' },
      { name: 'ABC Analysis', path: '/manager/abc-analysis', icon: 'charts' },
      { name: 'Market Basket', path: '/manager/reports/market-basket', icon: 'shop' },
      { name: 'RFM Segmentation', path: '/manager/rfm-segmentation', icon: 'profile' },
      { name: 'Demand Forecast', path: '/manager/demand-forecast', icon: 'charts' },
      { name: 'Cashier Leaderboard', path: '/manager/cashier-leaderboard', icon: 'charts' },
      { name: 'Activity Log', path: '/manager/activity-log', icon: 'core' },
    ] },
  ],
  CASHIER: [
    { title: 'Point of Sale', links: [
      { name: 'Dashboard', path: '/cashier/dashboard', icon: 'dashboard' },
      { name: 'New Sale', path: '/cashier/sale', icon: 'shop' },
      { name: 'Bills', path: '/cashier/bills', icon: 'docs' },
      { name: 'Returns', path: '/cashier/returns', icon: 'core' },
      { name: 'Leaderboard', path: '/cashier/leaderboard', icon: 'charts' },
    ] },
  ],
};

const ROLE_LABEL = {
  SUPER_ADMIN: 'Super Admin',
  BRANCH_MANAGER: 'Branch Manager',
  CASHIER: 'Cashier',
};

const Layout = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const groups = useMemo(() => (user ? NAV_BY_ROLE[user.role] || [] : []), [user]);

  const currentName = useMemo(() => {
    const all = groups.flatMap((g) => g.links);
    const match = all
      .filter((l) => location.pathname === l.path || location.pathname.startsWith(l.path + '/'))
      .sort((a, b) => b.path.length - a.path.length)[0];
    return match?.name || 'Dashboard';
  }, [groups, location.pathname]);

  // Close the mobile drawer whenever the route changes.
  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  const initial = (user?.fullName || user?.email || 'U').trim().charAt(0).toUpperCase();

  return (
    <div className={`layout-container ${sidebarOpen ? 'sidebar-open' : ''}`}>
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="logo-link">
            <img src={logo} alt="" className="logo-img" />
            <span className="logo-text"><strong>Retail</strong>Pro</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          {groups.map((group) => (
            <React.Fragment key={group.title}>
              <div className="nav-title">{group.title}</div>
              {group.links.map((link) => {
                const [off, on] = ICONS[link.icon];
                return (
                  <NavLink
                    key={link.path}
                    to={link.path}
                    className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
                  >
                    <span className="nav-icon" aria-hidden="true">
                      <img src={off} alt="" className="icon-off" />
                      <img src={on} alt="" className="icon-on" />
                    </span>
                    {link.name}
                  </NavLink>
                );
              })}
            </React.Fragment>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <p className="user-name">{user?.fullName || 'User'}</p>
            <span className="user-role">{user?.role}</span>
          </div>
          <button className="logout-btn" onClick={logout}>
            <img src={logoutIcon} alt="" />
            Logout
          </button>
        </div>
      </aside>

      <div className="sidebar-backdrop" onClick={() => setSidebarOpen(false)} />

      <div className="content-wrap">
        <header className="navbar">
          <button
            type="button"
            className="navbar-toggle"
            aria-label="Toggle navigation"
            onClick={() => setSidebarOpen((v) => !v)}
          >
            ☰
          </button>
          <span className="navbar-title">{currentName}</span>
          <span className="navbar-crumb">/ {ROLE_LABEL[user?.role] || 'Workspace'}</span>
          <span className="navbar-spacer" />
          <div className="navbar-user">
            <div className="navbar-avatar">{initial}</div>
            <div className="navbar-user-text">
              <span className="navbar-user-name">{user?.fullName || 'User'}</span>
              <span className="navbar-user-role">{ROLE_LABEL[user?.role] || user?.role}</span>
            </div>
          </div>
        </header>

        <main className="main-content">
          <Outlet />
          {(user?.role === 'SUPER_ADMIN' || user?.role === 'BRANCH_MANAGER') && <RetailProAi />}
        </main>
      </div>
    </div>
  );
};

export default Layout;
