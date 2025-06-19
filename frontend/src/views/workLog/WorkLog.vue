<template>
  <main style="background-color: #f8f9fa; min-height: 100vh;">
    <Navbar/>
    <div class="container py-4">
      <div class="row justify-content-center">
        <div class="col-12 mt-5">
          <h1 class="text-center mb-4 text-primary">Общий журнал работ. Раздел 3</h1>

          <!-- Кнопки действий -->
          <div class="d-flex justify-content-start mb-4">
            <button @click="fillInTheLog" class="btn btn-primary mx-2 shadow-sm rounded-pill">
              <i class="bi bi-file-earmark-plus me-2"></i>Сформировать ОЖР
            </button>
            <button @click.prevent="generatePdf" class="btn btn-success mx-2 shadow-sm rounded-pill">
              <i class="bi bi-file-earmark-pdf me-2"></i>Выгрузить в PDF
            </button>
          </div>

          <!-- Таблица -->
          <div class="card shadow-sm border-0">
            <div class="card-body p-0">
              <div class="table-responsive" style="max-height: 75vh;">
                <table class="table table-hover mb-0">
                  <thead class="sticky-top" style="background-color: #002d72;">
                  <tr>
                    <th class="text-center text-white fw-normal" style="width: 6%; background-color: #000000;">№</th>
                    <th class="text-center text-white fw-normal" style="width: 7%; background-color: #000000;">Дата</th>
                    <th class="text-center text-white fw-normal" style="width: 50%; background-color: #000000;">Наименование работ</th>
                    <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">Ответственный</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="(workLog, index) in workLogs" :key="index"
                      :class="{'table-light': index % 2 === 0}">
                    <td class="text-center align-middle">{{ workLog.workLogNumber }}</td>
                    <td class="text-center align-middle">{{ formatDate(workLog.workDate) }}</td>
                    <td class="align-middle">{{ workLog.name }}</td>
                    <td class="text-center align-middle">Руководитель работ Трифонов А.Е.</td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script>
import Navbar from '../../components/Navbar.vue';

export default {
  name: 'WorkLog',
  components: { Navbar },

  data() {
    return {
      workLogs: []
    }
  },

  mounted() {
    this.getLogs()
  },

  methods: {
    getLogs() {
      fetch('http://localhost:8080/worklog', {
        mode: 'cors',
        headers: { 'Content-Type': 'application/json' }
      })
          .then(res => res.json())
          .then(data => {
            this.workLogs = data
          })
          .catch(error => {
            console.error("Ошибка при загрузке данных:", error)
          })
    },

    fillInTheLog() {
      fetch(`http://localhost:8080/worklog/fill3`)
          .then(() => {
            console.log("Запрос отправлен")
            this.getLogs() // Обновляем данные после формирования
          })
    },

    generatePdf() {
      window.open(`http://localhost:8080/worklog/3/pdf`, '_blank')
    },

    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('ru-RU')
    }
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

.btn-primary {
  background-color: #002d72;
  border-color: #002d72;
}

.btn-primary:hover {
  background-color: #001f4d;
  border-color: #001f4d;
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
  from { opacity: 0; }
  to { opacity: 1; }
}

.table tbody tr {
  animation: fadeIn 0.3s ease forwards;
}
</style>