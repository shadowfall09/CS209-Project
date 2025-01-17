<template>
  <t-space direction="vertical" :style="{ width: '100%' }">
    <t-row :gutter="16" class="row-container">
      <t-col :xs="12" :xl="12">
        <t-card :title="title" hover-shadow :loading="isLoading">
          <template #actions>
            <t-space align="center">
              <t-select autoWidth showArrow defaultValue="comprehensiveScore" :disabled="isLoading"
                        @change="handleSelectionChange">
                <t-option key="comprehensiveScore" label="Comprehensive Score" value="comprehensiveScore"/>
                <t-option key="threadNumber" label="Thread Number" value="threadNumber"/>
                <t-option key="threadNumber2023" label="Active Thread Number In 2023" value="threadNumber2023"/>
                <t-option key="averageViewCount" label="Average View Count" value="averageViewCount"/>
                <t-option key="averageVoteCount" label="Average Vote Count" value="averageVoteCount"/>
                <t-option key="discussionPeopleNumber" label="Discussion People Number" value="discussionPeopleNumber"/>
              </t-select>
              <t-switch size="large" :label="['&nbsp;&nbsp;Sorted&nbsp;&nbsp;', 'Unsorted']"
                        @change="handleSwitchChange"></t-switch>
              <t-button shape="circle" theme="primary" @click="refresh">
                <template #icon>
                  <font-awesome-icon :icon="['fas', 'rotate-right']"/>
                </template>
              </t-button>
            </t-space>
          </template>
          <div
            id="topicPopularityBarChartContainer"
            ref="topicPopularityBarChartContainer"
            style="width: 100%; height: 351px; display: flex; align-items: center; justify-content: center;"
          >
            <t-alert v-if="isLoadingFailed" theme="error"
                     @close="refresh">
              <template #close>
                <font-awesome-icon :icon="['fas', 'rotate-right']"/>
              </template>
              Loading Failed
            </t-alert>
          </div>
        </t-card>
      </t-col>
    </t-row>
    <t-row :gutter="[16, 16]" class="row-container">
      <t-col :xs="12" :xl="7">
        <t-card title="Rank of 10 Chosen Topics" hover-shadow :loading="isLoading">
          <template #actions>
            <t-space align="center">
              <t-select autoWidth showArrow defaultValue="comprehensiveScore" :disabled="isLoading"
                        @change="handleSelectionChangeRank">
                <t-option key="comprehensiveScore" label="Comprehensive Score" value="comprehensiveScore"/>
                <t-option key="threadNumber" label="Thread Number" value="threadNumber"/>
                <t-option key="threadNumber2023" label="Active Thread Number In 2023" value="threadNumber2023"/>
                <t-option key="averageViewCount" label="Average View Count" value="averageViewCount"/>
                <t-option key="averageVoteCount" label="Average Vote Count" value="averageVoteCount"/>
                <t-option key="discussionPeopleNumber" label="Discussion People Number" value="discussionPeopleNumber"/>
              </t-select>
              <t-button shape="circle" theme="primary" @click="refresh">
                <template #icon>
                  <font-awesome-icon :icon="['fas', 'rotate-right']"/>
                </template>
              </t-button>
            </t-space>
          </template>
          <div
            :style="isLoadingFailed ? {
              overflow: 'auto',
              width: '100%',
              height: '351px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            } : {
              overflow: 'auto',
              width: '100%',
              height: '351px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'left'
            }"
          >
            <t-space v-if="(!isLoading)&&(!isLoadingFailed)" align="center">
              <t-table :data="TOPIC_DATA_LIST.slice(0, 5)" :columns="RANK_COLUMNS" row-key="topicData">
              </t-table>
              <t-divider layout="vertical" :style="{ height: '300px' }"/>
              <t-table :data="TOPIC_DATA_LIST.slice(5, 10)" :columns="RANK_COLUMNS" row-key="topicData">
              </t-table>
            </t-space>
            <t-alert v-if="isLoadingFailed" theme="error"
                     @close="refresh">
              <template #close>
                <font-awesome-icon :icon="['fas', 'rotate-right']"/>
              </template>
              Loading Failed
            </t-alert>
          </div>
        </t-card>
      </t-col>
      <t-col :xs="12" :xl="5">
        <t-card title="Percentage of 10 Chosen Topics" hover-shadow :loading="isLoading">
          <template #actions>
            <t-space align="center">
              <t-select autoWidth showArrow defaultValue="comprehensiveScore" :disabled="isLoading"
                        @change="handleSelectionChangePercentage">
                <t-option key="comprehensiveScore" label="Comprehensive Score" value="comprehensiveScore"/>
                <t-option key="threadNumber" label="Thread Number" value="threadNumber"/>
                <t-option key="threadNumber2023" label="Active Thread Number In 2023" value="threadNumber2023"/>
                <t-option key="averageViewCount" label="Average View Count" value="averageViewCount"/>
                <t-option key="averageVoteCount" label="Average Vote Count" value="averageVoteCount"/>
                <t-option key="discussionPeopleNumber" label="Discussion People Number" value="discussionPeopleNumber"/>
              </t-select>
              <t-button shape="circle" theme="primary" @click="refresh">
                <template #icon>
                  <font-awesome-icon :icon="['fas', 'rotate-right']"/>
                </template>
              </t-button>
            </t-space>
          </template>
          <div
            id="topicPopularityPercentageChartContainer"
            ref="topicPopularityPercentageChartContainer"
            style="width: 100%; height: 351px; display: flex; align-items: center; justify-content: center;"
          >
            <t-alert v-if="isLoadingFailed" theme="error"
                     @close="refresh">
              <template #close>
                <font-awesome-icon :icon="['fas', 'rotate-right']"/>
              </template>
              Loading Failed
            </t-alert>
          </div>
        </t-card>
      </t-col>
    </t-row>
  </t-space>
</template>

<script lang="ts">
export default {
  name: 'TopicPopularity',
};
</script>

<script setup lang="ts">
import {computed, nextTick, onMounted, onUnmounted, ref, watch} from 'vue';

import * as echarts from 'echarts/core';
import {
  GridComponent,
  LegendComponent,
  LegendComponentOption,
  ToolboxComponent,
  ToolboxComponentOption,
  TooltipComponent
} from 'echarts/components';
import {BarChart, LineChart, PieChart, PieSeriesOption} from 'echarts/charts';
import {LabelLayout} from 'echarts/features';
import {CanvasRenderer, SVGRenderer} from 'echarts/renderers';
import {useSettingStore} from '@/store';
import {changeChartsTheme} from '@/utils/color';
import {
  constructTopicPopularityBarChartInitDataset,
  constructTopicPopularityPercentageChartInitDataset,
  constructTopicPopularityRankChartInitDataset
} from "@/pages/showcase/topic/index";
import {getTopicList} from "@/api/topic";

echarts.use([ToolboxComponent, TooltipComponent, LegendComponent, GridComponent, LineChart, BarChart, PieChart, CanvasRenderer, SVGRenderer, LabelLayout]);

type EChartsOption = echarts.ComposeOption<
  ToolboxComponentOption | LegendComponentOption | PieSeriesOption
>;

const store = useSettingStore();
const resizeTime = ref(1);

const chartColors = computed(() => store.chartColors);

const title = 'Popularity of 10 Chosen Topics';
let isLoading = ref(false);

let topicPopularityBarChartContainer: HTMLElement;
let topicPopularityBarChart: echarts.ECharts;
let topicPopularityRankChartContainer: HTMLElement;
let topicPopularityRankChart: echarts.ECharts;
let topicPopularityPercentageChartContainer: HTMLElement;
let topicPopularityPercentageChart: echarts.ECharts;
let popularityData = undefined;
let isLoadingFailed = false;
let metricBarChart: number = 0;
let metricRankChart: number = 0;
let metricPercentageChart: number = 0;
let sort: boolean = false;

let RANK_COLUMNS = ref([]);
let TOPIC_DATA_LIST = ref([]);

const handleSelectionChange = (value: string, context: { trigger: string; }) => {
  if (context.trigger === 'check') {
    switch (value) {
      case "comprehensiveScore":
        metricBarChart = 0;
        break;
      case "threadNumber":
        metricBarChart = 1;
        break;
      case "threadNumber2023":
        metricBarChart = 2;
        break;
      case "averageViewCount":
        metricBarChart = 3;
        break;
      case "averageVoteCount":
        metricBarChart = 4;
        break;
      case "discussionPeopleNumber":
        metricBarChart = 5;
        break;
    }
    if (topicPopularityBarChart !== undefined) {
      [topicPopularityBarChart].forEach((item) => {
        item.dispose();
      });

      renderTopicPopularityBarChart(metricBarChart, sort);
    }
  }
};

const handleSelectionChangeRank = (value: string, context: { trigger: string; }) => {
  if (context.trigger === 'check') {
    switch (value) {
      case "comprehensiveScore":
        metricRankChart = 0;
        break;
      case "threadNumber":
        metricRankChart = 1;
        break;
      case "threadNumber2023":
        metricRankChart = 2;
        break;
      case "averageViewCount":
        metricRankChart = 3;
        break;
      case "averageVoteCount":
        metricRankChart = 4;
        break;
      case "discussionPeopleNumber":
        metricRankChart = 5;
        break;
    }
    renderTopicPopularityRankChart(metricRankChart);
  }
};

const handleSelectionChangePercentage = (value: string, context: { trigger: string; }) => {
  if (context.trigger === 'check') {
    switch (value) {
      case "comprehensiveScore":
        metricPercentageChart = 0;
        break;
      case "threadNumber":
        metricPercentageChart = 1;
        break;
      case "threadNumber2023":
        metricPercentageChart = 2;
        break;
      case "averageViewCount":
        metricPercentageChart = 3;
        break;
      case "averageVoteCount":
        metricPercentageChart = 4;
        break;
      case "discussionPeopleNumber":
        metricPercentageChart = 5;
        break;
    }
    if (topicPopularityPercentageChart !== undefined) {
      [topicPopularityPercentageChart].forEach((item) => {
        item.dispose();
      });

      renderTopicPopularityPercentageChart(metricPercentageChart);
    }
  }
};

const refresh = () => {
  fetchData().then(() => {
    renderTopicPopularityBarChart(metricBarChart, sort);
    renderTopicPopularityRankChart(metricRankChart);
    renderTopicPopularityPercentageChart(metricPercentageChart);
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

    renderTopicPopularityBarChart(metricBarChart, sort);
  }
};
const fetchData = async () => {
  isLoading.value = true;
  isLoadingFailed = false;
  await nextTick();
  popularityData = undefined;
  sessionStorage.removeItem("popularityData");
  try {
    const {popularity} = await getTopicList(-1);
    popularityData = popularity;
    sessionStorage.setItem("popularityData", JSON.stringify(popularity));
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
  if ((popularityData !== undefined) && (topicPopularityBarChartContainer)) {
    topicPopularityBarChart = echarts.init(topicPopularityBarChartContainer, null, {
      renderer: 'svg'
    });
    topicPopularityBarChart.setOption(constructTopicPopularityBarChartInitDataset({...chartColors.value}, popularityData, metric, sort));
  }
};

const renderTopicPopularityRankChart = (metric: number) => {
  if (popularityData !== undefined) {
    let dataset = constructTopicPopularityRankChartInitDataset({...chartColors.value}, popularityData, metric);
    RANK_COLUMNS.value = dataset.columnTitle;
    TOPIC_DATA_LIST.value = dataset.data;
  }
};

const renderTopicPopularityPercentageChart = (metric: number) => {
  if (!topicPopularityPercentageChartContainer) {
    topicPopularityPercentageChartContainer = document.getElementById('topicPopularityPercentageChartContainer');
  }
  if ((popularityData !== undefined) && (topicPopularityPercentageChartContainer)) {
    topicPopularityPercentageChart = echarts.init(topicPopularityPercentageChartContainer, null, {
      renderer: 'svg'
    });
    topicPopularityPercentageChart.setOption(constructTopicPopularityPercentageChartInitDataset({...chartColors.value}, popularityData, metric));
  }
};

const renderCharts = (metric: number, sort: boolean) => {
  renderTopicPopularityBarChart(metric, sort);
  renderTopicPopularityRankChart(metric);
  renderTopicPopularityPercentageChart(metric);
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
  if (topicPopularityPercentageChart !== undefined) {
    topicPopularityPercentageChart.resize({
      width: topicPopularityPercentageChartContainer.clientWidth,
      height: topicPopularityPercentageChartContainer.clientHeight,
    });
  }
};

onMounted(() => {
  let popularityDataString = sessionStorage.getItem("popularityData");
  if (popularityDataString !== null) {
    popularityData = JSON.parse(popularityDataString);
  }
  if (popularityData === undefined) {
    fetchData().then(() => {
      renderCharts(0, sort);
      nextTick(() => {
        updateContainer();
      });
      window.addEventListener('resize', updateContainer, false);
    })
  } else {
    renderCharts(0, sort);
    nextTick(() => {
      updateContainer();
    });
    window.addEventListener('resize', updateContainer, false);
  }
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
    if (topicPopularityPercentageChart !== undefined) {
      changeChartsTheme([topicPopularityPercentageChart]);
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

      renderTopicPopularityBarChart(metricBarChart, sort);
    }
    if (topicPopularityPercentageChart !== undefined) {
      [topicPopularityPercentageChart].forEach((item) => {
        item.dispose();
      });

      renderTopicPopularityPercentageChart(metricPercentageChart);
    }
  },
);

</script>

<style scoped>
.row-container:not(:last-child) {
  margin-bottom: 16px;
}

:deep() .t-card__body {
  padding-top: 0;
}
</style>
