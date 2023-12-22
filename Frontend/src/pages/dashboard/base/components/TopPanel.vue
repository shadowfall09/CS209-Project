<template>
  <t-card :title="title" hover-shadow :style="{ width: '100%' }" :loading="isLoading">
    <template #actions>
      <t-space align="center">
        <t-select autoWidth showArrow defaultValue="comprehensiveScore" :disabled="isLoading" @change="handleSelectionChange">
          <t-option key="comprehensiveScore" label="Comprehensive Score" value="comprehensiveScore" />
          <t-option key="threadNumber" label="Thread Number" value="threadNumber" />
          <t-option key="threadNumber2023" label="Active Thread Number In 2023" value="threadNumber2023" />
          <t-option key="averageViewCount" label="Average View Count" value="averageViewCount" />
          <t-option key="averageVoteCount" label="Average Vote Count" value="averageVoteCount" />
          <t-option key="discussionPeopleNumber" label="Discussion People Number" value="discussionPeopleNumber" />
        </t-select>
        <t-switch size="large" :label="['&nbsp;&nbsp;Sorted&nbsp;&nbsp;', 'Unsorted']" @change="handleSwitchChange"></t-switch>
      </t-space>
    </template>
    <div
      id="topicPopularityBarChartContainer"
      ref="topicPopularityBarChartContainer"
      style="width: 100%; height: 351px; display: flex; align-items: center; justify-content: center;"
    >
      <t-alert v-if="isLoadingFailed" theme="error">Loading Failed</t-alert>
    </div>
  </t-card>
</template>
<script setup lang="ts">
import {MessagePlugin} from 'tdesign-vue-next';
import { onMounted, watch, ref, onUnmounted, nextTick, computed } from 'vue';

import * as echarts from 'echarts/core';
import { TooltipComponent, LegendComponent, GridComponent } from 'echarts/components';
import { LineChart } from 'echarts/charts';
import { BarChart } from 'echarts/charts';
import { CanvasRenderer } from 'echarts/renderers';
import { useSettingStore } from '@/store';
import { LAST_7_DAYS } from '@/utils/date';
import { changeChartsTheme } from '@/utils/color';
import {constructInitDataset} from "@/pages/dashboard/base/index";
import {constructTopicPopularityInitDataset} from "@/pages/dashboard/base/index";

import Trend from '@/components/trend/index.vue';
import {getTopicList} from "@/api/topic";
import { SVGRenderer } from 'echarts/renderers';

echarts.use([TooltipComponent, LegendComponent, GridComponent, LineChart, BarChart, CanvasRenderer, SVGRenderer]);

const store = useSettingStore();
const resizeTime = ref(1);

const chartColors = computed(() => store.chartColors);

const title = 'Popularity of 10 Chosen Topics';
let isLoading = ref(false);

let topicPopularityBarChartContainer: HTMLElement;
let topicPopularityBarChart: echarts.ECharts;
let popularityData = undefined;
let isLoadingFailed = false;
let metric: number = 0;
let sort: boolean = false;

const handleSelectionChange = (value: string, context: { trigger: string; }) => {
  switch (value) {
    case "comprehensiveScore":
      metric = 0;
      break;
    case "threadNumber":
      metric = 1;
      break;
    case "threadNumber2023":
      metric = 2;
      break;
    case "averageViewCount":
      metric = 3;
      break;
    case "averageVoteCount":
      metric = 4;
      break;
    case "discussionPeopleNumber":
      metric = 5;
      break;
  }
  if (context.trigger === 'check') {
    if (topicPopularityBarChart !== undefined) {
      [topicPopularityBarChart].forEach((item) => {
        item.dispose();
      });

      renderCharts(metric, sort);
    }
  }
};

const handleSwitchChange = (value: boolean) => {
  sort = value;
  if (topicPopularityBarChart !== undefined) {
    [topicPopularityBarChart].forEach((item) => {
      item.dispose();
    });

    renderCharts(metric, sort);
  }
};

const fetchData = async () => {
  isLoading.value = true;
  isLoadingFailed= false;
  await nextTick();
  try {
    const { popularity } = await getTopicList(-1);
    popularityData = popularity;
  } catch (e) {
    console.log(e);
    isLoadingFailed = true;
  } finally {
    isLoading.value = false;
    await nextTick();
  }
  return;
};

const renderTopicPopularityBarChart = (metric: number, sort: boolean) => {
  if (!topicPopularityBarChartContainer) {
    topicPopularityBarChartContainer = document.getElementById('topicPopularityBarChartContainer');
  }
  if (popularityData !== undefined) {
    topicPopularityBarChart = echarts.init(topicPopularityBarChartContainer, null, {
      renderer: 'svg'
    });
    topicPopularityBarChart.setOption(constructTopicPopularityInitDataset({ ...chartColors.value }, popularityData, metric, sort));
  }
  else {
  }
};

const renderCharts = (metric: number, sort: boolean) => {
  renderTopicPopularityBarChart(metric, sort);
};

const updateContainer = () => {
  if (document.documentElement.clientWidth >= 1400 && document.documentElement.clientWidth < 1920) {
    resizeTime.value = Number((document.documentElement.clientWidth / 2080).toFixed(2));
  } else if (document.documentElement.clientWidth < 1080) {
    resizeTime.value = Number((document.documentElement.clientWidth / 1080).toFixed(2));
  } else {
    resizeTime.value = 1;
  }

  if (topicPopularityBarChart !== undefined) {
    topicPopularityBarChart.resize({
      width: topicPopularityBarChartContainer.clientWidth,
      height: topicPopularityBarChartContainer.clientHeight,
    });
  }
};

onMounted(() => {
  fetchData().then(() => {
    renderCharts(metric, sort);
    nextTick(() => {
      updateContainer();
    });
    window.addEventListener('resize', updateContainer, false);
  })
});

onUnmounted(() => {
  window.removeEventListener('resize', updateContainer);
});

watch(
  () => store.brandTheme,
  () => {
    if (topicPopularityBarChart !== undefined) {
      changeChartsTheme([topicPopularityBarChart]);
    }
  },
);

watch(
  () => store.mode,
  () => {
    if (topicPopularityBarChart !== undefined) {
      [topicPopularityBarChart].forEach((item) => {
        item.dispose();
      });

      renderCharts(metric, sort);
    }
  },
);

</script>
