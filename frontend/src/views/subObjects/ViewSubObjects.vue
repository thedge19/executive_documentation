<template>
  <Navbar/>
  <div class="container py-3">
    <div class="row justify-content-center mt-5">
      <div class="col-12">
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
            <div class="table-responsive" style="max-height: 90vh;">
              <table class="table table-hover align-middle mb-0 w-100">
                <thead class="table-dark sticky-header">
                <tr>
                  <th class="text-center" style="width: 7%">ID</th>
                  <th class="text-center" style="width: 40%">
                    Наименование
                  </th>
                  <th class="text-center" style="width: 12%">
                    Обозначение
                  </th>
                  <th class="text-center" style="width: 20%">
                    Объект
                  </th>
                  <th class="text-center" style="width: 21%">
                    Действия
                  </th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(subObject, index) in subObjects" :key="subObject.id"
                    :class="{'table-light': index % 2 === 0}">
                  <td class="text-center align-middle fw-semibold text-muted">{{ subObject.id }}</td>
                  <td class="align-middle">
                    <!-- Ссылка на works -->
                    <router-link
                        :to="`/works/${subObject.id}`"
                        class="text-decoration-none text-primary fw-semibold"
                    >
                      {{ subObject.name }}
                    </router-link>
                  </td>
                  <td class="text-center align-middle">
                    {{ subObject.title }}
                  </td>
                  <td class="text-center align-middle">{{ subObject.project?.name }}</td>
                  <td class="text-center align-middle">
                    <!-- Кнопки действий -->
                    <button
                        @click="openEditModal(subObject)"
                        class="btn btn-sm btn-outline-primary rounded-pill px-3 me-2"
                    >
                      <i class="bi bi-pencil-square me-1"></i>Изменить
                    </button>
                    <button
                        @click="deleteSubObject(subObject.id)"
                        class="btn btn-sm btn-outline-danger rounded-pill px-3"
                    >
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

  <!-- Floating action buttons -->
  <div class="floating-buttons">
    <!-- Project switcher buttons -->
    <button
        class="btn floating-btn project-btn"
        :class="{ 'active': projectId === 5 }"
        @click="switchProject(5)"
    >
      <i class="bi bi-building"></i>
      <span class="floating-btn-text">Проект: Шесхарис</span>
    </button>

    <button
        class="btn floating-btn project-btn"
        :class="{ 'active': projectId === 4 }"
        @click="switchProject(4)"
    >
      <i class="bi bi-tree"></i>
      <span class="floating-btn-text">Проект: Грушовая</span>
    </button>

    <!-- Add subobject button -->
    <button
        class="btn btn-info floating-btn add-btn"
        @click="showAddForm = true"
    >
      <i class="bi bi-plus-lg"></i>
      <span class="floating-btn-text">Добавить подобъект</span>
    </button>
  </div>

  <!-- Floating add form -->
  <div class="floating-add-form" :class="{ 'floating-add-form--open': showAddForm }">
    <div class="floating-form-content">
      <div class="card shadow-sm border-0">
        <div class="card-header bg-primary text-white py-3">
          <div class="d-flex justify-content-between align-items-center">
            <h5 class="mb-0">
              <i class="bi bi-plus-circle me-2"></i>Добавить подобъект
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeAddForm"></button>
          </div>
        </div>

        <div class="card-body p-4">
          <form @submit.prevent="addSubObject">
            <!-- Наименование -->
            <div class="mb-3">
              <label for="name" class="form-label fw-semibold small">
                <i class="bi bi-building me-1"></i>Наименование
              </label>
              <input id="name" type="text" class="form-control form-control-sm"
                     placeholder="Введите наименование подобъекта"
                     required v-model="subObjectData.name">
            </div>

            <!-- Аббревиатура -->
            <div class="mb-3">
              <label for="title" class="form-label fw-semibold small">
                <i class="bi bi-textarea-t me-1"></i>Аббревиатура
              </label>
              <input id="title" type="text" class="form-control form-control-sm"
                     placeholder="Введите аббревиатуру"
                     required v-model="subObjectData.title">
            </div>

            <!-- Выбор проекта -->
            <div class="mb-4">
              <label class="form-label fw-semibold small d-block mb-2">
                <i class="bi bi-diagram-2 me-1"></i>Проект
              </label>
              <div class="btn-group w-100" role="group">
                <input type="radio" class="btn-check" name="projectId"
                       id="project1" autocomplete="off"
                       :value="4" v-model="subObjectData.projectId">
                <label class="btn btn-outline-primary btn-sm" for="project1">
                  <i class="bi bi-tree me-1"></i>Грушовая
                </label>

                <input type="radio" class="btn-check" name="projectId"
                       id="project2" autocomplete="off"
                       :value="5" v-model="subObjectData.projectId">
                <label class="btn btn-outline-primary btn-sm" for="project2">
                  <i class="bi bi-building me-1"></i>Шесхарис
                </label>
              </div>
            </div>

            <!-- Ошибка -->
            <div v-if="formError" class="alert alert-danger mb-3 py-2">
              <i class="bi bi-exclamation-triangle-fill me-1"></i>{{ formError }}
            </div>

            <!-- Кнопки -->
            <div class="d-flex gap-2">
              <button type="button" class="btn btn-secondary btn-sm flex-fill" @click="closeAddForm">
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

  <!-- Floating edit form -->
  <div class="floating-add-form" :class="{ 'floating-add-form--open': showEditForm }">
    <div class="floating-form-content">
      <div class="card shadow-sm border-0">
        <div class="card-header bg-warning text-dark py-3">
          <div class="d-flex justify-content-between align-items-center">
            <h5 class="mb-0">
              <i class="bi bi-pencil-square me-2"></i>Редактировать подобъект
            </h5>
            <button type="button" class="btn-close" @click="closeEditForm"></button>
          </div>
        </div>

        <div class="card-body p-4">
          <form @submit.prevent="updateSubObject">
            <!-- Наименование -->
            <div class="mb-3">
              <label for="editName" class="form-label fw-semibold small">
                <i class="bi bi-building me-1"></i>Наименование
              </label>
              <input id="editName" type="text" class="form-control form-control-sm"
                     placeholder="Введите наименование подобъекта"
                     required v-model="editSubObjectData.name">
            </div>

            <!-- Аббревиатура -->
            <div class="mb-3">
              <label for="editTitle" class="form-label fw-semibold small">
                <i class="bi bi-textarea-t me-1"></i>Аббревиатура
              </label>
              <input id="editTitle" type="text" class="form-control form-control-sm"
                     placeholder="Введите аббревиатуру"
                     required v-model="editSubObjectData.title">
            </div>

            <!-- Выбор проекта -->
            <div class="mb-4">
              <label class="form-label fw-semibold small d-block mb-2">
                <i class="bi bi-diagram-2 me-1"></i>Проект
              </label>
              <div class="btn-group w-100" role="group">
                <input type="radio" class="btn-check" name="editProjectId"
                       id="editProject1" autocomplete="off"
                       :value="4" v-model="editSubObjectData.projectId">
                <label class="btn btn-outline-primary btn-sm" for="editProject1">
                  <i class="bi bi-tree me-1"></i>Грушовая
                </label>

                <input type="radio" class="btn-check" name="editProjectId"
                       id="editProject2" autocomplete="off"
                       :value="5" v-model="editSubObjectData.projectId">
                <label class="btn btn-outline-primary btn-sm" for="editProject2">
                  <i class="bi bi-building me-1"></i>Шесхарис
                </label>
              </div>
            </div>

            <!-- Ошибка -->
            <div v-if="editFormError" class="alert alert-danger mb-3 py-2">
              <i class="bi bi-exclamation-triangle-fill me-1"></i>{{ editFormError }}
            </div>

            <!-- Кнопки -->
            <div class="d-flex gap-2">
              <button type="button" class="btn btn-secondary btn-sm flex-fill" @click="closeEditForm">
                <i class="bi bi-x-circle me-1"></i>Отмена
              </button>
              <button type="submit" class="btn btn-warning btn-sm flex-fill"
                      :disabled="isSubmitting">
                <template v-if="isSubmitting">
                  <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                  Сохранение...
                </template>
                <template v-else>
                  <i class="bi bi-check-circle me-1"></i>Сохранить
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
import { ref, onBeforeMount } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()

// Reactive state
const subObjects = ref([])
const projectId = ref(4) // Значение по умолчанию
const error = ref(null)
const isLoading = ref(false)
const showAddForm = ref(false)
const showEditForm = ref(false)
const isSubmitting = ref(false)
const formError = ref(null)
const editFormError = ref(null)

// Form data
const subObjectData = ref({
  name: '',
  title: '',
  projectId: 4
})

const editSubObjectData = ref({
  id: null,
  name: '',
  title: '',
  projectId: 4
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

// Methods
const getSubObjects = async () => {
  try {
    isLoading.value = true
    error.value = null
    const token = localStorage.getItem('token')

    if (!token) {
      await router.push('/login')
      return
    }

    const response = await fetch(`http://localhost:8080/subobjects/${projectId.value}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      credentials: 'include'
    })

    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem('token')
        await router.push('/login')
      }
      error.value = `Ошибка загрузки данных! Статус: ${response.status}`
      return
    }

    const rawResponse = await response.text()
    try {
      subObjects.value = JSON.parse(rawResponse)
    } catch (parseError) {
      console.error("Ошибка парсинга JSON:", parseError)
      error.value = "Сервер вернул некорректные данные"
    }
  } catch (err) {
    error.value = err.message
    console.error('Ошибка при загрузке подобъектов:', err)
  } finally {
    isLoading.value = false
  }
}

const openEditModal = (subObject) => {
  editSubObjectData.value = {
    id: subObject.id,
    name: subObject.name,
    title: subObject.title,
    projectId: subObject.project?.id || 4
  }
  showEditForm.value = true
  editFormError.value = null
}

const updateSubObject = async () => {
  isSubmitting.value = true
  editFormError.value = null

  try {
    const token = localStorage.getItem('token')
    if (!token) {
      editFormError.value = 'Требуется авторизация'
      await router.push('/login')
      return
    }

    const updateData = {
      name: editSubObjectData.value.name,
      title: editSubObjectData.value.title
    }

    const response = await fetch(`http://localhost:8080/subobjects/${editSubObjectData.value.id}`, {
      method: 'PATCH',
      headers: getAuthHeaders(),
      body: JSON.stringify(updateData)
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      editFormError.value = errorData.message || 'Ошибка при обновлении подобъекта'

      if (response.status === 401) {
        localStorage.removeItem('token')
        await router.push('/login')
      }
      return
    }

    // Обновляем данные в таблице
    await getSubObjects()
    closeEditForm()

  } catch (err) {
    console.error('Ошибка:', err)
    editFormError.value = err.message
  } finally {
    isSubmitting.value = false
  }
}

const closeEditForm = () => {
  showEditForm.value = false
  editSubObjectData.value = {
    id: null,
    name: '',
    title: '',
    projectId: 4
  }
  editFormError.value = null
}

const deleteSubObject = async (id) => {
  if (!confirm('Вы действительно хотите удалить подобъект?')) return

  try {
    const token = localStorage.getItem('token')
    if (!token) {
      await router.push('/login')
      return
    }

    const response = await fetch(`http://localhost:8080/subobjects/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      credentials: 'include'
    })

    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem('token')
        await router.push('/login')
      }
      error.value = `Ошибка удаления! Статус: ${response.status}`
      return
    }

    await getSubObjects()
  } catch (err) {
    error.value = err.message
    console.error('Ошибка при удалении подобъекта:', err)
  }
}

const switchProject = (newProjectId) => {
  projectId.value = newProjectId
  getSubObjects()
}

const resetForm = () => {
  subObjectData.value = {
    name: '',
    title: '',
    projectId: projectId.value
  }
  formError.value = null
}

const closeAddForm = () => {
  showAddForm.value = false
  resetForm()
}

const addSubObject = async () => {
  isSubmitting.value = true
  formError.value = null

  try {
    const token = localStorage.getItem('token')
    if (!token) {
      formError.value = 'Требуется авторизация'
      await router.push('/login')
      return
    }

    const response = await fetch('http://localhost:8080/subobjects', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(subObjectData.value)
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      formError.value = errorData.message || 'Ошибка при добавлении подобъекта'

      if (response.status === 401) {
        localStorage.removeItem('token')
        await router.push('/login')
      }
      return
    }

    // Успешное сохранение
    await getSubObjects()
    closeAddForm()

  } catch (err) {
    console.error('Ошибка:', err)
    formError.value = err.message

    if (err.message.includes('401') || err.message.includes('авторизация')) {
      await router.push('/login')
    }
  } finally {
    isSubmitting.value = false
  }
}

// Lifecycle
onBeforeMount(() => {
  getSubObjects()
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

.btn-check:checked + .btn-outline-secondary {
  background: #6c757d;
  color: white;
  border-color: #6c757d;
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

/* Курсор при наведении на редактируемые ячейки */
.cursor-pointer {
  cursor: pointer;
}

.cursor-pointer:hover {
  background-color: rgba(0, 123, 255, 0.1);
  border-radius: 4px;
  padding: 4px 8px;
  margin: -4px -8px;
}

/* Заголовок таблицы */
.sticky-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: #002d72 !important;
}

.sticky-header th {
  background-color: #000000 !important;
  position: sticky;
  top: 0;
  z-index: 11;
  border-bottom: 2px solid #dee2e6;
  color: white;
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

.floating-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.floating-btn:disabled:hover {
  transform: none;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
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

.project-btn {
  animation: floatUp 0.5s ease-out 0.1s both;
  z-index: 1001;
  background: linear-gradient(135deg, #6c757d 0%, #5a6268 100%) !important;
  border: none !important;
  color: white !important;
}

.project-btn.active {
  background: linear-gradient(135deg, #198754 0%, #157347 100%) !important;
  box-shadow: 0 6px 20px rgba(25, 135, 84, 0.4) !important;
}

.project-btn:hover {
  background: linear-gradient(135deg, #5a6268 0%, #495057 100%) !important;
}

.project-btn.active:hover {
  background: linear-gradient(135deg, #157347 0%, #146c43 100%) !important;
}

.project-btn:active {
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

.btn-pulse {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 4px 20px rgba(25, 135, 84, 0.5);
  }
  50% {
    box-shadow: 0 6px 25px rgba(25, 135, 84, 0.8);
  }
  100% {
    box-shadow: 0 4px 20px rgba(25, 135, 84, 0.5);
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
}

@media (max-width: 576px) {
  .floating-form-content .card-body {
    padding: 1rem;
  }
}

.card-header.bg-warning {
  background: linear-gradient(135deg, #ffc107 0%, #e0a800 100%) !important;
}

.btn-warning {
  background: linear-gradient(135deg, #ffc107 0%, #e0a800 100%);
  border: none;
  color: #212529;
}

.btn-warning:hover {
  background: linear-gradient(135deg, #e0a800 0%, #d39e00 100%);
  color: #212529;
}
</style>