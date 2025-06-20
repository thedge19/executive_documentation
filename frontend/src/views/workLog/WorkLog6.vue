<template>
  <main style="background-color: #f8f9fa; min-height: 100vh;">
    <Navbar/>
    <div class="container py-4">
      <div class="row justify-content-center">
        <div class="col-12 mt-5">
          <h1 class="text-center mb-4 text-primary">Общий журнал работ. Раздел 6</h1>

          <!-- Кнопки действий -->
          <div class="d-flex justify-content-start mb-4">
            <button @click="fillInTheLog" class="btn btn-primary mx-2 shadow-sm rounded-pill">
              <i class="bi bi-file-earmark-plus me-2"></i>Сформировать 6 раздел
            </button>
            <button @click="generatePdf" class="btn btn-success mx-2 shadow-sm rounded-pill">
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
                    <th class="text-center text-white fw-normal" style="width: 5%; background-color: #000000;">№ п/п</th>
                    <th class="text-center text-white fw-normal" style="width: 60%; background-color: #000000;">Наименование исполнительной документации</th>
                    <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">Дата подписания акта</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="(act, index) in acts" :key="act.id"
                      :class="{'table-light': index % 2 === 0}">
                    <td class="text-center align-middle">{{ index + 1 }}</td>
                    <td class="align-middle">Акт освидетельствования скрытых работ № {{ act.actNumber }} {{ act.works }}</td>
                    <td class="text-center align-middle">{{ act.endDate }}</td>
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

<script setup>
import { ref, onMounted } from 'vue'
import Navbar from '../../components/Navbar.vue'

const acts = ref([])

const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    throw new Error('Требуется авторизация')
  }
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  }
}

const handleUnauthorized = () => {
  localStorage.removeItem('token')
  window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
}

// Получение актов
const getActs = async () => {
  try {
    const response = await fetch('http://localhost:8080/worklog/6', {
      mode: 'cors',
      headers: getAuthHeaders()
    })
    acts.value = await response.json()
  } catch (error) {
    console.error('Ошибка при загрузке актов:', error)
  }
}


// Формирование раздела
const fillInTheLog = async () => {
  try {
    await fetch('http://localhost:8080/worklog/fill')
    console.log("Запрос отправлен")
    await getActs() // Обновляем список после формирования
  } catch (error) {
    console.error('Ошибка при формировании раздела:', error)
  }
}

// Генерация PDF
const generatePdf = () => {
  window.open('http://localhost:8080/worklog/6/pdf', '_blank')
}

// Загружаем акты при монтировании компонента
onMounted(getActs)
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
  font-weight: 500;
}

.btn-primary {
  background-color: #002d72;
  border-color: #002d72;
}

.btn-primary:hover {
  background-color: #001f4d;
  border-color: #001f4d;
}

.btn-success {
  background-color: #28a745;
  border-color: #28a745;
}

.btn-success:hover {
  background-color: #218838;
  border-color: #1e7e34;
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
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.table tbody tr {
  animation: fadeIn 0.3s ease forwards;
}

/* Иконки для кнопок */
.bi {
  font-size: 1rem;
}
</style>