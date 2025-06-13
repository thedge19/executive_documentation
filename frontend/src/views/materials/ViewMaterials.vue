<template>
  <main>
    <Navbar/>

    <!-- Table-->
    <div class="container w-75">
      <div class="row mx-5 w-75" style="position: absolute; top: 0; bottom: 0; left: 0; right: 0;">
        <div class="col-md-24">
          <h1 class="text-center mt-5">Материалы</h1>
          <!--Add button -->
          <div class="my-3">
            <a href="/addMaterial" class="btn btn-primary">Добавить материал</a>
          </div>

          <table class="table table-striped text-center" style="width:100%">
            <thead>
            <tr>
              <th scope="col">Наименование</th>
              <th scope="col">Ед. изм.</th>
              <th scope="col">Документы</th>
              <th scope="col">Автор</th>
              <th scope="col">ГОСТ, ТУ</th>
              <th scope="col">Кол. стр.</th>
              <th scope="col" class="text-center" style="width:20%">Действие</th>
            </tr>
            </thead>
            <tbody>
            <tr v-if="materials.content && materials.content.length > 0" v-for="material in materials.content"
                :key="material.id">
              <td>{{ material.name }}</td>
              <td>{{ material.units }}</td>
              <td>
                <span v-if="!material.certificateUrl">{{ material.documents }}</span>
                <a v-else
                   :href="material.certificateUrl"
                   target="_blank"
                   class="text-decoration-none"
                   :title="material.documents">
                  {{ material.documents || 'Скачать сертификат' }}
                  <i class="bi bi-file-earmark-pdf ms-1 text-danger"></i>
                </a>
              </td>
              <td>{{ material.author }}</td>
              <td>{{ material.standard }}</td>
              <td>{{ material.numberOfPages }}</td>
              <td class="text-center">
                <a class="btn btn-primary" :href="`/editMaterial/${material.id}`">Edit</a>
                <button class="btn btn-danger mx-2" @click="deleteMaterial(material.id)">Delete</button>
                <button v-if="material.certificateUrl" class="btn btn-outline-danger mx-2" @click="deleteCertificate(material.id)">Удалить сертификат</button>
              </td>
            </tr>
            <tr v-else>
              <td colspan="7" class="text-center">Нет данных для отображения</td>
            </tr>
            </tbody>
          </table>

          <!-- Пагинация -->
          <nav aria-label="Page navigation">
            <ul class="pagination justify-content-center">
              <li class="page-item" :class="{ disabled: materials.first }">
                <button class="page-link" @click="changePage(0)">First</button>
              </li>
              <li class="page-item" :class="{ disabled: materials.first }">
                <button class="page-link" @click="changePage(materials.number - 1)">Previous</button>
              </li>

              <li class="page-item" v-for="page in pageNumbers" :key="page"
                  :class="{ active: materials.number === page }">
                <button class="page-link" @click="changePage(page)">{{ page + 1 }}</button>
              </li>

              <li class="page-item" :class="{ disabled: materials.last }">
                <button class="page-link" @click="changePage(materials.number + 1)">Next</button>
              </li>
              <li class="page-item" :class="{ disabled: materials.last }">
                <button class="page-link" @click="changePage(materials.totalPages - 1)">Last</button>
              </li>
            </ul>
          </nav>
          <div class="text-center" v-if="materials.totalElements > 0">
            <small class="text-muted">
              Показано {{ materials.numberOfElements }} из {{ materials.totalElements }} материалов
              (Страница {{ materials.number + 1 }} из {{ materials.totalPages }})
            </small>
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
        size: 10,
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true,
        numberOfElements: 0
      },
      error: null,
      pageSize: 10
    }
  },

  computed: {
    pageNumbers() {
      const current = this.materials.number;
      const total = this.materials.totalPages;
      const range = 2; // Количество отображаемых страниц вокруг текущей

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
        const response = await fetch(`http://localhost:8080/materials?page=${this.materials.number}&size=${this.pageSize}&sort=name`, {
          headers: {
            'Accept': 'application/json'
          }
        })
        if (!response.ok) {
          throw new Error('Ошибка загрузки материалов')
        }
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
        this.materials.number = pageNumber;
        this.getMaterials();
      }
    },

    deleteMaterial(id) {
      if (confirm('Вы уверены, что хотите удалить этот материал?')) {
        fetch(`http://localhost:8080/materials/${id}`, {
          method: 'DELETE'
        })
            .then(response => {
              if (response.ok) {
                this.getMaterials()
                alert('Материал успешно удален')
              } else {
                throw new Error('Ошибка удаления материала')
              }
            })
            .catch(err => {
              console.error('Ошибка:', err)
              this.error = 'Не удалось удалить материал'
              alert('Не удалось удалить материал')
            })
      }
    },

    deleteCertificate(id) {
      fetch(`http://localhost:8080/materials/certificate/${id}`, {
        method: 'DELETE'
      })
          .then(data => {
            console.log(data)
            this.getMaterials()
          })
    }
  }
}
</script>

<style scoped>
.pagination {
  margin-top: 20px;
}

.page-item.active .page-link {
  background-color: #0d6efd;
  border-color: #0d6efd;
}

.page-link {
  cursor: pointer;
}

.table {
  margin-top: 20px;
}

.text-center {
  margin-bottom: 20px;
}
</style>