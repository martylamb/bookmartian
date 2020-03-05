<template>
  <div class='home'>
    <div class='fixed-header'>
      <SearchBar />
      <TileArray />
      <div class='page-tabs'>
        <div class='page-tab'>Home</div>
        <div class='page-tab'>Work</div>
        <div class='page-tab'>D&amp;D</div>
        <!-- <div class='page-tab settings'>tags</div> -->
        <div class='page-tab settings'>bookmartian</div>
      </div>
    </div>
    <div class='page-container'>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur tortor enim,
        adipiscing in semper a, pellentesque quis sem. Vivamus eu odio at tellus sollicitudin
        viverra. Sed dignissim molestie imperdiet. Nulla sit amet tortor eu nisl vestibulum
        dignissim. Donec scelerisque vulputate tortor, non dapibus massa molestie at. Pellentesque
        habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.
        Etiam a mauris sit amet augue suscipit dapibus non in lectus. Nunc mattis eros ligula,
        ut tincidunt sapien. Nulla eget velit ipsum. Suspendisse potenti. Suspendisse et
        sem at sem sagittis lobortis. Quisque lacinia, nisl in elementum aliquet, arcu neque
        rhoncus tellus, at ornare mi enim sit amet sem. Phasellus a nisi vel ipsum laoreet
        mollis.
      </p>
      <p>
        In hac habitasse platea dictumst. Aliquam erat volutpat. Vestibulum massa dui, mattis
        eget consectetur a, scelerisque vitae eros. Duis ut ligula nulla. Aenean varius
        odio in eros consectetur tincidunt. Etiam faucibus odio non turpis aliquet viverra.
        Morbi quis massa a velit fermentum vehicula. Quisque felis turpis, consequat eget
        rutrum et, placerat id orci. Pellentesque risus orci, vestibulum ac adipiscing et,
        scelerisque vitae ligula. Morbi fermentum, nisl tincidunt mollis rhoncus, elit ipsum
        porttitor tortor, eu molestie turpis nisl quis mi. In sit amet lorem massa. Aenean
        nisl massa, laoreet a egestas sit amet, pellentesque a quam. Quisque sit amet tortor
        sit amet risus interdum volutpat eget a turpis. Mauris suscipit commodo ligula,
        sed suscipit arcu tristique eget. Integer mattis nisl vitae purus posuere malesuada
        ornare nibh ornare. Aliquam erat volutpat. Proin aliquam elit sit amet ante pharetra
        dictum. Sed malesuada sollicitudin sodales.
      </p>
      <p>
        Vivamus eget tempus nisi. Quisque tincidunt malesuada pharetra. Quisque semper,
        lectus at posuere auctor, nunc purus scelerisque felis, adipiscing tincidunt augue
        lorem placerat nunc. Etiam feugiat magna ac lacus egestas sit amet vestibulum neque
        mattis. Nullam vel massa ut enim sollicitudin hendrerit eu in mi. Maecenas in metus
        eu lacus rhoncus sollicitudin in eu orci. Morbi mi turpis, imperdiet non bibendum
        vel, pharetra at purus. Nulla risus lacus, laoreet eget viverra eget, tincidunt
        sit amet turpis. Proin sit amet neque arcu, vitae accumsan lorem. Nullam tellus
        nibh, condimentum in ultricies eu, placerat vel massa. Pellentesque nec dolor quis
        mauris commodo luctus. Duis nec rhoncus eros. Etiam rutrum rhoncus libero. Donec
        augue diam, rhoncus vitae fringilla ut, tincidunt non ipsum. Fusce nec urna leo,
        non tristique nibh. Sed eget nulla turpis, nec ultrices metus. Aenean in arcu quis
        erat placerat facilisis. Nulla euismod lorem at enim sollicitudin dignissim. Nam
        vitae dignissim mi.
      </p>
      <p>
        Mauris commodo nisl vel est venenatis ornare. Nam porta libero vitae nisl pulvinar
        dapibus. Aliquam hendrerit, dui nec egestas tempor, lorem ipsum vestibulum nulla,
        at faucibus nisi purus eu quam. Nam in ornare felis. Duis sapien ligula, sodales
        nec tincidunt id, imperdiet in ipsum. In interdum posuere hendrerit. Vestibulum
        sed ipsum turpis. Maecenas libero lectus, condimentum sed ultricies quis, aliquet
        vitae nulla. Vivamus eget ipsum tellus. Maecenas cursus egestas lacus, volutpat
        dictum odio faucibus ut. Mauris et risus augue. In tincidunt, felis et scelerisque
        condimentum, ipsum tortor tempus eros, vel adipiscing eros nibh at purus. Fusce
        nec dolor ligula. Nunc felis urna, viverra non sodales venenatis, hendrerit et ipsum.
        Duis nec erat risus. Nunc dapibus felis at est ultricies facilisis. Maecenas vel
        velit in nisi malesuada iaculis vitae bibendum lorem. Donec eu dolor non urna dapibus
        facilisis at in risus.
      </p>
    </div>
  </div>
</template>

<script>
// @ is an alias to /src
import SearchBar from '@/components/SearchBar.vue'
import TileArray from '@/components/TileArray.vue'

export default {
  name: 'Home',
  components: {
    SearchBar,
    TileArray
  }
}
</script>

<!-- Add 'scoped' attribute to limit CSS to this component only -->
<style scoped>
.fixed-header {
  background: no-repeat
    url('http://4.bp.blogspot.com/-2j8EVE2rPfs/UYDPbNbDr3I/AAAAAAAABHQ/0Irk6WfORXw/s1600/BingWallpaper-2013-05-01.jpg')
    fixed;
  position: fixed;
  width: 100%;
}

.page-tabs {
  padding: 1px;
  background-color: #f7f7f7;
  width: 100%;
  font-size: 16px;
  line-height: 24px;
  font-weight: 600;
  display: flex;
  justify-content: center;
}

.page-tab {
  cursor: pointer;
  padding: 12px 24px 12px 24px;
  height: 100%;
}

.page-tab:hover {
  background-color: #eeeeee;
}

.settings {
  color: grey;
}

.page-container {
  background-color: #f7f7f7;
  padding-top: 300px;
  width: 100%;
  height: 100vh;
  margin: 0px;
  color: black;
}
</style>
