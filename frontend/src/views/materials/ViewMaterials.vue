<template>
  <main class="bg-light min-vh-100" style="overflow-y: hidden;">
    <Navbar />

    <div class="container py-4 mx-5" style="max-width: 96%;">
      <div class="card shadow-sm border-0">
        <div class="card-header text-primary py-3 mt-3">
          <h1 class="mb-0 text-center">Учет материалов</h1>
        </div>

        <div class="card-body">
          <!-- Кнопка добавления -->
          <div class="d-flex justify-content-start mb-4">
            <a href="/addMaterial" class="btn btn-primary">
              <i class="bi bi-plus-circle me-2"></i>Добавить материал
            </a>
          </div>

          <!-- Таблица с увеличенной шириной -->
          <div class="table-responsive" style="width: 110%;">
            <table class="table table-hover align-middle text-center" style="width: 100%;">
              <!-- Остальной код таблицы без изменений -->
              <thead class="table-dark">
              <tr>
                <th scope="col" class="text-nowrap" style="width: 20%;">Наименование</th>
                <th scope="col" class="text-nowrap" style="width: 8%;">Ед. изм.</th>
                <th scope="col" class="text-nowrap" style="width: 20%;">Документы</th>
                <th scope="col" class="text-nowrap" style="width: 15%;">Автор</th>
                <th scope="col" class="text-nowrap" style="width: 10%;">ГОСТ, ТУ</th>
                <th scope="col" class="text-nowrap text-start" style="width: 15%;">Действие</th>
              </tr>
              </thead>
              <tbody>
              <tr v-if="materials.content && materials.content.length > 0"
                  v-for="material in materials.content"
                  :key="material.id">
                <td>{{ material.name }}</td>
                <td>{{ material.units }}</td>
                <td>
                  <span v-if="!material.certificateUrl">{{ material.documents }}</span>
                  <a v-else
                     :href="material.certificateUrl"
                     target="_blank"
                     class="text-decoration-none text-primary"
                     :title="material.documents">
                    {{ material.documents || 'Скачать сертификат' }}
                    <i class="bi bi-file-earmark-pdf ms-1 text-danger"></i>
                  </a>
                </td>
                <td>{{ material.author }}</td>
                <td>{{ material.standard }}</td>
                <td>
                  <div class="d-flex justify-content-start gap-2">
                    <a class="btn btn-sm btn-outline-primary" :href="`/editMaterial/${material.id}`">
                      <i class="bi bi-pencil"></i>
                    </a>
                    <button class="btn btn-sm btn-outline-danger" @click="deleteMaterial(material.id)">
                      <i class="bi bi-trash"></i>
                    </button>
                    <button v-if="material.certificateUrl"
                            class="btn btn-sm btn-outline-secondary"
                            @click="deleteCertificate(material.id)">
                      <i class="bi bi-file-earmark-minus text-danger"></i>
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

          <!-- Пагинация -->
          <div class="d-flex flex-column align-items-center mt-4">
            <nav aria-label="Page navigation">
              <ul class="pagination pagination-sm">
                <li class="page-item" :class="{ disabled: materials.first }">
                  <button class="page-link" @click="changePage(0)">
                    <i class="bi bi-chevron-double-left"></i>
                  </button>
                </li>
                <li class="page-item" :class="{ disabled: materials.first }">
                  <button class="page-link" @click="changePage(materials.number - 1)">
                    <i class="bi bi-chevron-left"></i>
                  </button>
                </li>

                <li class="page-item" v-for="page in pageNumbers" :key="page"
                    :class="{ active: materials.number === page }">
                  <button class="page-link" @click="changePage(page)">{{ page + 1 }}</button>
                </li>

                <li class="page-item" :class="{ disabled: materials.last }">
                  <button class="page-link" @click="changePage(materials.number + 1)">
                    <i class="bi bi-chevron-right"></i>
                  </button>
                </li>
                <li class="page-item" :class="{ disabled: materials.last }">
                  <button class="page-link" @click="changePage(materials.totalPages - 1)">
                    <i class="bi bi-chevron-double-right"></i>
                  </button>
                </li>
              </ul>
            </nav>

            <div v-if="materials.totalElements > 0" class="text-muted small mt-2">
              Показано {{ materials.numberOfElements }} из {{ materials.totalElements }} материалов
              (Страница {{ materials.number + 1 }} из {{ materials.totalPages }})
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
import Navbar from '../../components/Navbar.vue'

export default {
  name: 'ViewMaterials',
  components: {
    Navbar
  },
  data() {
    return {
      isLoading: false,
      materials: {
        content: [],
        number: 0,
        size: 15, // Изменено на 15 элементов
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true,
        numberOfElements: 0
      },
      error: null,
      pageSize: 15 // Изменено на 15 элементов
    }
  },
  computed: {
    pageNumbers() {
      const current = this.materials.number;
      const total = this.materials.totalPages;
      const range = 2;

      let start = Math.max(0, current - range);
      let end = Math.min(total - 1, current + range);

      if (current - range < 0) {
        end = Math.min(total - 1, end + (range - current));
      }

      if (current + range >= total) {
        start = Math.max(0, start - (current + range - total + 1));
      }

      const pages = [];
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
      return pages;
    }
  },
  mounted() {
    this.getMaterials()
  },
  methods: {
    async getMaterials() {
      this.isLoading = true
      this.error = null
      try {
        const response = await fetch(`http://localhost:8080/materials?page=${this.materials.number}&size=${this.pageSize}&sort=name`)
        if (!response.ok) throw new Error('Ошибка загрузки материалов')
        this.materials = await response.json()
      } catch (err) {
        console.error('Ошибка:', err)
        this.error = 'Не удалось загрузить материалы'
      } finally {
        this.isLoading = false
      }
    },
    changePage(pageNumber) {
      if (pageNumber >= 0 && pageNumber < this.materials.totalPages) {
        this.materials.number = pageNumber
        this.getMaterials()
      }
    },
    deleteMaterial(id) {
      if (confirm('Вы уверены, что хотите удалить этот материал?')) {
        fetch(`http://localhost:8080/materials/${id}`, {
          method: 'DELETE'
        })
            .then(response => {
              if (!response.ok) throw new Error('Ошибка удаления материала')
              this.getMaterials()
              alert('Материал успешно удален')
            })
            .catch(err => {
              console.error('Ошибка:', err)
              alert('Не удалось удалить материал')
            })
      }
    },
    deleteCertificate(id) {
      if (confirm('Вы уверены, что хотите удалить сертификат?')) {
        fetch(`http://localhost:8080/materials/certificate/${id}`, {
          method: 'DELETE'
        })
            .then(() => this.getMaterials())
            .catch(console.error)
      }
    }
  }
}
</script>

<style scoped>
body {
  overflow-y: hidden;
}

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

.btn-outline-primary:hover, .btn-outline-danger:hover, .btn-outline-secondary:hover {
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