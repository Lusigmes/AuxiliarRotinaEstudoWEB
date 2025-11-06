import { createRouter, createWebHistory } from 'vue-router';
// import { useAuth } from '@/composables/useAuth'

const routes = [
  { path: "/", name: "Home", component: () => import('@/components/Home.vue') }
//   { path: "/login", name: "Login", component: () => import('@/components/LoginForm.vue') },
//   { path: "/registro", name: "Registro", component: () => import('@/components/RegistroForm.vue') },
  
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// router.beforeEach(async (to) => { ciclo de rotas? nao lembro
//   const { token, fetchUsuario, usuario } = useAuth();

//   if (token.value && !usuario.value) {
//     await fetchUsuario();
//   }
//   if (to.meta.requiresAuth && !token.value) {
//     return "/login";
//   }
//   if(token.value && to.path === '/') {
//     return "/dashboard";
//   }
//   if(token.value && (to.path === '/login' || to.path === '/registro')){
//     return "/dashboard";
//   }

//   return true; 
// })

export default router