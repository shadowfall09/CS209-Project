<template>
  <div class="dashboard-panel-detail">
    <t-card title="Exception VS Error Popularity" class="dashboard-detail-card">
      <t-row :gutter="[16, 16]">
        <t-col v-for="(item, index) in PANE_LIST_DATA" :key="index" :xs="6" :xl="3">
          <t-card class="dashboard-list-card" :description="item.title">
            <div class="dashboard-list-card__number">{{ item.number }}</div>
            <div class="dashboard-list-card__text">
              <div class="dashboard-list-card__text-left">
                <trend class="icon" :type="item.upTrend ? 'up' : 'down'" :describe="item.upTrend || item.downTrend" />
              </div>
              <t-icon name="chevron-right" />
            </div>
          </t-card>
        </t-col>
      </t-row>
    </t-card>
    <t-row :gutter="[16, 16]" class="row-margin">
      <t-col :xs="12" :xl="9">
        <t-card class="dashboard-detail-card" title="Top Bug Popularity">
          <div id="lineContainer" style="width: 100%; height: 410px" />
        </t-card>
      </t-col>
      <t-col :xs="12" :xl="3">
          <t-card title="Exception VS Error" class="dashboard-chart-card">
            <div id="pieContainer" style="width: 100%; height: 165px" />
          </t-card>
          <t-card title="Fatal Error VS Syntax Error" :class="['row-margin','dashboard-chart-card']">
            <div id="pieContainer"  style="width: 100%; height: 165px" />
          </t-card>
        </t-col>
    </t-row>
    <t-card :class="['dashboard-detail-card', 'row-margin']" title="Top Exception Popularity">
      <div id="scatterContainer" style="width: 100%; height: 330px" />
    </t-card>
    <t-card :class="['dashboard-detail-card', 'row-margin']" title="Top Syntax Error Popularity">
      <div id="scatterContainer" style="width: 100%; height: 330px" />
    </t-card>
    <t-card :class="['dashboard-detail-card', 'row-margin']" title="Top Fatal Error Popularity">
      <div id="scatterContainer" style="width: 100%; height: 330px" />
    </t-card>
  </div>
</template>

<script lang="ts">
export default {
  name: 'DashboardDetail',
};
</script>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, watch, computed, onDeactivated } from 'vue';
import * as echarts from 'echarts/core';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { LineChart, ScatterChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';
import { getFolderLineDataSet, getScatterDataSet } from './index';
import { PANE_LIST_DATA } from './constants';
import { useSettingStore } from '@/store';
import { changeChartsTheme } from '@/utils/color';
import Trend from '@/components/trend/index.vue';
import { getErrorAndException } from '@/api/bug';
echarts.use([GridComponent, LegendComponent, TooltipComponent, LineChart, ScatterChart, CanvasRenderer]);

const store = useSettingStore();
const chartColors = computed(() => store.chartColors);

const fetchData = async () => {
  try {
    const { Error } = await getErrorAndException();
    console.log(Error);
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
  lineChart.setOption(getFolderLineDataSet({ ...chartColors.value }));
};

// scatterChart logic
let scatterContainer: HTMLElement;
let scatterChart: echarts.ECharts;
const renderScatterChart = () => {
  scatterContainer = document.getElementById('scatterContainer');
  scatterChart = echarts.init(scatterContainer);
  scatterChart.setOption(getScatterDataSet({ ...chartColors.value }));
};

// chartSize update
const updateContainer = () => {
  lineChart?.resize({
    width: lineContainer.clientWidth,
    height: lineContainer.clientHeight,
  });
  scatterChart?.resize({
    width: scatterContainer.clientWidth,
    height: scatterContainer.clientHeight,
  });
};

const renderCharts = () => {
  renderScatterChart();
  renderLineChart();
};

onMounted(() => {
  fetchData();
  renderCharts();
  window.addEventListener('resize', updateContainer, false);
  nextTick(() => {
    updateContainer();
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', updateContainer);
});

onDeactivated(() => {
  storeModeWatch();
  storeBrandThemeWatch();
});

const storeModeWatch = watch(
  () => store.mode,
  () => {
    renderCharts();
  },
);

const storeBrandThemeWatch = watch(
  () => store.brandTheme,
  () => {
    changeChartsTheme([lineChart, scatterChart]);
  },
);

const onSatisfyChange = () => {
  scatterChart.setOption(getScatterDataSet({ ...chartColors.value }));
};

const onMaterialChange = (value: string[]) => {
  const chartColors = computed(() => store.chartColors);
  lineChart.setOption(getFolderLineDataSet({ dateTime: value, ...chartColors.value }));
};
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

.dashboard-chart-card{
  display: flex;
  flex-direction: column;
  flex: 1;
  height: 255px;
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

      .icon {
        margin: 0 8px;
      }
    }
  }
}
</style>
