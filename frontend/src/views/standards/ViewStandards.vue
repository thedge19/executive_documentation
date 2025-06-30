<template>
  <!-- Шаблон остается без изменений -->
  <Navbar/>

  <div class="container py-5">
    <div class="card shadow-sm border-0">
      <div class="card-header bg-white py-3">
        <h1 class="text-center mb-0 fw-semibold text-primary">СП</h1>
        <a href="/addStandard" class="btn btn-primary rounded-pill px-4">
          <i class="bi bi-plus-lg me-2"></i>Добавить СП
        </a>
      </div>

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
/* Стили остаются без изменений */
.card {
  border-radius: 12px;
  overflow: hidden;
}

.table {
  font-size: 0.95rem;
  margin-bottom: 0;
  width: 100%;
}

.table th {
  font-weight: 500;
  vertical-align: middle;
  background-color: #000000;
  color: white;
  white-space: nowrap;
}

.table td {
  vertical-align: middle;
}

.table-light {
  background-color: #f8f9fa;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 45, 114, 0.05);
}

.btn-outline-primary:hover {
  background-color: #0d6efd;
  color: white;
}

.btn-outline-danger:hover {
  background-color: #dc3545;
  color: white;
}

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

@media (max-width: 768px) {
  .table-responsive {
    font-size: 0.8rem;
  }

  .btn-sm {
    padding: 0.25rem 0.5rem;
    font-size: 0.8rem;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .table th, .table td {
    padding: 0.5rem;
  }

  .table th:nth-child(1),
  .table td:nth-child(1) {
    width: 20%;
  }

  .table th:nth-child(2),
  .table td:nth-child(2) {
    width: 50%;
  }

  .table th:nth-child(3),
  .table td:nth-child(3) {
    width: 30%;
  }
}
</style>