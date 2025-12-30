import { useAuth } from '@/composables/useAuth';
import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  { 
    path: "/",
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
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/DashboardContent.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'cronograma',
        name: 'Cronograma',
        component: () => import('@/views/cronograma/Cronograma.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'item-cronograma-form',
        name: 'ItemCronogramaForm',
        component: () => import('@/views/cronograma/AdicionarItensForm.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'estudo',
        name: 'Estudo',
        component: () => import('@/views/estudos/Estudo.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'revisao',
        name: 'Revisao',
        component: () => import('@/views/revisoes/Revisao.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'relatorio',
        name: 'Relatorio',
        component: () => import('@/views/relatorios/Relatorio.vue'),
        meta: { requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach(async (to) => {
  const { token, fetchUsuario, usuario } = useAuth();

  if (token.value && !usuario.value) {
    await fetchUsuario();
  }
  if (to.meta.requiresAuth && !token.value) {
    return "/";
  }
  if(token.value && to.path === '/') {
    return "/tela-principal";
  }
  if(token.value && (to.path === '/' || to.path === '/registro')){
    return "/tela-principal";
  }

  return true; 
})

export default router