<template>
  <Navbar/>
  <div class="container py-4">
    <div class="row justify-content-center mt-5">
      <div class="col-12">
        <!-- Заголовок по центру -->
        <div class="d-flex align-items-center mb-4 position-relative justify-content-center">
          <h1 class="text-light" style="width: max-content;">
            СП
          </h1>
        </div>
        <!-- Table -->
        <div class="card shadow-sm border-0">
          <div class="card-body p-0">
            <div class="table-responsive" style="max-height: 85vh;">
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

  <!-- Floating action button -->
  <div class="floating-buttons">
    <!-- Add standard button -->
    <a
        href="/addStandard"
        class="btn btn-primary floating-btn add-standard-btn"
    >
      <i class="bi bi-plus-lg"></i>
      <span class="floating-btn-text">Добавить СП</span>
    </a>
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

/* Floating buttons styles */
.floating-buttons {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: flex-end;
}

.floating-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: visible;
  font-size: 1.2rem;
  border: none;
  z-index: 1001;
  opacity: 1;
  cursor: pointer;
  text-decoration: none;
}

.floating-btn:hover {
  transform: translateY(-4px) scale(1.08);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
  z-index: 1002;
  opacity: 1;
  text-decoration: none;
}

.floating-btn:active {
  transform: translateY(2px) scale(0.95);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.4);
  transition: all 0.1s ease;
}

/* Эффект волны при нажатии */
.floating-btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255,255,255,0.4) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%) scale(0);
  opacity: 0;
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.floating-btn:active::after {
  transform: translate(-50%, -50%) scale(2);
  opacity: 1;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

/* Эффект свечения при нажатии */
.floating-btn:active {
  filter: brightness(1.3);
}

.floating-btn-text {
  position: absolute;
  right: 100%;
  margin-right: 15px;
  background: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 0.85rem;
  white-space: nowrap;
  opacity: 0;
  transform: translateX(10px);
  transition: all 0.3s ease;
  pointer-events: none;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  z-index: 1003;
}

.floating-btn:hover .floating-btn-text {
  opacity: 1;
  transform: translateX(0);
}

.floating-btn-text::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 100%;
  margin-top: -5px;
  border-width: 5px;
  border-style: solid;
  border-color: transparent transparent transparent rgba(0, 0, 0, 0.9);
}

/* Add standard button */
.add-standard-btn {
  animation: floatUp 0.5s ease-out 0.2s both;
  z-index: 1001;
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%) !important;
  border: none !important;
  color: white !important;
}

.add-standard-btn:active {
  background: linear-gradient(135deg, #0056b3 0%, #004085 100%) !important;
  box-shadow: 0 2px 15px rgba(0, 123, 255, 0.6) !important;
}

@keyframes floatUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.8);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Анимация появления подсказки */
.floating-btn:hover .floating-btn-text {
  animation: tooltipFadeIn 0.3s ease-out;
}

@keyframes tooltipFadeIn {
  from {
    opacity: 0;
    transform: translateX(10px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

/* Адаптивность */
@media (max-width: 768px) {
  .floating-buttons {
    bottom: 20px;
    right: 20px;
  }

  .floating-btn {
    width: 55px;
    height: 55px;
    font-size: 1.1rem;
  }

  .floating-btn-text {
    font-size: 0.8rem;
    padding: 8px 12px;
    white-space: normal;
    width: 140px;
    text-align: center;
  }
}

/* Убедимся, что кнопки поверх всего контента */
.floating-buttons * {
  z-index: inherit;
}
</style>