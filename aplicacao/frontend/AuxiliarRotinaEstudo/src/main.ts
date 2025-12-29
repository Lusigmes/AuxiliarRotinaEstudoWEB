import { createApp } from 'vue';
import router from './router';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';
import '@/api/connect/connect';
import 'vuetify/styles';
import '@mdi/font/css/materialdesignicons.css';
import VueTheMask from 'vue-the-mask';
import App from './App.vue';
import { createPinia } from 'pinia';
import notificacoesPlugin from './plugins/notificacoes';

const pinia = createPinia();
const vuetify = createVuetify({
  components,
  directives,
});

createApp(App)
  .use(pinia)
  .use(vuetify)
  .use(notificacoesPlugin)
  .use(router)
  .use(VueTheMask)
  .mount('#app')