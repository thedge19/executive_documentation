<template>
  <Navbar/>
  <div class="container py-3">
    <div class="row justify-content-center mt-5">
      <div class="col-12">
        <!-- Заголовок и элементы управления -->
        <div class="d-flex align-items-center mb-3 position-relative">
          <!-- Кнопки слева -->
          <div class="d-flex">
            <a :href="`/addWork/${subObjectId}?page=${works.totalPages - 1}`"
               class="btn btn-info mx-2 shadow-sm rounded-pill">
              <i class="bi bi-plus-circle me-2"></i>Добавить работу
            </a>
            <router-link
                v-if="works.content && works.content.length > 0"
                :to="`/subObjects/${works.content[0].projectId}`"
                class="btn btn-outline-secondary rounded-pill mx-2">
              <i class="bi bi-arrow-left me-2"></i>В подобъекты
            </router-link>
          </div>

          <!-- Заголовок по центру -->
          <h1 class="text-light position-absolute start-50" style="width: max-content;">
            Работы
          </h1>

          <!-- Выбор подобъекта справа -->
          <div class="flex-grow-1 ms-auto" style="max-width: 400px;">
            <div class="input-group">
              <label class="input-group-text bg-white border-end-0"><i class="bi bi-building"></i></label>
              <select class="form-select border-start-0" v-model="subObjectId" @change="onChangeSubObject()">
                <option value="" disabled selected>Выберите подобъект...</option>
                <option v-for="subObject in subObjects" :value="subObject.id">
                  {{ subObject.name }}
                </option>
              </select>
            </div>
          </div>
        </div>

        <!-- Error message -->
        <div v-if="error" class="alert alert-danger mb-2">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
        </div>

        <!-- Table -->
        <div class="card shadow-sm border-0">
          <div class="card-body p-0">
            <div class="table-responsive" style="max-height: 85vh;">
              <table class="table table-hover mb-0">
                <thead class="sticky-top" style="background-color: #002d72;">
                <tr>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">ID</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Наименование</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Ед. изм.</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Количество</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Выполнено</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Закрыто, руб.</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Осталось</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Не закрыто</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Всего, руб.</th>
                  <th class="text-center text-white fw-normal" style="background-color: #000000;">Действия</th>
                </tr>
                </thead>
                <tbody>
                <tr v-if="works.content && works.content.length > 0"
                    v-for="(work, index) in works.content"
                    :key="work.id"
                    :class="{'table-light': index % 2 === 0}">
                  <td class="text-center align-middle fw-semibold">{{ work.id }}</td>
                  <td class="align-middle" :class="{ 'fw-bold': work.unitPrice > 0 }">{{ work.name }}</td>
                  <td class="text-center align-middle">{{ work.units }}</td>
                  <td class="text-center align-middle">{{ work.quantity }}</td>
                  <td class="text-center align-middle">{{ work.done }}</td>
                  <td class="text-center align-middle">{{ work.doneAmount.toFixed(2) }}</td>
                  <td class="text-center align-middle">{{ work.finalQuantity }}</td>
                  <td class="text-center align-middle">{{ work.remainingAmount.toFixed(2) }}</td>
                  <td class="text-center align-middle">{{ work.totalAmount.toFixed(2) }}</td>
                  <td class="text-center align-middle">
                    <div class="d-flex justify-content-center gap-2">
                      <a class="btn btn-sm btn-outline-primary rounded-pill px-3"
                         :href="`/editWork/${work.id}?page=${works.number}&subObjectId=${subObjectId}`">
                        <i class="bi bi-pencil me-1"></i>Изменить
                      </a>
                      <button class="btn btn-sm btn-outline-danger rounded-pill" @click="deleteWork(work.id)">
                        <i class="bi bi-trash me-1"></i>Удалить
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-else>
                  <td colspan="10" class="text-center py-4 text-muted">
                    <i class="bi bi-exclamation-circle fs-4 d-block mb-2"></i>
                    Нет данных для отображения
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Pagination and Total -->
        <div class="d-flex justify-content-between align-items-center mt-2">
          <!-- Pagination (centered) -->
          <div class="flex-grow-1 d-flex justify-content-center">
            <nav aria-label="Page navigation">
              <ul class="pagination pagination-sm mb-0">
                <li class="page-item" :class="{ disabled: works.first }">
                  <button class="page-link rounded-pill mx-1" @click="changePage(0)">
                    <i class="bi bi-chevron-double-left"></i>
                  </button>
                </li>
                <li class="page-item" :class="{ disabled: works.first }">
                  <button class="page-link rounded-pill mx-1" @click="changePage(works.number - 1)">
                    <i class="bi bi-chevron-left"></i>
                  </button>
                </li>

                <li class="page-item" v-for="page in pageNumbers" :key="page"
                    :class="{ active: works.number === page }">
                  <button class="page-link rounded-pill mx-1" @click="changePage(page)">{{ page + 1 }}</button>
                </li>

                <li class="page-item" :class="{ disabled: works.last }">
                  <button class="page-link rounded-pill mx-1" @click="changePage(works.number + 1)">
                    <i class="bi bi-chevron-right"></i>
                  </button>
                </li>
                <li class="page-item" :class="{ disabled: works.last }">
                  <button class="page-link rounded-pill mx-1" @click="changePage(works.totalPages - 1)">
                    <i class="bi bi-chevron-double-right"></i>
                  </button>
                </li>
              </ul>
            </nav>
          </div>

          <!-- Total amount (right-aligned) -->
          <div v-if="works.content && works.content.length > 0" class="ms-3">
            <div class="alert alert-success mb-0 py-2 px-3 rounded-pill">
              <strong>Итого по подобъекту:</strong>
              <span class="ms-2 fw-bold">{{ formatCurrency(totalAmountBySubObject) }}</span>
            </div>
          </div>
        </div>

        <!-- Page info -->
        <div v-if="works.totalElements > 0" class="text-light small mt-2 text-center">
          Показано {{ works.numberOfElements }} из {{ works.totalElements }} работ
          (Страница {{ works.number + 1 }} из {{ works.totalPages }})
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const route = useRoute()
const error = ref("")
const totalAmountBySubObject = ref(0)
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
    const page = parseInt(route.query.page) || 0;

    const response = await fetch(
        `http://localhost:8080/workings/${subObjectId.value}?page=${page}&size=${pageSize.value}`,
        {headers}
    );

    if (!response.ok) throw new Error('Ошибка загрузки данных');

    const data = await response.json();

    works.value = {
      content: data.content,
      number: data.metadata.number,
      size: data.metadata.size,
      totalElements: data.metadata.totalElements,
      totalPages: data.metadata.totalPages,
      first: data.metadata.number === 0,
      last: data.metadata.number === data.metadata.totalPages - 1
    };

  } catch (err) {
    error.value = err.message;
  } finally {
    isLoading.value = false;
  }
};

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
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message || 'Не удалось удалить работу';
  }
}

const getSubObjects = async () => {
  try {
    const headers = getAuthHeaders()
    const response = await fetch('http://localhost:8080/subobjects', {headers})

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
    error.value = err.message || 'Ошибка загрузки данных';
    if (err.message.includes('авторизация')) {
      handleUnauthorized()
    }
  }
}

const formatCurrency = (value) => {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value || 0)
}

const fetchTotalAmountBySubObject = async () => {
  if (!subObjectId.value) return;

  try {
    const headers = getAuthHeaders();
    const response = await fetch(
        `http://localhost:8080/workings/subobject/${subObjectId.value}/total-sum`,
        {headers}
    );

    if (!response.ok) {
      error.value = "Ошибка при получении суммы";
      return;
    }

    totalAmountBySubObject.value = await response.json();
  } catch (err) {
    console.error("Ошибка загрузки суммы:", err);
    totalAmountBySubObject.value = 0;
  }
};

const onChangeSubObject = () => {
  works.value.number = 0
  getWorks()
}

const changePage = (pageNumber) => {
  if (pageNumber >= 0 && pageNumber < works.value.totalPages) {
    works.value.number = pageNumber;
    router.push({query: {...route.query, page: pageNumber}});
    getWorks();
  }
};

onMounted(() => {
  if (route.query.page) {
    works.value.number = parseInt(route.query.page);
  }
  getWorks();
  getSubObjects();
});

watch(subObjectId, async () => {
  await fetchTotalAmountBySubObject();
});
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

/* Пагинация */
.page-link {
  border-radius: 50px !important;
  margin: 0 2px;
  min-width: 36px;
  text-align: center;
}

.page-item.active .page-link {
  background-color: #002d72;
  border-color: #002d72;
}

/* Форма выбора */
.input-group-text {
  border-right: none;
  background-color: white;
}

.form-select {
  border-left: none;
}

.input-group:focus-within {
  box-shadow: 0 0 0 0.25rem rgba(0, 45, 114, 0.25);
  border-radius: 0.375rem;
}

/* Адаптивность */
@media (max-width: 992px) {
  .d-flex.align-items-center {
    flex-direction: column;
    gap: 1rem;
  }

  .position-absolute {
    position: relative !important;
    left: auto !important;
    transform: none !important;
    margin: 1rem 0;
    width: 100% !important;
    text-align: center;
  }

  .flex-grow-1 {
    width: 100%;
    max-width: 100% !important;
  }
}

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