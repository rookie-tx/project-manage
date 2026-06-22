import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginPage.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/views/dashboard/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard',
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardHome.vue'),
      },
      {
        path: 'projects',
        name: 'ProjectList',
        component: () => import('@/views/project/ProjectListPage.vue'),
      },
      {
        path: 'projects/new',
        name: 'ProjectCreate',
        component: () => import('@/views/project/ProjectCreatePage.vue'),
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/ProjectDetailPage.vue'),
      },
      {
        path: 'projects/:id/edit',
        name: 'ProjectEdit',
        component: () => import('@/views/project/ProjectEditPage.vue'),
      },
      {
        path: 'projects/:id/stats',
        name: 'ProjectPurchaseStats',
        component: () => import('@/views/project/ProjectPurchaseStatsPage.vue'),
      },
      {
        path: 'procurement',
        name: 'PurchaseRequestList',
        component: () => import('@/views/purchase/PurchaseRequestListPage.vue'),
      },
      {
        path: 'procurement/new',
        name: 'PurchaseRequestCreate',
        component: () => import('@/views/purchase/PurchaseRequestCreatePage.vue'),
      },
      {
        path: 'procurement/:id',
        name: 'PurchaseRequestDetail',
        component: () => import('@/views/purchase/PurchaseRequestDetailPage.vue'),
      },
      {
        path: 'procurement/:id/edit',
        name: 'PurchaseRequestEdit',
        component: () => import('@/views/purchase/PurchaseRequestEditPage.vue'),
      },
      {
        path: 'tasks',
        name: 'TaskList',
        component: () => import('@/views/approval/TaskListPage.vue'),
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('@/views/user/UserListPage.vue'),
        meta: { requiresAuth: true, permission: 'user:read' },
      },
      {
        path: 'users/new',
        name: 'UserCreate',
        component: () => import('@/views/user/UserCreatePage.vue'),
        meta: { requiresAuth: true, permission: 'user:create' },
      },
      {
        path: 'users/:id/edit',
        name: 'UserEdit',
        component: () => import('@/views/user/UserEditPage.vue'),
        meta: { requiresAuth: true, permission: 'user:update' },
      },
      {
        path: 'roles',
        name: 'RoleList',
        component: () => import('@/views/role/RoleListPage.vue'),
        meta: { requiresAuth: true, permission: 'role:read' },
      },
      {
        path: 'warehouses',
        name: 'WarehouseList',
        component: () => import('@/views/warehouse/WarehouseListPage.vue'),
        meta: { requiresAuth: true, permission: 'warehouse:read' },
      },
      {
        path: 'materials',
        name: 'MaterialList',
        component: () => import('@/views/warehouse/MaterialListPage.vue'),
        meta: { requiresAuth: true, permission: 'material:read' },
      },
      {
        path: 'stock-records',
        name: 'StockRecordList',
        component: () => import('@/views/warehouse/StockRecordListPage.vue'),
        meta: { requiresAuth: true, permission: 'warehouse:read' },
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/views/attendance/AttendancePage.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'construction-wastes',
        name: 'ConstructionWasteList',
        component: () => import('@/views/waste/ConstructionWasteListPage.vue'),
        meta: { requiresAuth: true, permission: 'waste:read' },
      },
      {
        path: 'construction-wastes/new',
        name: 'ConstructionWasteCreate',
        component: () => import('@/views/waste/ConstructionWasteCreatePage.vue'),
        meta: { requiresAuth: true, permission: 'waste:create' },
      },
      {
        path: 'construction-wastes/:id',
        name: 'ConstructionWasteDetail',
        component: () => import('@/views/waste/ConstructionWasteDetailPage.vue'),
        meta: { requiresAuth: true, permission: 'waste:read' },
      },
      {
        path: 'purchase-orders',
        name: 'PurchaseOrderSummary',
        component: () => import('@/views/purchase-order/PurchaseOrderSummaryPage.vue'),
        meta: { requiresAuth: true, permission: 'purchase:read' },
      },
      {
        path: 'purchase-orders/pending',
        name: 'PendingPurchaseList',
        component: () => import('@/views/purchase-order/PendingPurchasePage.vue'),
        meta: { requiresAuth: true, permission: 'purchase:pending:read' },
      },
      {
        path: 'purchase-orders/purchased',
        name: 'PurchasedPurchaseList',
        component: () => import('@/views/purchase-order/PurchasedPurchasePage.vue'),
        meta: { requiresAuth: true, permission: 'purchase:purchased:read' },
      },
      {
        path: 'purchase-orders/arrived',
        name: 'ArrivedPurchaseList',
        component: () => import('@/views/purchase-order/ArrivedPurchasePage.vue'),
        meta: { requiresAuth: true, permission: 'purchase:arrived:read' },
      },
      {
        path: 'purchase-orders/:id/edit',
        name: 'PurchaseOrderEdit',
        component: () => import('@/views/purchase-order/PurchaseOrderFormPage.vue'),
        meta: { requiresAuth: true, permission: 'purchase:update' },
      },
      {
        path: 'suppliers',
        name: 'SupplierList',
        component: () => import('@/views/supplier/SupplierListPage.vue'),
        meta: { requiresAuth: true, permission: 'supplier:read' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/SettingsPage.vue'),
        meta: { requiresAuth: true },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (auth.loading) return true

  if (to.meta.requiresAuth && !auth.isAuth) {
    return '/login'
  }

  if (to.meta.permission && !auth.hasPermission(to.meta.permission)) {
    return '/dashboard'
  }

  if (to.path === '/login' && auth.isAuth) {
    // 临时工登录后跳转到打卡页
    if (auth.user?.roles?.includes('temp_worker')) {
      return '/attendance'
    }
    return '/dashboard'
  }

  // 临时工访问首页时重定向到打卡页
  if (auth.isAuth && auth.user?.roles?.includes('temp_worker')
      && (to.path === '/' || to.path === '/dashboard')) {
    return '/attendance'
  }

  return true
})

export default router
