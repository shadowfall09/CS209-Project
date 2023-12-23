<template>
  <div class="dashboard-panel-detail">
    <t-card title="Exception VS Error Popularity" class="dashboard-detail-card">
      <t-row :gutter="[16, 16]">
        <t-col v-for="(item, index) in paneListData" :key="index" :xs="6" :xl="3">
          <t-card class="dashboard-list-card" :description="item.title" :loading="item.loading">
            <div class="dashboard-list-card__number">{{ item.number }}</div>
            <div class="dashboard-list-card__text">
              <div class="dashboard-list-card__text-left">
                {{item.name}}
              </div>
            </div>
          </t-card>
        </t-col>
      </t-row>
    </t-card>
    <t-row :gutter="[16, 16]" class="row-margin">
      <t-col :xs="12" :xl="9">
        <t-card class="dashboard-detail-card" title="Top Bug Popularity" :loading="line_loading">
          <div id="lineContainer"
               :style="{ width: `100%`, height: `${resizeTime * 580}px`, margin: '0 auto' }"
          />
        </t-card>
      </t-col>
      <t-col :xs="12" :xl="3">
        <t-card title="Error VS Exception" class="dashboard-chart-card" :loading="EE_loading">
          <div
            id="EEContainer"
            ref="EEContainer"
            :style="{ width: `${resizeTime * 326}px`, height: `${resizeTime * 300}px`, margin: '0 auto' }"
          />
        </t-card>
        <t-card title="Syntax Error VS Fatal Error" subtitle="Top 7" :class="['row-margin','dashboard-chart-card']" :loading="SFE_loading">
          <div
            id="SFEContainer"
            ref="SFEContainer"
            :style="{ width: `${resizeTime * 326}px`, height: `${resizeTime * 300}px`, margin: 'auto' }"
          />
        </t-card>
      </t-col>
    </t-row>
    <t-card title="Treemap & SunBurst" :class="['row-margin','dashboard-detail-card']">

    </t-card>
  </div>
</template>

<script lang="ts">
export default {
  name: 'DashboardDetail',
};
</script>

<script setup lang="ts">
import {reactive, nextTick, ref, onMounted, onUnmounted, watch, computed, onDeactivated} from 'vue';
import * as echarts from 'echarts/core';
import {GridComponent, TooltipComponent, LegendComponent} from 'echarts/components';
import {LineChart,PieChart} from 'echarts/charts';
import {CanvasRenderer} from 'echarts/renderers';
import {getFolderLineDataSet,getPieChartDataSet} from './index';
import {PANE_LIST_DATA} from './constants';
import {useSettingStore} from '@/store';
import {changeChartsTheme} from '@/utils/color';
import * as bugApi from '@/api/bug';


echarts.use([GridComponent, LegendComponent, TooltipComponent, LineChart,PieChart, CanvasRenderer]);
let line_loading = ref(true);
let EE_loading = ref(true);
let SFE_loading = ref(true);
const store = useSettingStore();
const chartColors = computed(() => store.chartColors);
const paneListData = ref(PANE_LIST_DATA);
const  state = reactive({
  Error: null,
  Exception: null,
  SyntaxErrorList: null,
  FatalErrorList: null,
  ExceptionList: null,
});

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
};


let EEContainer: HTMLElement;
let EEChart: echarts.ECharts;
const renderEEChart = () => {
  if (!EEContainer) {
    EEContainer = document.getElementById('EEContainer');
  }
  const data = [
    { value: state.Error, name: 'Error' },
    { value: state.Exception, name: 'Exception' },
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
    { value: syntaxErrorData, name: 'Syntax Error' },
    { value: fatalErrorData, name: 'Fatal Error' },
  ];
  SFEChart = echarts.init(SFEContainer);
  SFEChart.setOption(getPieChartDataSet({
    ...chartColors.value,
    data: data,
    name: "Syntax Error VS Fatal Error",
  }));
};

const renderCharts = () => {
  renderEEChart();
  renderSFEChart();
  renderLineChart();
};

onMounted(() => {
  Promise.all([
    getErrorAndException(),
    getFatalError(),
    getSyntaxError(),
    getException(),
  ]).then(() => {
      line_loading.value = false;
      EE_loading.value = false;
      SFE_loading.value = false;
      nextTick(() => {
        renderCharts();
        window.addEventListener('resize', updateContainer, false);
        nextTick(() => {
          updateContainer();
        });
      });
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', updateContainer);
});

onDeactivated(() => {
  storeModeWatch();
  storeBrandThemeWatch();
});


const storeBrandThemeWatch = watch(
  () => store.brandTheme,
  () => {
    changeChartsTheme([lineChart, EEChart, SFEChart]);
  },
);

// const onSatisfyChange = () => {
//   scatterChart.setOption(getScatterDataSet({...chartColors.value}));
// };
//
// const onMaterialChange = (value: string[]) => {
//   const chartColors = computed(() => store.chartColors);
//   lineChart.setOption(getFolderLineDataSet({dateTime: value, ...chartColors.value}));
// };
const storeModeWatch = watch(
  () => store.mode,
  () => {
    [lineChart, EEChart, SFEChart].forEach((item) => {
      item.dispose();
    });
    renderCharts();
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
