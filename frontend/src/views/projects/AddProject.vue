<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Добавить объект</h2>
        </div>

        <div class="card-body">
          <form @submit.prevent="addProject">
            <!-- Наименование -->
            <div class="mb-4">
              <label for="name" class="form-label fw-semibold">
                <i class="bi bi-building me-2"></i>Наименование объекта
              </label>
              <input id="name" type="text" class="form-control"
                     placeholder="Введите наименование объекта"
                     required v-model="project.name">
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
                  Обработка...
                </template>
                <template v-else>
                  <i class="bi bi-check-circle me-2"></i>Добавить объект
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
const project = ref({
  name: ''
})
const error = ref(null)
const isLoading = ref(false)

const addProject = async () => {
  error.value = null
  isLoading.value = true

  try {
    const token = localStorage.getItem('token')
    if (!token) {
      error.value = 'Требуется авторизация'
      router.push('/login')
      return
    }

    const response = await fetch('http://localhost:8080/projects', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(project.value)
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Ошибка при добавлении объекта'

      if (response.status === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      return
    }

    // Успешное создание
    await router.push('/projects')
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message

    if (err.message.includes('401') || err.message.includes('авторизация')) {
      router.push('/login')
    }
  } finally {
    isLoading.value = false
  }
}
</script>