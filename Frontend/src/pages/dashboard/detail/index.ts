import {TChartColor} from '@/config/color';
import {getChartListColor} from '@/utils/color';

/** 折线图数据 */
export function getFolderLineDataSet({
                                       placeholderColor,
                                       borderColor,
                                       syntaxErrorData = [],
                                       fatalErrorData = [],
                                       exceptionData = [],
                                     }: {
  syntaxErrorData?: Array<{ value: number, name: string }>,
  fatalErrorData?: Array<{ value: number, name: string }>,
  exceptionData?: Array<{ value: number, name: string }>
} & TChartColor) {
  let topArray: Array<string> = ['Top1', 'Top2', 'Top3', 'Top4', 'Top5', 'Top6', 'Top7'];
  return {
    color: getChartListColor(),
    grid: {
      top: '5%',
      right: '10px',
      left: '30px',
      bottom: '60px',
    },
    legend: {
      left: 'center',
      bottom: '0',
      orient: 'horizontal', // legend 横向布局。
      data: ['Exception', 'Fatal Error', 'Syntax Error'],
      textStyle: {
        fontSize: 14,
        color: placeholderColor,
      },
    },
    xAxis: {
      type: 'category',
      data: topArray,
      boundaryGap: false,
      axisLabel: {
        color: placeholderColor,
      },
      axisLine: {
        lineStyle: {
          color: borderColor,
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
    tooltip: {
      trigger: 'item',
      formatter: function (params) {
        return params.seriesName + '<br/>' + params.data.name + ': ' + params.data.value;
      },
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
    series: [
      {
        showSymbol: true,
        symbol: 'circle',
        symbolSize: 8,
        name: 'Syntax Error',
        stack: 'Popularity',
        data: syntaxErrorData,
        type: 'line',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        itemStyle: {
          borderColor,
          borderWidth: 1,
        },
      },
      {
        showSymbol: true,
        symbol: 'circle',
        symbolSize: 8,
        name: 'Fatal Error',
        stack: 'Popularity',
        data: fatalErrorData,
        type: 'line',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        itemStyle: {
          borderColor,
          borderWidth: 1,
        },
      },
      {
        showSymbol: true,
        symbol: 'circle',
        symbolSize: 8,
        name: 'Exception',
        stack: 'Popularity',
        data: exceptionData,
        type: 'line',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        itemStyle: {
          borderColor,
          borderWidth: 1,
        },
      },
    ],
  };
}

/**
 * 获取饼图数据
 *
 * @export
 * @returns {*}
 */
interface PieChartDataSetParams {
  textColor: string;
  placeholderColor: string;
  containerColor: string;
  data: { value: number; name: string }[];
  name: string;
}
export function getPieChartDataSet({
                                     textColor,
                                     placeholderColor,
                                     containerColor,
                                     data = [],
                                     name,
                                   }: PieChartDataSetParams) {
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
    toolbox: {
      show: true,
      feature: {
        mark: {show: true},
        dataView: {show: false},
        restore: {show: false},
        saveAsImage: {show: true}
      }
    },
    legend: {
      selectedMode: false,
      itemWidth: 12,
      itemHeight: 4,
      textStyle: {
        fontSize: 14,
        color: placeholderColor,
      },
      left: 'center',
      bottom: '15%',
      orient: 'horizontal', // legend 横向布局。
    },
    series: [
      {
        name: name,
        type: 'pie',
        radius: ['50%', '65%'],
        center: ['50%', '37%'],
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
          formatter: ['{value|{d}%}', '{name|{b}}'].join('\n'),
          rich: {
            value: {
              color: textColor,
              fontSize: 28,
              fontWeight: 'normal',
              lineHeight: 46,
            },
            name: {
              color: placeholderColor,
              fontSize: 16,
              lineHeight: 14,
            },
          },
        },
        emphasis: {
          scale: true,
          label: {
            show: true,
            formatter: ['{value|{d}%}', '{name|{b}}'].join('\n'),
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
        data: data,
      },
    ],
  };
}



/** 柱状图数据源 */
export function constructTopicPopularityBarChartInitDataset({
  placeholderColor,
    borderColor,
    syntaxErrorData = [],
    fatalErrorData = [],
    exceptionData = [],
    metric,
}: {
  syntaxErrorData?: Array<{ value: number, name: string }>,
    fatalErrorData?: Array<{ value: number, name: string }>,
    exceptionData?: Array<{ value: number, name: string }>
} & TChartColor&{metric:number}) {
  let topArray = [];
  let scoreArray = [];
  switch (metric) {
    case 0:
      scoreArray = exceptionData;
      for (let i = 0; i < exceptionData.length; i++) {
        topArray.push(exceptionData[i].name);
      }
      break;
    case 1:
      scoreArray = fatalErrorData;
      for (let i = 0; i < fatalErrorData.length; i++) {
        topArray.push(fatalErrorData[i].name);
      }
      break;
    case 2:
      scoreArray = syntaxErrorData;
      for (let i = 0; i < syntaxErrorData.length; i++) {
        topArray.push(syntaxErrorData[i].name);
      }
      break;
  }
  return {
    toolbox: {
      show: true,
      feature: {
        mark: {show: true},
        dataView: {show: false},
        restore: {show: false},
        saveAsImage: {show: true}
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: function (params) {
        return params.data.name + ': ' + params.data.value;
      },
    },
    color: getChartListColor(),
    xAxis: {
      type: 'category',
      data: topArray,
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
}
