import { createRouter, createWebHistory } from 'vue-router';

const routes = [
    { 
    path: "/",
    name: "Home", 
    redirect: '/login'
  },
  { 
    path: "/login",
    name: "Login", 
    component: () => import('@/components/Login.vue'),
    meta: { requiresAuth: false }
  },
  { 
    path: "/registro",
    name: "Registro", 
    component: () => import('@/components/Registro.vue'),
    meta: { requiresAuth: false }
  },
  { 
    path: "/tela-principal",
    name: "Tela Principal", 
    component: () => import('@/views/TelaPrincipal.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("jwt");
  
  if (to.meta.requiresAuth && !token) {
    next('/login');
  } else if (token && (to.path === '/' || to.path === '/login')) {
    next('/tela-principal');
  } else {
    next();
  }
})

export default router