<template>
  <div class="dashboard-panel-detail">
    <t-card title="Exception VS Error Popularity"
            hover-shadow
            class="dashboard-detail-card">
      <t-row :gutter="[16, 16]">
        <t-col v-for="(item, index) in paneListData" :key="index" :xs="6" :xl="3">
          <t-card class="dashboard-list-card" :description="item.title" :loading="item.loading">
            <div class="dashboard-list-card__number">{{ item.number }}</div>
            <div class="dashboard-list-card__text">
              <div class="dashboard-list-card__text-left">
                {{ item.name }}
              </div>
            </div>
          </t-card>
        </t-col>
      </t-row>
    </t-card>
    <t-row :gutter="[16, 16]" class="row-margin">
      <t-col :xs="12" :xl="9">
        <t-card class="dashboard-detail-card" hover-shadow title="Top Bug Popularity" :loading="is_loading">
          <div id="lineContainer"
               :style="{ width: `100%`, height: `${resizeTime * 580}px`, margin: '0 auto' }"
          />
        </t-card>
      </t-col>
      <t-col :xs="12" :xl="3">
        <t-card title="Error VS Exception" hover-shadow class="dashboard-chart-card" :loading="is_loading">
          <div
            id="EEContainer"
            ref="EEContainer"
            :style="{ width: `${resizeTime * 326}px`, height: `${resizeTime * 300}px`, margin: '0 auto' }"
          />
        </t-card>
        <t-card title="Syntax Error VS Fatal Error" hover-shadow subtitle="Top 7"
                :class="['row-margin','dashboard-chart-card']" :loading="is_loading">
          <div
            id="SFEContainer"
            ref="SFEContainer"
            :style="{ width: `${resizeTime * 326}px`, height: `${resizeTime * 300}px`, margin: 'auto' }"
          />
        </t-card>
      </t-col>
    </t-row>
    <t-card title="Treemap & SunBurst" hover-shadow :class="['row-margin','dashboard-detail-card']"
            :loading="is_loading">
      <div
        id="TSContainer"
        ref="TSContainer"
        :style="{ width: `100%`, height: `${resizeTime * 580}px`, margin: '0 auto' }"
      />
    </t-card>
    <t-card title="Category Comparison" hover-shadow :class="['row-margin','dashboard-detail-card']"
            :loading="is_loading">
      <template #actions>
        <t-space align="center">
          <t-select autoWidth showArrow defaultValue="Exception" :disabled="is_loading"
                    @change="handleSelectionChange">
            <t-option key="Exception" label="Exception" value="Exception"/>
            <t-option key="FatalError" label="Fatal Error" value="FatalError"/>
            <t-option key="SyntaxError" label="Syntax Error" value="SyntaxError"/>
          </t-select>
        </t-space>
      </template>
      <div
        id="CEContainer"
        ref="CEContainer"
        style="width: 100%; height: 351px; display: flex; align-items: center; justify-content: center;"
      >
        <t-alert v-if="isLoadingFailed" theme="error"
                 @close="handleLoadingFailedAlertClose">
          <template #close>
            <font-awesome-icon :icon="['fas', 'rotate-right']"/>
          </template>
          Loading Failed
        </t-alert>
      </div>
    </t-card>
  </div>
</template>

<script lang="ts">
export default {
  name: 'BugPopularity',
};
</script>

<script setup lang="ts">
import {reactive, nextTick, ref, onMounted, onUnmounted, watch, computed, onDeactivated, onActivated} from 'vue';
import * as echarts from 'echarts/core';
import {GridComponent, TooltipComponent, LegendComponent,ToolboxComponent} from 'echarts/components';
import {BarChart,LineChart, PieChart, SunburstChart, TreemapChart} from 'echarts/charts';
import {CanvasRenderer} from 'echarts/renderers';
import {getFolderLineDataSet, getPieChartDataSet, constructTopicPopularityBarChartInitDataset} from './index';
import {PANE_LIST_DATA} from './constants';
import {useSettingStore} from '@/store';
import {changeChartsTheme, getChartListColor} from '@/utils/color';
import * as bugApi from '@/api/bug';
import {UniversalTransition} from 'echarts/features';


echarts.use([BarChart,ToolboxComponent,GridComponent, LegendComponent, TooltipComponent, LineChart, PieChart, TreemapChart, UniversalTransition, SunburstChart, CanvasRenderer]);
let is_loading = ref(true);
const store = useSettingStore();
const chartColors = computed(() => store.chartColors);
const paneListData = ref(PANE_LIST_DATA);
const state = reactive({
  Error: null,
  Exception: null,
  SyntaxErrorList: null,
  FatalErrorList: null,
  ExceptionList: null,
});
let isMounted = ref(false);
let undeactivated = false;

const getErrorAndException = async () => {
  try {
    const {Error, Exception} = await bugApi.getErrorAndException();
    paneListData.value[0].number = Exception;
    paneListData.value[1].number = Error;
    paneListData.value[0].loading = false;
    paneListData.value[1].loading = false;
    state.Error = Error;
    state.Exception = Exception;
  } catch (e) {
    console.log(e);
  } finally {
  }
};

const getSyntaxError = async () => {
  try {
    const {list: SyntaxErrorList} = await bugApi.getSyntaxError(10);
    paneListData.value[2].number = SyntaxErrorList[0].value;
    paneListData.value[2].name = SyntaxErrorList[0].name;
    paneListData.value[5].number = SyntaxErrorList[1].value;
    paneListData.value[5].name = SyntaxErrorList[1].name;
    paneListData.value[2].loading = false;
    paneListData.value[5].loading = false;
    state.SyntaxErrorList = SyntaxErrorList;
  } catch (e) {
    console.log(e);
  } finally {
  }
};

const getFatalError = async () => {
  try {
    const {list: FatalErrorList} = await bugApi.getFatalError(10);
    paneListData.value[3].number = FatalErrorList[0].value;
    paneListData.value[3].name = FatalErrorList[0].name;
    paneListData.value[6].number = FatalErrorList[1].value;
    paneListData.value[6].name = FatalErrorList[1].name;
    paneListData.value[3].loading = false;
    paneListData.value[6].loading = false;
    state.FatalErrorList = FatalErrorList;
  } catch (e) {
    console.log(e);
  } finally {
  }
};

const getException = async () => {
  try {
    const {list: ExceptionList} = await bugApi.getException(10);
    paneListData.value[4].number = ExceptionList[0].value;
    paneListData.value[4].name = ExceptionList[0].name;
    paneListData.value[7].number = ExceptionList[1].value;
    paneListData.value[7].name = ExceptionList[1].name;
    paneListData.value[4].loading = false;
    paneListData.value[7].loading = false;
    state.ExceptionList = ExceptionList;
  } catch (e) {
    console.log(e);
  } finally {
  }
};

// lineChart logic
let lineContainer: HTMLElement;
let lineChart: echarts.ECharts;
const renderLineChart = () => {
  lineContainer = document.getElementById('lineContainer');
  lineChart = echarts.init(lineContainer);
  lineChart.setOption(getFolderLineDataSet({
    ...chartColors.value,
    syntaxErrorData: state.SyntaxErrorList,
    fatalErrorData: state.FatalErrorList,
    exceptionData: state.ExceptionList,
  }));
};
const resizeTime = ref(1);
// chartSize update
const updateContainer = () => {
  lineChart?.resize({
    width: lineContainer.clientWidth,
    height: lineContainer.clientHeight,
  });
  if (document.documentElement.clientWidth >= 1400 && document.documentElement.clientWidth < 1920) {
    resizeTime.value = Number((document.documentElement.clientWidth / 2080).toFixed(2));
  } else if (document.documentElement.clientWidth < 1080) {
    resizeTime.value = Number((document.documentElement.clientWidth / 1080).toFixed(2));
  } else {
    resizeTime.value = 1;
  }
  EEChart.resize({
    width: resizeTime.value * 326,
    height: resizeTime.value * 326,
  });
  SFEChart.resize({
    width: resizeTime.value * 326,
    height: resizeTime.value * 326,
  });
  TSChart?.resize({
    width: TSContainer.clientWidth,
    height: TSContainer.clientHeight,
  });
  topicPopularityBarChart?.resize({
    width: CEContainer.clientWidth,
    height: CEContainer.clientHeight,
  });
};


let EEContainer: HTMLElement;
let EEChart: echarts.ECharts;
const renderEEChart = () => {
  if (!EEContainer) {
    EEContainer = document.getElementById('EEContainer');
  }
  const data = [
    {value: state.Error, name: 'Error'},
    {value: state.Exception, name: 'Exception'},
  ];
  EEChart = echarts.init(EEContainer);
  EEChart.setOption(getPieChartDataSet({
    ...chartColors.value,
    data: data,
    name: "Error VS Exception",
  }));
};


let SFEContainer: HTMLElement;
let SFEChart: echarts.ECharts;
const renderSFEChart = () => {
  if (!SFEContainer) {
    SFEContainer = document.getElementById('SFEContainer');
  }
  let syntaxErrorData = 0;
  let fatalErrorData = 0;
  if (state.SyntaxErrorList) {
    syntaxErrorData = state.SyntaxErrorList.reduce((total: number, item: { value: number; }) => {
      return total + item.value;
    }, 0);
  }
  if (state.FatalErrorList) {
    fatalErrorData = state.FatalErrorList.reduce((total: number, item: { value: number; }) => {
      return total + item.value;
    }, 0);
  }
  const data = [
    {value: syntaxErrorData, name: 'Syntax Error'},
    {value: fatalErrorData, name: 'Fatal Error'},
  ];
  SFEChart = echarts.init(SFEContainer);
  SFEChart.setOption(getPieChartDataSet({
    ...chartColors.value,
    data: data,
    name: "Syntax Error VS Fatal Error",
  }));
};
let metricBarChart: number = 0;
let CEContainer: HTMLElement;
let topicPopularityBarChart: echarts.ECharts;
let isLoadingFailed = false;
let reloadTSChart = undefined;


const handleSelectionChange = (value: string, context: { trigger: string; }) => {
  if (context.trigger === 'check') {
    switch (value) {
      case "Exception":
        metricBarChart = 0;
        break;
      case "FatalError":
        metricBarChart = 1;
        break;
      case "SyntaxError":
        metricBarChart = 2;
        break;
    }
    if (topicPopularityBarChart !== undefined) {
      [topicPopularityBarChart].forEach((item) => {
        item.dispose();
      });
      renderTopicPopularityBarChart(metricBarChart);
    }
  }
};

const renderTopicPopularityBarChart = (metric: number) => {
  if (!CEContainer) {
    CEContainer = document.getElementById('CEContainer');
  }
  topicPopularityBarChart = echarts.init(CEContainer, null, {
    renderer: 'svg'
  });
  topicPopularityBarChart.setOption(constructTopicPopularityBarChartInitDataset({
    ...chartColors.value,
    syntaxErrorData: state.SyntaxErrorList,
    fatalErrorData: state.FatalErrorList,
    exceptionData: state.ExceptionList,
    metric
  }));
};
const handleLoadingFailedAlertClose = () => {

}

let TSContainer: HTMLElement;
let TSChart: echarts.ECharts;
const renderTSChart = () => {
  TSContainer = document.getElementById('TSContainer');
  TSChart = echarts.init(TSContainer);
  let data = {
    name: 'Bugs',
    children: [
      {
        name: 'Exceptions',
        children: state.ExceptionList
      },
      {
        name: 'Syntax Errors',
        children: state.SyntaxErrorList
      },
      {
        name: 'Fatal Errors',
        children: state.FatalErrorList
      },]
  }
  const treemapOption: echarts.EChartsCoreOption = {
    color: getChartListColor(),
    tooltip: {
      trigger: 'item',
      formatter: '{b}'
    },
    series: [
      {
        type: 'treemap',
        id: 'echarts-package-size',
        animationDurationUpdate: 1000,
        roam: false,
        nodeClick: undefined,
        data: data.children,
        universalTransition: true,
        label: {
          show: true
        },
        breadcrumb: {
          show: false
        }
      }
    ]
  };

  const sunburstOption: echarts.EChartsCoreOption = {
    color: getChartListColor(),
    tooltip: {
      trigger: 'item',
      formatter: '{b}'
    },
    series: [
      {
        type: 'sunburst',
        id: 'echarts-package-size',
        radius: ['20%', '90%'],
        animationDurationUpdate: 1000,
        nodeClick: undefined,
        data: data.children,
        universalTransition: true,
        itemStyle: {
          borderWidth: 1,
          borderColor: 'rgba(255,255,255,.5)'
        },
        label: {
          show: false
        }
      }
    ]
  };
  let currentOption = treemapOption;
  TSChart.setOption(currentOption);
  reloadTSChart = setInterval(function () {
    currentOption =
      currentOption === treemapOption ? sunburstOption : treemapOption;
    currentOption.color = getChartListColor();
    TSChart.setOption(currentOption);
    changeChartsTheme([TSChart])
  }, 5000);
};

const renderCharts = () => {
  if (reloadTSChart !== undefined) {
  clearInterval(reloadTSChart)
}
  if (document.documentElement.clientWidth >= 1400 && document.documentElement.clientWidth < 1920) {
    resizeTime.value = Number((document.documentElement.clientWidth / 2080).toFixed(2));
  } else if (document.documentElement.clientWidth < 1080) {
    resizeTime.value = Number((document.documentElement.clientWidth / 1080).toFixed(2));
  } else {
    resizeTime.value = 1;
  }
  renderTopicPopularityBarChart(0);
  renderEEChart();
  renderSFEChart();
  renderLineChart();
  renderTSChart();
};

onMounted(() => {
  Promise.all([
    getErrorAndException(),
    getFatalError(),
    getSyntaxError(),
    getException(),
  ]).then(() => {
    is_loading.value = false;
    is_loading.value = false;
    is_loading.value = false;
    is_loading.value = false;
    is_loading.value = false;
    undeactivated = false;
    nextTick(() => {
      renderCharts();
      window.addEventListener('resize', updateContainer, false);
      nextTick(() => {
        updateContainer();
      });
      isMounted.value = true;
    });
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', updateContainer);
});

onDeactivated(() => {
undeactivated = true;
});

onActivated(() => {
  undeactivated = false;
  if (!isMounted.value) {
    return;
  }
  changeChartsTheme([lineChart, EEChart, SFEChart, TSChart,topicPopularityBarChart]);
  [lineChart, EEChart, SFEChart, TSChart,topicPopularityBarChart].forEach((item) => {
    item.dispose();
  });
  renderCharts();
});

watch(
  () => store.brandTheme,
  () => {
    if(!undeactivated) {
      [lineChart, EEChart, SFEChart, TSChart, topicPopularityBarChart].forEach((item) => {
        item.dispose();
      });
      renderCharts();
      changeChartsTheme([lineChart, EEChart, SFEChart, TSChart, topicPopularityBarChart]);
    }
  },
);
watch(
  () => store.mode,
  () => {
    if(!undeactivated) {
      [lineChart, EEChart, SFEChart, TSChart, topicPopularityBarChart].forEach((item) => {
        item.dispose();
      });
      renderCharts();
    }
  },
);
</script>

<style lang="less" scoped>
.row-margin {
  margin-top: 16px;
}

// 统一增加8px;
.dashboard-detail-card {
  padding: 12px;

  :deep(.t-card__title) {
    font-size: 30px;
    font-weight: 500;
  }

  :deep(.t-card__actions) {
    display: flex;
    align-items: center;
  }
}

.dashboard-chart-card {
  :deep(.t-card__header) {
    padding-top: 8px;
    padding-bottom: 8px;
  }

  :deep(.t-card__body) {
    padding: 0;
  }

  display: flex;
  flex-direction: column;
  flex: 0;
  //height: 255px;
}

.dashboard-list-card {
  display: flex;
  flex-direction: column;
  flex: 1;
  height: 160px;
  padding: 8px;

  :deep(.t-card__description) {
    font-size: 18px;
    color: var(--td-text-color-brand);
    text-align: left;
    line-height: 18px;
  }

  :deep(.t-card__header) {
    padding-bottom: 8px;
  }

  :deep(.t-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding-top: 0;
  }

  &.dark {
    &:hover {
      background: var(--td-gray-color-14);
      cursor: pointer;
    }
  }

  &.light {
    &:hover {
      background: var(--td-gray-color-14);
      cursor: pointer;
    }
  }

  &__number {
    font-size: 36px;
    line-height: 44px;
    color: var(--td-text-color-primary);
  }

  &__text {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    color: var(--td-text-color-placeholder);
    text-align: left;
    line-height: 18px;

    &-left {
      display: flex;
      color: #00a870;

      .icon {
        margin: 0 8px;
      }
    }
  }
}
</style>
