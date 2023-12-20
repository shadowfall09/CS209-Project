<template>
  <div class="detail-base">
    <t-card :title="title" hover-shadow :bordered="false">
      {{ infoMessage }}
    </t-card>

    <t-card class="container-base-margin-top" :bordered="false" hover-shadow>
      <t-space>
        <t-input placeholder="Search Input" clearable style="width: 700px">
        </t-input>
        <t-button :icon="renderIcon"> Search</t-button>
      </t-space>
    </t-card>

    <t-card class="container-base-margin-top" :bordered="false" hover-shadow>
      <t-base-table
        disableDataPage
        hover
        row-key="index"
        :data="data"
        :columns="columns"
      ></t-base-table>
    </t-card>
  </div>
</template>
<script setup lang="jsx">

import {SearchIcon} from 'tdesign-icons-vue-next';
import {ref} from 'vue';

const data = [];
const total = 5;
for (let i = 0; i < total; i++) {
  data.push({
    index: i + 1,

    Topic: ['Spring', 'AOC', 'SpringBoot'][i % 3],
    Popularity: ['1234', '345', '1234'][i % 3],
   });
}
const columns = ref([
  {colKey: 'Topic', title: 'Topic'},
  {colKey: 'Popularity', title: 'Popularity'},
]);

const renderIcon = () => {
  return <SearchIcon/>;
};
const title = 'Related Topic';
const infoMessage = `Experience a refined exploration of Stack Overflow topics. Simply input any word or phrase, and witness related topics ranked by their "intimacy." This unique metric provides a nuanced understanding of connections. Visualize these relationships seamlessly, enhancing your knowledge journey. Elevate your exploration of information with our sophisticated feature.`;
</script>


<style lang="less" scoped>
@import './index.less';
</style>
