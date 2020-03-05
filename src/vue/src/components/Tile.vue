<template>
  <span class='tile'>
    {{this.val}}
  </span>
</template>

<script>

export default {
  name: 'Tile',
  props: {
    val: String
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

</style>
