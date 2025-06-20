<template>
  <main class="bg-light min-vh-100">
    <Navbar/>

    <div class="container py-4">
      <div class="card shadow-lg border-0">
        <div class="card-header text-white py-3 mt-5">
          <h1 class="mb-0 fw-semibold text-primary text-center">Исполнительные схемы</h1>
        </div>
        <div class="card-body p-0">
          <div class="table-responsive" style="max-height: 70vh;">
            <table class="table table-hover align-middle mb-0">
              <thead class="sticky-top" style="background-color: #002d72;">
              <tr>
                <th scope="col" class="text-center text-black fw-normal">№</th>
                <th scope="col" class="text-center text-black fw-normal">
                  <div class="d-flex justify-content-center align-items-center">
                    <span>Номер схемы</span>
                    <div class="btn-group ms-2" role="group">
                      <button @click="sortAsc" class="btn btn-sm btn-outline-dark p-1 border-0">
                        <i class="bi bi-arrow-up"></i>
                      </button>
                      <button @click="sortDesc" class="btn btn-sm btn-outline-dark p-1 border-0">
                        <i class="bi bi-arrow-down"></i>
                      </button>
                    </div>
                  </div>
                </th>
                <th scope="col" class="text-center text-black fw-normal">Действие</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(schema, index) in schemas" :key="schema.id" :class="{'table-light': index % 2 === 0}">
                <td class="text-center fw-semibold">{{ index + 1 }}</td>
                <td class="text-center">
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
                <td class="text-center">
                  <button @click="confirmDelete(schema)" class="btn btn-sm btn-outline-danger rounded-pill px-3 py-1">
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

    <!-- Модальное окно подтверждения -->
    <div v-if="showDeleteModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5)">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">
          <div class="modal-header border-0 bg-primary text-white">
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
  </main>
</template>

<script setup>
import { ref, onBeforeMount } from 'vue'
import Navbar from '../../components/Navbar.vue'

const schemas = ref([])
const path = ref('http://localhost:8080/acts/schemaAsc')
const showDeleteModal = ref(false)
const schemaToDelete = ref(null)
const sortDirection = ref('asc') // 'asc' или 'desc'

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
    const response = await fetch(path.value, {
      mode: 'cors',
      headers: getAuthHeaders()
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      throw new Error('Ошибка загрузки схем')
    }

    schemas.value = await response.json()
  } catch (error) {
    console.error('Ошибка при загрузке схем:', error)
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
    await fetch(`http://localhost:8080/acts/schema/${schemaToDelete.value}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    })
    await getSchemas()
    showDeleteModal.value = false
  } catch (error) {
    console.error('Ошибка при удалении схемы:', error)
  }
}

onBeforeMount(() => {
  getSchemas()
})
</script>

<style scoped>
/* Добавим стили для кнопок сортировки */
.btn-group .btn {
  line-height: 1;
  min-width: 24px;
}

.btn-group .btn:focus {
  box-shadow: none;
}

.btn-group .btn i {
  font-size: 0.8rem;
}

/* Подсветка активной кнопки сортировки */
.btn-group .btn.active {
  background-color: rgba(255, 255, 255, 0.2);
}

/* Основные стили */
body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* Стили для карточки */
.card {
  border-radius: 12px;
  overflow: hidden;
}

/* Стили для таблицы */
.table {
  font-size: 0.95rem;
  margin-bottom: 0;
}

.table th, .table td {
  padding: 1rem;
  vertical-align: middle;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 45, 114, 0.05);
}

/* Стили для кнопок */
.btn {
  transition: all 0.2s ease;
}

.btn-outline-danger {
  border-width: 2px;
}

.btn-outline-danger:hover {
  background-color: #dc3545;
  color: white;
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
  border-radius: 12px;
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
</style>