<template>
  <Navbar/>
  <div class="container-fluid px-4 py-1">
    <div class="card shadow-sm border-0">
      <div class="card-body p-0">
        <!-- Table with black header -->
        <div class="table-responsive mt-5" style="max-height: 94vh;">
          <table class="table table-hover align-middle mb-0">
            <thead class="sticky-top">
            <tr>
              <th class="text-white text-center" style="width: 7%; background-color: #000000">№</th>
              <th class="text-white text-center" style="width: 5%; background-color: #000000">Дата</th>
              <th class="text-white text-center" style="width: 15%; background-color: #000000">Объект</th>
              <th class="text-white text-center" style="width: 20%; background-color: #000000">Выполненные работы</th>
              <th class="text-white text-center" style="width: 7%; background-color: #000000">Начало</th>
              <th class="text-white text-center" style="width: 25%; background-color: #000000">Материалы</th>
              <th class="text-white text-center" style="width: 20%; background-color: #000000">Предъявлены документы
              </th>
              <th class="text-white text-center" style="width: 25%; background-color: #000000">Выполнено в соответствии
                с
              </th>
              <th class="text-white text-center" style="width: 20%; background-color: #000000">Разрешается выполнение
              </th>
              <th class="text-white text-center" style="width: 12%; background-color: #000000">Действие</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="act in acts" :key="act.id">
              <td class="text-center">{{ act.actNumber }}</td>
              <td class="text-center" :style="[act.executiveSchemaId != null ? `color:blue` : `color:red`]">
                {{ act.endDate }}
              </td>
              <td>{{ act.projectName }}</td>
              <td>
                <a href="#" @click.prevent="generatePdf(act.id)" class="text-decoration-none">
                  {{ act.works }}
                  <i class="bi bi-file-earmark-pdf ms-1 text-danger"></i>
                </a>
              </td>
              <td>{{ act.startDate }}</td>
              <td class="text-center">{{ act.materials }}</td>
              <td>{{ act.submittedDocuments }}</td>
              <td>{{ act.inAccordWith }}</td>
              <td>{{ act.nextWorks }}</td>
              <td class="text-center">
                <div class="d-flex justify-content-center">
                  <a class="btn btn-primary btn-sm" :href="`/editAct/${act.id}`">Edit</a>
                  <button class="btn btn-danger btn-sm mx-1" @click="deleteAct(act.id)">Delete</button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <div
      v-if="isDatePanelOpen"
      class="floating-date-overlay"
      @click="toggleDatePanel"
  ></div>
  <!-- Floating date pickers -->
  <div class="floating-date-pickers" :class="{ 'floating-date-pickers--open': isDatePanelOpen }">
    <div class="floating-date-item">
      <label class="floating-date-label">
        <i class="bi bi-calendar-plus me-2"></i>Начало периода
      </label>
      <VDatePicker v-model="startDate" mode="date" class="floating-date-picker" />
    </div>

    <div class="floating-date-item">
      <label class="floating-date-label">
        <i class="bi bi-calendar-check me-2"></i>Окончание периода
      </label>
      <VDatePicker v-model="endDate" mode="date" class="floating-date-picker" />
    </div>

    <div class="floating-date-actions">
      <button
          @click.prevent="addDates"
          class="btn btn-success rounded-pill px-3 btn-sm"
          :disabled="isGeneratingRegistry"
      >
        <template v-if="isGeneratingRegistry">
          <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
          Формирование...
        </template>
        <template v-else>
          <i class="bi bi-file-earmark-pdf me-2"></i>Сформировать
        </template>
      </button>
      <button
          @click.prevent="toggleDatePanel"
          class="btn btn-danger rounded-pill px-3 btn-sm"
      >
        <i class="bi bi-x me-1"></i>Отмена
      </button>
    </div>
  </div>

  <!-- Floating action buttons -->
  <div class="floating-buttons">
    <!-- Journal button -->
    <button
        class="btn floating-btn journal-btn"
        @click="generateLogPdf"
        :disabled="isLoading"
        :class="{ 'btn-pulse': isLoading }"
    >
      <i class="bi bi-file-earmark-pdf"></i>
      <span class="floating-btn-text">Сформировать журнал входного контроля</span>
    </button>

    <!-- Registry button -->
    <button
        class="btn btn-outline-secondary floating-btn registry-btn"
        @click="toggleDatePanel"
    >
      <i class="bi bi-border-width"></i>
      <span class="floating-btn-text">{{ isDatePanelOpen ? 'Скрыть даты' : 'Сформировать комплект ИД' }}</span>
    </button>

    <!-- Add act button -->
    <a
        href="/addAct"
        class="btn btn-info floating-btn add-btn"
    >
      <i class="bi bi-plus-lg"></i>
      <span class="floating-btn-text">Добавить новый АОСР</span>
    </a>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import Navbar from '../../components/Navbar.vue'

const acts = ref([])
const path = ref('http://localhost:8080/acts')
const startDate = ref(new Date())
const endDate = ref(new Date())
const isDatePanelOpen = ref(false)
const isLoading = ref(false)
const error = ref(null)
const isGeneratingRegistry = ref(false)

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

const getActs = async () => {
  try {
    isLoading.value = true
    error.value = null

    const response = await fetch(path.value, {
      headers: getAuthHeaders()
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      error.value = 'Ошибка загрузки актов';
      return;
    }

    acts.value = await response.json()
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message
  } finally {
    isLoading.value = false
  }
}

const deleteAct = async (id) => {
  if (!confirm('Вы уверены, что хотите удалить этот акт?')) return

  try {
    const response = await fetch(`http://localhost:8080/acts/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      error.value = 'Ошибка удаления акта';
      return;
    }

    await getActs()
  } catch (err) {
    console.error('Ошибка:', err)
    error.value = err.message
  }
}

const generatePdf = async (actId) => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      handleUnauthorized();
      return;
    }

    const pdfWindow = window.open('', '_blank');
    const response = await fetch(`http://localhost:8080/acts/${actId}/pdf`, {
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

    const blob = await response.blob();
    pdfWindow.location.href = URL.createObjectURL(blob);

  } catch (err) {
    console.error('Ошибка при генерации PDF:', err);
    error.value = 'Не удалось сформировать PDF';
  }
}

const addDates = async () => {
  try {
    isGeneratingRegistry.value = true;
    error.value = null;

    const formatDate = (date) => {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }

    const response = await fetch('http://localhost:8080/acts/registries', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({
        startDate: formatDate(startDate.value),
        endDate: formatDate(endDate.value)
      })
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      error.value = 'Ошибка сервера';
      return;
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'реестр.pdf'
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)

    toggleDatePanel();

  } catch (err) {
    console.error('Ошибка:', err)
    error.value = 'Не удалось сформировать реестр'
  } finally {
    isGeneratingRegistry.value = false;
  }
}

const generateLogPdf = async () => {
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      handleUnauthorized();
      return;
    }

    const pdfWindow = window.open('', '_blank');
    const response = await fetch(`http://localhost:8080/acts/pdf/controlLog`, {
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

    const blob = await response.blob();
    pdfWindow.location.href = URL.createObjectURL(blob);

  } catch (err) {
    console.error('Ошибка при генерации PDF:', err);
    error.value = 'Не удалось сформировать PDF';
  }
}

const toggleDatePanel = () => {
  isDatePanelOpen.value = !isDatePanelOpen.value
}

onMounted(() => {
  getActs()
})
</script>

<style scoped>
.card {
  border-radius: 10px;
}

.table {
  font-size: 0.9rem;
}

.table th {
  font-weight: 500;
  white-space: nowrap;
}

.table-hover tbody tr:hover {
  background-color: rgba(0, 0, 0, 0.03);
}

.table-responsive::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.table-responsive::-webkit-scrollbar-thumb {
  background-color: #000000;
  border-radius: 4px;
}

.table-responsive::-webkit-scrollbar-track {
  background-color: #f1f1f1;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.8rem;
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
}

.btn:disabled {
  opacity: 0.65;
  pointer-events: none;
}

.pdf-link {
  color: inherit;
  transition: color 0.2s ease;
}

.pdf-link:hover {
  color: #dc3545;
}

.pdf-link .bi {
  color: #6c757d;
  transition: color 0.2s ease;
}

.pdf-link:hover .bi {
  color: #dc3545;
}

/* Floating Date Pickers Styles */
.floating-date-pickers {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(1);
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 2rem;
  z-index: 1070;
  opacity: 0;
  visibility: hidden;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 320px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  height: 850px;
}

.floating-date-pickers--open {
  opacity: 1;
  visibility: visible;
  transform: translate(-50%, -50%) scale(1);
}

.floating-date-item {
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.floating-date-label {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center; /* Центрируем по горизонтали */
  text-align: center;
  width: 100%;
  max-width: 300px;
}

.floating-date-picker {
  width: 300px; /* Фиксированная ширина */
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 0.875rem 1rem;
  font-size: 0.9rem;
  background: #f8f9fa;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.floating-date-picker:hover {
  border-color: #adb5bd;
  background: white;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  transform: translateY(-1px);
}

.floating-date-picker:focus {
  border-color: #0d6efd;
  box-shadow: 0 0 0 3px rgba(13, 110, 253, 0.1);
  outline: none;
  background: white;
}

.floating-date-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 2rem;
}

.floating-date-actions .btn {
  width: 160px; /* Фиксированная ширина для обеих кнопок */
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
}

/* Overlay for floating date pickers */
.floating-date-pickers::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
  display: none; /* Полностью убираем затемненный фон */
}

.floating-date-pickers--open::before {
  opacity: 1;
}

/* Overlay для закрытия date pickers */
.floating-date-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: transparent;
  z-index: 1069; /* На 1 меньше чем у date pickers */
  cursor: pointer;
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

/* Individual button animations */
.add-btn {
  animation: floatUp 0.5s ease-out 0.2s both;
  z-index: 1001;
  background: linear-gradient(135deg, #17a2b8 0%, #138496 100%) !important;
  border: none !important;
}

.add-btn:active {
  background: linear-gradient(135deg, #138496 0%, #117a8b 100%) !important;
  box-shadow: 0 2px 15px rgba(23, 162, 184, 0.6) !important;
}

.registry-btn {
  animation: floatUp 0.5s ease-out 0.1s both;
  z-index: 1001;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  border: 2px solid #6c757d !important;
  color: #6c757d !important;
}

.registry-btn:hover {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%) !important;
  border-color: #495057 !important;
  color: #495057 !important;
}

.registry-btn:active {
  background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%) !important;
  box-shadow: 0 2px 15px rgba(108, 117, 125, 0.4) !important;
}

.journal-btn {
  animation: floatUp 0.5s ease-out both;
  z-index: 1001;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  border: 2px solid #6c757d !important;
  color: #6c757d !important;
}

.journal-btn:hover {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%) !important;
  border-color: #dc3545 !important;
  color: #dc3545 !important;
}

.journal-btn:active {
  background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%) !important;
  border-color: #c82333 !important;
  color: #c82333 !important;
  box-shadow: 0 2px 15px rgba(220, 53, 69, 0.4) !important;
}

.journal-btn .bi {
  transition: color 0.3s ease;
}

.journal-btn:hover .bi {
  color: #dc3545 !important;
}

.journal-btn:active .bi {
  color: #c82333 !important;
}

.journal-btn:disabled {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%) !important;
  border-color: #6c757d !important;
  color: #6c757d !important;
  opacity: 0.6;
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

.btn-pulse {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 4px 20px rgba(220, 53, 69, 0.5);
  }
  50% {
    box-shadow: 0 6px 25px rgba(220, 53, 69, 0.8);
  }
  100% {
    box-shadow: 0 4px 20px rgba(220, 53, 69, 0.5);
  }
}

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
  .floating-date-pickers {
    width: 90%;
    min-width: unset;
    padding: 1.5rem;
  }

  .floating-date-actions {
    flex-direction: column;
  }

  .floating-date-actions .btn {
    width: 100%;
  }

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

/* Стилизация календаря */
:deep(.vc-container) {
  border: none !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15) !important;
  border-radius: 12px !important;
}

:deep(.vc-header) {
  margin-bottom: 1rem;
  background: transparent !important;
  color: white !important;
  padding: 1.5rem !important;
}

:deep(.vc-title) {
  font-weight: 600 !important;
  font-size: 1.1rem !important;
}
</style>