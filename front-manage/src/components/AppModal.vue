<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal" :style="{ width }">
      <div class="modal-header">
        <h3>{{ title }}</h3>
        <button type="button" class="modal-close" @click="$emit('close')">×</button>
      </div>
      <div class="modal-body"><slot /></div>
      <div v-if="$slots.footer" class="modal-footer"><slot name="footer" /></div>
    </div>
  </div>
</template>

<script setup>
defineProps({ title: String, width: { type: String, default: '520px' } })
defineEmits(['close'])
</script>
