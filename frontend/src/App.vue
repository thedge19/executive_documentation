<template>
  <div
      id="app"
      class="font-sans fs-6"
      :style="{
      'background-image': 'url(/09-12-2016_yuzhno-russkoe_2.jpg)',
      'background-size': 'cover',       // чтобы изображение заполняло экран
      'background-attachment': 'fixed', // фиксированный фон (опционально)
      'min-height': '100vh'
    }">
    <router-view></router-view>
  </div>
</template>
class="font-sans"