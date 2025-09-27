<template>
  <Navbar/>
  <div class="container py-4">
    <div class="row justify-content-center">
      <div class="col-12">
        <!-- Кнопки действий -->
        <div class="row justify-content-center mt-5">
          <div class="col-12">
            <!-- Таблица -->
            <div class="card shadow-sm border-0">
              <div class="card-body p-0">
                <div class="table-responsive" style="max-height: 90vh;">
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

  <!-- Floating action button -->
  <div class="floating-buttons">
    <!-- Add material button -->
    <a
        href="/addMaterial"
        class="btn btn-success floating-btn add-material-btn"
    >
      <i class="bi bi-plus-lg"></i>
      <span class="floating-btn-text">Добавить материал</span>
    </a>
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

/* Floating buttons styles */
.floating-buttons {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: flex-end;
}

.floating-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: visible;
  font-size: 1.2rem;
  border: none;
  z-index: 1001;
  opacity: 1;
  cursor: pointer;
  text-decoration: none;
}

.floating-btn:hover {
  transform: translateY(-4px) scale(1.08);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
  z-index: 1002;
  opacity: 1;
  text-decoration: none;
}

.floating-btn:active {
  transform: translateY(2px) scale(0.95);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.4);
  transition: all 0.1s ease;
}

/* Эффект волны при нажатии */
.floating-btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255,255,255,0.4) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%) scale(0);
  opacity: 0;
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.floating-btn:active::after {
  transform: translate(-50%, -50%) scale(2);
  opacity: 1;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

/* Эффект свечения при нажатии */
.floating-btn:active {
  filter: brightness(1.3);
}

.floating-btn-text {
  position: absolute;
  right: 100%;
  margin-right: 15px;
  background: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 0.85rem;
  white-space: nowrap;
  opacity: 0;
  transform: translateX(10px);
  transition: all 0.3s ease;
  pointer-events: none;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  z-index: 1003;
}

.floating-btn:hover .floating-btn-text {
  opacity: 1;
  transform: translateX(0);
}

.floating-btn-text::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 100%;
  margin-top: -5px;
  border-width: 5px;
  border-style: solid;
  border-color: transparent transparent transparent rgba(0, 0, 0, 0.9);
}

/* Add material button */
.add-material-btn {
  animation: floatUp 0.5s ease-out 0.2s both;
  z-index: 1001;
  background: linear-gradient(135deg, #28a745 0%, #218838 100%) !important;
  border: none !important;
  color: white !important;
}

.add-material-btn:active {
  background: linear-gradient(135deg, #218838 0%, #1e7e34 100%) !important;
  box-shadow: 0 2px 15px rgba(40, 167, 69, 0.6) !important;
}

@keyframes floatUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.8);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Анимация появления подсказки */
.floating-btn:hover .floating-btn-text {
  animation: tooltipFadeIn 0.3s ease-out;
}

@keyframes tooltipFadeIn {
  from {
    opacity: 0;
    transform: translateX(10px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

/* Адаптивность */
@media (max-width: 768px) {
  .floating-buttons {
    bottom: 20px;
    right: 20px;
  }

  .floating-btn {
    width: 55px;
    height: 55px;
    font-size: 1.1rem;
  }

  .floating-btn-text {
    font-size: 0.8rem;
    padding: 8px 12px;
    white-space: normal;
    width: 140px;
    text-align: center;
  }
}

/* Убедимся, что кнопки поверх всего контента */
.floating-buttons * {
  z-index: inherit;
}
</style>