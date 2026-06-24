<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal" :style="{ width: width }">
      <div class="modal-header">
        <div class="modal-title">{{ title }}</div>
        <button type="button" class="modal-close" @click="$emit('close')">×</button>
      </div>
      <div class="modal-body">
        <slot />
      </div>
      <div v-if="$slots.footer" class="modal-actions">
        <slot name="footer" />
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  width: { type: String, default: '440px' }
})

defineEmits(['close'])
</script>
