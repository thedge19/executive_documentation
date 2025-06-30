<template>
  <Navbar/>

  <div class="container py-5">
    <div class="card shadow-sm border-0 mx-auto" style="max-width: 600px;">
      <div class="card-header bg-white py-4">
        <h2 class="h4 mb-0 text-center text-primary">Добавить работы</h2>
      </div>

      <div class="card-body">
        <form @submit.prevent="addWork">
          <!-- Подобъект -->
          <div class="input-group mb-4">
              <span class="input-group-text bg-light fw-semibold">
                <i class="bi bi-building me-2"></i>Подобъект
              </span>
            <input type="text" class="form-control" :value="subObject.name" readonly>
          </div>

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
            <input id="quantity" type="number" step="0.001" class="form-control"
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

          <!-- Ошибка -->
          <div v-if="error" class="alert alert-danger mb-4">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
          </div>

          <!-- Кнопка отправки -->
          <div class="d-grid">
            <button type="submit" class="btn btn-primary py-2" :disabled="isLoading">
              <template v-if="isLoading">
                <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                Добавление...
              </template>
              <template v-else>
                <i class="bi bi-check-circle me-2"></i>Добавить работу
              </template>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const route = useRoute()

const subObject = ref({name: ''})
const standards = ref([])
const work = ref({
  name: '',
  units: '',
  quantity: '',
  unitPrice: 0,
  done: 0,
  standardId: '',
  subObjectId: route.params.id
})
const error = ref('')
const isLoading = ref(false)

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
  router.push('/login?redirect=' + encodeURIComponent(route.fullPath))
}

const getSubObject = async () => {
  try {
    const headers = getAuthHeaders()

    const response = await fetch(`http://localhost:8080/subobjects/subObject/${route.params.id}`, {
      headers
    })

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized()
        return
      }
      error.value = 'Не удалось загрузить данные подобъекта';
      return;
    }

    subObject.value = await response.json()
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message
    if (err.message.includes('авторизация')) {
      handleUnauthorized()
    }
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
    console.error('Ошибка:', err)
    error.value = err.message
    if (err.message.includes('авторизация')) {
      handleUnauthorized()
    }
  }
}

const addWork = async () => {
  try {
    if (!work.value.standardId) {
      error.value = 'Пожалуйста, выберите стандарт'
      return
    }

    isLoading.value = true
    error.value = ''

    const headers = getAuthHeaders()

    const response = await fetch('http://localhost:8080/workings', {
      method: 'POST',
      headers,
      body: JSON.stringify(work.value)
    })

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized()
        return;
      }
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Ошибка при добавлении работы';
      return;
    }

    // Получаем номер последней страницы из URL или используем 0
    const lastPage = parseInt(route.query.page) || 0

    // Перенаправляем на последнюю страницу
    await router.push(`/works/${route.params.id}?page=${lastPage}`)

  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  getSubObject()
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

.input-group-text {
  border-radius: 8px 0 0 8px;
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