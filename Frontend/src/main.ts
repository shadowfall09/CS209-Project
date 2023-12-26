import {createApp} from 'vue';

import TDesign from 'tdesign-vue-next';
import 'tdesign-vue-next/es/style/index.css';

import {store} from './store';
import router from './router';
import '@/style/index.less';
import './permission';
import App from './App.vue';

import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import {faRotateRight} from '@fortawesome/free-solid-svg-icons'

const app = createApp(App);

library.add(faRotateRight)

app.use(TDesign);
app.use(store);
app.use(router);

app.component('font-awesome-icon', FontAwesomeIcon).mount('#app');
