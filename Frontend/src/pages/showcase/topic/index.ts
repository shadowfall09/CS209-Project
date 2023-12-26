import {TChartColor} from '@/config/color';
import {getChartListColor} from '@/utils/color';
import {TopicInfo} from "@/api/model/topicModel";
import {TdBaseTableProps} from "tdesign-vue-next";

/** 柱状图数据源 */
export function constructTopicPopularityBarChartInitDataset({
  placeholderColor,
  borderColor,
}: TChartColor, popularity: Array<TopicInfo>, metric: number, sort: boolean) {
  let topicArray = popularity.map(topicInfo => topicInfo.topic);
  let scoreArray = popularity.map(topicInfo => topicInfo.comprehensiveScore);
  switch (metric) {
    case 1:
      scoreArray = popularity.map(topicInfo => topicInfo.threadNumber);
      break;
    case 2:
      scoreArray = popularity.map(topicInfo => topicInfo.threadNumber2023);
      break;
    case 3:
      scoreArray = popularity.map(topicInfo => topicInfo.averageViewCount);
      break;
    case 4:
      scoreArray = popularity.map(topicInfo => topicInfo.averageVoteCount);
      break;
    case 5:
      scoreArray = popularity.map(topicInfo => topicInfo.discussionPeopleNumber);
      break;
  }
  if (sort) {
    let tempArray: Array<[string, number]> = []
    for (let i = 0; i < popularity.length; i++) {
      let tempTopicTuple: [string, number] = [topicArray[i], scoreArray[i]];
      tempArray.push(tempTopicTuple);
    }
    tempArray.sort((o1, o2) => o2[1] - o1[1]);
    topicArray = tempArray.map(tempTopicInfo => tempTopicInfo[0]);
    scoreArray = tempArray.map(tempTopicInfo => tempTopicInfo[1]);
  }

  const dataset = {
    toolbox: {
      show: true,
      feature: {
        mark: {show: true},
        dataView: {show: false},
        restore: {show: false},
        saveAsImage: {show: true}
      }
    },
    color: getChartListColor(),
    tooltip: {
      trigger: 'item',
    },
    xAxis: {
      type: 'category',
      data: topicArray,
      axisLabel: {
        color: placeholderColor,
      },
      axisLine: {
        lineStyle: {
          color: getChartListColor()[1],
          width: 1,
        },
      },
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: placeholderColor,
      },
      splitLine: {
        lineStyle: {
          color: borderColor,
        },
      },
    },
    grid: {
      top: '5%',
      left: '50px',
      right: '35px',
      bottom: '60px',
    },
    series: [
      {
        data: scoreArray,
        type: 'bar',
        label: {
          show: true,
          position: 'top',
          color: placeholderColor
        }
      }
    ]
  };

  return dataset;
}

export interface TopicData {
  rank: number;
  topic: string;
  count: number;
  percentage: string;
}

export function constructTopicPopularityRankChartInitDataset({
  placeholderColor,
  borderColor,
}: TChartColor, popularity: Array<TopicInfo>, metric: number) {
  let topicArray = popularity.map(topicInfo => topicInfo.topic);
  let scoreArray = popularity.map(topicInfo => topicInfo.comprehensiveScore);
  switch (metric) {
    case 1:
      scoreArray = popularity.map(topicInfo => topicInfo.threadNumber);
      break;
    case 2:
      scoreArray = popularity.map(topicInfo => topicInfo.threadNumber2023);
      break;
    case 3:
      scoreArray = popularity.map(topicInfo => topicInfo.averageViewCount);
      break;
    case 4:
      scoreArray = popularity.map(topicInfo => topicInfo.averageVoteCount);
      break;
    case 5:
      scoreArray = popularity.map(topicInfo => topicInfo.discussionPeopleNumber);
      break;
  }
  let tempArray: Array<[string, number]> = []
  for (let i = 0; i < popularity.length; i++) {
    let tempTopicTuple: [string, number] = [topicArray[i], scoreArray[i]];
    tempArray.push(tempTopicTuple);
  }
  tempArray.sort((o1, o2) => o2[1] - o1[1]);
  topicArray = tempArray.map(tempTopicInfo => tempTopicInfo[0]);
  scoreArray = tempArray.map(tempTopicInfo => tempTopicInfo[1]);
  let sum = scoreArray.reduce((accumulator, currentValue) => accumulator + currentValue, 0);

  const RANK_COLUMNS: TdBaseTableProps['columns'] = [
    {
      align: 'center',
      colKey: 'rank',
      title: 'Rank',
      width: 70,
    },
    {
      align: 'left',
      colKey: 'topic',
      title: 'Topic',
      width: 135,
    },
    {
      align: 'center',
      colKey: 'count',
      title: 'Count',
      width: 80,
    },
    {
      align: 'center',
      colKey: 'percentage',
      width: 110,
      title: 'Percentage',
    }
  ];

  let TOPIC_DATA_LIST: Array<TopicData> = topicArray.map((topic, index) => {
    return {
      rank: index + 1,
      topic: topic,
      count: scoreArray[index],
      percentage: ((scoreArray[index] / sum) * 100).toFixed(2) + "%"
    }
  })

  return {
    columnTitle: RANK_COLUMNS,
    data: TOPIC_DATA_LIST
  };
}

export function constructTopicPopularityPercentageChartInitDataset({
  placeholderColor,
  borderColor,
}: TChartColor, popularity: Array<TopicInfo>, metric: number) {
  let topicArray = popularity.map(topicInfo => topicInfo.topic);
  let scoreArray = popularity.map(topicInfo => topicInfo.comprehensiveScore);
  switch (metric) {
    case 1:
      scoreArray = popularity.map(topicInfo => topicInfo.threadNumber);
      break;
    case 2:
      scoreArray = popularity.map(topicInfo => topicInfo.threadNumber2023);
      break;
    case 3:
      scoreArray = popularity.map(topicInfo => topicInfo.averageViewCount);
      break;
    case 4:
      scoreArray = popularity.map(topicInfo => topicInfo.averageVoteCount);
      break;
    case 5:
      scoreArray = popularity.map(topicInfo => topicInfo.discussionPeopleNumber);
      break;
  }
  let tempArray: Array<[string, number]> = []
  for (let i = 0; i < popularity.length; i++) {
    let tempTopicTuple: [string, number] = [topicArray[i], scoreArray[i]];
    tempArray.push(tempTopicTuple);
  }
  tempArray.sort((o1, o2) => o2[1] - o1[1]);
  topicArray = tempArray.map(tempTopicInfo => tempTopicInfo[0]);
  scoreArray = tempArray.map(tempTopicInfo => tempTopicInfo[1]);
  const mergedData = topicArray.map((topic, index) => {
    return {
      name: topic,
      value: scoreArray[index]
    };
  });

  const dataset = {
    tooltip: {
      trigger: 'item',
      formatter: function(params) {
        return params.marker + ' ' + params.name + '&nbsp&nbsp&nbsp&nbsp&nbsp<strong>' + params.data.value + ' (' + params.percent + '%)</strong>';
      }
    },
    legend: {
      left: 'center',
      top: 'bottom',
      textStyle: {
        color: placeholderColor
      }
    },
    toolbox: {
      show: true,
      feature: {
        mark: { show: true },
        dataView: { show: false },
        restore: { show: true },
        saveAsImage: { show: true }
      }
    },
    series: [
      {
        name: 'Percentage',
        type: 'pie',
        label: {
          show: true,
          formatter: '{b}: {d}%',
          lineHeight: 13,
          overflow: 'break',
          color: placeholderColor
        },
        radius: ['5%', '75%'],
        center: ['50%', '40%'],
        roseType: 'area',
        itemStyle: {
          borderRadius: 8
        },
        data: mergedData
      }
    ]
  };

  return dataset;
}
