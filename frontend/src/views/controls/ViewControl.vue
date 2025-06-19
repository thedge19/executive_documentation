<template>
  <main class="bg-light min-vh-100" style="overflow-y: hidden;">
    <Navbar />

    <div class="container-fluid px-4 py-4">
      <div class="card shadow-sm border-0">
        <div class="card-header text-primary py-3 mt-5">
          <h1 class="h3 mb-0 text-center">Входной контроль</h1>
        </div>

        <div class="card-body p-0">
          <!-- Кнопка выгрузки -->
          <div class="d-flex justify-content-start px-4 py-3">
            <button @click="generateLogPdf" class="btn btn-primary">
              <i class="bi bi-file-earmark-pdf me-2"></i>Выгрузить журнал в PDF
            </button>
          </div>

          <!-- Таблица -->
          <div class="table-responsive" style="height: calc(100vh - 220px); overflow-y: auto;">
            <table class="table table-hover align-middle mb-0">
              <thead class="sticky-top bg-dark">
              <tr>
                <th style="width: 5%; background-color: #000000; color: white;">№</th>
                <th style="width: 7%; background-color: #000000; color: white;">Дата</th>
                <th class="text-white fw-normal" style="width: 20%; background-color: #000000; color: white;">Материалы</th>
                <th class="text-white fw-normal" style="width: 30%; background-color: #000000; color: white;">Документы</th>
                <th class="text-white fw-normal" style="width: 10%; background-color: #000000; color: white;">Автор серта</th>
                <th class="text-white fw-normal" style="width: 15%; background-color: #000000; color: white;">ГОСТ, ТУ</th>
                <th class="text-white fw-normal text-center" style="width: 5%; background-color: #000000; color: white;">Кол. стр.</th>
                <th class="text-white fw-normal text-center" style="width: 10%; background-color: #000000; color: white;">Действие</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="control in controls" :key="control.id">
                <td>{{ control.controlNumber }}</td>
                <td>{{ control.date }}</td>
                <td>
                  <a href="#" @click.prevent="generatePdf(control.id)" class="text-decoration-none text-primary">
                    {{ control.materials }}
                    <i class="bi bi-file-earmark-pdf ms-1 text-danger"></i>
                  </a>
                </td>
                <td>{{ control.documents }}</td>
                <td>{{ control.author }}</td>
                <td>{{ control.standard }}</td>
                <td class="text-center">{{ control.controlSheetNumbers }}</td>
                <td class="text-center">
                  <div class="d-flex justify-content-center gap-2">
                    <a class="btn btn-sm btn-outline-primary" :href="`/editControl/${control.id}`">
                      <i class="bi bi-pencil"></i>
                    </a>
                    <button class="btn btn-sm btn-outline-danger" @click="deleteControl(control.id)" disabled>
                      <i class="bi bi-trash"></i>
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
  </main>
</template>

<script>
import Navbar from '../../components/Navbar.vue'

export default {
  name: 'ViewControl',
  components: {
    Navbar
  },
  data() {
    return {
      controls: []
    }
  },
  mounted() {
    this.getControls()
  },
  methods: {
    getControls() {
      fetch('http://localhost:8080/acts/entrance', {
        mode: 'cors',
        headers: {
          'Content-Type': 'application/json',
        }
      })
          .then(res => res.json())
          .then(data => {
            this.controls = data
          })
          .catch(console.error)
    },
    generatePdf(id) {
      window.open(`http://localhost:8080/acts/${id}/pdf/control`, '_blank')
    },
    generateLogPdf() {
      window.open(`http://localhost:8080/acts/pdf/controlLog`, '_blank')
    },
    deleteControl(id) {
      if(confirm('Вы уверены, что хотите удалить эту запись?')) {
        fetch(`http://localhost:8080/acts/entrance/${id}`, {
          method: 'DELETE'
        })
            .then(() => this.getControls())
            .catch(console.error)
      }
    }
  }
}
</script>

<style scoped>
.card {
  border-radius: 10px;
  overflow: hidden;
}

.table {
  font-size: 0.9rem;
}

.table th {
  font-weight: 500;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 45, 114, 0.05);
}

.btn {
  transition: all 0.2s;
}

.btn-outline-primary:hover {
  background-color: #0d6efd;
  color: white;
}

.btn-outline-danger:hover {
  background-color: #dc3545;
  color: white;
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