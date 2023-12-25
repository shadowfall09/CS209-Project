import { request } from '@/utils/request';
import type { TopicListResult } from '@/api/model/topicModel';
import type { RelatedTopicListResult, RelevanceInfo } from '@/api/model/topicRelatedModel';

const Api = {
  TopicList: '/topic/popularity',
  TopicRelevance: '/topic/related/search',
};

export function getTopicList(limit: number) {
  return request.get<TopicListResult>({
    url: Api.TopicList + '/' + limit,
  });
}

export function getRelatedTopicList(topic: string) {
  return request.get<RelatedTopicListResult>({
    url: Api.TopicRelevance + '/' + topic,
  });
}

export function getRelevance(topic1: string, topic2: string) {
  return request.get<RelatedTopicListResult>({
    url: Api.TopicRelevance + '/' + topic1 + '/' + topic2,
  });
}
