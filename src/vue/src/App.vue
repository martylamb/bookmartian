<template>
  <div id="app">
    <router-view/>
    <div id="nav">
      <router-link to="/">Home</router-link> |
      <router-link to="/about">About</router-link>
    </div>
  </div>
</template>

<style>

body {
  background-image: url("https://wallpapertag.com/wallpaper/full/1/1/7/884301-gorgerous-xbox-360-wallpaper-hd-1920x1200-smartphone.jpg");
}

#app {
  font-family: Segoe UI, Segoe UI Midlevel, Segoe WP, Arial, Sans-Serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#nav {
  padding: 30px;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #42b983;
}

</style>
