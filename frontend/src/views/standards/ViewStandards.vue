<template>
  <main class="bg-light min-vh-100">
    <Navbar />

    <div class="container py-5">
      <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3">
          <h1 class="text-center mb-0 fw-semibold text-primary">СП</h1>
          <a href="/addStandard" class="btn btn-primary rounded-pill px-4">
            <i class="bi bi-plus-lg me-2"></i>Добавить СП
          </a>
        </div>

        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0 w-100"> <!-- Добавлен w-100 -->
              <thead class="table-dark">
              <tr>
                <th class="ps-4" style="width: 15%">ID</th> <!-- Указана примерная ширина -->
                <th style="width: 55%">Наименование</th> <!-- Указана примерная ширина -->
                <th class="text-end pe-4" style="width: 30%">Действие</th> <!-- Указана примерная ширина -->
              </tr>
              </thead>
              <tbody>
              <tr v-for="(standard, index) in standards" :key="standard.id"
                  :class="{'table-light': index % 2 === 0}" class="border-top">
                <td class="ps-4 fw-semibold text-muted">{{ standard.id }}</td>
                <td class="fw-medium">{{ standard.name }}</td>
                <td class="text-end pe-4">
                  <a :href="`/editStandard/${standard.id}`"
                     class="btn btn-sm btn-outline-primary rounded-pill px-3 me-2">
                    <i class="bi bi-pencil-square me-1"></i>Изменить
                  </a>
                  <button @click="deleteStandard(standard.id)"
                          class="btn btn-sm btn-outline-danger rounded-pill px-3">
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
          .catch(console.error)
    },
    deleteStandard(id) {
      if(confirm('Вы уверены, что хотите удалить этот СП?')) {
        fetch(`http://localhost:8080/standards/${id}`, {
          method: 'DELETE'
        })
            .then(() => this.getStandards())
            .catch(console.error)
      }
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
  font-size: 0.95rem;
  margin-bottom: 0;
  width: 100%; /* Гарантирует растягивание на всю ширину */
}

.table th {
  font-weight: 500;
  vertical-align: middle;
  background-color: #000000;
  color: white;
  white-space: nowrap; /* Запрет переноса текста в шапке */
}

.table td {
  vertical-align: middle;
}

.table-light {
  background-color: #f8f9fa;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 45, 114, 0.05);
}

.btn-outline-primary:hover {
  background-color: #0d6efd;
  color: white;
}

.btn-outline-danger:hover {
  background-color: #dc3545;
  color: white;
}

/* Анимация загрузки */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.table tbody tr {
  animation: fadeIn 0.3s ease forwards;
}

@media (max-width: 768px) {
  .table-responsive {
    font-size: 0.8rem;
  }

  .btn-sm {
    padding: 0.25rem 0.5rem;
    font-size: 0.8rem;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  /* Адаптация колонок для мобильных */
  .table th, .table td {
    padding: 0.5rem;
  }

  .table th:nth-child(1),
  .table td:nth-child(1) {
    width: 20%;
  }

  .table th:nth-child(2),
  .table td:nth-child(2) {
    width: 50%;
  }

  .table th:nth-child(3),
  .table td:nth-child(3) {
    width: 30%;
  }
}
</style>