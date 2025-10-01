<template>
  <Navbar/>
  <div class="container py-3">
    <div class="row justify-content-center mt-5">
      <div class="col-12">
        <!-- Информация о подобъекте -->
        <div class="card bg-light border-0 mb-2">
          <div class="card-body py-3">
            <div class="row align-items-center">
              <div class="col-md-6">
                <p class="mb-0 text-muted small">Подобъект: {{ subObject.name }}</p>
              </div>
              <div class="col-md-6 text-md-end">
                <div class="alert alert-success mb-0 py-2 px-3 rounded-pill d-inline-block">
                  <strong>Итого:</strong>
                  <span class="ms-2 fw-bold">{{ formatCurrency(totalAmountBySubObject) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Error message -->
        <div v-if="error" class="alert alert-danger mb-4">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
        </div>

        <!-- Loading indicator -->
        <div v-if="isLoading" class="text-center mb-4">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Загрузка...</span>
          </div>
        </div>

        <!-- Table -->
        <div class="card shadow-sm border-0">
          <div class="card-body p-0">
            <div class="table-responsive" style="max-height: 82vh;">
              <table class="table table-hover mb-0">
                <thead class="sticky-top" style="background-color: #002d72;">
                <tr>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">ID</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Наименование</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Ед. изм.</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Количество</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Выполнено</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Закрыто, руб.</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Осталось</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Не закрыто</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Всего, руб.</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Действия</th>
                </tr>
                </thead>
                <tbody>
                <tr v-if="works && works.length > 0"
                    v-for="(work, index) in works"
                    :key="work.id"
                    :class="{'table-light': index % 2 === 0}">
                  <td class="text-center align-middle fw-semibold">{{ work.id }}</td>
                  <td class="align-middle" :class="{ 'fw-bold': work.unitPrice > 0 }">
                    {{ work.name }}
                    <span v-if="work.standard" class="badge bg-secondary ms-1">{{ work.standard.name }}</span>
                  </td>
                  <td class="text-center align-middle">{{ work.units }}</td>
                  <td class="text-center align-middle">{{ work.quantity }}</td>
                  <td class="text-center align-middle">{{ work.done }}</td>
                  <td class="text-center align-middle">{{ work.doneAmount.toFixed(2) }}</td>
                  <td class="text-center align-middle">{{ work.finalQuantity }}</td>
                  <td class="text-center align-middle">{{ work.remainingAmount.toFixed(2) }}</td>
                  <td class="text-center align-middle">{{ work.totalAmount.toFixed(2) }}</td>
                  <td class="text-center align-middle">
                    <div class="d-flex justify-content-center gap-2">
                      <a class="btn btn-sm btn-outline-primary rounded-pill px-3"
                         :href="`/editWork/${work.id}?subObjectId=${subObjectId}`">
                        <i class="bi bi-pencil me-1"></i>
                      </a>
                      <button class="btn btn-sm btn-outline-danger rounded-pill" @click="deleteWork(work.id)">
                        <i class="bi bi-trash me-1"></i>
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-else>
                  <td colspan="10" class="text-center py-4 text-muted">
                    <i class="bi bi-exclamation-circle fs-4 d-block mb-2"></i>
                    Нет работ для отображения
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

  <!-- Floating action buttons -->
  <div class="floating-buttons">
    <!-- Back to subobjects button -->
    <button
        class="btn floating-btn back-btn"
        @click="goToSubObjects"
    >
      <i class="bi bi-arrow-left"></i>
      <span class="floating-btn-text">В подобъекты</span>
    </button>

    <!-- Add work button -->
    <button
        class="btn btn-info floating-btn add-btn"
        @click="showAddForm = true"
    >
      <i class="bi bi-plus-lg"></i>
      <span class="floating-btn-text">Добавить работу</span>
    </button>
  </div>

  <!-- Floating add form -->
  <div class="floating-add-form" :class="{ 'floating-add-form--open': showAddForm }">
    <div class="floating-form-content">
      <div class="card shadow-sm border-0">
        <div class="card-header bg-primary text-white py-3">
          <div class="d-flex justify-content-between align-items-center">
            <h5 class="mb-0">
              <i class="bi bi-plus-circle me-2"></i>Добавить работу
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeForm"></button>
          </div>
        </div>

        <div class="card-body p-4">
          <form @submit.prevent="addWork">
            <!-- Подобъект -->
            <div class="input-group mb-3">
              <span class="input-group-text bg-light fw-semibold small">
                <i class="bi bi-building me-1"></i>Подобъект
              </span>
              <input type="text" class="form-control form-control-sm" :value="subObject.name" readonly>
            </div>

            <!-- Наименование -->
            <div class="mb-3">
              <label for="name" class="form-label fw-semibold small">
                <i class="bi bi-card-text me-1"></i>Наименование
              </label>
              <input id="name" type="text" class="form-control form-control-sm"
                     placeholder="Введите наименование работы"
                     required v-model="workData.name">
            </div>

            <!-- Единицы измерения -->
            <div class="mb-3">
              <label for="units" class="form-label fw-semibold small">
                <i class="bi bi-rulers me-1"></i>Ед. изм.
              </label>
              <input id="units" type="text" class="form-control form-control-sm"
                     placeholder="Введите единицы измерения"
                     required v-model="workData.units">
            </div>

            <!-- Количество -->
            <div class="mb-3">
              <label for="quantity" class="form-label fw-semibold small">
                <i class="bi bi-123 me-1"></i>Количество
              </label>
              <input id="quantity" type="number" step="0.001" class="form-control form-control-sm"
                     placeholder="Введите количество"
                     required v-model="workData.quantity">
            </div>

            <!-- Цена за единицу -->
            <div class="mb-3">
              <label for="unitPrice" class="form-label fw-semibold small">
                <i class="bi bi-currency-dollar me-1"></i>Цена за единицу
              </label>
              <input id="unitPrice" type="number" step="0.01" min="0" class="form-control form-control-sm"
                     placeholder="Введите цену за единицу"
                     required v-model="workData.unitPrice">
            </div>

            <!-- Стандарт -->
            <div class="mb-4">
              <label class="form-label fw-semibold small">
                <i class="bi bi-file-earmark-text me-1"></i>Стандарт
              </label>
              <select class="form-select form-select-sm" v-model="workData.standardId" required>
                <option value="" selected disabled>Выберите стандарт...</option>
                <option v-for="standard in standards" :value="standard.id">
                  {{ standard.name }}
                </option>
              </select>
            </div>

            <!-- Ошибка -->
            <div v-if="formError" class="alert alert-danger mb-3 py-2">
              <i class="bi bi-exclamation-triangle-fill me-1"></i>{{ formError }}
            </div>

            <!-- Кнопки отправки -->
            <div class="d-flex gap-2">
              <button type="button" class="btn btn-secondary btn-sm flex-fill" @click="closeForm">
                <i class="bi bi-x-circle me-1"></i>Отмена
              </button>
              <button type="submit" class="btn btn-primary btn-sm flex-fill"
                      :disabled="isSubmitting">
                <template v-if="isSubmitting">
                  <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                  Добавление...
                </template>
                <template v-else>
                  <i class="bi bi-check-circle me-1"></i>Добавить
                </template>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const route = useRoute()

// Reactive state
const works = ref([])
const subObject = ref({ name: '', title: '' })
const standards = ref([])
const subObjectId = ref(route.params.id)
const error = ref(null)
const isLoading = ref(false)
const totalAmountBySubObject = ref(0)
const showAddForm = ref(false)
const isSubmitting = ref(false)
const formError = ref(null)

// Form data
const workData = ref({
  name: '',
  units: '',
  quantity: '',
  unitPrice: 0,
  done: 0,
  standardId: '',
  subObjectId: route.params.id
})

// Methods
const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    throw new Error('Требуется авторизация')
  }
  return {
    'Authorization': `Bearer ${token}`
  }
}

const handleUnauthorized = () => {
  localStorage.removeItem('token')
  router.push('/login?redirect=' + encodeURIComponent(route.fullPath))
}

const getWorks = async () => {
  try {
    isLoading.value = true
    error.value = null
    const headers = getAuthHeaders()

    const response = await fetch(
        `http://localhost:8080/workings/${subObjectId.value}`,
        { headers }
    )

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized()
        return
      }
      error.value = 'Ошибка загрузки работ'
      return
    }

    works.value = await response.json()

  } catch (err) {
    console.error('Ошибка:', err)
    error.value = 'Не удалось загрузить работы'
    if (err.message.includes('авторизация')) {
      handleUnauthorized()
    }
  } finally {
    isLoading.value = false
  }
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
      error.value = 'Не удалось загрузить данные подобъекта'
      return
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
      error.value = 'Не удалось загрузить стандарты'
      return
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

const fetchTotalAmountBySubObject = async () => {
  if (!subObjectId.value) return

  try {
    const headers = getAuthHeaders()
    const response = await fetch(
        `http://localhost:8080/workings/subobject/${subObjectId.value}/total-sum`,
        { headers }
    )

    if (!response.ok) {
      error.value = "Ошибка при получении суммы"
      return
    }

    totalAmountBySubObject.value = await response.json()
  } catch (err) {
    console.error("Ошибка загрузки суммы:", err)
    totalAmountBySubObject.value = 0
  }
}

const deleteWork = async (id) => {
  if (!confirm('Вы действительно хотите удалить эту работу?')) return

  try {
    const headers = getAuthHeaders()
    const response = await fetch(`http://localhost:8080/workings/${id}`, {
      method: 'DELETE',
      headers
    })

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized()
        return
      }
      error.value = 'Ошибка при удалении'
      return
    }

    await getWorks()
    await fetchTotalAmountBySubObject()
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = 'Не удалось удалить работу'
  }
}

const formatCurrency = (value) => {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value || 0)
}

const goToSubObjects = () => {
  if (subObject.value.projectId) {
    router.push(`/subObjects/${subObject.value.projectId}`)
  } else {
    router.push(`/subObjects/${subObject.value.projectId}`)
  }
}

const resetForm = () => {
  workData.value = {
    name: '',
    units: '',
    quantity: '',
    unitPrice: 0,
    done: 0,
    standardId: '',
    subObjectId: route.params.id
  }
  formError.value = null
}

const closeForm = () => {
  showAddForm.value = false
  resetForm()
}

const addWork = async () => {
  try {
    if (!workData.value.standardId) {
      formError.value = 'Пожалуйста, выберите стандарт'
      return
    }

    isSubmitting.value = true
    formError.value = ''

    const headers = getAuthHeaders()

    const response = await fetch('http://localhost:8080/workings', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': headers.Authorization
      },
      body: JSON.stringify(workData.value)
    })

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized()
        return
      }
      const errorData = await response.json().catch(() => ({}))
      formError.value = errorData.message || 'Ошибка при добавлении работы'
      return
    }

    // Успешное сохранение
    closeForm()
    await getWorks()
    await fetchTotalAmountBySubObject()

  } catch (err) {
    console.error('Ошибка:', err)
    formError.value = err.message
  } finally {
    isSubmitting.value = false
  }
}

// Lifecycle
onMounted(() => {
  getSubObject()
  getWorks()
  getStandards()
  fetchTotalAmountBySubObject()
})

watch(subObjectId, async () => {
  await getWorks()
  await fetchTotalAmountBySubObject()
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

.btn-sm {
  padding: 0.25rem 0.75rem;
  font-size: 0.85rem;
}

.btn-outline-primary {
  border: 1px solid #002d72;
  color: #002d72;
  background: transparent;
}

.btn-outline-primary:hover {
  background: #002d72;
  color: white;
}

.btn-outline-danger {
  border: 1px solid #dc3545;
  color: #dc3545;
  background: transparent;
}

.btn-outline-danger:hover {
  background: #dc3545;
  color: white;
}

.btn-outline-secondary {
  border: 1px solid #6c757d;
  color: #6c757d;
  background: transparent;
}

.btn-outline-secondary:hover {
  background: #6c757d;
  color: white;
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

/* Ссылки */
.text-primary {
  color: #002d72 !important;
}

a.text-primary:hover {
  color: #001a3d !important;
  text-decoration: underline;
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
}

.floating-btn:hover {
  transform: translateY(-4px) scale(1.08);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
  z-index: 1002;
  opacity: 1;
}

.floating-btn:active {
  transform: translateY(2px) scale(0.95);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.4);
  transition: all 0.1s ease;
}

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

/* Individual button animations */
.add-btn {
  animation: floatUp 0.5s ease-out 0.2s both;
  z-index: 1001;
  background: linear-gradient(135deg, #17a2b8 0%, #138496 100%) !important;
  border: none !important;
}

.add-btn:active {
  background: linear-gradient(135deg, #138496 0%, #117a8b 100%) !important;
  box-shadow: 0 2px 15px rgba(23, 162, 184, 0.6) !important;
}

.back-btn {
  animation: floatUp 0.5s ease-out 0.1s both;
  z-index: 1001;
  background: linear-gradient(135deg, #6c757d 0%, #5a6268 100%) !important;
  border: none !important;
  color: white !important;
}

.back-btn:hover {
  background: linear-gradient(135deg, #5a6268 0%, #495057 100%) !important;
}

.back-btn:active {
  transform: translateY(2px) scale(0.95);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.4) !important;
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

/* Floating add form styles */
.floating-add-form {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0.9);
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  z-index: 1100;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.floating-add-form--open {
  opacity: 1;
  visibility: visible;
  transform: translate(-50%, -50%) scale(1);
}

.floating-form-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  max-height: 90vh;
  overflow-y: auto;
  border: 2px solid #e9ecef;
}

.floating-form-content .card {
  margin: 0;
  border: none;
}

.floating-form-content .card-body {
  max-height: calc(90vh - 80px);
  overflow-y: auto;
}

/* Стили для информации о подобъекте */
.card.bg-light {
  border-radius: 8px;
}

.alert.rounded-pill {
  border-radius: 50px !important;
}

.badge {
  font-size: 0.7em;
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

  .floating-add-form {
    width: 95%;
    max-height: 95vh;
  }

  .floating-form-content .card-body {
    max-height: calc(95vh - 80px);
    padding: 1.5rem;
  }

  .card.bg-light .row {
    text-align: center;
  }

  .card.bg-light .text-md-end {
    text-align: center !important;
    margin-top: 1rem;
  }
}

@media (max-width: 576px) {
  .floating-form-content .card-body {
    padding: 1rem;
  }

  .table-responsive {
    font-size: 0.8rem;
  }

  .btn-sm {
    padding: 0.25rem 0.5rem;
  }

  .table td, .table th {
    padding: 0.5rem;
  }
}
</style>