import dayjs from 'dayjs';
import { TChartColor } from '@/config/color';
import { getChartListColor } from '@/utils/color';
import { getRandomArray } from '@/utils/charts';
import {TopicInfo, TopicListResult} from "@/api/model/topicModel";
import {TdBaseTableProps} from "tdesign-vue-next";

/** 首页 dashboard 折线图 */
export function constructInitDashboardDataset(type: string) {
  const dateArray: Array<string> = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const datasetAxis = {
    xAxis: {
      type: 'category',
      show: false,
      data: dateArray,
    },
    yAxis: {
      show: false,
      type: 'value',
    },
    grid: {
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
    },
  };

  if (type === 'line') {
    const lineDataset = {
      ...datasetAxis,
      color: ['#fff'],
      series: [
        {
          data: [150, 230, 224, 218, 135, 147, 260],
          type,
          showSymbol: true,
          symbol: 'circle',
          symbolSize: 0,
          markPoint: {
            data: [
              { type: 'max', name: '最大值' },
              { type: 'min', name: '最小值' },
            ],
          },
          lineStyle: {
            width: 2,
          },
        },
      ],
    };
    return lineDataset;
  }
  const barDataset = {
    ...datasetAxis,
    color: getChartListColor(),
    series: [
      {
        data: [
          100,
          130,
          184,
          218,
          {
            value: 135,
            itemStyle: {
              opacity: 0.2,
            },
          },
          {
            value: 118,
            itemStyle: {
              opacity: 0.2,
            },
          },
          {
            value: 60,
            itemStyle: {
              opacity: 0.2,
            },
          },
        ],
        type,
        barWidth: 9,
      },
    ],
  };
  return barDataset;
}

/** 柱状图数据源 */
export function constructInitDataset({
  dateTime = [],
  placeholderColor,
  borderColor,
}: { dateTime: Array<string> } & TChartColor) {
  const divideNum = 10;
  const timeArray = [];
  const inArray = [];
  const outArray = [];
  for (let i = 0; i < divideNum; i++) {
    if (dateTime.length > 0) {
      const dateAbsTime: number = (new Date(dateTime[1]).getTime() - new Date(dateTime[0]).getTime()) / divideNum;
      const enhandTime: number = new Date(dateTime[0]).getTime() + dateAbsTime * i;
      timeArray.push(dayjs(enhandTime).format('YYYY-MM-DD'));
    } else {
      timeArray.push(
        dayjs()
          .subtract(divideNum - i, 'day')
          .format('YYYY-MM-DD'),
      );
    }

    inArray.push(getRandomArray().toString());
    outArray.push(getRandomArray().toString());
  }

  const dataset = {
    color: getChartListColor(),
    tooltip: {
      trigger: 'item',
    },
    xAxis: {
      type: 'category',
      data: timeArray,
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
      left: '25px',
      right: 0,
      bottom: '60px',
    },
    legend: {
      icon: 'rect',
      itemWidth: 12,
      itemHeight: 4,
      itemGap: 48,
      textStyle: {
        fontSize: 12,
        color: placeholderColor,
      },
      left: 'center',
      bottom: '0',
      orient: 'horizontal',
      data: ['本月', '上月'],
    },
    series: [
      {
        name: '本月',
        data: outArray,
        type: 'bar',
      },
      {
        name: '上月',
        data: inArray,
        type: 'bar',
      },
    ],
  };

  return dataset;
}

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
        saveAsImage: {show: false}
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
      right: 0,
      bottom: '60px',
    },
    series: [
      {
        data: scoreArray,
        type: 'bar',
        label: {
          show: true,
          position: 'top',
          textStyle: {
            color: placeholderColor
          }
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
      fixed: 'left',
    },
    {
      align: 'left',
      colKey: 'topic',
      title: 'Topic',
      width: 100,
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
        saveAsImage: { show: false }
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
          textStyle: {
            color: placeholderColor
          }
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

/**
 *  线性图表数据源
 *
 * @export
 * @param {Array<string>} [dateTime=[]]
 * @returns {*}
 */
export function getLineChartDataSet({
  dateTime = [],
  placeholderColor,
  borderColor,
}: { dateTime?: Array<string> } & TChartColor) {
  const divideNum = 10;
  const timeArray = [];
  const inArray = [];
  const outArray = [];
  for (let i = 0; i < divideNum; i++) {
    if (dateTime.length > 0) {
      const dateAbsTime: number = (new Date(dateTime[1]).getTime() - new Date(dateTime[0]).getTime()) / divideNum;
      const enhandTime: number = new Date(dateTime[0]).getTime() + dateAbsTime * i;
      // console.log('dateAbsTime..', dateAbsTime, enhandTime);
      timeArray.push(dayjs(enhandTime).format('MM-DD'));
    } else {
      timeArray.push(
        dayjs()
          .subtract(divideNum - i, 'day')
          .format('MM-DD'),
      );
    }

    inArray.push(getRandomArray().toString());
    outArray.push(getRandomArray().toString());
  }

  const dataSet = {
    color: getChartListColor(),
    tooltip: {
      trigger: 'item',
    },
    grid: {
      left: '0',
      right: '20px',
      top: '5px',
      bottom: '36px',
      containLabel: true,
    },
    legend: {
      left: 'center',
      bottom: '0',
      orient: 'horizontal', // legend 横向布局。
      data: ['本月', '上月'],
      textStyle: {
        fontSize: 12,
        color: placeholderColor,
      },
    },
    xAxis: {
      type: 'category',
      data: timeArray,
      boundaryGap: false,
      axisLabel: {
        color: placeholderColor,
      },
      axisLine: {
        lineStyle: {
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
    series: [
      {
        name: '本月',
        data: outArray,
        type: 'line',
        smooth: false,
        showSymbol: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          borderColor,
          borderWidth: 1,
        },
        areaStyle: {
          opacity: 0.1,
        },
      },
      {
        name: '上月',
        data: inArray,
        type: 'line',
        smooth: false,
        showSymbol: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          borderColor,
          borderWidth: 1,
        },
      },
    ],
  };
  return dataSet;
}

/**
 * 获取饼图数据
 *
 * @export
 * @param {number} [radius=1]
 * @returns {*}
 */
export function getPieChartDataSet({
  radius = 42,
  textColor,
  placeholderColor,
  containerColor,
}: { radius?: number } & Record<string, string>) {
  return {
    color: getChartListColor(),
    tooltip: {
      show: false,
      trigger: 'axis',
      position: null,
    },
    grid: {
      top: '0',
      right: '0',
    },
    legend: {
      selectedMode: false,
      itemWidth: 12,
      itemHeight: 4,
      textStyle: {
        fontSize: 12,
        color: placeholderColor,
      },
      left: 'center',
      bottom: '0',
      orient: 'horizontal', // legend 横向布局。
    },
    series: [
      {
        name: '销售渠道',
        type: 'pie',
        radius: ['48%', '60%'],
        avoidLabelOverlap: true,
        selectedMode: true,
        silent: true,
        itemStyle: {
          borderColor: containerColor,
          borderWidth: 1,
        },
        label: {
          show: true,
          position: 'center',
          formatter: ['{value|{d}%}', '{name|{b}渠道占比}'].join('\n'),
          rich: {
            value: {
              color: textColor,
              fontSize: 28,
              fontWeight: 'normal',
              lineHeight: 46,
            },
            name: {
              color: '#909399',
              fontSize: 12,
              lineHeight: 14,
            },
          },
        },
        emphasis: {
          scale: true,
          label: {
            show: true,
            formatter: ['{value|{d}%}', '{name|{b}渠道占比}'].join('\n'),
            rich: {
              value: {
                color: textColor,
                fontSize: 28,
                fontWeight: 'normal',
                lineHeight: 46,
              },
              name: {
                color: '#909399',
                fontSize: 14,
                lineHeight: 14,
              },
            },
          },
        },
        labelLine: {
          show: false,
        },
        data: [
          {
            value: 1048,
            name: '线上',
          },
          { value: radius * 7, name: '门店' },
        ],
      },
    ],
  };
}
