import { request } from '@/utils/request';
import type { TopicListResult } from '@/api/model/topicModel';

const Api = {
  TopicList: '/topic/popularity',
};

export function getTopicList(limit: number) {
  return request.get<TopicListResult>({
    url: Api.TopicList + '/' + limit,
  });
}
