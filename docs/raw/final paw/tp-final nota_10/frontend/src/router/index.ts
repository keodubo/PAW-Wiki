import { createRouter, createWebHistory } from 'vue-router/auto';
import { setupLayouts } from 'virtual:generated-layouts';
import { routes } from 'vue-router/auto-routes';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    ...setupLayouts(routes),
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/layouts/default.vue'),
      children: [
        {
          path: '',
          component: () => import('@/pages/not-found.vue'),
        },
      ],
    },
  ],
});

router.onError((err, to) => {
  if (err?.message?.includes?.('Failed to fetch dynamically imported module')) {
    if (localStorage.getItem('vuetify:dynamic-reload')) {
      console.error('Dynamic import error, reloading page did not fix it', err);
    } else {
      localStorage.setItem('vuetify:dynamic-reload', 'true');
      location.assign(to.fullPath);
    }
  } else {
    console.error(err);
  }
});

router.isReady().then(() => {
  localStorage.removeItem('vuetify:dynamic-reload');
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  const isAuthRequired = to.meta.requiresAuth || (to.meta.allowedRoles?.length ?? 0) > 0;
  const isGuestOnly = to.meta.requiresGuest;

  if (isGuestOnly && authStore.isAuthenticated) {
    return next({ path: '/' });
  }

  if (isAuthRequired && !authStore.isAuthenticated) {
    return next({ path: '/login', query: { redirect: to.fullPath } });
  }

  if (to.meta.allowedRoles) {
    const userRole = authStore.role;

    if (!userRole || !to.meta.allowedRoles.includes(userRole as any)) {
      return next({ path: '/' });
    }
  }

  next();
});

export default router;
