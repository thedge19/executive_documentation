<template>
  <main :style="{'background-image': 'url(/09-12-2016_yuzhno-russkoe_2.jpg)', 'min-height': '100vh'}">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Добавить СП</h2>
        </div>

        <div class="card-body">
          <form @submit.prevent="addStandard">
            <!-- Наименование -->
            <div class="mb-4">
              <label for="name" class="form-label fw-semibold">
                <i class="bi bi-card-text me-2"></i>Наименование
              </label>
              <input id="name" type="text" class="form-control"
                     placeholder="Введите наименование стандарта"
                     required v-model="standard.name">
            </div>

            <!-- Ошибка -->
            <div v-if="error" class="alert alert-danger mb-4">
              <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
            </div>

            <!-- Кнопка отправки -->
            <div class="d-grid">
              <button type="submit" class="btn btn-primary py-2" :disabled="isLoading">
                <template v-if="isLoading">
                  <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Сохранение...
                </template>
                <template v-else>
                  <i class="bi bi-check-circle me-2"></i>Добавить стандарт
                </template>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const standard = ref({
  name: ''
})
const isLoading = ref(false)
const error = ref(null)

const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    throw new Error('Требуется авторизация')
  }
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  }
}

const handleUnauthorized = () => {
  localStorage.removeItem('token')
  router.push('/login?redirect=' + encodeURIComponent(router.currentRoute.value.fullPath))
}

const addStandard = async () => {
  try {
    isLoading.value = true
    error.value = null

    const response = await fetch('http://localhost:8080/standards', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(standard.value)
    })

    if (response.status === 401) {
      handleUnauthorized()
      return;
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Ошибка при добавлении стандарта';
      return;
    }

    await router.push("/standards")
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message

    if (err.message.includes('авторизация')) {
      handleUnauthorized()
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.card {
  border-radius: 12px;
  overflow: hidden;
}

.form-control {
  border-radius: 8px;
  padding: 10px 15px;
}

.form-label {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.btn {
  border-radius: 8px;
  transition: all 0.2s;
}

.alert {
  border-radius: 8px;
}

@media (max-width: 576px) {
  .card {
    border-radius: 0;
    border-left: none;
    border-right: none;
  }

  .container {
    padding-left: 0;
    padding-right: 0;
  }
}
</style>