<template>
  <Navbar/>
  <div class="container py-4">
    <div class="row justify-content-center">
      <div class="col-12">
        <!-- Таблица -->
        <div class="card shadow-sm border-0 mt-5">
          <div class="card-body p-0">
            <div class="table-responsive" style="max-height: 90vh;">
              <table class="table table-hover mb-0">
                <thead class="sticky-top" style="background-color: #002d72;">
                <tr>
                  <th class="text-center text-white fw-normal" style="width: 40%; background-color: #000000;">
                    Наименование
                  </th>
                  <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">
                    Ед. изм.
                  </th>
                  <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">
                    ГОСТ, ТУ
                  </th>
                  <th class="text-center text-white fw-normal" style="width: 15%; background-color: #000000;">
                    Действие
                  </th>
                </tr>
                </thead>
                <tbody>
                <tr v-if="materials && materials.length > 0"
                    v-for="(material, index) in materials"
                    :key="material.id"
                    :class="{'table-light': index % 2 === 0}">
                  <td class="align-middle">
                    <div>{{ material.name }}</div>
                    <div v-if="material.certificates && Object.keys(material.certificates).length > 0"
                         class="mt-2">
                      <a href="#"
                         @click.prevent="toggleDocuments(material.id)"
                         class="small text-primary text-decoration-none document-toggle">
                        <i class="bi"
                           :class="{'bi-chevron-down': !expandedDocuments[material.id],
                           'bi-chevron-up': expandedDocuments[material.id]}"></i>
                        посмотреть документы
                      </a>

                      <div v-if="expandedDocuments[material.id]" class="mt-2 small document-list">
                        <div v-for="(url, name) in material.certificates"
                             :key="name"
                             class="mb-1">
                          <a :href="url"
                             target="_blank"
                             class="text-decoration-none text-primary document-link">
                            <i class="bi bi-file-earmark-pdf me-1 text-danger"></i>
                            {{ name }}
                          </a>
                        </div>
                      </div>
                    </div>
                  </td>
                  <td class="text-center align-middle">{{ material.units }}</td>
                  <td class="text-center align-middle">{{ material.standard }}</td>
                  <td class="text-center align-middle">
                    <div class="d-flex justify-content-center gap-2">
                      <a class="btn btn-sm btn-outline-primary" :href="`/editMaterial/${material.id}`">
                        <i class="bi bi-pencil"></i>
                      </a>
                      <button class="btn btn-sm btn-outline-danger" @click="deleteMaterial(material.id)">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-else>
                  <td colspan="4" class="text-center py-4 text-muted">
                    <i class="bi bi-exclamation-circle fs-4 d-block mb-2"></i>
                    Нет данных для отображения
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Floating action button -->
  <div class="floating-buttons">
    <!-- Add material button -->
    <button
        class="btn btn-success floating-btn add-material-btn"
        @click="showAddForm = true"
    >
      <i class="bi bi-plus-lg"></i>
      <span class="floating-btn-text">Добавить материал</span>
    </button>
  </div>

  <!-- Floating add form -->
  <div class="floating-add-form" :class="{ 'floating-add-form--open': showAddForm }">
    <div class="floating-form-content">
      <div class="card shadow-sm border-0">
        <div class="card-header bg-primary text-white py-3">
          <div class="d-flex justify-content-between align-items-center">
            <h5 class="mb-0">
              <i class="bi bi-plus-circle me-2"></i>Добавить материал
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeForm"></button>
          </div>
        </div>

        <div class="card-body p-4">
          <form @submit.prevent="addMaterial">
            <!-- Наименование -->
            <div class="mb-3">
              <label for="name" class="form-label fw-semibold small">
                <i class="bi bi-tag me-1"></i>Наименование материала
              </label>
              <input id="name" type="text" class="form-control form-control-sm"
                     placeholder="Введите наименование материала"
                     required v-model="material.name">
            </div>

            <!-- Единицы измерения -->
            <div class="mb-3">
              <label for="units" class="form-label fw-semibold small">
                <i class="bi bi-rulers me-1"></i>Ед. изм.
              </label>
              <input id="units" type="text" class="form-control form-control-sm"
                     placeholder="Введите единицы измерения"
                     required v-model="material.units">
            </div>

            <!-- Данные сертификата -->
            <div class="mb-3">
              <label class="form-label fw-semibold small">
                <i class="bi bi-file-earmark-text me-1"></i>Данные сертификата
              </label>

              <!-- Тип документа -->
              <div class="mb-2">
                <label class="form-label small">Тип документа</label>
                <select class="form-select form-select-sm" v-model="material.certificateType">
                  <option value="" disabled selected>Выберите тип</option>
                  <option v-for="type in documentTypes" :value="type">{{ type }}</option>
                </select>
              </div>

              <!-- Номер и дата -->
              <div class="row g-2 mb-2">
                <div class="col-md-6">
                  <label class="form-label small">Номер документа</label>
                  <input type="text" class="form-control form-control-sm" placeholder="Номер" v-model="material.certificateNumber">
                </div>
                <div class="col-md-6">
                  <label class="form-label small">Дата документа</label>
                  <input type="date" class="form-control form-control-sm" v-model="material.certificateDate">
                </div>
              </div>

              <!-- Автор сертификата -->
              <div class="mb-2">
                <label class="form-label small">Автор сертификата</label>
                <input type="text" class="form-control form-control-sm" placeholder="Введите автора сертификата" v-model="material.author">
              </div>
            </div>

            <!-- ГОСТ, ТУ -->
            <div class="mb-3">
              <label for="standard" class="form-label fw-semibold small">
                <i class="bi bi-file-earmark-check me-1"></i>ГОСТ, ТУ
              </label>
              <input id="standard" type="text" class="form-control form-control-sm"
                     placeholder="Введите ГОСТ или ТУ"
                     required v-model="material.standard">
            </div>

            <!-- Загрузка файла -->
            <div class="mb-4">
              <label class="form-label fw-semibold small">
                <i class="bi bi-file-earmark-pdf me-1"></i>Файл сертификата (PDF)
              </label>
              <input @change="handleFileUpload" class="form-control form-control-sm"
                     type="file" accept=".pdf">
              <small class="text-muted small" v-if="file">Выбран файл: {{ file.name }}</small>

              <div v-if="uploadProgress > 0 && uploadProgress < 100" class="mt-1">
                <div class="progress" style="height: 20px;">
                  <div class="progress-bar progress-bar-striped progress-bar-animated"
                       :style="{ width: uploadProgress + '%' }">
                    {{ uploadProgress }}%
                  </div>
                </div>
              </div>

              <div v-if="uploadError" class="alert alert-danger mt-1 py-1 small">
                <i class="bi bi-exclamation-triangle-fill me-1"></i>{{ uploadError }}
              </div>
            </div>

            <!-- Ошибка -->
            <div v-if="error" class="alert alert-danger mb-3 py-2">
              <i class="bi bi-exclamation-triangle-fill me-1"></i>{{ error }}
            </div>

            <!-- Кнопки отправки -->
            <div class="d-flex gap-2">
              <button type="button" class="btn btn-secondary btn-sm flex-fill" @click="closeForm">
                <i class="bi bi-x-circle me-1"></i>Отмена
              </button>
              <button type="submit" class="btn btn-primary btn-sm flex-fill" :disabled="isUploading">
                <template v-if="isUploading">
                  <span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                  Загрузка...
                </template>
                <template v-else>
                  <i class="bi bi-check-circle me-1"></i>Сохранить
                </template>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Navbar from '../../components/Navbar.vue'

// Reactive state
const expandedDocuments = ref({})
const isLoading = ref(false)
const error = ref(null)
const materials = ref([])
const showAddForm = ref(false)

// Form data
const material = ref({
  name: '',
  units: '',
  standard: '',
  author: '',
  certificateType: '',
  certificateNumber: '',
  certificateDate: '',
})

const file = ref(null)
const isUploading = ref(false)
const uploadProgress = ref(0)
const uploadError = ref(null)

const documentTypes = ref([
  'Декларация о соответствии',
  'Информационное письмо',
  'Паспорт изделия',
  'Письмо',
  'Свидетельство о государственной регистрации',
  'Сертификат качества',
  'Сертификат соответствия',
])

// Methods
const toggleDocuments = (materialId) => {
  expandedDocuments.value = {
    ...expandedDocuments.value,
    [materialId]: !expandedDocuments.value[materialId]
  }
}

const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    throw new Error('Требуется авторизация')
  }
  return {
    'Authorization': `Bearer ${token}`
  }
}

const handleUnauthorized = () => {
  localStorage.removeItem('token')
  window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
}

const getMaterials = async () => {
  try {
    isLoading.value = true
    error.value = null
    const token = localStorage.getItem('token')

    if (!token) {
      handleUnauthorized()
      return
    }

    const response = await fetch(
        `http://localhost:8080/materials`,
        {
          headers: getAuthHeaders()
        }
    )

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      error.value = 'Ошибка загрузки материалов'
      return
    }

    materials.value = await response.json() || []

  } catch (err) {
    console.error('Ошибка:', err)
    error.value = 'Не удалось загрузить материалы'
    if (err.message.includes('авторизация')) {
      handleUnauthorized()
    }
  } finally {
    isLoading.value = false
  }
}

const deleteMaterial = async (id) => {
  if (!confirm('Вы уверены, что хотите удалить этот материал?')) return

  try {
    const response = await fetch(`http://localhost:8080/materials/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    })

    if (response.status === 401) {
      handleUnauthorized()
      return
    }

    if (!response.ok) {
      error.value = 'Ошибка удаления материалов'
      return
    }

    await getMaterials()
    alert('Материал успешно удален')
  } catch (err) {
    console.error('Ошибка:', err)
    alert('Не удалось удалить материал')
  }
}

const handleFileUpload = (event) => {
  const selectedFile = event.target.files[0]
  if (!selectedFile) {
    file.value = null
    return
  }

  if (selectedFile.type !== 'application/pdf') {
    uploadError.value = 'Пожалуйста, загрузите файл в формате PDF'
    file.value = null
    return
  }

  file.value = selectedFile
  uploadError.value = null
}

const formatDateForDisplay = (isoDate) => {
  if (!isoDate) return ''
  const [year, month, day] = isoDate.split('-')
  return `${day}.${month}.${year}`
}

const generateCertificateName = () => {
  const type = material.value.certificateType
  const number = material.value.certificateNumber
  const date = material.value.certificateDate

  if (type && number && date) {
    return `${type} №${number} от ${formatDateForDisplay(date)} г.`
  }
  return ''
}

const resetForm = () => {
  material.value = {
    name: '',
    units: '',
    standard: '',
    author: '',
    certificateType: '',
    certificateNumber: '',
    certificateDate: '',
  }
  file.value = null
  uploadError.value = null
  error.value = null
  uploadProgress.value = 0
}

const closeForm = () => {
  showAddForm.value = false
  resetForm()
}

const addMaterial = async () => {
  try {
    isUploading.value = true
    error.value = null
    uploadError.value = null

    const token = localStorage.getItem('token')
    if (!token) {
      error.value = 'Токен отсутствует'
      return
    }

    if (!file.value) {
      uploadError.value = 'Пожалуйста, загрузите файл сертификата'
      return
    }

    const formData = new FormData()
    const certificateName = generateCertificateName()

    const materialDto = {
      name: material.value.name,
      units: material.value.units,
      standard: material.value.standard,
      author: material.value.author,
      certificateName: certificateName,
    }

    formData.append('material', new Blob([JSON.stringify(materialDto)], {
      type: 'application/json'
    }))

    formData.append('file', file.value)

    const response = await fetch('http://localhost:8080/materials', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData,
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      error.value = errorData.message || `Ошибка ${response.status}: ${response.statusText}`
      return
    }

    // Успешное сохранение
    closeForm()
    await getMaterials()

  } catch (err) {
    error.value = err.message || 'Произошла ошибка при сохранении'
    console.error('Error:', err)
  } finally {
    isUploading.value = false
    uploadProgress.value = 0
  }
}

// Lifecycle
onMounted(() => {
  getMaterials()
})
</script>

<style scoped>
/* Все стили остаются без изменений */
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
  text-decoration: none;
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

/* Add material button */
.add-material-btn {
  animation: floatUp 0.5s ease-out 0.2s both;
  z-index: 1001;
  background: linear-gradient(135deg, #28a745 0%, #218838 100%) !important;
  border: none !important;
  color: white !important;
}

.add-material-btn:active {
  background: linear-gradient(135deg, #218838 0%, #1e7e34 100%) !important;
  box-shadow: 0 2px 15px rgba(40, 167, 69, 0.6) !important;
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

/* Floating add form styles */
.floating-add-form {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0.9);
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  z-index: 1100;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.floating-add-form--open {
  opacity: 1;
  visibility: visible;
  transform: translate(-50%, -50%) scale(1);
}

.floating-form-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  max-height: 90vh;
  overflow-y: auto;
  border: 2px solid #e9ecef;
}

.floating-form-content .card {
  margin: 0;
  border: none;
}

.floating-form-content .card-body {
  max-height: calc(90vh - 80px);
  overflow-y: auto;
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

  .floating-add-form {
    width: 95%;
    max-height: 95vh;
  }

  .floating-form-content .card-body {
    max-height: calc(95vh - 80px);
    padding: 1.5rem;
  }
}

@media (max-width: 576px) {
  .floating-form-content .card-body {
    padding: 1rem;
  }
}

/* Убедимся, что кнопки поверх всего контента */
.floating-buttons * {
  z-index: inherit;
}
</style>