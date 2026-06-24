<template>
  <div id="app-root">
    <NavBar />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <ToastContainer />
  </div>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
import ToastContainer from './components/ToastContainer.vue'
</script>
