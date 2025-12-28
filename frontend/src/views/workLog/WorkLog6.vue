<template>
  <Navbar/>
  <div class="container py-4">
    <div class="row justify-content-center">
      <div class="col-12">
        <!-- Кнопки действий -->
        <div class="row justify-content-center mt-5">
          <div class="col-12">
            <!-- Заголовок по центру -->
            <div class="d-flex align-items-center position-relative justify-content-center">
              <h1 class="text-light" style="width: max-content;">
                Общий журнал работ. Раздел 6
              </h1>
            </div>

            <!-- Таблица -->
            <div class="card shadow-sm border-0">
              <div class="card-body p-0">
                <div class="table-responsive" style="max-height: 84vh;">
                  <table class="table table-hover mb-0">
                    <thead class="sticky-top" style="background-color: #002d72;">
                    <tr>
                      <th class="text-center text-white fw-normal" style="width: 5%; background-color: #000000;">№ п/п
                      </th>
                      <th class="text-center text-white fw-normal" style="width: 60%; background-color: #000000;">
                        Наименование исполнительной документации
                      </th>
                      <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">Дата
                        подписания акта
                      </th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="(act, index) in acts" :key="act.id"
                        :class="{'table-light': index % 2 === 0}">
                      <td class="text-center align-middle">{{ index + 1 }}</td>
                      <td class="align-middle">Акт освидетельствования скрытых работ № {{ act.actNumber }} {{
                          act.works
                        }}
                      </td>
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
    </div>
  </div>

  <!-- Floating action button -->
  <div class="floating-buttons">
    <!-- Generate PDF button -->
    <button
        class="btn btn-info floating-btn generate-pdf-btn"
        @click="generatePdf"
        :disabled="isLoading"
    >
      <i class="bi bi-file-earmark-pdf"></i>
      <span class="floating-btn-text">Выгрузить в PDF</span>
    </button>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import Navbar from '../../components/Navbar.vue'

const acts = ref([])
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

// Получение актов
const getActs = async () => {
  try {
    const response = await fetch('http://localhost:8080/acts/worklog/6', {
      mode: 'cors',
      headers: getAuthHeaders()
    })
    acts.value = await response.json()
  } catch (error) {
    console.error('Ошибка при загрузке актов:', error)
  }
}

const handleUnauthorized = () => {
  localStorage.removeItem('token')
  window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
}

// Генерация PDF
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
    const response = await fetch(`http://localhost:8080/acts/worklog/6/pdf`, {
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
  letter-spacing: 1px;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 45, 114, 0.05);
}

/* Стили для карточки */
.card {
  border-radius: 8px;
  overflow: hidden;
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
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.table tbody tr {
  animation: fadeIn 0.3s ease forwards;
}

/* Иконки для кнопок */
.bi {
  font-size: 1rem;
}

/* Floating buttons styles */
.floating-buttons {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: flex-end;
}

.floating-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: visible;
  font-size: 1.2rem;
  border: none;
  z-index: 1001;
  opacity: 1;
  cursor: pointer;
  text-decoration: none;
}

.floating-btn:hover {
  transform: translateY(-4px) scale(1.08);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
  z-index: 1002;
  opacity: 1;
}

.floating-btn:active {
  transform: translateY(2px) scale(0.95);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.4);
  transition: all 0.1s ease;
}

/* Эффект волны при нажатии */
.floating-btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255,255,255,0.4) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%) scale(0);
  opacity: 0;
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.floating-btn:active::after {
  transform: translate(-50%, -50%) scale(2);
  opacity: 1;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

/* Эффект свечения при нажатии */
.floating-btn:active {
  filter: brightness(1.3);
}

.floating-btn-text {
  position: absolute;
  right: 100%;
  margin-right: 15px;
  background: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 0.85rem;
  white-space: nowrap;
  opacity: 0;
  transform: translateX(10px);
  transition: all 0.3s ease;
  pointer-events: none;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  z-index: 1003;
}

.floating-btn:hover .floating-btn-text {
  opacity: 1;
  transform: translateX(0);
}

.floating-btn-text::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 100%;
  margin-top: -5px;
  border-width: 5px;
  border-style: solid;
  border-color: transparent transparent transparent rgba(0, 0, 0, 0.9);
}

/* Generate PDF button */
.generate-pdf-btn {
  animation: floatUp 0.5s ease-out 0.2s both;
  z-index: 1001;
  background: linear-gradient(135deg, #17a2b8 0%, #138496 100%) !important;
  border: none !important;
  color: white !important;
}

.generate-pdf-btn:active {
  background: linear-gradient(135deg, #138496 0%, #117a8b 100%) !important;
  box-shadow: 0 2px 15px rgba(23, 162, 184, 0.6) !important;
}

.generate-pdf-btn:disabled {
  background: linear-gradient(135deg, #6c757d 0%, #5a6268 100%) !important;
  opacity: 0.6;
  cursor: not-allowed;
}

.generate-pdf-btn:disabled:hover {
  transform: none;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.25);
}

@keyframes floatUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.8);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Анимация появления подсказки */
.floating-btn:hover .floating-btn-text {
  animation: tooltipFadeIn 0.3s ease-out;
}

@keyframes tooltipFadeIn {
  from {
    opacity: 0;
    transform: translateX(10px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

/* Адаптивность */
@media (max-width: 768px) {
  .floating-buttons {
    bottom: 20px;
    right: 20px;
  }

  .floating-btn {
    width: 55px;
    height: 55px;
    font-size: 1.1rem;
  }

  .floating-btn-text {
    font-size: 0.8rem;
    padding: 8px 12px;
    white-space: normal;
    width: 140px;
    text-align: center;
  }
}

/* Убедимся, что кнопки поверх всего контента */
.floating-buttons * {
  z-index: inherit;
}
</style>