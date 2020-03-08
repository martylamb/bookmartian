<template>
  <span class='tile'>
    <a :href="'http://localhost:4567/api/visit?url=' + this.url">
      <img :src='this.imageUrl' class='tileImage'/>
    </a>
  </span>
</template>

<script>

export default {
  name: 'Tile',
  props: {
    title: String,
    url: String,
    imageUrl: String
  },
  directives: {
  }
}

</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>

.tile {
  width: 108px;
  height: 90px;
  margin-top: 24px;
  margin-bottom: 24px;
  margin-left: 8px;
  margin-right: 8px;
  float: left;
  background-color: grey;
  border-radius: 12px;
  box-shadow: 0 0.3px 0.9px rgba(0, 0, 0, .12), 0 1.6px 3.6px rgba(0, 0, 0, .12);
}

.tileImage {
  width: 100%;
  height: 100%;
  border-radius: 12px;
}

</style>
