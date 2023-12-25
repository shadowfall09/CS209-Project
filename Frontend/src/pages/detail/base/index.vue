<template>
  <div class="detail-base">
    <t-card :title="title" hover-shadow :bordered="false">
      {{ infoMessage }}
    </t-card>

    <t-card class="container-base-margin-top" :bordered="false" hover-shadow>
      <t-space>
        <t-input v-model="inputValue" :status="isInputInvalid ? 'warning' : null" :tips="isInputInvalid ? 'Please input a topic' : null" placeholder="Search Input" clearable style="width: 700px" @change="updateSearchTopic">
        </t-input>
        <t-button @click="searchRelatedTopic">
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
          alignItems: 'flex-start',
          justifyContent: 'left'
        }"
      >
        <t-base-table
          v-if="(!isLoading)&&(!isLoadingFailed)"
          hover
          row-key="index"
          :data="tableData"
          :columns="columns"
          :pagination="pagination"
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
  </div>
</template>
<script setup lang="ts">

import {SearchIcon} from 'tdesign-icons-vue-next';
import {nextTick, onMounted, reactive, ref} from 'vue';
import {getRelatedTopicList} from "@/api/topic";
import {RelatedTopicInfo} from "@/api/model/topicRelatedModel";

const tableData = ref([]);
const total = 5;
let inputTopic: string = undefined;
let isInputInvalid = ref(false);
let isLoading = ref(false);
let isLoadingFailed = false;
let inputValue = ref("");
let relatedTopicData: Array<RelatedTopicInfo> = undefined;


const updateSearchTopic = (value: string) => {
  isInputInvalid.value = value === "";
  inputTopic = value;
}

const fetchData = async () => {
  isLoading.value = true;
  isLoadingFailed= false;
  await nextTick();
  relatedTopicData = undefined;
  sessionStorage.removeItem("inputTopic");
  sessionStorage.removeItem("relatedTopicData");
  try {
    const { relatedTopicList } = await getRelatedTopicList(inputTopic);
    relatedTopicData = relatedTopicList;
    sessionStorage.setItem("inputTopic", inputTopic);
    sessionStorage.setItem("relatedTopicData", JSON.stringify(relatedTopicData));
  } catch (e) {
    console.log(e);
    isLoadingFailed = true;
  } finally {
    isLoading.value = false;
    await nextTick();
  }
  return;
};

const searchRelatedTopic = () => {
  if ((inputTopic !== undefined) && (inputTopic !== "")) {
    fetchData().then(() => {
      if (relatedTopicData !== undefined) {
        renderTable();
      }
    })
  }
}

const renderTable = () => {
  tableData.value = relatedTopicData.map((relatedTopicInfo: RelatedTopicInfo, index: number) => {
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
  defaultCurrent: 1,
  defaultPageSize: 5,
  total: tableData.value.length,
  showJumper: true,
});

const title = 'Related Topic';
const infoMessage = `Experience a refined exploration of Stack Overflow topics. Simply input any word or phrase, and witness related topics ranked by their "intimacy." This unique metric provides a nuanced understanding of connections. Visualize these relationships seamlessly, enhancing your knowledge journey. Elevate your exploration of information with our sophisticated feature.`;

onMounted(() => {
  let inputTopicStored = sessionStorage.getItem("inputTopic");
  let relatedTopicDataString = sessionStorage.getItem("relatedTopicData");
  if ((inputTopicStored != null) && (relatedTopicDataString != null)) {
    inputTopic = inputTopicStored;
    inputValue.value = inputTopicStored;
    relatedTopicData = JSON.parse(relatedTopicDataString);
    renderTable();
  }
});

</script>


<style lang="less" scoped>
@import './index.less';
</style>
