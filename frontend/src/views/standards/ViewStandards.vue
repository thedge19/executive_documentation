<template>
  <Navbar/>
  <div class="container py-2">
    <div class="row justify-content-center mt-5">
      <div class="col-12">
        <!-- Заголовок по центру -->
        <div class="d-flex align-items-center position-relative justify-content-center">
          <h1 class="text-light" style="width: max-content;">
            СП
          </h1>
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
            <div class="table-responsive" style="max-height: 81vh;">
              <table class="table table-hover align-middle mb-0 w-100">
                <thead class="table-dark sticky-header">
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
                  <td>
                    <!-- Режим редактирования -->
                    <div v-if="editingId === standard.id" class="d-flex align-items-center">
                      <input
                          type="text"
                          class="form-control form-control-sm"
                          v-model="editingName"
                          ref="nameInput"
                          @keyup.enter="saveEdit(standard.id)"
                          @keyup.esc="cancelEdit"
                      >
                    </div>
                    <!-- Режим просмотра -->
                    <div v-else class="fw-medium">
                      {{ standard.name }}
                    </div>
                  </td>
                  <td class="text-end pe-4">
                    <!-- Режим редактирования -->
                    <template v-if="editingId === standard.id">
                      <button
                          @click="saveEdit(standard.id)"
                          class="btn btn-sm btn-success rounded-pill px-3 me-2"
                          :disabled="isSubmitting"
                      >
                        <template v-if="isSubmitting">
                          <span class="spinner-border spinner-border-sm me-1" role="status"></span>
                          Сохранение...
                        </template>
                        <template v-else>
                          <i class="bi bi-check-lg me-1"></i>OK
                        </template>
                      </button>
                      <button
                          @click="cancelEdit"
                          class="btn btn-sm btn-secondary rounded-pill px-3"
                          :disabled="isSubmitting"
                      >
                        <i class="bi bi-x-lg me-1"></i>Отмена
                      </button>
                    </template>
                    <!-- Режим просмотра -->
                    <template v-else>
                      <button
                          @click="startEdit(standard)"
                          class="btn btn-sm btn-outline-primary rounded-pill px-3 me-2"
                          :disabled="isEditing"
                      >
                        <i class="bi bi-pencil-square me-1"></i>Изменить
                      </button>
                      <button
                          @click="confirmDelete(standard.id, standard.name)"
                          class="btn btn-sm btn-outline-danger rounded-pill px-3"
                          :disabled="isEditing"
                      >
                        <i class="bi bi-trash3 me-1"></i>Удалить
                      </button>
                    </template>
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
    <button
        class="btn btn-primary floating-btn"
        @click="showAddForm = true"
        :disabled="isEditing"
    >
      <i class="bi bi-plus-lg"></i>
      <span class="floating-btn-text">Добавить СП</span>
    </button>
  </div>

  <!-- Floating add form -->
  <div v-if="showAddForm" class="modal-backdrop fade show" @click="closeForm"></div>
  <div class="modal fade" :class="{'show d-block': showAddForm}" tabindex="-1" v-if="showAddForm">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header bg-primary text-white">
          <h5 class="modal-title">
            <i class="bi bi-plus-circle me-2"></i>Добавить СП
          </h5>
          <button type="button" class="btn-close btn-close-white" @click="closeForm"></button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="addStandard">
            <!-- Наименование -->
            <div class="mb-3">
              <label for="name" class="form-label fw-semibold">Наименование</label>
              <input
                  id="name"
                  type="text"
                  class="form-control"
                  placeholder="Введите наименование стандарта"
                  required
                  v-model="standardData.name"
                  :disabled="isSubmitting"
              >
            </div>

            <!-- Ошибка -->
            <div v-if="formError" class="alert alert-danger mb-3">
              <i class="bi bi-exclamation-triangle-fill me-1"></i>{{ formError }}
            </div>

            <!-- Кнопки отправки -->
            <div class="d-flex gap-2">
              <button
                  type="button"
                  class="btn btn-secondary flex-fill"
                  @click="closeForm"
                  :disabled="isSubmitting"
              >
                <i class="bi bi-x-circle me-1"></i>Отмена
              </button>
              <button
                  type="submit"
                  class="btn btn-primary flex-fill"
                  :disabled="isSubmitting || !standardData.name.trim()"
              >
                <template v-if="isSubmitting">
                  <span class="spinner-border spinner-border-sm me-1" role="status"></span>
                  Сохранение...
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
import {ref, onMounted, nextTick, computed} from 'vue'
import Navbar from '../../components/Navbar.vue'
import {useRouter} from 'vue-router'

const router = useRouter()

// Reactive state
const standards = ref([])
const isLoading = ref(false)
const error = ref(null)
const showAddForm = ref(false)
const isSubmitting = ref(false)
const formError = ref(null)

// Inline editing state
const editingId = ref(null)
const editingName = ref('')
const nameInput = ref(null)

// Computed properties
const isEditing = computed(() => editingId.value !== null)

// Form data
const standardData = ref({
  name: ''
})

const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    throw new Error('Требуется авторизация')
  }
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
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
      error.value = 'Ошибка загрузки СП'
      return
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

const startEdit = (standard) => {
  editingId.value = standard.id
  editingName.value = standard.name

  // Фокусируемся на input после обновления DOM
  nextTick(() => {
    if (nameInput.value) {
      nameInput.value.focus()
      nameInput.value.select()
    }
  })
}

const saveEdit = async (id) => {
  if (!editingName.value.trim()) {
    error.value = 'Наименование не может быть пустым'
    return
  }

  try {
    isSubmitting.value = true
    error.value = null

    const response = await fetch(`http://localhost:8080/standards/${id}`, {
      method: 'PATCH',
      headers: getAuthHeaders(),
      body: JSON.stringify({name: editingName.value})
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || 'Ошибка при обновлении стандарта'
      return
    }

    // Обновляем данные в таблице
    const index = standards.value.findIndex(s => s.id === id)
    if (index !== -1) {
      standards.value[index].name = editingName.value
    }

    // Выходим из режима редактирования
    editingId.value = null
    editingName.value = ''

  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message
  } finally {
    isSubmitting.value = false
  }
}

const cancelEdit = () => {
  editingId.value = null
  editingName.value = ''
  error.value = null
}

const confirmDelete = (id, name) => {
  if (confirm(`Вы действительно хотите удалить стандарт "${name}"?`)) {
    deleteStandard(id)
  }
}

const deleteStandard = async (id) => {
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
      error.value = 'Ошибка удаления СП'
      return
    }

    await getStandards()
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message
  }
}

const resetForm = () => {
  standardData.value = {
    name: ''
  }
  formError.value = null
}

const closeForm = () => {
  showAddForm.value = false
  resetForm()
}

const addStandard = async () => {
  try {
    isSubmitting.value = true
    formError.value = null

    const response = await fetch('http://localhost:8080/standards', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(standardData.value)
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      formError.value = errorData.message || 'Ошибка при добавлении стандарта'
      return
    }

    // Успешное сохранение
    closeForm()
    await getStandards()

  } catch (err) {
    console.error('Ошибка:', err)
    formError.value = err.message

    if (err.message.includes('авторизация')) {
      handleUnauthorized()
    }
  } finally {
    isSubmitting.value = false
  }
}

onMounted(() => {
  getStandards()
})
</script>

<style scoped>
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

/* Стили для inline редактирования */
.form-control {
  border-radius: 6px;
  border: 2px solid #007bff;
  transition: all 0.2s ease;
}

.form-control:focus {
  border-color: #0056b3;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

/* Анимация для перехода между режимами */
.table tbody tr td {
  transition: all 0.3s ease;
}

/* Стили для отключенных кнопок */
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Подсветка редактируемой строки */
.table tbody tr:has(input:focus) {
  background-color: rgba(0, 123, 255, 0.05) !important;
  box-shadow: inset 0 0 0 1px #007bff;
}

/* Модальное окно */
.modal-backdrop {
  z-index: 1040;
}

.modal {
  z-index: 1050;
}

.modal-content {
  border: none;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.modal-header {
  border-radius: 12px 12px 0 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.modal-title {
  font-weight: 600;
}

/* Floating action button */
.floating-buttons {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1030;
}

.floating-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  box-shadow: 0 4px 20px rgba(0, 45, 114, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #002d72 0%, #0044aa 100%);
  border: none;
}

.floating-btn:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 25px rgba(0, 45, 114, 0.4);
  width: auto;
  padding: 0 25px;
  border-radius: 50px;
}

.floating-btn:hover .floating-btn-text {
  max-width: 200px;
  opacity: 1;
  margin-left: 8px;
}

.floating-btn:active {
  transform: translateY(-1px) scale(1.02);
}

.floating-btn-text {
  max-width: 0;
  opacity: 0;
  white-space: nowrap;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
  font-size: 0.9rem;
}

.floating-btn .bi {
  font-size: 1.2rem;
  transition: transform 0.3s ease;
}

.floating-btn:hover .bi {
  transform: scale(1.1);
}

/* Анимация появления кнопки */
.floating-btn {
  animation: floatIn 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

@keyframes floatIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.8);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>