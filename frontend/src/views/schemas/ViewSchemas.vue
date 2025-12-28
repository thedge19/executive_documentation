<template>
  <Navbar/>
  <div class="container py-4">
    <div class="row justify-content-center mt-5">
      <div class="col-12">
        <!-- Заголовок -->
        <h1 class="text-light text-center mb-4">Исполнительные схемы</h1>

        <!-- Error message -->
        <div v-if="error" class="alert alert-danger mb-4">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
        </div>

        <!-- Table -->
        <div class="card shadow-sm border-0">
          <div class="card-body p-0">
            <div class="table-responsive" style="max-height: 81vh;">
              <table class="table table-hover mb-0">
                <thead class="sticky-top" style="background-color: #002d72;">
                <tr>
                  <th class="text-center text-white fw-normal" style="width: 10%; background-color: #000000">№</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000">
                    <div class="d-flex justify-content-center align-items-center">
                      <span>Номер схемы</span>
                      <div class="btn-group ms-2" role="group">
                        <button @click="sortAsc"
                                class="btn btn-sm p-1 border-0"
                                :class="{'btn-info': sortDirection === 'asc', 'btn-outline-light': sortDirection !== 'asc'}">
                          <i class="bi bi-arrow-up"></i>
                        </button>
                        <button @click="sortDesc"
                                class="btn btn-sm p-1 border-0"
                                :class="{'btn-info': sortDirection === 'desc', 'btn-outline-light': sortDirection !== 'desc'}">
                          <i class="bi bi-arrow-down"></i>
                        </button>
                      </div>
                    </div>
                  </th>
                  <th class="text-center text-white fw-normal" style="width: 20%; background-color: #000000">Действие</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(schema, index) in schemas" :key="schema.id" :class="{'table-light': index % 2 === 0}">
                  <td class="text-center align-middle fw-semibold">{{ index + 1 }}</td>
                  <td class="text-center align-middle">
                    <span v-if="!schema.schemaPath" class="text-muted">{{ schema.schemasActNumber }}</span>
                    <a v-else
                       :href="schema.schemaPath"
                       target="_blank"
                       class="text-decoration-none text-primary fw-medium"
                       :title="schema.schemasActNumber">
                      {{ schema.schemasActNumber || 'Скачать схему' }}
                      <i class="bi bi-file-earmark-pdf ms-1 text-danger"></i>
                    </a>
                  </td>
                  <td class="text-center align-middle">
                    <button @click="confirmDelete(schema)" class="btn btn-sm btn-outline-danger rounded-pill px-3">
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

  <!-- Модальное окно подтверждения -->
  <div v-if="showDeleteModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5)">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow-lg">
        <div class="modal-header border-0" style="background-color: #002d72; color: white;">
          <h5 class="modal-title">Подтверждение удаления</h5>
          <button type="button" class="btn-close btn-close-white" @click="showDeleteModal = false"></button>
        </div>
        <div class="modal-body py-4">
          <p class="lead">Вы уверены, что хотите удалить эту схему?</p>
          <p class="text-muted small">Это действие нельзя будет отменить.</p>
        </div>
        <div class="modal-footer border-0">
          <button type="button" class="btn btn-outline-secondary rounded-pill px-4" @click="showDeleteModal = false">
            Отмена
          </button>
          <button type="button" class="btn btn-danger rounded-pill px-4" @click="executeDelete">
            <i class="bi bi-trash3 me-1"></i>Удалить
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onBeforeMount} from 'vue'
import Navbar from '../../components/Navbar.vue'

const schemas = ref([])
const path = ref('http://localhost:8080/acts/schemaAsc')
const showDeleteModal = ref(false)
const schemaToDelete = ref(null)
const sortDirection = ref('asc')
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
  window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
}

const getSchemas = async () => {
  try {
    error.value = null
    const response = await fetch(path.value, {
      headers: getAuthHeaders()
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      error.value = 'Ошибка загрузки схем';
      return;
    }

    schemas.value = await response.json()
  } catch (err) {
    console.error('Ошибка при загрузке схем:', err)
    error.value = err.message
  }
}

const sortAsc = () => {
  path.value = 'http://localhost:8080/acts/schemaAsc'
  sortDirection.value = 'asc'
  getSchemas()
}

const sortDesc = () => {
  path.value = 'http://localhost:8080/acts/schemaDesc'
  sortDirection.value = 'desc'
  getSchemas()
}

const confirmDelete = (schema) => {
  schemaToDelete.value = schema.id
  showDeleteModal.value = true
}

const executeDelete = async () => {
  try {
    const response = await fetch(`http://localhost:8080/acts/schema/${schemaToDelete.value}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    })

    if (!response.ok) {
      error.value = 'Ошибка при удалении схемы';
      return;
    }

    await getSchemas()
    showDeleteModal.value = false
  } catch (err) {
    console.error('Ошибка при удалении схемы:', err)
    error.value = err.message
  }
}

onBeforeMount(() => {
  getSchemas()
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

.btn-outline-light {
  border: 1px solid #f8f9fa;
  color: #f8f9fa;
  background: transparent;
}

.btn-outline-light:hover {
  background: #f8f9fa;
  color: #212529;
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

.btn-danger {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
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

/* Модальное окно */
.modal-content {
  border-radius: 8px;
  overflow: hidden;
}

.modal-header {
  padding: 1.2rem 1.5rem;
}

.modal-footer {
  padding: 1rem 1.5rem;
}

/* Анимации */
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

/* Иконки */
.bi {
  font-size: 1rem;
  vertical-align: middle;
}

/* Ссылки */
.text-primary {
  color: #002d72 !important;
}

a.text-primary:hover {
  color: #001a3d !important;
  text-decoration: underline;
}

/* Кнопки сортировки */
.btn-group .btn {
  line-height: 1;
  min-width: 24px;
  padding: 0.25rem;
}

.btn-group .btn:focus {
  box-shadow: none;
}

.btn-group .btn i {
  font-size: 0.8rem;
}

/* Адаптивность */
@media (max-width: 768px) {
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