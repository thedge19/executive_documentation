<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
        <div class="card-header bg-white py-4">
          <h2 class="h4 mb-0 text-center text-primary">Редактирование работы</h2>
        </div>

        <div class="card-body">
          <form @submit.prevent="updateWork">
            <!-- Наименование -->
            <div class="mb-4">
              <label for="name" class="form-label fw-semibold">
                <i class="bi bi-card-text me-2"></i>Наименование
              </label>
              <input id="name" type="text" class="form-control"
                     placeholder="Введите наименование работы"
                     required v-model="work.name">
            </div>

            <!-- Единицы измерения -->
            <div class="mb-4">
              <label for="units" class="form-label fw-semibold">
                <i class="bi bi-rulers me-2"></i>Ед. изм.
              </label>
              <input id="units" type="text" class="form-control"
                     placeholder="Введите единицы измерения"
                     required v-model="work.units">
            </div>

            <!-- Количество -->
            <div class="mb-4">
              <label for="quantity" class="form-label fw-semibold">
                <i class="bi bi-123 me-2"></i>Количество
              </label>
              <input id="quantity" type="number" step="0.01" class="form-control"
                     placeholder="Введите количество"
                     required v-model="work.quantity">
            </div>

            <!-- Цена за единицу -->
            <div class="mb-4">
              <label for="unitPrice" class="form-label fw-semibold">
                <i class="bi bi-currency-dollar me-2"></i>Цена за единицу
              </label>
              <input id="unitPrice" type="number" step="0.01" min="0" class="form-control"
                     placeholder="Введите цену за единицу"
                     required v-model="work.unitPrice">
            </div>

            <!-- Стандарт -->
            <div class="mb-4">
              <label class="form-label fw-semibold">
                <i class="bi bi-file-earmark-text me-2"></i>Стандарт
              </label>
              <select class="form-select" v-model="work.standardId" required>
                <option value="" selected disabled>Выберите стандарт...</option>
                <option v-for="standard in standards" :value="standard.id">
                  {{ standard.name }}
                </option>
              </select>
            </div>

            <!-- Выполнено -->
            <div class="mb-4">
              <label for="done" class="form-label fw-semibold">
                <i class="bi bi-check-circle me-2"></i>Выполнено
              </label>
              <input id="done" type="number" step="0.01" class="form-control"
                     placeholder="Введите выполненный объем"
                     required v-model="work.done">
            </div>

            <!-- Ошибка -->
            <div v-if="error" class="alert alert-danger mb-4">
              <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
            </div>

            <!-- Кнопки -->
            <div class="d-flex gap-3">
              <button type="button" @click="goBack"
                      class="btn btn-outline-secondary flex-grow-1 py-2">
                <i class="bi bi-arrow-left me-2"></i>Назад
              </button>

              <button type="submit" class="btn btn-primary flex-grow-1 py-2"
                      :disabled="isLoading">
                <template v-if="isLoading">
                  <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Сохранение...
                </template>
                <template v-else>
                  <i class="bi bi-check-circle me-2"></i>Сохранить
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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const route = useRoute()

// Сохраняем параметры из URL при загрузке компонента
const sourcePage = ref(route.query.page || 0)
const sourceSubObjectId = ref(route.query.subObjectId || '')


const work = ref({
  id: '',
  name: '',
  units: '',
  quantity: '',
  unitPrice: 0, // Добавлено поле для цены
  done: '',
  doneAmount: 0,
  remainingAmount: 0,
  standardId: '',
  subObjectId: sourceSubObjectId.value,
})
const standards = ref([])
const error = ref(null)
const isLoading = ref(false)

const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    error.value = 'Требуется авторизация';
    return;
  }
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  }
}

const handleUnauthorized = () => {
  localStorage.removeItem('token')
  router.push('/login?redirect=' + encodeURIComponent(route.fullPath))
}

const getWork = async () => {
  try {
    isLoading.value = true
    const headers = getAuthHeaders()

    const response = await fetch(`http://localhost:8080/workings/working/${route.params.id}`, {
      headers
    })

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized()
        return
      }
      error.value = 'Не удалось загрузить данные работы';
      return;
    }

    work.value = await response.json()
  } catch (err) {
    error.value = err.message
    console.error('Ошибка загрузки:', err)
    if (err.message.includes('авторизация')) {
      handleUnauthorized()
    }
  } finally {
    isLoading.value = false
  }
}

const getStandards = async () => {
  try {
    const headers = getAuthHeaders()

    const response = await fetch('http://localhost:8080/standards', {
      headers
    })

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized()
        return
      }
      error.value = 'Не удалось загрузить стандарты';
      return;
    }

    standards.value = await response.json()
  } catch (err) {
    error.value = err.message
    console.error('Ошибка загрузки стандартов:', err)
    if (err.message.includes('авторизация')) {
      handleUnauthorized()
    }
  }
}

const updateWork = async () => {
  try {
    isLoading.value = true
    error.value = null

    if (!work.value.standardId) {
      error.value = 'Пожалуйста, выберите стандарт'
      return
    }

    const headers = getAuthHeaders()

    const response = await fetch(`http://localhost:8080/workings/${route.params.id}`, {
      method: 'PATCH',
      headers,
      body: JSON.stringify(work.value)
    })

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized()
        return
      }
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Ошибка при обновлении работы';
      return;
    }

    await router.push({
      path: `/works/${sourceSubObjectId.value}`,
      query: { page: sourcePage.value }
    })

  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message
  } finally {
    isLoading.value = false
  }
}

const goBack = () => {
  router.push({
    path: `/works/${sourceSubObjectId.value}`,
    query: { page: sourcePage.value }
  })
}

onMounted(() => {
  getWork()
  getStandards()
})
</script>

<style scoped>
.card {
  border-radius: 12px;
  overflow: hidden;
}

.form-control, .form-select {
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

.btn-group {
  gap: 8px;
}

.btn-group .btn {
  flex: 1;
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

  .d-flex {
    flex-direction: column;
    gap: 12px;
  }
}
</style>