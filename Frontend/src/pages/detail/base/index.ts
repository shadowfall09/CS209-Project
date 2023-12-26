import dayjs from 'dayjs';
import { TChartColor } from '@/config/color';
import { getChartListColor } from '@/utils/color';
import { getRandomArray } from '@/utils/charts';
import {TopicInfo, TopicListResult} from "@/api/model/topicModel";
import {TdBaseTableProps} from "tdesign-vue-next";
import {RelatedTopicInfo, RelevanceInfo} from "@/api/model/topicRelatedModel";
import 'echarts/lib/component/dataZoom';

/** 柱状图数据源 */
export function constructRelatedTopicBarChartInitDataset({
                                                              placeholderColor,
                                                              borderColor,
                                                            }: TChartColor, popularity: Array<RelatedTopicInfo>, metric: number) {
  let topicArray = popularity.map(relatedTopicInfo => relatedTopicInfo.topic);
  let scoreArray = popularity.map(relatedTopicInfo => relatedTopicInfo.relatedPostNumber);
  if (metric === 1) {
    scoreArray = popularity.map(relatedTopicInfo => Math.round(relatedTopicInfo.relevance * 100000) / 100000);
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
        rotate: 45
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
      left: '100px',
      right: '100px',
      bottom: '160px',
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
    ],
    dataZoom: [{
      type: 'slider',
      bottom: '10px',
      xAxisIndex: 0,
      start: 0,
      end: (20 / popularity.length) * 100
    }]
  };

  return dataset;
}

export function constructTopicRelevanceChartInitDataset({
                                                              placeholderColor,
                                                              borderColor,
                                                            }: TChartColor, topic1: string, topic2: string, relevance: RelevanceInfo, metric: number) {
  const dataset = {
    title: {
      text: topic1 + ' VS ' + topic2,
      left: 'center',
      textStyle: {
        color: placeholderColor
      }
    },
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
      formatter: 'Related Post Number: <strong> ' + relevance.relatedPostNumber + ' </strong> <br/> Relevance: <strong> ' + relevance.relevance * 100 + '% </strong>'
    },
    series: [
      {
        type: 'gauge',
        progress: {
          show: true
        },
        detail: {
          valueAnimation: true,
          formatter: '{value}',
          color: placeholderColor
        },
        data: [
          {
            value: Math.round(relevance.relevance * 100 * 100000) / 100000,
            name: 'RELEVANCE',
          }
        ]
      }
    ]
  };

  return dataset;
}
