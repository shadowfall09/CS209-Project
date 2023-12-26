import Layout from '@/layouts/index.vue';
import DashboardIcon from '@/assets/assets-slide-detail.svg';
import {h} from "vue";

const IframeComponent = {
  props: ['src'],
  render() {
    return h('iframe', {
      src: this.src,
      width: '100%',
      height: '100%',
      frameborder: '0'
    });
  }
};

export default [
  {
    path: '/query',
    component: Layout,
    redirect: '/query/topic',
    name: 'query',
    meta: { title: 'Query', icon: DashboardIcon },
    children: [
      {
        path: 'topic',
        name: 'QueryTopic',
        component: () => import('@/pages/detail/base/index.vue'),
        meta: { title: 'Related Topic' },
      },
      {
        path: 'rest-api',
        name: 'QueryRestApi',
        component: IframeComponent,
        props: { src: 'http://localhost:8082/swagger-ui/index.html' },
        meta: { title: 'REST API' }
      },
    ],
  },
];
