<template>
  <div class="detail-base">
    <t-card :title="title" hover-shadow :bordered="false">
      {{ infoMessage }}
    </t-card>

    <t-card class="container-base-margin-top" :bordered="false" hover-shadow>
      <t-space>
        <t-input :disabled="isLoading" v-model="inputValue" :status="isInputInvalid ? 'warning' : null" :tips="isInputInvalid ? 'Please input a topic' : null" placeholder="Search Input" clearable style="width: 700px" @change="updateSearchTopic">
        </t-input>
        <t-button :loading="isLoading" @click="searchRelatedTopic">
          <template #icon> <SearchIcon /></template>
          Search</t-button>
      </t-space>
    </t-card>

    <t-card class="container-base-margin-top" :bordered="false" hover-shadow :loading="isLoading">
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
        <t-base-table
          v-if="(!isLoading)&&(!isLoadingFailed)"
          height="285px"
          hover
          row-key="index"
          :data="tableData"
          :columns="columns"
          :pagination="pagination"
          @page-change="onPageChange"
        ></t-base-table>
        <t-alert v-if="isLoadingFailed" theme="error"
                 @close="searchRelatedTopic">
          <template #close>
            <font-awesome-icon :icon="['fas', 'rotate-right']" />
          </template>
          Loading Failed
        </t-alert>
      </div>
    </t-card>

    <t-card class="container-base-margin-top" :title="titleBarChart" hover-shadow :loading="isLoading">
      <template #actions>
        <t-space align="center">
          <t-select autoWidth showArrow defaultValue="relatedPostNumber" :disabled="isLoading"
                    @change="handleSelectionChange">
            <t-option key="relatedPostNumber" label="Related Post Number" value="relatedPostNumber"/>
            <t-option key="relevance" label="Relevance" value="relevance"/>
          </t-select>
        </t-space>
      </template>
      <div
        id="relatedTopicBarChartContainer"
        ref="relatedTopicBarChartContainer"
        style="width: 100%; height: 600px; display: flex; align-items: center; justify-content: center;"
      >
        <t-alert v-if="(!relatedTopicData) && (!isLoading)">
          Please Search
        </t-alert>
        <t-alert v-if="isLoadingFailed" theme="error"
                 @close="searchRelatedTopic">
          <template #close>
            <font-awesome-icon :icon="['fas', 'rotate-right']"/>
          </template>
          Loading Failed
        </t-alert>
      </div>
    </t-card>
    <t-card class="container-base-margin-top" :bordered="false" hover-shadow>
      <t-space>
        <t-input :disabled="isLoading1" v-model="inputValueTopic1" :status="isInputInvalidTopic1 ? 'warning' : null" :tips="isInputInvalidTopic1 ? 'Please input a topic' : null" placeholder="Input Topic1" clearable style="width: 350px" @change="updateSearchTopic1">
        </t-input>
        <t-input :disabled="isLoading1" v-model="inputValueTopic2" :status="isInputInvalidTopic2 ? 'warning' : null" :tips="isInputInvalidTopic2 ? 'Please input a topic' : null" placeholder="Input Topic2" clearable style="width: 350px" @change="updateSearchTopic2">
        </t-input>
        <t-button :loading="isLoading1" @click="searchTopicRelevance">
          <template #icon> <SearchIcon /></template>
          Search</t-button>
      </t-space>
    </t-card>
    <t-card class="container-base-margin-top" :title="titleTopicRelevanceChart" hover-shadow :loading="isLoading1">
      <div
        id="topicRelevanceChartContainer"
        ref="topicRelevanceChartContainer"
        style="width: 100%; height: 600px; display: flex; align-items: center; justify-content: center;"
      >
        <t-alert v-if="(!topicRelevanceData) && (!isLoading1)">
          Please Search
        </t-alert>
        <t-alert v-if="isLoadingFailed1" theme="error"
                 @close="searchTopicRelevance">
          <template #close>
            <font-awesome-icon :icon="['fas', 'rotate-right']"/>
          </template>
          Loading Failed
        </t-alert>
      </div>
    </t-card>
  </div>
</template>
<script setup lang="ts">

import {SearchIcon} from 'tdesign-icons-vue-next';
import {computed, nextTick, onMounted, onUnmounted, reactive, Ref, ref, watch} from 'vue';
import {getRelatedTopicList, getRelevance} from "@/api/topic";
import {RelatedTopicInfo, RelevanceInfo} from "@/api/model/topicRelatedModel";
import {EChartOption} from 'echarts';
import * as echarts from "echarts/core";
import {constructTopicPopularityBarChartInitDataset} from "@/pages/dashboard/base/index";
import {useSettingStore} from "@/store";
import {changeChartsTheme} from "@/utils/color";
import {
  constructRelatedTopicBarChartInitDataset,
  constructTopicRelevanceChartInitDataset
} from "@/pages/detail/base/index";
import {GridComponent, LegendComponent, ToolboxComponent, TooltipComponent, TitleComponent} from "echarts/components";
import {BarChart, LineChart, PieChart, GaugeChart} from "echarts/charts";
import {CanvasRenderer, SVGRenderer} from "echarts/renderers";
import {LabelLayout} from "echarts/features";

echarts.use([ToolboxComponent, TooltipComponent, LegendComponent, GridComponent, TitleComponent, LineChart, BarChart, PieChart, CanvasRenderer, SVGRenderer, LabelLayout, GaugeChart]);

const tableData = ref([]);
const total = 5;
let inputTopic: string = undefined;
let inputTopic1: string = undefined;
let inputTopic2: string = undefined;
let isInputInvalid = ref(false);
let isInputInvalidTopic1 = ref(false);
let isInputInvalidTopic2 = ref(false);
let isLoading = ref(false);
let isLoading1 = ref(false);
let isLoadingFailed = false;
let isLoadingFailed1 = false;
let inputValue = ref("");
let inputValueTopic1 = ref("");
let inputValueTopic2 = ref("");
let relatedTopicData: Ref<Array<RelatedTopicInfo>> = ref(undefined);
let topicRelevanceData: Ref<RelevanceInfo> = ref(undefined);

let relatedTopicBarChartContainer: HTMLElement;
let relatedTopicBarChart: echarts.ECharts;
let topicRelevanceChartContainer: HTMLElement;
let topicRelevanceChart: echarts.ECharts;
let metricBarChart: number = 0;
let metricRelevanceChart: number = 0;

const store = useSettingStore();
const resizeTime = ref(1);

const chartColors = computed(() => store.chartColors);

const updateSearchTopic = (value: string) => {
  isInputInvalid.value = value === "";
  inputTopic = value;
}

const updateSearchTopic1 = (value: string) => {
  isInputInvalidTopic1.value = value === "";
  inputTopic1 = value;
}

const updateSearchTopic2 = (value: string) => {
  isInputInvalidTopic2.value = value === "";
  inputTopic2 = value;
}

const fetchData = async () => {
  isLoading.value = true;
  isLoadingFailed= false;
  await nextTick();
  relatedTopicData.value = undefined;
  sessionStorage.removeItem("inputTopic");
  sessionStorage.removeItem("relatedTopicData");
  try {
    const { relatedTopicList } = await getRelatedTopicList(inputTopic);
    relatedTopicData.value = relatedTopicList;
    sessionStorage.setItem("inputTopic", inputTopic);
    sessionStorage.setItem("relatedTopicData", JSON.stringify(relatedTopicData.value));
  } catch (e) {
    console.log(e);
    isLoadingFailed = true;
  } finally {
    isLoading.value = false;
    await nextTick();
  }
  return;
};

const fetchData1 = async () => {
  isLoading1.value = true;
  isLoadingFailed1 = false;
  await nextTick();
  topicRelevanceData.value = undefined;
  sessionStorage.removeItem("inputTopic1");
  sessionStorage.removeItem("inputTopic2");
  sessionStorage.removeItem("topicRelevanceData");
  try {
    topicRelevanceData.value = await getRelevance(inputTopic1, inputTopic2);
    sessionStorage.setItem("inputTopic1", inputTopic1);
    sessionStorage.setItem("inputTopic2", inputTopic2);
    sessionStorage.setItem("topicRelevanceData", JSON.stringify(topicRelevanceData.value));
  } catch (e) {
    console.log(e);
    isLoadingFailed1 = true;
  } finally {
    isLoading1.value = false;
    await nextTick();
  }
  return;
};

const searchRelatedTopic = () => {
  if ((inputTopic !== undefined) && (inputTopic !== "")) {
    isInputInvalid.value = false;
    fetchData().then(() => {
      pagination.current = 1;
      pagination.pageSize = 5;
      if (relatedTopicData.value !== undefined) {
        renderTable();
        renderCharts(0);
        nextTick(() => {
          updateContainer();
        });
      }
    })
  }
  else {
    isInputInvalid.value = true;
  }
}

const searchTopicRelevance = () => {
  if ((inputTopic1 !== undefined) && (inputTopic1 !== "") && (inputTopic2 !== undefined) && (inputTopic2 !== "")) {
    isInputInvalidTopic1.value = false;
    isInputInvalidTopic2.value = false;
    fetchData1().then(() => {
      if (topicRelevanceData.value !== undefined) {
        renderCharts1(0);
        nextTick(() => {
          updateContainer1();
        });
      }
    })
  }
  else {
    if ((inputTopic1 === undefined) || (inputTopic1 === "")) {
      isInputInvalidTopic1.value = true;
    }
    if ((inputTopic2 === undefined) || (inputTopic2 === "")) {
      isInputInvalidTopic2.value = true;
    }
  }
}

const renderTable = () => {
  tableData.value = relatedTopicData.value.map((relatedTopicInfo: RelatedTopicInfo, index: number) => {
    return {
      rank: index + 1,
      topic: relatedTopicInfo.topic,
      relatedPostNumber: relatedTopicInfo.relatedPostNumber,
      relevance: relatedTopicInfo.relevance
    }
  });
  pagination.total = tableData.value.length;
}

const columns = ref([
  {colKey: 'rank', title: 'Rank'},
  {colKey: 'topic', title: 'Topic'},
  {colKey: 'relatedPostNumber', title: 'Related Post Number'},
  {colKey: 'relevance', title: 'Relevance'},
]);

const pagination = reactive({
  current: 1,
  pageSize: 5,
  total: tableData.value.length,
  showJumper: true,
});

const onPageChange = (pageInfo: { current: number; pageSize: number; }) => {
  sessionStorage.setItem("currentPage", pageInfo.current.toString());
  sessionStorage.setItem("currentPageSize", pageInfo.pageSize.toString());
  pagination.current = pageInfo.current;
  pagination.pageSize = pageInfo.pageSize;
};

const handleSelectionChange = (value: string, context: { trigger: string; }) => {
  if (context.trigger === 'check') {
    switch (value) {
      case "relatedPostNumber":
        metricBarChart = 0;
        break;
      case "relevance":
        metricBarChart = 1;
        break;
    }
    if (relatedTopicBarChart !== undefined) {
      [relatedTopicBarChart].forEach((item) => {
        item.dispose();
      });

      renderRelatedTopicBarChart(metricBarChart);
    }
  }
};
const renderRelatedTopicBarChart = (metric: number) => {
  if (!relatedTopicBarChartContainer) {
    relatedTopicBarChartContainer = document.getElementById('relatedTopicBarChartContainer');
  }
  if ((relatedTopicData.value !== undefined) && (relatedTopicBarChartContainer)) {
    relatedTopicBarChart = echarts.init(relatedTopicBarChartContainer, null, {
      renderer: 'svg'
    });
    relatedTopicBarChart.setOption(constructRelatedTopicBarChartInitDataset({...chartColors.value}, relatedTopicData.value, metric));
    relatedTopicBarChart.on('datazoom', function(event: EChartOption.DataZoom) {
      const start = event.start;
      const end = event.end;
      const numberOfBars = (end - start) / 100 * relatedTopicData.value.length;
      const shouldShowLabel = numberOfBars < 30;
      relatedTopicBarChart.setOption({
        series: [{
          label: {
            show: shouldShowLabel
          }
        }]
      });
    });
  }
};

const renderTopicRelevanceChart = (metric: number) => {
  if (!topicRelevanceChartContainer) {
    topicRelevanceChartContainer = document.getElementById('topicRelevanceChartContainer');
  }
  if ((topicRelevanceData.value !== undefined) && (topicRelevanceChartContainer)) {
    topicRelevanceChart = echarts.init(topicRelevanceChartContainer, null, {
      renderer: 'svg'
    });
    topicRelevanceChart.setOption(constructTopicRelevanceChartInitDataset({...chartColors.value}, inputTopic1, inputTopic2, topicRelevanceData.value, metric));
  }
};

const renderCharts = (metric: number) => {
  renderRelatedTopicBarChart(metric);
};

const renderCharts1 = (metric: number) => {
  renderTopicRelevanceChart(metric);
};

const updateContainer = () => {
  if (document.documentElement.clientWidth >= 1400 && document.documentElement.clientWidth < 1920) {
    resizeTime.value = Number((document.documentElement.clientWidth / 2080).toFixed(2));
  } else if (document.documentElement.clientWidth < 1080) {
    resizeTime.value = Number((document.documentElement.clientWidth / 1080).toFixed(2));
  } else {
    resizeTime.value = 1;
  }

  if (relatedTopicBarChart !== undefined) {
    relatedTopicBarChart.resize({
      width: relatedTopicBarChartContainer.clientWidth,
      height: relatedTopicBarChartContainer.clientHeight,
    });
  }
};

const updateContainer1 = () => {
  if (document.documentElement.clientWidth >= 1400 && document.documentElement.clientWidth < 1920) {
    resizeTime.value = Number((document.documentElement.clientWidth / 2080).toFixed(2));
  } else if (document.documentElement.clientWidth < 1080) {
    resizeTime.value = Number((document.documentElement.clientWidth / 1080).toFixed(2));
  } else {
    resizeTime.value = 1;
  }

  if (topicRelevanceChart !== undefined) {
    topicRelevanceChart.resize({
      width: topicRelevanceChartContainer.clientWidth,
      height: topicRelevanceChartContainer.clientHeight,
    });
  }
};

const title = 'Related Topic';
const titleBarChart = 'Related Topic Bar Chart';
const infoMessage = `Experience a refined exploration of Stack Overflow topics. Simply input any word or phrase, and witness related topics ranked by their "intimacy." This unique metric provides a nuanced understanding of connections. Visualize these relationships seamlessly, enhancing your knowledge journey. Elevate your exploration of information with our sophisticated feature.`;
const titleTopicRelevanceChart = 'Topic Relevance Chart';

onMounted(() => {
  let inputTopicStored = sessionStorage.getItem("inputTopic");
  let relatedTopicDataString = sessionStorage.getItem("relatedTopicData");
  let inputTopic1Stored = sessionStorage.getItem("inputTopic1");
  let inputTopic2Stored = sessionStorage.getItem("inputTopic2");
  let topicRelevanceDataString = sessionStorage.getItem("topicRelevanceData");
  let currentPage = sessionStorage.getItem("currentPage");
  let currentPageSize = sessionStorage.getItem("currentPageSize");
  if (currentPage !== null) {
    pagination.current = Number(currentPage);
  }
  if (currentPageSize !== null) {
    pagination.pageSize = Number(currentPageSize);
  }
  if ((inputTopicStored != null) && (relatedTopicDataString != null)) {
    inputTopic = inputTopicStored;
    inputValue.value = inputTopicStored;
    relatedTopicData.value = JSON.parse(relatedTopicDataString);
    renderTable();
    renderCharts(0);
    nextTick(() => {
      updateContainer();
    });
  }
  if ((inputTopic1Stored != null) && (inputTopic2Stored != null) && (topicRelevanceDataString != null)) {
    inputTopic1 = inputTopic1Stored;
    inputTopic2 = inputTopic2Stored;
    inputValueTopic1.value = inputTopic1Stored;
    inputValueTopic2.value = inputTopic2Stored;
    topicRelevanceData.value = JSON.parse(topicRelevanceDataString);
    renderCharts1(0);
    nextTick(() => {
      updateContainer1();
    });
  }
  window.addEventListener('resize', updateContainer, false);
  window.addEventListener('resize', updateContainer1, false);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateContainer);
  window.removeEventListener('resize', updateContainer1);
});

watch(
  () => store.brandTheme,
  () => {
    if (relatedTopicBarChart !== undefined) {
      changeChartsTheme([relatedTopicBarChart]);
    }
    if (topicRelevanceChart !== undefined) {
      changeChartsTheme([topicRelevanceChart]);
    }
  },
);

watch(
  () => store.mode,
  () => {
    if (relatedTopicBarChart !== undefined) {
      [relatedTopicBarChart].forEach((item) => {
        item.dispose();
      });

      renderRelatedTopicBarChart(metricBarChart);
    }
    if (topicRelevanceChart !== undefined) {
      [topicRelevanceChart].forEach((item) => {
        item.dispose();
      });

      renderTopicRelevanceChart(metricRelevanceChart);
    }
  },
);

</script>


<style lang="less" scoped>
@import './index.less';
</style>
