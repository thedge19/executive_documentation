<template>
  <Navbar/>
  <div class="container-fluid px-4 py-4">
    <div class="card shadow-sm border-0">
      <div class="card-body p-0">
        <h1 class="text-center text-primary my-4 mt-5">АОСР</h1>

        <!-- Action buttons -->
        <div class="d-flex justify-content-start px-4">
          <div>
            <a href="/addAct" class="btn btn-primary rounded-pill px-4">
              <i class="bi bi-plus-lg me-2"></i>Добавить акт
            </a>
          </div>
          <div class="mx-4">
            <button v-if="showDates" @click.prevent="showRegistryDates"
                    class="btn btn-outline-dark rounded-pill px-4">
              <i class="bi bi-border-width me-2"></i>Реестр
            </button>
          </div>
        </div>

        <!-- Date selection form -->
        <div v-if="!showDates" class="px-4 py-3">
          <div class="d-flex flex-wrap align-items-center gap-3">
            <div>
              <label class="input-group-text"><i class="bi bi-calendar-month me-2"></i>Дата начала периода</label>
              <VDatePicker v-model="startDate" mode="date"/>
            </div>
            <div>
              <label class="input-group-text"><i class="bi bi-calendar-month me-2"></i>Дата окончания периода</label>
              <VDatePicker v-model="endDate" mode="date"/>
            </div>
          </div>
          <div class="d-flex mt-3">
            <button @click.prevent="addDates" class="btn btn-outline-success rounded-pill px-4 me-2">
              <i class="bi bi-file-earmark-pdf me-2 text-danger"></i>Сформировать реестр
            </button>
            <button @click.prevent="showRegistryDates" class="btn btn-outline-danger rounded-pill px-4">
              <i class="bi bi-x"></i>Отмена
            </button>
          </div>
        </div>

        <!-- Table with black header -->
        <div class="table-responsive mt-3" style="max-height: 78vh;">
          <table class="table table-hover align-middle mb-0">
            <thead class="sticky-top">
            <tr>
              <th class="text-white text-center" style="width: 7%; background-color: #000000">№</th>
              <th class="text-white text-center" style="width: 5%; background-color: #000000">Дата</th>
              <th class="text-white text-center" style="width: 15%; background-color: #000000">Объект</th>
              <th class="text-white text-center" style="width: 20%; background-color: #000000">Выполненные работы</th>
              <th class="text-white text-center" style="width: 7%; background-color: #000000">Начало</th>
              <th class="text-white text-center" style="width: 25%; background-color: #000000">Материалы</th>
              <th class="text-white text-center" style="width: 20%; background-color: #000000">Предъявлены документы
              </th>
              <th class="text-white text-center" style="width: 25%; background-color: #000000">Выполнено в соответствии
                с
              </th>
              <th class="text-white text-center" style="width: 20%; background-color: #000000">Разрешается выполнение
              </th>
              <th class="text-white text-center" style="width: 12%; background-color: #000000">Действие</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="act in acts" :key="act.id">
              <td class="text-center">{{ act.actNumber }}</td>
              <td class="text-center" :style="[act.executiveSchemaId != null ? `color:blue` : `color:red`]">
                {{ act.endDate }}
              </td>
              <td>{{ act.projectName }}</td>
              <td>
                <a href="#" @click.prevent="generatePdf(act.id)" class="text-decoration-none">
                  {{ act.works }}
                  <i class="bi bi-file-earmark-pdf ms-1 text-danger"></i>
                </a>
              </td>
              <td>{{ act.startDate }}</td>
              <td class="text-center">{{ act.materials }}</td>
              <td>{{ act.submittedDocuments }}</td>
              <td>{{ act.inAccordWith }}</td>
              <td>{{ act.nextWorks }}</td>
              <td class="text-center">
                <div class="d-flex justify-content-center">
                  <a class="btn btn-primary btn-sm" :href="`/editAct/${act.id}`">Edit</a>
                  <button class="btn btn-danger btn-sm mx-1" @click="deleteAct(act.id)">Delete</button>
                </div>
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

const acts = ref([])
const path = ref('http://localhost:8080/acts')
const containerPath = ref('http://localhost:8080/acts/container')
const startDate = ref(new Date())
const endDate = ref(new Date())
const showDates = ref(true)
const isLoading = ref(false)
const error = ref(null)
const container = ref("");

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

const getActs = async () => {
  try {
    isLoading.value = true
    error.value = null

    const response = await fetch(path.value, {
      headers: getAuthHeaders()
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      error.value = 'Ошибка загрузки актов';
      return;
    }

    acts.value = await response.json()
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message
  } finally {
    isLoading.value = false
  }
}

const deleteAct = async (id) => {
  if (!confirm('Вы уверены, что хотите удалить этот акт?')) return

  try {
    const response = await fetch(`http://localhost:8080/acts/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      error.value = 'Ошибка удаления акта';
      return;
    }

    await getActs()
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message
  }
}

const generatePdf = async (actId) => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      handleUnauthorized();
      return;
    }

    // Открываем новое окно заранее, чтобы блокировщики не мешали
    const pdfWindow = window.open('', '_blank');

    // Делаем запрос с заголовками авторизации
    const response = await fetch(`http://localhost:8080/acts/${actId}/pdf`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    if (response.status === 401) {
      handleUnauthorized();
      pdfWindow.close();
      return;
    }

    if (!response.ok) {
      error.value = 'Ошибка сервера';
      return;
    }

    // Получаем PDF как blob
    const blob = await response.blob();
    // Отображаем PDF в новом окне
    pdfWindow.location.href = URL.createObjectURL(blob);

  } catch (err) {
    console.error('Ошибка при генерации PDF:', err);
    error.value = 'Не удалось сформировать PDF';
  }
}

const addDates = async () => {
  try {
    const formatDate = (date) => {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }

    const response = await fetch('http://localhost:8080/acts/registries', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({
        startDate: formatDate(startDate.value),
        endDate: formatDate(endDate.value)
      })
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      error.value = 'Ошибка сервера';
      return;
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'реестр.pdf'
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)

  } catch (error) {
    console.error('Ошибка:', error)
    error.value = 'Не удалось сформировать реестр'
  }
}

const showRegistryDates = () => {
  showDates.value = !showDates.value
}

onMounted(() => {
  getActs()
})
</script>

<style scoped>
.card {
  border-radius: 10px;
}

.table {
  font-size: 0.9rem;
}

.table th {
  font-weight: 500;
  white-space: nowrap;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 0, 0, 0.03);
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

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.8rem;
}

@media (max-width: 768px) {
  .table-responsive {
    font-size: 0.8rem;
  }
}
</style>