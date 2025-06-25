<template>
  <!-- Шаблон остается без изменений -->
  <main class="bg-light min-vh-100">
    <Navbar/>

    <div class="container py-4">
      <div class="card shadow-sm border-0">
        <div class="card-header bg-primary text-white py-3 mt-5">
          <h1 class="h3 mb-0 text-center">Учет выполненных работ</h1>
        </div>

        <div class="card-body">-
          <!-- Controls -->
          <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
            <div class="d-flex justify-content-start">
              <a :href="`/addWork/${subObjectId}?page=${works.totalPages - 1}`"
                 class="btn btn-primary rounded-pill my-2">
                <i class="bi bi-plus-circle me-2"></i>Добавить работу
              </a>
              <router-link
                  v-if="works.content && works.content.length > 0"
                  :to="`/subObjects/${works.content[0].projectId}`"
                  class="btn btn-outline-secondary rounded-pill m-lg-2">
                <i class="bi bi-arrow-left me-2"></i>В подобъекты
              </router-link>
            </div>

            <div class="flex-grow-1 mx-md-3" style="max-width: 500px;">
              <div class="input-group">
                <label class="input-group-text bg-white"><i class="bi bi-building"></i></label>
                <select class="form-select" v-model="subObjectId" @change="onChangeSubObject()">
                  <option value="" disabled selected>Выберите подобъект...</option>
                  <option v-for="subObject in subObjects" :value="subObject.id">
                    {{ subObject.name }}
                  </option>
                </select>
              </div>
            </div>
          </div>

          <!-- Table -->
          <div class="table-responsive">
            <table class="table table-hover align-middle">
              <thead class="table-dark">
              <tr>
                <th scope="col" class="text-nowrap">ID</th>
                <th scope="col" class="text-nowrap">Наименование</th>
                <th scope="col" class="text-nowrap">Ед. изм.</th>
                <th scope="col" class="text-nowrap">Количество</th>
                <th scope="col" class="text-nowrap">Выполнено</th>
                <th scope="col" class="text-nowrap">Закрыто, руб.</th>
                <th scope="col" class="text-nowrap">Осталось</th>
                <th scope="col" class="text-nowrap">Не закрыто, руб.</th>
                <th scope="col" class="text-nowrap text-end" style="width:15%">Действие</th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="works.content && works.content.length > 0" v-for="work in works.content" :key="work.id">
                <th scope="row" class="fw-semibold">{{ work.id }}</th>
                <td :class="{ 'fw-bold': work.unitPrice > 0 }">{{ work.name }}</td>
                <td class="text-center">{{ work.units }}</td>
                <td class="text-center">{{ work.quantity }}</td>
                <td class="text-center">{{ work.done }}</td>
                <td class="text-center">{{ work.doneAmount.toFixed(2) }}</td>
                <td class="text-center">{{ work.finalQuantity }}</td>
                <td class="text-center">{{ work.remainingAmount.toFixed(2) }}</td>
                <td class="text-end">
                  <div class="d-flex justify-content-end gap-2">
                    <a class="btn btn-sm btn-outline-primary"
                       :href="`/editWork/${work.id}?page=${works.number}&subObjectId=${subObjectId}`">
                      <i class="bi bi-pencil"></i>
                    </a>
                    <button class="btn btn-sm btn-outline-danger" @click="deleteWork(work.id)">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-else>
                <td colspan="7" class="text-center py-4 text-muted">
                  <i class="bi bi-exclamation-circle fs-4 d-block mb-2"></i>
                  Нет данных для отображения
                </td>
              </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div class="d-flex flex-column align-items-center mt-4">
            <nav aria-label="Page navigation">
              <ul class="pagination pagination-sm">
                <li class="page-item" :class="{ disabled: works.first }">
                  <button class="page-link" @click="changePage(0)">
                    <i class="bi bi-chevron-double-left"></i>
                  </button>
                </li>
                <li class="page-item" :class="{ disabled: works.first }">
                  <button class="page-link" @click="changePage(works.number - 1)">
                    <i class="bi bi-chevron-left"></i>
                  </button>
                </li>

                <li class="page-item" v-for="page in pageNumbers" :key="page"
                    :class="{ active: works.number === page }">
                  <button class="page-link" @click="changePage(page)">{{ page + 1 }}</button>
                </li>

                <li class="page-item" :class="{ disabled: works.last }">
                  <button class="page-link" @click="changePage(works.number + 1)">
                    <i class="bi bi-chevron-right"></i>
                  </button>
                </li>
                <li class="page-item" :class="{ disabled: works.last }">
                  <button class="page-link" @click="changePage(works.totalPages - 1)">
                    <i class="bi bi-chevron-double-right"></i>
                  </button>
                </li>
              </ul>
            </nav>

            <div v-if="works.totalElements > 0" class="text-muted small mt-2">
              Показано {{ works.numberOfElements }} из {{ works.totalElements }} работ
              (Страница {{ works.number + 1 }} из {{ works.totalPages }})
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const route = useRoute()
const error = ref("")

const isLoading = ref(false)
const works = ref({
  content: [],
  number: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true
})
const subObjects = ref([])
const subObjectId = ref(route.params.id)
const pageSize = ref(10)

const pageNumbers = computed(() => {
  const current = works.value.number
  const total = works.value.totalPages
  const range = 2

  let start = Math.max(0, current - range)
  let end = Math.min(total - 1, current + range)

  if (current - range < 0) {
    end = Math.min(total - 1, end + (range - current))
  }

  if (current + range >= total) {
    start = Math.max(0, start - (current + range - total + 1))
  }

  const pages = []
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    error.value = 'Требуется авторизация';
    return;
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
  isLoading.value = true;
  try {
    const headers = getAuthHeaders();

    // Получаем page из URL или используем 0
    const page = parseInt(route.query.page) || 0;

    // Обязательно сохраняем номер страницы в состоянии
    works.value.number = page;

    const response = await fetch(
        `http://localhost:8080/workings/${subObjectId.value}?page=${page}&size=${pageSize.value}`,
        { headers }
    )

    if (!response.ok) {
      // обработка ошибок
    }

    const data = await response.json();
    works.value = {
      ...data,
      number: page // Сохраняем актуальный номер страницы
    }

    // Если в URL не было параметра page - добавляем его
    if (!route.query.page && page !== 0) {
      await router.replace({ query: { ...route.query, page } })
    }

  } catch (err) {
    console.error('Ошибка:', err)
    if (err.message.includes('авторизация')) {
      handleUnauthorized()
    }
  } finally {
    isLoading.value = false
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
      error.value = 'Ошибка при удалении';
      return;
    }

    await getWorks()
    alert('Работа успешно удалена')
  } catch (err) {
    console.error('Ошибка:', err)
    alert(err.message || 'Не удалось удалить работу')
  }
}

const getSubObjects = async () => {
  try {
    const headers = getAuthHeaders()

    const response = await fetch('http://localhost:8080/subobjects', { headers })

    if (!response.ok) {
      if (response.status === 401) {
        handleUnauthorized()
        return;
      }
      error.value = 'Ошибка загрузки подобъектов';
      return;
    }

    subObjects.value = await response.json()
  } catch (err) {
    console.error('Ошибка:', err)
    if (err.message.includes('авторизация')) {
      handleUnauthorized()
    }
  }
}

const onChangeSubObject = () => {
  works.value.number = 0
  getWorks()
}

const changePage = (pageNumber) => {
  if (pageNumber >= 0 && pageNumber < works.value.totalPages) {
    works.value.number = pageNumber;
    // Обновляем URL с новым параметром page
    router.push({ query: { ...route.query, page: pageNumber } });
    getWorks();
  }
};

onMounted(() => {
  // Инициализируем номер страницы из URL
  if (route.query.page) {
    works.value.number = parseInt(route.query.page);
  }
  getWorks();
  getSubObjects();
});
</script>

<style scoped>
/* Стили остаются без изменений */
.card {
  border-radius: 10px;
  overflow: hidden;
}

.table {
  font-size: 0.9rem;
  margin-bottom: 0;
}

.table th {
  font-weight: 500;
  white-space: nowrap;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 0, 0, 0.03);
}

.btn {
  transition: all 0.2s;
}

.btn-outline-primary:hover, .btn-outline-danger:hover {
  color: white;
}

.page-item.active .page-link {
  background-color: #0d6efd;
  border-color: #0d6efd;
}

.page-link {
  min-width: 36px;
  text-align: center;
}

.input-group-text {
  border-right: none;
}

.form-select {
  border-left: none;
}

.input-group:focus-within {
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
  border-radius: 0.375rem;
}

@media (max-width: 768px) {
  .table-responsive {
    font-size: 0.8rem;
  }

  .btn-sm {
    padding: 0.25rem 0.5rem;
    font-size: 0.7rem;
  }
}
</style>