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
      <t-alert v-if="isLoadingFailed" theme="error" :close="renderCloseButton" @close="handleLoadingFailedAlertClose">Loading Failed</t-alert>
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

const handleLoadingFailedAlertClose = () => {
  fetchData().then(() => {
    renderCharts(metric, sort);
    nextTick(() => {
      updateContainer();
    });
  })
}

const handleSwitchChange = (value: boolean) => {
  sort = value;
  if (topicPopularityBarChart !== undefined) {
    [topicPopularityBarChart].forEach((item) => {
      item.dispose();
    });

    renderCharts(metric, sort);
  }
};

const renderCloseButton = (h) => {
  return h('svg', {
    xmlns: "http://www.w3.org/2000/svg",
    height: "1em",
    width: "1em",
    viewBox: "0 0 512 512"
  }, [
    h('path', {
      d: "M463.5 224H472c13.3 0 24-10.7 24-24V72c0-9.7-5.8-18.5-14.8-22.2s-19.3-1.7-26.2 5.2L413.4 96.6c-87.6-86.5-228.7-86.2-315.8 1c-87.5 87.5-87.5 229.3 0 316.8s229.3 87.5 316.8 0c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0c-62.5 62.5-163.8 62.5-226.3 0s-62.5-163.8 0-226.3c62.2-62.2 162.7-62.5 225.3-1L327 183c-6.9 6.9-8.9 17.2-5.2 26.2s12.5 14.8 22.2 14.8H463.5z",
      fill: "currentcolor"
    })
  ]);
}

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
