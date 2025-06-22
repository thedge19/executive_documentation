<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container px-5 py-4">
      <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3">
          <h1 class="mb-0 mt-3 fw-semibold text-primary text-center">Подобъекты</h1>

          <div class="d-flex justify-content-between align-items-center mt-2">
            <a href="/addSubObject" class="btn btn-primary rounded-pill px-4">
              <i class="bi bi-plus-lg me-2"></i>Добавить подобъект
            </a>

            <div class="btn-group" role="group">
              <input type="radio" class="btn-check" id="project4" @change="onChangeProject"
                     name="project" v-model="projectId" :value="4" autocomplete="off">
              <label class="btn btn-outline-secondary" for="project4">
                <i class="bi bi-tree me-1"></i>Грушовая
              </label>

              <input type="radio" class="btn-check" id="project5" @change="onChangeProject"
                     name="project" v-model="projectId" :value="5" autocomplete="off">
              <label class="btn btn-outline-secondary" for="project5">
                <i class="bi bi-building me-1"></i>Шесхарис
              </label>
            </div>
          </div>
        </div>

        <div class="card-body p-0">
          <div class="table-responsive" style="max-height: 78vh;">
            <table class="table table-hover align-middle mb-0">
              <thead class="sticky-top">
              <tr>
                <th class="text-white ps-4" style="background-color: #000000; width: 7%">ID</th>
                <th class="text-white text-center" style="background-color: #000000; width: 40%">Наименование</th>
                <th class="text-white text-center" style="background-color: #000000; width: 12%">Обозначение</th>
                <th class="text-white text-center" style="background-color: #000000; width: 20%">Объект</th>
                <th class="text-white text-center" style="background-color: #000000; width: 16%">Действие</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(subObject, index) in subObjects" :key="subObject.id"
                  :class="{'table-light': index % 2 === 0}" class="border-top">
                <td class="ps-4 fw-semibold text-muted">{{ subObject.id }}</td>
                <td>
                  <a :href="`/works/${subObject.id}`" class="text-decoration-none text-primary">
                    {{ subObject.name }}
                  </a>
                </td>
                <td class="text-center">{{ subObject.title }}</td>
                <td class="text-center">{{ subObject.project?.name }}</td>
                <td class="text-center pe-4">
                  <div class="d-flex justify-content-center">
                    <a :href="`/editSubObject/${subObject.id}`" class="btn btn-sm btn-outline-primary rounded-pill px-2 me-2 my-2">
                      <i class="bi bi-pencil-square me-1"></i>Изменить
                    </a>
                    <button @click="deleteSubObject(subObject.id)" class="btn btn-sm btn-outline-danger rounded-pill px-2 my-2">
                      <i class="bi bi-trash3 me-1"></i>Удалить
                    </button>
                  </div>
                </td>
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
import { ref, onBeforeMount } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const subObjects = ref([])
const projectId = ref(4) // Значение по умолчанию
const error = ref(null)
const isLoading = ref(false)

const getSubObjects = async () => {
  try {
    isLoading.value = true
    const token = localStorage.getItem('token')

    if (!token) {
      router.push('/login')
      return
    }

    const response = await fetch(`http://localhost:8080/subobjects/${projectId.value}`, {
      mode: 'cors',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      credentials: 'include'
    })

    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    subObjects.value = await response.json()
  } catch (err) {
    error.value = err.message
    console.error('Ошибка при загрузке подобъектов:', err)
  } finally {
    isLoading.value = false
  }
}

const deleteSubObject = async (id) => {
  if (!confirm('Вы действительно хотите удалить подобъект?')) return

  try {
    const token = localStorage.getItem('token')

    if (!token) {
      router.push('/login')
      return
    }

    const response = await fetch(`http://localhost:8080/subobjects/${id}`, {
      method: 'DELETE',
      mode: 'cors',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      credentials: 'include'
    })

    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    await getSubObjects()
  } catch (err) {
    error.value = err.message
    console.error('Ошибка при удалении подобъекта:', err)
  }
}

const onChangeProject = () => {
  getSubObjects()
}

onBeforeMount(() => {
  getSubObjects()
})
</script>

<style scoped>
.card {
  border-radius: 12px;
  overflow: hidden;
}

.table {
  font-size: 0.95rem;
}

.table th {
  font-weight: 500;
  position: sticky;
  top: 0;
  vertical-align: middle;
}

.table td {
  vertical-align: middle;
}

.table-light {
  background-color: #f8f9fa;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 0, 0, 0.03);
}

.btn-group .btn {
  border-radius: 20px;
  margin: 0 2px;
}

.btn-check:checked + .btn-outline-secondary {
  background-color: #0d6efd;
  color: white;
  border-color: #0d6efd;
}

.table-responsive::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.table-responsive::-webkit-scrollbar-thumb {
  background-color: #000000;
  border-radius: 4px;
}

.table-responsive::-webkit-scrollbar-track {
  background-color: #f1f1f1;
}

@media (max-width: 768px) {
  .btn-group {
    flex-wrap: wrap;
    gap: 5px;
  }

  .btn-group .btn {
    margin: 2px;
    flex-grow: 1;
  }

  .table td, .table th {
    padding: 0.5rem;
  }

  .btn-sm {
    padding: 0.25rem 0.5rem;
    font-size: 0.8rem;
  }
}

/* Анимация загрузки */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.table tbody tr {
  animation: fadeIn 0.3s ease forwards;
}
</style>