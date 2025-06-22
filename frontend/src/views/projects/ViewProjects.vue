<template>
  <main class="bg-light min-vh-100">
    <Navbar/>

    <div class="container py-5">
      <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3 align-items-center">
          <h1 class="mb-0 fw-semibold text-primary text-center">Объекты</h1>
          <a href="/addProject" class="btn btn-primary rounded-pill px-4">
            <i class="bi bi-plus-lg me-2"></i>Добавить объект
          </a>
        </div>

        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
              <tr>
                <th scope="col" class="ps-4">ID</th>
                <th scope="col">Наименование</th>
                <th scope="col" class="text-end pe-4">Действие</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="project in projects" :key="project.id" class="border-top">
                <td class="ps-4 fw-semibold text-muted">{{ project.id }}</td>
                <td class="fw-medium">
                  <a :href="`/subObjects/${project.id}`" class="text-decoration-none text-primary">
                    {{ project.name }}
                  </a>
                </td>
                <td class="text-end pe-4">
                  <a :href="`/editProject/${project.id}`" class="btn btn-sm btn-outline-primary rounded-pill px-3 me-2">
                    <i class="bi bi-pencil-square me-1"></i>Изменить
                  </a>
                  <button @click="confirmDelete(project.id, project.name)"
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
  </main>
</template>

<script setup>
import {ref, onBeforeMount} from 'vue'
import {useRouter} from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const projects = ref([])
const error = ref(null)

const getProjects = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      router.push('/login')
      return
    }

    const response = await fetch('http://localhost:8080/projects', {
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
      error.value = `Ошибка HTTP: ${response.status}`;
      return;
    }

    projects.value = await response.json()
  } catch (err) {
    error.value = err.message
    console.error('Ошибка при загрузке проектов:', err)
  }
}

const confirmDelete = (id, name) => {
  if (confirm(`Вы действительно хотите удалить объект "${name}"?`)) {
    deleteProject(id)
  }
}

const deleteProject = async (id) => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      router.push('/login')
      return
    }

    const response = await fetch(`http://localhost:8080/projects/${id}`, {
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
      error.value = `Ошибка HTTP: ${response.status}`;
      return;
    }

    await getProjects()
  } catch (err) {
    error.value = err.message
    console.error('Ошибка при удалении проекта:', err)
  }
}

onBeforeMount(getProjects)
</script>

<style scoped>
.card {
  border-radius: 12px;
  overflow: hidden;
}

.table {
  margin-bottom: 0;
}

.table th, .table td {
  padding: 1rem;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 123, 255, 0.05);
}
</style>