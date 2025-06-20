<template>
  <main class="bg-light min-vh-100" style="overflow-y: hidden;">
    <Navbar />

    <div class="container-fluid px-5 py-4">
      <div class="card shadow-sm border-0">
        <div class="card-header text-primary py-3 mt-3">
          <h1 class="mb-0 text-center">Входной контроль</h1>
        </div>

        <div class="card-body p-0">
          <!-- Кнопка выгрузки -->
          <div class="d-flex justify-content-start px-4 py-3">
            <button @click="generateLogPdf" class="btn btn-primary rounded-pill px-4" :disabled="isLoading">
              <i class="bi bi-file-earmark-pdf me-2"></i>Выгрузить журнал в PDF
            </button>
          </div>

          <!-- Таблица -->
          <div class="table-responsive" style="height: calc(100vh - 220px); overflow-y: auto;">
            <table class="table table-hover align-middle mb-0">
              <thead class="sticky-top bg-dark">
              <tr>
                <th style="width: 5%; background-color: #000000; color: white;">№</th>
                <th style="width: 7%; background-color: #000000; color: white;">Дата</th>
                <th class="text-white fw-normal" style="width: 20%; background-color: #000000; color: white;">Материалы</th>
                <th class="text-white fw-normal" style="width: 30%; background-color: #000000; color: white;">Документы</th>
                <th class="text-white fw-normal" style="width: 10%; background-color: #000000; color: white;">Автор серта</th>
                <th class="text-white fw-normal" style="width: 15%; background-color: #000000; color: white;">ГОСТ, ТУ</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="control in controls" :key="control.id">
                <td>{{ control.controlNumber }}</td>
                <td>{{ control.date }}</td>
                <td>
                  <a href="#" @click.prevent="generatePdf(control.id)" class="text-decoration-none text-primary">
                    {{ control.materials }}
                    <i class="bi bi-file-earmark-pdf ms-1 text-danger"></i>
                  </a>
                </td>
                <td>{{ control.documents }}</td>
                <td>{{ control.author }}</td>
                <td>{{ control.standard }}</td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Navbar from '../../components/Navbar.vue'

const controls = ref([])
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
  window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
}

const getControls = async () => {
  try {
    isLoading.value = true
    error.value = null

    const response = await fetch('http://localhost:8080/acts/entrance', {
      headers: getAuthHeaders()
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      throw new Error('Ошибка загрузки данных входного контроля')
    }

    controls.value = await response.json()
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message
  } finally {
    isLoading.value = false
  }
}

const generatePdf = (id) => {
  const token = localStorage.getItem('token')
  if (!token) {
    handleUnauthorized()
    return
  }
  window.open(`http://localhost:8080/acts/${id}/pdf/control?token=${token}`, '_blank')
}

const generateLogPdf = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    handleUnauthorized()
    return
  }
  window.open(`http://localhost:8080/acts/pdf/controlLog?token=${token}`, '_blank')
}

onMounted(() => {
  getControls()
})
</script>

<style scoped>
.card {
  border-radius: 10px;
  overflow: hidden;
}

.table {
  font-size: 0.9rem;
}

.table th {
  font-weight: 500;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 45, 114, 0.05);
}

.btn {
  transition: all 0.2s;
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

@media (max-width: 768px) {
  .table-responsive {
    font-size: 0.8rem;
  }
}
</style>