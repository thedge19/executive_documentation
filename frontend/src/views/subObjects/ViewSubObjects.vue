<template>
  <Navbar/>
  <div class="container py-3">
    <div class="row justify-content-center mt-5">
      <div class="col-12">
        <!-- Заголовок и кнопки с правильным центрированием -->
        <div class="d-flex align-items-center mb-4 position-relative">
          <!-- Кнопки слева -->
          <div class="d-flex">
            <a href="/addSubObject" class="btn btn-info mx-2 shadow-sm rounded-pill">
              <i class="bi bi-plus-lg me-2"></i>Добавить подобъект
            </a>
          </div>

          <!-- Заголовок по центру оставшегося пространства -->
          <h1 class="text-light position-absolute start-50" style="width: max-content;">
            Подобъекты
          </h1>

          <!-- Переключатели проектов справа -->
          <div class="btn-group ms-auto" role="group">
            <input type="radio" class="btn-check" id="project4" @change="onChangeProject"
                   name="project" v-model="projectId" :value="4" autocomplete="off">
            <label class="btn btn-outline-secondary rounded-pill mx-1" for="project4">
              <i class="bi bi-tree me-1"></i>Грушовая
            </label>

            <input type="radio" class="btn-check" id="project5" @change="onChangeProject"
                   name="project" v-model="projectId" :value="5" autocomplete="off">
            <label class="btn btn-outline-secondary rounded-pill mx-1" for="project5">
              <i class="bi bi-building me-1"></i>Шесхарис
            </label>
          </div>
        </div>

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
            <div class="table-responsive" style="max-height: 85vh;">
              <table class="table table-hover mb-0">
                <thead class="sticky-top" style="background-color: #002d72;">
                <tr>
                  <th class="text-center text-white fw-normal" style="width: 7%; background-color: #000000;">ID</th>
                  <th class="text-center text-white fw-normal" style="width: 40%; background-color: #000000;">
                    Наименование
                  </th>
                  <th class="text-center text-white fw-normal" style="width: 12%; background-color: #000000;">
                    Обозначение
                  </th>
                  <th class="text-center text-white fw-normal" style="width: 20%; background-color: #000000;">
                    Объект
                  </th>
                  <th class="text-center text-white fw-normal" style="width: 21%; background-color: #000000;">
                    Действия
                  </th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(subObject, index) in subObjects" :key="subObject.id"
                    :class="{'table-light': index % 2 === 0}">
                  <td class="text-center align-middle fw-semibold text-muted">{{ subObject.id }}</td>
                  <td class="align-middle">
                    <a :href="`/works/${subObject.id}`" class="text-decoration-none text-primary">
                      {{ subObject.name }}
                    </a>
                  </td>
                  <td class="text-center align-middle">{{ subObject.title }}</td>
                  <td class="text-center align-middle">{{ subObject.project?.name }}</td>
                  <td class="text-center align-middle">
                    <div class="d-flex justify-content-center">
                      <a :href="`/editSubObject/${subObject.id}`"
                         class="btn btn-sm btn-outline-primary rounded-pill px-3 me-2">
                        <i class="bi bi-pencil-square me-1"></i>Изменить
                      </a>
                      <button @click="deleteSubObject(subObject.id)"
                              class="btn btn-sm btn-outline-danger rounded-pill px-3">
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
    </div>
  </div>
</template>

<script setup>
import {ref, onBeforeMount} from 'vue'
import {useRouter} from 'vue-router'
import Navbar from '../../components/Navbar.vue'

const router = useRouter()
const subObjects = ref([])
const projectId = ref(4) // Значение по умолчанию
const error = ref(null)
const isLoading = ref(false)

const getSubObjects = async () => {
  try {
    isLoading.value = true;
    error.value = null;
    const token = localStorage.getItem('token');

    if (!token) {
      await router.push('/login');
      return;
    }

    const response = await fetch(`http://localhost:8080/subobjects/${projectId.value}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      },
      credentials: 'include'
    });

    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem('token');
        await router.push('/login');
      }
      error.value = `Ошибка загрузки данных! Статус: ${response.status}`;
      return;
    }

    const rawResponse = await response.text();
    try {
      subObjects.value = JSON.parse(rawResponse);
    } catch (parseError) {
      console.error("Ошибка парсинга JSON:", parseError);
      error.value = "Сервер вернул некорректные данные";
    }
  } catch (err) {
    error.value = err.message;
    console.error('Ошибка при загрузке подобъектов:', err);
  } finally {
    isLoading.value = false;
  }
};

const deleteSubObject = async (id) => {
  if (!confirm('Вы действительно хотите удалить подобъект?')) return;

  try {
    const token = localStorage.getItem('token');
    if (!token) {
      await router.push('/login');
      return;
    }

    const response = await fetch(`http://localhost:8080/subobjects/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      credentials: 'include'
    });

    if (!response.ok) {
      if (response.status === 401) {
        localStorage.removeItem('token');
        await router.push('/login');
      }
      error.value = `Ошибка удаления! Статус: ${response.status}`;
      return;
    }

    await getSubObjects();
  } catch (err) {
    error.value = err.message;
    console.error('Ошибка при удалении подобъекта:', err);
  }
};

const onChangeProject = () => {
  getSubObjects();
};

onBeforeMount(() => {
  getSubObjects();
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

/* Переключатели проектов */
.btn-group {
  gap: 0.5rem;
}

@media (max-width: 768px) {
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

  .btn-group {
    width: 100%;
    flex-wrap: wrap;
  }

  .btn-group .btn {
    flex: 1 0 auto;
  }
}
</style>