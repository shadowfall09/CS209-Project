export interface TopicListResult {
  popularity: Array<TopicInfo>;
}
export interface TopicInfo {
  topic: string;
  threadNumber: number;
  threadNumber2023: number;
  averageViewCount: number;
  averageVoteCount: number;
  discussionPeopleNumber: number;
  comprehensiveScore: number;
}
