<template>
  <!-- Шаблон остается без изменений -->
  <Navbar/>
  <div class="container py-4">
    <div class="row justify-content-center mt-5">
      <div class="col-12">
        <!-- Заголовок и кнопки с правильным центрированием -->
        <div class="d-flex align-items-center mb-4 position-relative">
          <!-- Кнопки слева -->
          <div class="d-flex">
            <a href="/addStandard" class="btn btn-primary rounded-pill px-2">
              <i class="bi bi-plus-lg me-2"></i>Добавить СП
            </a>
          </div>
          <!-- Заголовок по центру оставшегося пространства -->
          <h1 class="text-light position-absolute start-50" style="width: max-content;">
            СП
          </h1>
        </div>
        <!-- Table -->
        <div class="card shadow-sm border-0">
          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table table-hover align-middle mb-0 w-100">
                <thead class="table-dark">
                <tr>
                  <th class="ps-4" style="width: 15%">ID</th>
                  <th style="width: 55%">Наименование</th>
                  <th class="text-end pe-4" style="width: 30%">Действие</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(standard, index) in standards" :key="standard.id"
                    :class="{'table-light': index % 2 === 0}" class="border-top">
                  <td class="ps-4 fw-semibold text-muted">{{ standard.id }}</td>
                  <td class="fw-medium">{{ standard.name }}</td>
                  <td class="text-end pe-4">
                    <a :href="`/editStandard/${standard.id}`"
                       class="btn btn-sm btn-outline-primary rounded-pill px-3 me-2">
                      <i class="bi bi-pencil-square me-1"></i>Изменить
                    </a>
                    <button @click="deleteStandard(standard.id)"
                            class="btn btn-sm btn-outline-danger rounded-pill px-3">
                      <i class="bi bi-trash3 me-1"></i>Удалить
                    </button>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import Navbar from '../../components/Navbar.vue'
import {useRouter} from 'vue-router'

const router = useRouter()
const standards = ref([])
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

const getStandards = async () => {
  try {
    isLoading.value = true
    error.value = null

    const response = await fetch('http://localhost:8080/standards', {
      headers: getAuthHeaders()
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      error.value = 'Ошибка загрузки СП';
      return;
    }

    standards.value = await response.json()
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

const deleteStandard = async (id) => {
  if (!confirm('Вы уверены, что хотите удалить этот СП?')) return

  try {
    const response = await fetch(`http://localhost:8080/standards/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      error.value = 'Ошибка удаления СП';
      return;
    }

    await getStandards()
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message
  }
}

onMounted(() => {
  getStandards()
})
</script>

<style scoped>
/* Основные стили */
body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Стили для таблицы */
.table {
  font-size: 0.95rem;
}

.table th {
  font-weight: 500;
  letter-spacing: 1px;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 45, 114, 0.05);
}

/* Стили для карточки */
.card {
  border-radius: 8px;
  overflow: hidden;
}

/* Стили для кнопок с эффектами */
.btn {
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateY(0);
  position: relative;
  overflow: hidden;
  border: none;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
  padding: 0.5rem 1.25rem;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* Внутренняя граница */
.btn::before {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  right: 2px;
  bottom: 2px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-radius: 50px;
  pointer-events: none;
}

/* Эффект нажатия */
.btn:active {
  transform: translateY(2px);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1) !important;
}

/* Эффект наведения */
.btn:hover {
  filter: brightness(1.1);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

/* Специфичные цвета для кнопок */
.btn-success {
  background: linear-gradient(135deg, #28a745 0%, #218838 100%);
}

.btn-info {
  background: linear-gradient(135deg, #17a2b8 0%, #138496 100%);
}

/* Эффект "волны" при клике */
.btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 5px;
  height: 5px;
  background: rgba(255, 255, 255, 0.5);
  opacity: 0;
  border-radius: 100%;
  transform: scale(1, 1) translate(-50%);
  transform-origin: 50% 50%;
}

.btn:focus:not(:active)::after {
  animation: ripple 0.6s ease-out;
}

@keyframes ripple {
  0% {
    transform: scale(0, 0);
    opacity: 0.5;
  }
  100% {
    transform: scale(20, 20);
    opacity: 0;
  }
}

/* Иконки в кнопках */
.btn .bi {
  transition: transform 0.2s ease;
}

.btn:hover .bi {
  transform: scale(1.1);
}

/* Убираем стандартный outline и добавляем кастомный */
.btn:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(0, 45, 114, 0.3);
}

/* Скролл таблицы */
.table-responsive {
  scrollbar-width: thin;
  scrollbar-color: #002d72 #f1f1f1;
}

.table-responsive::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.table-responsive::-webkit-scrollbar-thumb {
  background-color: #002d72;
  border-radius: 4px;
}

.table-responsive::-webkit-scrollbar-track {
  background-color: #f1f1f1;
}

/* Анимация загрузки */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.table tbody tr {
  animation: fadeIn 0.3s ease forwards;
}

/* Иконки для кнопок */
.bi {
  font-size: 1rem;
}
</style>