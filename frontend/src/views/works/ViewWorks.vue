<template>
  <main>
    <Navbar/>

    <!-- Table-->
    <div class="container">
      <div class="row">
        <div class="col-md-24">
          <h1 class="text-center mt-5">Работы</h1>
          <!--Add button -->
          <div class="d-flex justify-content-between h3">
            <div class="my-3">
              <a :href="`/addWork/${subObjectId}`" class="btn btn-primary">Добавить работу</a>
            </div>
<!--            <div class="my-3">-->
<!--              <button class="btn btn-primary" @click.prevent="getSomething">Жми</button>-->
<!--            </div>-->
            <div class="input-group mb-3 mt-3" style="width: 50%">
              <label class="input-group-text" for="inputGroupSelect01">Подобъекты</label>
              <select class="form-select" id="inputGroupSelect01"
                      v-model="subObjectId" @change="onChangeSubObject()">
                <option selected>Choose...</option>
                <option style="width: min-content"
                        v-for="subObject in subObjects" :value="subObject.id">{{ subObject.name }}
                </option>
              </select>
            </div>
          </div>
          <table class="table table-striped" style="width:100%">
            <thead>
            <tr>
              <th scope="col">Id</th>
              <th scope="col">Наименование</th>
              <th scope="col">Ед. изм.</th>
              <th scope="col">Количество</th>
              <th scope="col">Выполнено</th>
              <th scope="col">Осталось</th>
              <!--              <th scope="col">Подобъект</th>-->
              <th scope="col" style="width:15%">Действие</th>
            </tr>
            </thead>
            <tbody>
            <tr v-if="works.content && works.content.length > 0" v-for="work in works.content" :key="work.id">
              <th scope="row">{{ work.id }}</th>
              <td>{{ work.name }}</td>
              <td>{{ work.units }}</td>
              <td>{{ work.quantity }}</td>
              <td>{{ work.done }}</td>
              <td>{{ work.finalQuantity }}</td>
              <!--              <td>{{ work.subObject.title }}</td>-->
              <td>
                <a class="btn btn-primary" :href="`/editWork/${work.id}`">Edit</a>
                <button class="btn btn-danger mx-2" @click="deleteWork(work.id)">Delete</button>
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
              <li class="page-item" :class="{ disabled: works.first }">
                <button class="page-link" @click="changePage(0)">First</button>
              </li>
              <li class="page-item" :class="{ disabled: works.first }">
                <button class="page-link" @click="changePage(works.number - 1)">Previous</button>
              </li>

              <li class="page-item" v-for="page in pageNumbers" :key="page"
                  :class="{ active: works.number === page }">
                <button class="page-link" @click="changePage(page)">{{ page + 1 }}</button>
              </li>

              <li class="page-item" :class="{ disabled: works.last }">
                <button class="page-link" @click="changePage(works.number + 1)">Next</button>
              </li>
              <li class="page-item" :class="{ disabled: works.last }">
                <button class="page-link" @click="changePage(works.totalPages - 1)">Last</button>
              </li>
            </ul>
          </nav>
          <div class="text-center" v-if="works.totalElements > 0">
            <small class="text-muted">
              Показано {{ works.numberOfElements }} из {{ works.totalElements }} работ
              (Страница {{ works.number + 1 }} из {{ works.totalPages }})
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
  name: 'ViewWorks',
  components: {
    Navbar
  },
  data() {
    return {
      isLoading: false,
      works: {
        content: [],
        number: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true
      },
      subObjects: [],
      subObjectId: this.$route.params.id,
      pageSize: 10
    }
  },

  computed: {
    pageNumbers() {
      const current = this.works.number;
      const total = this.works.totalPages;
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
    this.getWorks()
    this.getSubObjects()
  },

  methods: {
    getWorks() {
      this.isLoading = true;

      fetch(`http://localhost:8080/workings/${this.subObjectId}?page=${this.works.number}&size=${this.pageSize}`)
          .then(res => res.json())
          .then(data => {
            this.works = data;
            console.log("Works data loaded:", {
              size: this.works.size,
              contentLength: this.works.content.length
            });
          })
          .catch(error => {
            console.error("Fetch error:", error);
          })
          .finally(() => {
            this.isLoading = false;
          });
    },

    deleteWork(id) {
      if (confirm('Вы действительно хотите удалить эту работу?')) {
        fetch(`http://localhost:8080/workings/${id}`, {
          method: 'DELETE'
        })
            .then(response => {
              if (!response.ok) {
                throw new Error('Ошибка при удалении');
              }
              return response;
            })
            .then(data => {
              console.log(data);
              this.getWorks();
              // Можно добавить уведомление об успешном удалении
              alert('Работа успешно удалена');
            })
            .catch(error => {
              console.error('Ошибка:', error);
              alert('Не удалось удалить работу');
            });
      }
    },

    getSubObjects() {
      fetch(`http://localhost:8080/subobjects`,
      )
          .then(res => res.json())
          .then(data => {
            this.subObjects = data
          })
    },

    onChangeSubObject() {
      console.log(this.subObjectId)
      this.getWorks()
    },

    changePage(pageNumber) {
      if (pageNumber >= 0 && pageNumber < this.works.totalPages) {
        this.works.number = pageNumber;
        this.getWorks();
      }
    },

    getSomething() {
      console.log("Full works object:", this.works);
      console.log("Works size:", this.works.size);
      console.log("Works content:", this.works.content);
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
</style>