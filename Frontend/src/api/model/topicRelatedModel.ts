export interface RelatedTopicListResult {
  relatedTopicList: Array<RelatedTopicInfo>;
}
export interface RelatedTopicInfo {
  topic: string;
  relatedPostNumber: number;
  relevance: number;
}

export interface RelevanceInfo {
  relatedPostNumber: number;
  relevance: number;
}
