import dayjs from 'dayjs';
import { TChartColor } from '@/config/color';
import { getChartListColor } from '@/utils/color';
import { getRandomArray } from '@/utils/charts';
import {TopicInfo, TopicListResult} from "@/api/model/topicModel";
import {TdBaseTableProps} from "tdesign-vue-next";
import {RelatedTopicInfo} from "@/api/model/topicRelatedModel";
import 'echarts/lib/component/dataZoom';

/** 柱状图数据源 */
export function constructRelatedTopicBarChartInitDataset({
                                                              placeholderColor,
                                                              borderColor,
                                                            }: TChartColor, popularity: Array<RelatedTopicInfo>, metric: number) {
  let topicArray = popularity.map(relatedTopicInfo => relatedTopicInfo.topic);
  let scoreArray = popularity.map(relatedTopicInfo => relatedTopicInfo.relatedPostNumber);
  if (metric === 1) {
    scoreArray = popularity.map(relatedTopicInfo => relatedTopicInfo.relevance);
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
