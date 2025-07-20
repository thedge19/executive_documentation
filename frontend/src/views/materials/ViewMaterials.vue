<template>
  <Navbar/>
  <div class="container py-4">
    <div class="row justify-content-center">
      <div class="col-12">
        <!-- Кнопки действий -->
        <div class="row justify-content-center mt-5">
          <div class="col-12">
            <!-- Заголовок и кнопки с правильным центрированием -->
            <div class="d-flex align-items-center mb-4 position-relative">
              <!-- Кнопки слева -->
              <div class="d-flex">
                <a href="/addMaterial" class="btn btn-success mx-2 shadow-sm rounded-pill">
                  <i class="bi bi-plus-circle me-2"></i>Добавить материал
                </a>
              </div>

              <!-- Заголовок по центру оставшегося пространства -->
              <h1 class="text-light position-absolute start-50" style="width: max-content;">
                Материалы
              </h1>
            </div>

            <!-- Таблица -->
            <div class="card shadow-sm border-0">
              <div class="card-body p-0">
                <div class="table-responsive" style="max-height: 85vh;">
                  <table class="table table-hover mb-0">
                    <thead class="sticky-top" style="background-color: #002d72;">
                    <tr>
                      <th class="text-center text-white fw-normal" style="width: 40%; background-color: #000000;">
                        Наименование
                      </th>
                      <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">
                        Ед. изм.
                      </th>
                      <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">
                        ГОСТ, ТУ
                      </th>
                      <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">
                        Действие
                      </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-if="materials && materials.length > 0"
                        v-for="(material, index) in materials"
                        :key="material.id"
                        :class="{'table-light': index % 2 === 0}">
                      <td class="align-middle">
                        <div>{{ material.name }}</div>
                        <div v-if="material.certificates && Object.keys(material.certificates).length > 0"
                             class="mt-2">
                          <a href="#"
                             @click.prevent="toggleDocuments(material.id)"
                             class="small text-primary text-decoration-none document-toggle">
                            <i class="bi"
                               :class="{'bi-chevron-down': !expandedDocuments[material.id],
                           'bi-chevron-up': expandedDocuments[material.id]}"></i>
                            посмотреть документы
                          </a>

                          <div v-if="expandedDocuments[material.id]" class="mt-2 small document-list">
                            <div v-for="(url, name) in material.certificates"
                                 :key="name"
                                 class="mb-1">
                              <a :href="url"
                                 target="_blank"
                                 class="text-decoration-none text-primary document-link">
                                <i class="bi bi-file-earmark-pdf me-1 text-danger"></i>
                                {{ name }}
                              </a>
                            </div>
                          </div>
                        </div>
                      </td>
                      <td class="text-center align-middle">{{ material.units }}</td>
                      <td class="text-center align-middle">{{ material.standard }}</td>
                      <td class="text-center align-middle">
                        <div class="d-flex justify-content-center gap-2">
                          <a class="btn btn-sm btn-outline-primary" :href="`/editMaterial/${material.id}`">
                            <i class="bi bi-pencil"></i>
                          </a>
                          <button class="btn btn-sm btn-outline-danger" @click="deleteMaterial(material.id)">
                            <i class="bi bi-trash"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                    <tr v-else>
                      <td colspan="4" class="text-center py-4 text-muted">
                        <i class="bi bi-exclamation-circle fs-4 d-block mb-2"></i>
                        Нет данных для отображения
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
    </div>
  </div>
</template>

<script>
import {ref, onMounted} from 'vue';
import Navbar from '../../components/Navbar.vue';

export default {
  name: 'ViewMaterials',
  components: {
    Navbar
  },
  setup() {
    const expandedDocuments = ref({});
    const isLoading = ref(false);
    const error = ref(null);
    const materials = ref([]);

    const toggleDocuments = (materialId) => {
      expandedDocuments.value = {
        ...expandedDocuments.value,
        [materialId]: !expandedDocuments.value[materialId]
      };
    };

    const getAuthHeaders = () => {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('Требуется авторизация');
      }
      return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      };
    };

    const handleUnauthorized = () => {
      localStorage.removeItem('token');
      window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
    };

    const getMaterials = async () => {
      isLoading.value = true;
      error.value = null;
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          handleUnauthorized();
          return;
        }

        const response = await fetch(
            `http://localhost:8080/materials`,
            {
              headers: getAuthHeaders()
            }
        );

        if (response.status === 401) {
          handleUnauthorized();
          return;
        }

        if (!response.ok) {
          error.value = 'Ошибка загрузки материалов';
          isLoading.value = false;
          return;
        }

        // Получаем сразу массив материалов (без обертки в Page)
        materials.value = await response.json() || [];

      } catch (err) {
        console.error('Ошибка:', err);
        error.value = 'Не удалось загрузить материалы';
        if (err.message.includes('авторизация')) {
          handleUnauthorized();
        }
      } finally {
        isLoading.value = false;
      }
    };

    const deleteMaterial = async (id) => {
      if (!confirm('Вы уверены, что хотите удалить этот материал?')) return;

      try {
        const response = await fetch(`http://localhost:8080/materials/${id}`, {
          method: 'DELETE',
          headers: getAuthHeaders()
        });

        if (response.status === 401) {
          handleUnauthorized();
          return;
        }

        if (!response.ok) {
          error.value = 'Ошибка удаления материалов';
          isLoading.value = false;
          return;
        }

        await getMaterials();
        alert('Материал успешно удален');
      } catch (err) {
        console.error('Ошибка:', err);
        alert('Не удалось удалить материал');
      }
    };

    onMounted(() => {
      getMaterials();
    });

    return {
      isLoading,
      materials,
      error,
      deleteMaterial,
      expandedDocuments,
      toggleDocuments,
    };
  }
}
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
.btn-success {
  background: linear-gradient(135deg, #28a745 0%, #218838 100%);
}

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
</style>