<template>
  <Navbar/>
  <div class="container py-4">
    <div class="row justify-content-center">
      <div class="col-12 mt-5">
        <h1 class="text-center mb-4 text-light">Учет материалов</h1>

        <!-- Кнопка добавления -->
        <div class="d-flex justify-content-start mb-4">
          <a href="/addMaterial" class="btn btn-success mx-2 shadow-sm rounded-pill">
            <i class="bi bi-plus-circle me-2"></i>Добавить материал
          </a>
        </div>

        <!-- Таблица -->
        <div class="card shadow-sm border-0">
          <div class="card-body p-0">
            <div class="table-responsive" style="max-height: 75vh;">
              <table class="table table-hover mb-0">
                <thead class="sticky-top" style="background-color: #002d72;">
                <tr>
                  <th class="text-center text-white fw-normal" style="width: 40%; background-color: #000000;">
                    Наименование
                  </th>
                  <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">Ед. изм.
                  </th>
                  <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">ГОСТ, ТУ
                  </th>
                  <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">Действие
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
  font-size: 0.9rem;
}

.table th {
  font-weight: 500;
  letter-spacing: 0.5px;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 45, 114, 0.05);
}

/* Стили для карточки */
.card {
  border-radius: 8px;
  overflow: hidden;
}

/* Стили для кнопок */
.btn {
  transition: all 0.2s ease;
  border-radius: 6px;
  padding: 8px 16px;
}

.btn-success {
  background-color: #28a745;
  border-color: #28a745;
}

.btn-success:hover {
  background-color: #218838;
  border-color: #218838;
}

.btn-outline-primary {
  color: #002d72;
  border-color: #002d72;
}

.btn-outline-primary:hover {
  background-color: #002d72;
  color: white;
}

.btn-outline-danger:hover {
  color: white;
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

/* Документы */
.document-link {
  transition: all 0.2s;
  display: block;
  padding: 2px 5px;
  border-radius: 4px;
}

.document-link:hover {
  background-color: rgba(13, 110, 253, 0.1);
}

.document-toggle {
  cursor: pointer;
  user-select: none;
}

.document-toggle:hover {
  text-decoration: underline;
}

.document-list {
  border-left: 2px solid #dee2e6;
  padding-left: 10px;
  margin-left: 5px;
}

/* Loading state */
.btn:disabled {
  opacity: 0.7;
}
</style>