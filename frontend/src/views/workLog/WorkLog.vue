<template>
  <main style="background-color: #f8f9fa; min-height: 100vh;">
    <Navbar/>
    <div class="container py-4">
      <div class="row justify-content-center">
        <div class="col-12 mt-5">
          <h1 class="text-center mb-4 text-primary">Общий журнал работ. Раздел 3</h1>

          <!-- Action buttons -->
          <div class="d-flex justify-content-start mb-4">
            <button @click="fillInTheLog" class="btn btn-primary mx-2 shadow-sm rounded-pill" :disabled="isLoading">
              <i class="bi bi-file-earmark-plus me-2"></i>Сформировать ОЖР
            </button>
            <button @click.prevent="generatePdf" class="btn btn-success mx-2 shadow-sm rounded-pill" :disabled="isLoading">
              <i class="bi bi-file-earmark-pdf me-2"></i>Выгрузить в PDF
            </button>
          </div>

          <!-- Error message -->
          <div v-if="error" class="alert alert-danger mb-4">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ error }}
          </div>

          <!-- Table -->
          <div class="card shadow-sm border-0">
            <div class="card-body p-0">
              <div class="table-responsive" style="max-height: 75vh;">
                <table class="table table-hover mb-0">
                  <thead class="sticky-top" style="background-color: #002d72;">
                  <tr>
                    <th class="text-center text-white fw-normal" style="width: 6%; background-color: #000000;">№</th>
                    <th class="text-center text-white fw-normal" style="width: 7%; background-color: #000000;">Дата</th>
                    <th class="text-center text-white fw-normal" style="width: 50%; background-color: #000000;">
                      Наименование работ
                    </th>
                    <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">
                      Ответственный
                    </th>
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

<script setup>
import {onMounted, ref} from 'vue'
import Navbar from '../../components/Navbar.vue'

const workLogs = ref([])
const isLoading = ref(false)
const error = ref(null)

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

const getLogs = async () => {
  try {
    isLoading.value = true
    error.value = null

    const response = await fetch('http://localhost:8080/worklog', {
      headers: getAuthHeaders()
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      error.value = 'Ошибка загрузки журнала работ';
      return;
    }

    workLogs.value = await response.json()
  } catch (err) {
    console.error("Ошибка при загрузке данных:", err)
    error.value = err.message
  } finally {
    isLoading.value = false
  }
}

const fillInTheLog = async () => {
  try {
    isLoading.value = true;
    error.value = null;

    // 1. Проверяем токен перед запросом
    const token = localStorage.getItem('token');
    if (!token) {
      handleUnauthorized();
      return;
    }

    // 2. Добавляем обработку credentials для CORS
    const response = await fetch('http://localhost:8080/worklog/fill3', {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      credentials: 'include' // Важно для передачи кук и авторизации
    });

    // 3. Улучшенная обработка 401 ошибки
    if (response.status === 401 || response.status === 403) {
      handleUnauthorized();
      error.value = 'Сессия истекла. Требуется повторная авторизация';
      return;
    }

    // 4. Проверяем успешность запроса
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      error.value = errorData.message || 'Ошибка формирования журнала';
      return;
    }

    // 5. Обновляем данные
    await getLogs();

  } catch (err) {
    console.error("Ошибка:", err);
    error.value = err.message;

    // Не перенаправляем если это не ошибка авторизации
    if (!err.message.includes('Сессия истекла')) {
      error.value = 'Ошибка при формировании журнала: ' + err.message;
    }
  } finally {
    isLoading.value = false;
  }
}

const generatePdf = async () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      handleUnauthorized();
      return;
    }

    // Открываем новое окно заранее, чтобы блокировщики не мешали
    const pdfWindow = window.open('', '_blank');

    // Делаем запрос с заголовками авторизации
    const response = await fetch(`http://localhost:8080/worklog/3/pdf`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.status === 401) {
      handleUnauthorized();
      pdfWindow.close();
      return;
    }

    if (!response.ok) {
      error.value = 'Ошибка сервера';
      return;
    }

    // Получаем PDF как blob
    const blob = await response.blob();
    // Отображаем PDF в новом окне
    pdfWindow.location.href = URL.createObjectURL(blob);

  } catch (err) {
    console.error('Ошибка при генерации PDF:', err);
    error.value = 'Не удалось сформировать PDF';
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ru-RU')
}

onMounted(() => {
  getLogs()
})
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
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.table tbody tr {
  animation: fadeIn 0.3s ease forwards;
}

/* Loading state */
.btn:disabled {
  opacity: 0.7;
}
</style>