<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
          <h2 class="h4 mb-0 fw-semibold text-primary">СП</h2>
          <a href="/addStandard" class="btn btn-primary rounded-pill px-4">
            <i class="bi bi-plus-lg me-2"></i>Добавить СП
          </a>
        </div>

        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
              <tr>
                <th scope="col" class="ps-4">ID</th>
                <th scope="col">Наименование</th>
                <th scope="col" class="text-end pe-4">Действие</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="standard in standards" :key="standard.id" class="border-top">
                <td class="ps-4 fw-semibold text-muted">{{ standard.id }}</td>
                <td class="fw-medium">{{ standard.name }}</td>
                <td class="text-end pe-4">
                  <a :href="`/editStandard/${standard.id}`" class="btn btn-sm btn-outline-primary rounded-pill px-3 me-2">
                    <i class="bi bi-pencil-square me-1"></i>Изменить
                  </a>
                  <button @click="deleteStandard(standard.id)" class="btn btn-sm btn-outline-danger rounded-pill px-3">
                    <i class="bi bi-trash3 me-1"></i>Удалить
                  </button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
import Navbar from '../../components/Navbar.vue'

export default {
  name: 'ViewStandards',
  components: {
    Navbar
  },
  data() {
    return {
      standards: []
    }
  },
  beforeMount() {
    this.getStandards()
  },
  methods: {
    getStandards() {
      fetch('http://localhost:8080/standards', {
        mode: 'cors',
        headers: {
          'Content-Type': 'application/json',
        }
      })
          .then(res => res.json())
          .then(data => {
            this.standards = data
          })
    },
    deleteStandard(id) {
      fetch(`http://localhost:8080/standards/${id}`, {
        method: 'DELETE'
      })
          .then(data => {
            this.getStandards()
          })
    }
  }
}
</script>

<style scoped>
.card {
  border-radius: 12px;
  overflow: hidden;
}
.table {
  margin-bottom: 0;
}
.table th, .table td {
  padding: 1rem;
}
.table-hover tbody tr:hover {
  background-color: rgba(0, 123, 255, 0.05);
}
</style>