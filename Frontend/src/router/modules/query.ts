import Layout from '@/layouts/index.vue';
import DashboardIcon from '@/assets/assets-slide-detail.svg';

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
        // component: () => import('@/pages/dashboard/detail/index.vue'),
        meta: { title: 'REST API' ,frameSrc: 'http://localhost:8082/swagger-ui/index.html' },
      },
    ],
  },
];
