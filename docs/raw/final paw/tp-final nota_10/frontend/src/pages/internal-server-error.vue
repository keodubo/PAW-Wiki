<template>
  <div class="internal-server-error-page">
    <img src="@/assets/500-error.svg" alt="500 Internal Server Error" class="error-image" />
  </div>
</template>

<style scoped>
.internal-server-error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 2rem;
  min-height: 100vh;
}

.back-link {
  align-self: flex-start;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  color: rgb(var(--v-theme-primary));
  font-size: 1rem;
  margin-bottom: 2rem;
  transition: color 0.3s ease;
}

.back-link:hover {
  color: rgb(var(--v-theme-secondary));
}

.arrow {
  font-size: 1.2rem;
}

.error-image {
  max-width: 400px;
  width: 100%;
  height: auto;
}
</style>
