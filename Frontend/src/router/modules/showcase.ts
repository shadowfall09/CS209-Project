import Layout from '@/layouts/index.vue';
import DashboardIcon from '@/assets/assets-slide-dashboard.svg';

export default [
  {
    path: '/showcase',
    component: Layout,
    redirect: '/showcase/topic',
    name: 'ashowcase',
    meta: { title: 'Showcase', icon: DashboardIcon },
    children: [
      {
        path: 'topic',
        name: 'TopicPopularity',
        component: () => import('@/pages/showcase/topic/index.vue'),
        meta: { title: 'Topic Popularity' },
      },
      {
        path: 'bug',
        name: 'BugPopularity',
        component: () => import('@/pages/showcase/bug/index.vue'),
        meta: { title: 'Bug Popularity' },
      },
    ],
  },
];
